# Technical Deep Dive — Family League

This document explains the core business logic, how the database is managed, and how the key domain rules are enforced in code.

---

## 1. Prediction Validation

### Match Predictions

`MatchPredictionServiceImpl.submit()` runs three guards before any write reaches the DB, in this order:

**Guard 1 — Season must not be CLOSED**
```java
if (match.getSeason().getStatus() == SeasonStatus.CLOSED) {
    throw new SeasonClosedException("Season is closed — predictions are no longer accepted");
}
```
A closed season is completely immutable. Even an admin cannot submit predictions.

**Guard 2 — Lock window check**
```java
Instant lockTime = match.getStartTime().minus(predictionProperties.matchLockHours(), ChronoUnit.HOURS);
if (!Instant.now().isBefore(lockTime)) {
    throw new PredictionLockedException("Prediction window is closed. Lock time was " + lockTime);
}
```
`lockTime` is derived at runtime: `match.startTime − matchLockHours`. The lock duration is not hardcoded — it comes from `PredictionProperties`, which binds to `application.properties`:
```properties
app.prediction.match-lock-hours=1
```
So the window closes 1 hour before the match starts by default, and can be changed without a code deployment.

**Guard 3 — Team must belong to the match**
```java
boolean isMatchTeam = team.getId().equals(match.getHomeTeam().getId())
        || team.getId().equals(match.getAwayTeam().getId());
if (!isMatchTeam) {
    throw new ValidationException("Team " + teamId + " is not part of this match");
}
```
A user cannot predict a team that isn't playing in the match.

**Upsert, not insert**

The service finds the existing prediction for that user+match pair, or creates a new one:
```java
MatchPrediction prediction = matchPredictionRepository
        .findByMatchIdAndUserId(matchId, user.getId())
        .orElse(new MatchPrediction());
```
This means re-submitting before the lock time replaces the previous prediction rather than creating a duplicate. The DB enforces this with a unique constraint on `(match_id, user_id)`.

**Tie handling**

If the user predicts a tie, `predictedWinner` is explicitly set to `null`:
```java
prediction.setPredictedTie(request.predictedTie());
prediction.setPredictedWinner(request.predictedTie() ? null : resolveTeam(request.predictedWinnerId(), match));
```

---

### League Predictions

`LeaguePredictionServiceImpl.submit()` follows the same pattern, with one difference: the lock time is derived from the season's first match start time, not an individual match.

```java
private Instant deriveLockTime(Season season) {
    if (season.getFirstMatchStartTime() == null) return null;
    return season.getFirstMatchStartTime().minus(predictionProperties.leagueLockHours(), ChronoUnit.HOURS);
}
```

If `firstMatchStartTime` is not set yet (season has no scheduled matches), `lockTime` is `null` and the submission is allowed through:
```java
if (lockTime != null && !Instant.now().isBefore(lockTime)) {
    throw new PredictionLockedException(...);
}
```

The predicted standings are stored as a JSONB column (`predictedPositions`), which holds a list of `TeamPositionEntry(teamId, position)` records.

---

## 2. How Predictions Are Hidden Until the Match Starts

Other users' predictions are only visible via the **head-to-head endpoint**, and it enforces the same lock time used for submissions — but in reverse.

```java
// MatchPredictionServiceImpl.getHeadToHead()
Instant lockTime = deriveLockTime(match);

if (Instant.now().isBefore(lockTime)) {
    throw new PredictionLockedException(
            "Head-to-head predictions are only visible after the lock time: " + lockTime);
}
```

The logic is:
- **Before** lock time → submissions are open, head-to-head is blocked.
- **After** lock time → submissions are blocked, head-to-head is open.

The same lock time is the boundary in both directions. There is no separate flag or DB column controlling visibility — it is purely time-based, computed on every request from `match.startTime`.

The same applies to league predictions:
```java
// LeaguePredictionServiceImpl.getHeadToHead()
if (lockTime == null || Instant.now().isBefore(lockTime)) {
    throw new PredictionLockedException(
            "League head-to-head predictions are only visible after the lock time");
}
```

---

## 3. Result Publishing and the Scoring Event Chain

When an admin publishes a result, it triggers a chain of steps across multiple services. The services never call each other directly — they communicate through a Spring `ApplicationEvent`.

### Step 1 — Admin publishes result

`ResultServiceImpl.publishMatchResult()`:
1. Confirms the match exists and the season is not CLOSED.
2. Confirms no result has been published yet (`matchResultRepository.existsByMatchId(matchId)`).
3. Persists the `MatchResult` entity.
4. Fires `ResultPublishedEvent`:

```java
eventPublisher.publishEvent(new ResultPublishedEvent(this, matchId, season.getId()));
```

The event carries `matchId` (non-null for match results, null for league results) and `seasonId`.

### Step 2 — Leaderboard service picks it up asynchronously

`LeaderboardServiceImpl.handleResultPublished()` is annotated with three things:

```java
@Async("leaderboard-executor")
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Transactional(propagation = Propagation.REQUIRES_NEW)
```

- `@Async` — runs on a dedicated thread pool (`leaderboard-executor`), not on the HTTP request thread.
- `@TransactionalEventListener(AFTER_COMMIT)` — only fires after the result has been fully committed to the DB. If the result transaction rolls back, scoring never runs.
- `@Transactional(REQUIRES_NEW)` — opens a fresh transaction for the scoring work, independent of the triggering transaction.

### Step 3 — Scoring

`ScoringService.calculate()` compares each prediction field against the actual result and awards 1 point per correct field:

| Field | Points |
|---|---|
| Correct winner (or tie) | 1 |
| Correct toss winner | 1 |
| Correct player of match | 1 |
| **Max per match** | **3** |

For ties: a user who predicted a tie gets 1 point. A user who predicted a specific team gets 0 even if that team "won" in a tie scenario.

For league predictions, 1 point is awarded per team predicted at the correct final position.

`ScoringService` takes data as method parameters — it imports no repositories and holds no state. This keeps it purely functional and independently testable.

### Step 4 — Leaderboard upsert

For each `PointAward`, the service fetches (or creates) the `LeaderboardEntry` for that user+season and accumulates points:

```java
entry.setTotalPoints(entry.getTotalPoints() + award.points());
leaderboardEntryRepository.save(entry);
```

Points are additive. Each result published adds on top of existing totals.

### Step 5 — Rank recalculation

After all points are applied, ranks are recalculated in a single pass. The algorithm implements **dense rank with gap skipping** (standard sports ranking):

```java
List<LeaderboardEntry> entries =
        leaderboardEntryRepository.findBySeasonIdOrderByTotalPointsDesc(seasonId);

int rank = 1;
int prevPoints = -1;
int prevRank = 1;

for (int i = 0; i < entries.size(); i++) {
    LeaderboardEntry entry = entries.get(i);
    if (entry.getTotalPoints() == prevPoints) {
        entry.setRank(prevRank);   // tie: same rank
    } else {
        prevRank = rank;
        entry.setRank(rank);
        prevPoints = entry.getTotalPoints();
    }
    rank++;  // next slot always advances
}
```

Example with 4 users: 10 pts, 10 pts, 7 pts, 5 pts → ranks 1, 1, 3, 4. The `rank=2` slot is skipped because two users share rank 1.

---

## 4. Database Design

### Soft Delete on Every Table

Every entity extends `AuditableEntity`, which adds five audit columns:

| Column | Meaning |
|---|---|
| `created_at` | Set once on insert, never updated |
| `created_by` | Username from SecurityContext at insert time |
| `updated_at` | Updated on every write |
| `updated_by` | Username from SecurityContext at update time |
| `deleted_at` | `NULL` = active, non-null = soft-deleted |

Hard deletes (`DELETE FROM`) are never used. All deletes set `deleted_at`.

Hibernate automatically filters soft-deleted rows via `@SQLRestriction`:
```java
@SQLRestriction("deleted_at IS NULL")
```
This annotation appends `WHERE deleted_at IS NULL` to every query on the entity. The application code never sees deleted rows without explicitly bypassing it. (`@SQLRestriction` is the Hibernate 6 replacement for the deprecated `@Where`.)

### Schema Migrations via Flyway

The DB schema is managed exclusively through Flyway migration files in `src/main/resources/db/migration/`. Each file covers one concern:

```
V1__create_users.sql
V2__create_leagues.sql
V3__create_seasons.sql
V4__create_teams.sql
V5__create_players.sql
V6__create_matches.sql
V7__create_match_predictions.sql
V8__create_league_predictions.sql
V9__create_results.sql
V10__create_leaderboard.sql
V11__create_email_logs.sql
```

`spring.jpa.hibernate.ddl-auto=validate` in non-local profiles means Hibernate validates the entity model against the DB schema on startup but never modifies it. All structural changes must go through a new migration file.

### Unique Constraints (DB-level enforcement)

Key tables have unique constraints that back the upsert logic in services:

| Table | Unique constraint | Purpose |
|---|---|---|
| `match_predictions` | `(match_id, user_id)` | One prediction per user per match |
| `league_predictions` | `(season_id, user_id)` | One prediction per user per season |
| `leaderboard_entries` | `(season_id, user_id)` | One leaderboard row per user per season |
| `match_results` | `(match_id)` | One result per match |
| `league_results` | `(season_id)` | One result per season |

### Association Loading

All `@ManyToOne` and `@OneToMany` associations default to `FetchType.LAZY`. Nothing is loaded until explicitly accessed, which prevents accidental N+1 queries. Relations are traversed only where the service logic requires them.

### Transaction Boundaries

| Annotation | Where used | Effect |
|---|---|---|
| `@Transactional` | Write methods in service layer | Read+write transaction |
| `@Transactional(readOnly = true)` | All `get*` methods | Read-only hint; DB can skip locking overhead |
| `@Transactional(propagation = REQUIRES_NEW)` | `LeaderboardServiceImpl.handleResultPublished` | New transaction, independent of the event publisher's transaction |

### JSONB for League Predictions

League predictions (`LeaguePrediction.predictedPositions`) store the full team ranking as JSONB in PostgreSQL rather than a separate join table. This avoids schema complexity for a list that is always read and written together.

---

## 5. Season Immutability

`SeasonStatus` has three states: `UPCOMING`, `ACTIVE`, `CLOSED`.

Once a season is `CLOSED`, the service layer rejects all writes — predictions, result publishing, and match creation — for every relevant service:

```java
if (match.getSeason().getStatus() == SeasonStatus.CLOSED) {
    throw new SeasonClosedException("Season is closed — ...");
}
```

This check is at the top of every write method. There is no bypass for admins.

---

## 6. Cross-Slice Communication Rules

Slices (user, match, prediction, result, leaderboard, scoring, notification) are vertically isolated. They share data through these patterns only:

| Pattern | When to use |
|---|---|
| `ApplicationEvent` | Fire-and-forget after a state change (result → leaderboard) |
| Repository injection across slices | Read-only lookups (leaderboard reads prediction repo) |
| Method parameter passing | `ScoringService` receives result + predictions as arguments; imports no other slice |

Importing one slice's service class into another's service is never allowed. If it feels necessary, the logic belongs in `scoring/` or `common/`.

# Family League — Requirements & Task Breakdown

---

## Functional Requirements

### FR-01 · User & Authentication
- User can register with email and password
- User can log in and receive a JWT access token + refresh token
- User can update their own profile (display name, profile picture/avatar)
- Two roles exist: `ROLE_USER` and `ROLE_ADMIN`
- Admin routes are inaccessible to regular users

### FR-02 · League & Season Management
- Admin can create a **League** (umbrella concept, e.g. "IPL")
- Admin can create a **Season** as an instance of a League (e.g. "IPL 2026")
- Season has a lifecycle: `UPCOMING → ACTIVE → CLOSED`
- Once a Season is `CLOSED`, no further writes are permitted — not even by Admin
- Admin can list, view, and search leagues and seasons

### FR-03 · Team & Player Management
- Admin can create **Teams** (teams exist independently of any season)
- Admin can create **Players** assigned to a team
- Admin can list, view, and search teams and players

### FR-04 · Match Management
- Admin creates **Matches** under a Season with a `startTime` and two competing teams
- `lockTime` = `startTime - 1 hour` (configurable); derived at runtime, not stored
- Admin can list and view matches for a season

### FR-05 · Match-Level Predictions
- Users can submit predictions for a match: **match winner**, **toss winner**, **player of the match**
- Predictions can be updated until the lock time
- No submissions or edits allowed at or after lock time
- After lock time, users can view each other's predictions (head-to-head)
- Before lock time, each user's predictions are private

### FR-06 · League-Level Predictions
- Users can predict the full final leaderboard for a Season (team positions 1 to n)
- Prediction window closes 4 hours before the Season's first match start time (configurable)
- Same lock and post-lock visibility rules apply as match predictions

### FR-07 · Result Publishing
- Admin publishes a **match result**: winner, toss winner, player of the match
- Ties are valid results — a predicted "tie" earns the point if the result is a tie
- Admin publishes **final season standings** at the end of a season
- Points are never submitted via API — always calculated server-side only

### FR-08 · Scoring
- 1 point awarded per correct prediction field (winner, toss winner, player of match)
- League prediction: 1 point per correctly predicted team position
- Scoring runs automatically and asynchronously after each result is published
- Leaderboard recalculation is triggered by result publish — never called manually

### FR-09 · Leaderboard
- Season leaderboard is visible to all authenticated users
- Rankings are based on total points; equal-point users share the same rank
- Rankings recalculate after every confirmed result publication
- Admin receives an email when recalculation completes

### FR-10 · Notifications
- Users receive an **email reminder** before a match prediction window closes if they have not yet submitted
- Admin receives an **email alert** when leaderboard recalculation completes
- Admin can **bulk-send** custom emails by selecting user IDs, an event type, and a message body
- Every outbound email is persisted as an `EmailLog` record (recipient, subject, status, timestamp)

### FR-11 · Audit & Soft Delete
- Every entity records `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_at`
- No hard deletes anywhere — soft delete only
- All schema changes via Flyway migrations only

### FR-12 · Admin API Surface
- Admin has dedicated endpoints to set up the full data model: League → Season → Teams → Players → Matches
- OpenAPI/Swagger serves as the Admin's primary API guide
- Admin can view paginated email logs

---

## Non-Functional Requirements

### NFR-01 · Security
- All endpoints protected by JWT; stateless (no server-side sessions)
- Role-based access enforced via `@PreAuthorize` at method level
- Prediction lock enforced server-side in the service layer
- Passwords stored as BCrypt hashes; never logged
- No hardcoded credentials or secrets in source — use env vars or `application-local.properties`
- HTTPS assumed at deployment (local certs acceptable)

### NFR-02 · API Design Consistency
- All success responses wrapped in `ApiResponse<T>` with a timestamp
- All error responses use `ErrorResponse` with a machine-readable `code`
- `201 Created` + `Location` header on every resource creation
- Pagination, search, and sort supported on every list endpoint (`page`, `size`, `sort`, `search`)

### NFR-03 · Observability
- Structured logging: `DEBUG` / `INFO` / `WARN` / `ERROR` — no sensitive data in logs
- Logs output to both console and a daily rolling file (`logs/family-league.log`)

### NFR-04 · Configurability
- Lock durations (`app.prediction.match-lock-hours`, `app.prediction.league-lock-hours`) in properties
- All cron schedules in `application.properties`
- Profile-based config: `local` (dev + Docker), `test` (Testcontainers)

### NFR-05 · Reliability & Async
- Leaderboard recalculation must never block a web request — runs via `@Async`
- Email send failures are caught, logged, and marked `FAILED` in `EmailLog`
- `GlobalExceptionHandler` covers all unhandled exceptions — nothing raw reaches the client

### NFR-06 · Code Quality
- Interface-driven services (interface + impl)
- Constructor injection only — no `@Autowired` field injection
- DTOs (Java records) in all API layers — entities never exposed directly
- Cross-slice communication via Spring `ApplicationEvent` only — no direct service-to-service imports

### NFR-07 · Documentation
- OpenAPI Swagger UI auto-generated at `/swagger-ui.html`
- `README.md` at repo root: prerequisites, setup steps, run instructions, Swagger URL
- `strategy/decision_log.md`: every architectural choice with justification

### NFR-08 · Testing
- Unit tests for all business logic (Mockito — no Spring context)
- Integration tests with real PostgreSQL via Testcontainers (no H2)
- Controller slice tests with `@WebMvcTest` + `MockMvc`

---

## Task Breakdown

Ordered strictly by dependency — complete each phase before starting the next.

---

### Phase 1 — Foundation
*Goal: runnable skeleton, shared infrastructure all other phases depend on*

- [ ] **T-01** Add all Maven dependencies to `pom.xml`
  — Spring Security, Spring Data JPA, Flyway, PostgreSQL driver, MapStruct + processor,
    Lombok, SpringDoc OpenAPI, Spring Mail, Testcontainers (junit5 + postgresql)

- [ ] **T-02** Add `docker-compose.yml` at repo root
  — PostgreSQL 16 service; connection props in `application-local.properties` (git-ignored)

- [ ] **T-03** Configure `application.properties`
  — datasource via `${DB_URL}` / `${DB_USER}` / `${DB_PASS}` placeholders,
    `spring.jpa.hibernate.ddl-auto=validate`, Flyway enabled,
    JWT secret/expiry placeholders, `app.prediction.match-lock-hours=1`,
    `app.prediction.league-lock-hours=4`, cron expression placeholders, async executor settings

- [ ] **T-04** Create `AuditableEntity` (`common/audit/`)
  — `@MappedSuperclass` with `createdAt`, `createdBy`, `updatedAt`, `updatedBy`, `deletedAt`
    all annotated with JPA Auditing annotations

- [ ] **T-05** Create `AuditingConfig` (`common/audit/`)
  — `@EnableJpaAuditing` + `AuditorAware<String>` bean reading username from `SecurityContextHolder`;
    returns `"system"` when no auth context exists

- [ ] **T-06** Create `AsyncConfig` (`common/config/`)
  — `@EnableAsync` + named `ThreadPoolTaskExecutor` bean (`leaderboard-executor`)

- [ ] **T-07** Create `SchedulerConfig` (`common/config/`)
  — `@EnableScheduling` (empty class, activates `@Scheduled`)

- [ ] **T-08** Create `ApiResponse<T>`, `PagedResponse<T>`, `ErrorResponse` records (`common/response/`)

- [ ] **T-09** Create custom exception classes (`common/exception/`)
  — `ResourceNotFoundException`, `PredictionLockedException`,
    `SeasonClosedException`, `ValidationException`

- [ ] **T-10** Create `GlobalExceptionHandler` (`common/exception/`)
  — `@RestControllerAdvice`; handles all custom exceptions, `MethodArgumentNotValidException`,
    and a fallback `Exception` handler; all responses serialised as `ErrorResponse`

- [ ] **T-11** Create `logback-spring.xml` (`src/main/resources/`)
  — console appender + rolling file appender (`logs/family-league.log`, daily, 30-day retention)

---

### Phase 2 — User & Authentication
*Goal: working register/login with JWT; all later phases use the auth context*

- [ ] **T-12** Flyway `V1__create_users.sql`
  — `users` table with all audit columns + `role` column

- [ ] **T-13** Create `User` entity + `Role` enum (`user/entity/`)
  — extends `AuditableEntity`; `@SQLRestriction("deleted_at IS NULL")`

- [ ] **T-14** Create `UserRepository` (`user/repository/`)

- [ ] **T-15** Create `JwtService` (`common/security/`)
  — issue access token, validate token, extract subject/claims; no Spring context dependency

- [ ] **T-16** Create `UserDetailsServiceImpl` (`user/service/`)
  — loads `User` by email; returns Spring `UserDetails`
  — lives in user slice (not `common/security/`) to avoid entity coupling in infrastructure

- [ ] **T-17** Create `JwtAuthFilter` + `SecurityConfig` (`common/security/`)
  — `SecurityFilterChain`: permit `/api/v1/auth/**` and Swagger paths; lock everything else
  — `@EnableMethodSecurity` for `@PreAuthorize`

- [ ] **T-18** Create `UserMapper` (MapStruct, `user/mapper/`)

- [ ] **T-19** Create `UserService` interface + `UserServiceImpl` (`user/service/`)
  — `register` (hash password, persist), `getProfile`, `updateProfile`, `softDelete`

- [ ] **T-20** Create `AuthController` (`user/controller/`)
  — `POST /api/v1/auth/register` → `201`
  — `POST /api/v1/auth/login` → `LoginResponse(accessToken, expiresIn)`

- [ ] **T-21** Create `UserController` (`user/controller/`)
  — `GET /api/v1/users/me` → own profile
  — `PATCH /api/v1/users/me` → update display name / avatar URL

---

### Phase 3 — League & Season
*Goal: the container hierarchy that all matches and predictions live inside*

- [ ] **T-22** Flyway `V2__create_leagues.sql` — `leagues` table with audit columns

- [ ] **T-23** Flyway `V3__create_seasons.sql`
  — `seasons` table: FK → leagues, `status` VARCHAR, `first_match_start_time` TIMESTAMPTZ, audit columns

- [ ] **T-24** Create `League` entity + `LeagueRepository` + `LeagueMapper`

- [ ] **T-25** Create `Season` entity + `SeasonStatus` enum + `SeasonRepository` + `SeasonMapper`

- [ ] **T-26** Create `LeagueService` + `LeagueServiceImpl`
  — CRUD; paginated list with search by name; writes `ROLE_ADMIN` only

- [ ] **T-27** Create `SeasonService` + `SeasonServiceImpl`
  — CRUD + status transition (`UPCOMING → ACTIVE → CLOSED`);
    enforce closed-season immutability on any write attempt

- [ ] **T-28** Create `LeagueController` — `GET|POST /api/v1/leagues`, `GET|PUT|DELETE /api/v1/leagues/{id}`

- [ ] **T-29** Create `SeasonController` — `GET|POST /api/v1/seasons`, `GET|PUT|PATCH|DELETE /api/v1/seasons/{id}`

---

### Phase 4 — Team & Player
*Goal: the roster data used in matches and predictions*

- [ ] **T-30** Flyway `V4__create_teams.sql` — `teams` table with audit columns

- [ ] **T-31** Flyway `V5__create_players.sql` — `players` table: FK → teams, audit columns

- [ ] **T-32** Create `Team` entity + `TeamRepository` + `TeamMapper`

- [ ] **T-33** Create `Player` entity + `PlayerRepository` + `PlayerMapper`

- [ ] **T-34** Create `TeamService` + `TeamServiceImpl` — CRUD + paginated search

- [ ] **T-35** Create `PlayerService` + `PlayerServiceImpl` — CRUD + paginated search; filter by team

- [ ] **T-36** Create `TeamController` — `/api/v1/teams` (writes = ADMIN)

- [ ] **T-37** Create `PlayerController` — `/api/v1/players` (writes = ADMIN)

---

### Phase 5 — Match
*Goal: scheduled matches with configurable lock windows; unblocks predictions*

- [ ] **T-38** Flyway `V6__create_matches.sql`
  — `matches` table: FK → seasons, `home_team_id`, `away_team_id`, `start_time` TIMESTAMPTZ, audit columns
  — `lock_time` is NOT stored (derived); add DB-level partial index on `season_id` for query performance

- [ ] **T-39** Create `Match` entity + `MatchRepository` + `MatchMapper`

- [ ] **T-40** Create `PredictionProperties` (`prediction/config/`)
  — `@ConfigurationProperties(prefix = "app.prediction")` binding `matchLockHours` and `leagueLockHours`

- [ ] **T-41** Create `MatchService` + `MatchServiceImpl`
  — CRUD; derive and expose `lockTime = startTime - matchLockHours`; paginated list by season

- [ ] **T-42** Create `MatchController` — `/api/v1/seasons/{seasonId}/matches` (writes = ADMIN)

---

### Phase 6 — Predictions
*Goal: core user-facing feature — submitting and viewing predictions with enforced lock*

- [ ] **T-43** Flyway `V7__create_match_predictions.sql`
  — `match_predictions`: FK → matches + users, `predicted_winner_id`, `predicted_toss_winner_id`,
    `predicted_player_of_match_id`, audit columns; unique constraint on (match_id, user_id)

- [ ] **T-44** Flyway `V8__create_league_predictions.sql`
  — `league_predictions`: FK → seasons + users, `predicted_positions` JSONB
    (array of `{teamId, position}`); unique constraint on (season_id, user_id)

- [ ] **T-45** Create `MatchPrediction` entity + `MatchPredictionRepository` + `MatchPredictionMapper`

- [ ] **T-46** Create `LeaguePrediction` entity + `LeaguePredictionRepository` + `LeaguePredictionMapper`

- [ ] **T-47** Create `MatchPredictionService` + `MatchPredictionServiceImpl` (`prediction/match/service/`)
  — `submit`: check `now < match.startTime - matchLockHours` or throw `PredictionLockedException`;
    check season not CLOSED or throw `SeasonClosedException`; upsert prediction
  — `getHeadToHead(matchId)`: return all users' predictions only if `now >= lockTime`

- [ ] **T-48** Create `LeaguePredictionService` + `LeaguePredictionServiceImpl` (`prediction/league/service/`)
  — same lock check using `season.firstMatchStartTime - leagueLockHours`
  — same post-lock visibility rule

- [ ] **T-49** Create `MatchPredictionController` (`prediction/match/controller/`)
  — `POST   /api/v1/matches/{matchId}/predictions` — submit / update
  — `GET    /api/v1/matches/{matchId}/predictions/me` — own prediction
  — `GET    /api/v1/matches/{matchId}/predictions` — head-to-head (post-lock only)

- [ ] **T-50** Create `LeaguePredictionController` (`prediction/league/controller/`)
  — `POST /api/v1/seasons/{seasonId}/predictions/league`
  — `GET  /api/v1/seasons/{seasonId}/predictions/league/me`
  — `GET  /api/v1/seasons/{seasonId}/predictions/league` (post-lock head-to-head)

---

### Phase 7 — Results & Scoring
*Goal: admin publishes results; scoring service calculates points; event triggers leaderboard*

- [ ] **T-51** Flyway `V9__create_results.sql`
  — `match_results`: FK → matches (unique), `winner_id`, `toss_winner_id`, `player_of_match_id`,
    `is_tie` BOOLEAN, audit columns
  — `league_results`: FK → seasons (unique), `final_standings` JSONB, audit columns

- [ ] **T-52** Create `MatchResult` + `LeagueResult` entities + repositories + mappers (`result/`)

- [ ] **T-53** Create `ResultPublishedEvent` (`result/event/`)
  — extends `ApplicationEvent`; payload: `matchId` (nullable), `seasonId`

- [ ] **T-54** Create `ScoringService` + `ScoringServiceImpl` (`scoring/`)
  — `List<PointAward> calculate(MatchResult, List<MatchPrediction>)`
  — Rules: 1 point per correct field; if `is_tie=true`, user who predicted the same team
    as winner still scores (both sides of tie scenario handled per spec)
  — `PointAward` record: `(UUID userId, Long matchId, int points, String reason)`

- [ ] **T-55** Create `ResultService` + `ResultServiceImpl` (`result/`)
  — `publishMatchResult(matchId, request)`: check season not CLOSED; persist `MatchResult`;
    publish `ResultPublishedEvent`
  — `publishLeagueResult(seasonId, request)`: same guard; persist `LeagueResult`;
    publish `ResultPublishedEvent`

- [ ] **T-56** Create `ResultController` (`result/controller/`) — ADMIN only
  — `POST /api/v1/admin/matches/{id}/result`
  — `POST /api/v1/admin/seasons/{id}/result`

---

### Phase 8 — Leaderboard
*Goal: async rank recalculation and public leaderboard view*

- [ ] **T-57** Flyway `V10__create_leaderboard.sql`
  — `leaderboard_entries`: FK → seasons + users, `total_points` INT, `rank` INT, audit columns;
    unique on (season_id, user_id)

- [ ] **T-58** Create `LeaderboardEntry` entity + `LeaderboardEntryRepository` + `LeaderboardMapper`

- [ ] **T-59** Create `LeaderboardService` + `LeaderboardServiceImpl`
  — `@Async("leaderboard-executor")`
  — `@TransactionalEventListener` on `ResultPublishedEvent`
  — Calls `ScoringService.calculate(...)`, upserts `LeaderboardEntry` rows,
    recalculates and writes ranks (dense rank by total_points desc),
    then triggers admin notification via `NotificationService`

- [ ] **T-60** Create `LeaderboardController`
  — `GET /api/v1/seasons/{id}/leaderboard` — paginated, sorted by rank

---

### Phase 9 — Notifications
*Goal: email reminders for users + admin alerts + admin bulk send + full email log*

- [ ] **T-61** Flyway `V11__create_email_logs.sql`
  — `email_logs`: `to_address`, `subject`, `body`, `event_type`, `status` VARCHAR,
    `sent_at` TIMESTAMPTZ, audit columns

- [ ] **T-62** Create `EmailLog` entity + `EmailLogRepository`

- [ ] **T-63** Create `NotificationChannel` interface + `EmailChannel` implementation (`notification/channel/`)
  — `EmailChannel` persists `EmailLog(PENDING)` before sending;
    updates to `SENT` or `FAILED` after; never propagates send exceptions to callers

- [ ] **T-64** Create `NotificationService` + `NotificationServiceImpl` (`notification/service/`)
  — `sendReminder(List<User> users, Match match)` — builds reminder message, delegates to `EmailChannel`
  — `sendAdminAlert(String message)` — sends to admin email from properties
  — `sendBulk(List<Long> userIds, String eventType, String body)` — resolves users, delegates

- [ ] **T-65** Create `NotificationScheduler` (`notification/scheduler/`)
  — Job 1 (cron): query users per upcoming match who have no `MatchPrediction` → send reminder
  — Job 2: `@EventListener` on `ResultPublishedEvent` (or part of `LeaderboardServiceImpl`
    post-recalculation) → send admin alert

- [ ] **T-66** Create `NotificationController` (`notification/controller/`) — ADMIN only
  — `POST /api/v1/admin/notifications/bulk`
  — `GET  /api/v1/admin/notifications/logs` — paginated email log

---

### Phase 10 — Polish & Docs
*Goal: submission-ready state — complete Swagger, pagination verified, README written*

- [ ] **T-67** Create `SwaggerConfig` (`common/config/`)
  — `OpenAPI` bean with project metadata + JWT bearer security scheme

- [ ] **T-68** Add OpenAPI annotations (`@Operation`, `@ApiResponse`, `@Schema`) to all DTOs and controllers

- [ ] **T-69** Verify pagination + search + sort on every list endpoint
  — League, Season, Team, Player, Match, Leaderboard, EmailLog

- [ ] **T-70** Write `README.md` at repo root
  — prerequisites (Java 21, Docker), local setup steps, env vars, how to run, Swagger URL,
    how to run tests

- [ ] **T-71** Write `strategy/decision_log.md`
  — record every architectural decision with justification:
    PostgreSQL, JWT over sessions, async leaderboard, soft delete, event-driven cross-slice,
    feature-vertical slice structure, Testcontainers over H2, etc.

---

### Phase 11 — Testing
*Goal: confidence in business rules — especially lock enforcement and scoring correctness*

- [ ] **T-72** Create `BaseIntegrationTest` (`common/` in test tree)
  — `@SpringBootTest` + Testcontainers PostgreSQL + `@DynamicPropertySource` wiring datasource

- [ ] **T-73** Unit tests — `ScoringServiceTest`
  — correct winner only (1 pt), all 3 correct (3 pts), all wrong (0 pts),
    tie result + tie predicted (1 pt), tie result + winner predicted (0 pts)

- [ ] **T-74** Unit tests — `MatchPredictionServiceTest`
  — submit before lock (pass), submit exactly at lock (reject),
    submit after lock (reject `PredictionLockedException`),
    head-to-head before lock (reject), head-to-head after lock (pass),
    submit on closed season (reject `SeasonClosedException`)

- [ ] **T-75** Unit tests — `LeaguePredictionServiceTest`
  — same lock coverage using the 4-hour league lock window

- [ ] **T-76** Unit tests — `ResultServiceTest`
  — publish on active season (pass + event published),
    publish on closed season (reject `SeasonClosedException`),
    verify `ResultPublishedEvent` payload

- [ ] **T-77** Integration test — Auth flow
  — register → login → call protected endpoint with token → call admin endpoint as user (403)

- [ ] **T-78** Integration test — Full match prediction lifecycle
  — create match → submit prediction → advance time past lock → verify head-to-head
    → publish result → verify leaderboard entry created with correct points

- [ ] **T-79** Controller slice tests (`@WebMvcTest`)
  — `AuthController`: register validation, duplicate email
  — `MatchPredictionController`: 401 without token, 403 wrong role, 409 after lock
  — `ResultController`: 403 for ROLE_USER

---

## Dependency Map

```
Phase 1  (T-01 → T-11)   Foundation
    ↓
Phase 2  (T-12 → T-21)   User / Auth          ← JWT context used by everything below
    ↓
Phase 3  (T-22 → T-29)   League / Season
    ↓
Phase 4  (T-30 → T-37)   Team / Player
    ↓
Phase 5  (T-38 → T-42)   Match
    ↓
Phase 6  (T-43 → T-50)   Predictions          ← depends on Match + User + Season
    ↓
Phase 7  (T-51 → T-56)   Results / Scoring    ← depends on Predictions + Match
    ↓
Phase 8  (T-57 → T-60)   Leaderboard          ← depends on Scoring + ResultPublishedEvent
    ↓
Phase 9  (T-61 → T-66)   Notifications        ← depends on Leaderboard + User + Match
    ↓
Phase 10 (T-67 → T-71)   Polish / Docs
    ↓
Phase 11 (T-72 → T-79)   Testing
```

---

*Update checkbox status as tasks are completed. Do not reorder tasks — each phase depends on the previous.*

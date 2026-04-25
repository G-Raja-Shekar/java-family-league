# Family League — Claude Code Rules

## Project Overview

Spring Boot 4.x / Java 21 backend for a family-and-friends sports prediction platform.
Users predict league standings and match outcomes; points are calculated server-side.
No UI — backend service only.

**Group ID:** `com.rajashekar`  
**Artifact ID:** `family-league`  
**Root package:** `com.rajashekar.familyleague`

---

## Technology Stack

| Concern | Choice |
|---|---|
| Runtime | Java 21 — use records, sealed classes, text blocks where they add clarity |
| Framework | Spring Boot 4.x |
| Security | Spring Security 6 + JWT (stateless, no sessions) |
| Persistence | Spring Data JPA + Hibernate 6 + PostgreSQL |
| Migrations | Flyway (`src/main/resources/db/migration/`) |
| Validation | Spring Validation (`jakarta.validation`) |
| Mapping | MapStruct |
| Boilerplate | Lombok |
| Docs | SpringDoc OpenAPI 3 (Swagger UI at `/swagger-ui.html`) |
| Testing | JUnit 5 + Mockito + Spring Boot Test + Testcontainers (PostgreSQL) |
| Build | Maven |

---

## Package Structure

Feature-vertical slices inside a standard layered skeleton. Every slice owns its full stack.
Cross-slice communication uses Spring `ApplicationEvent` — slices never import each other's service classes directly.

```
src/main/java/com/rajashekar/familyleague/
│
├── FamilyLeagueApplication.java
│
├── common/
│   ├── audit/
│   │   ├── AuditableEntity.java        # MappedSuperclass: createdAt, createdBy,
│   │   │                               #   updatedAt, updatedBy, deletedAt
│   │   └── AuditingConfig.java         # @EnableJpaAuditing + AuditorAware bean
│   │
│   ├── config/
│   │   ├── AsyncConfig.java            # @EnableAsync + ThreadPoolTaskExecutor bean
│   │   ├── JacksonConfig.java          # ObjectMapper: ISO-8601 dates, no nulls
│   │   ├── SchedulerConfig.java        # @EnableScheduling
│   │   └── SwaggerConfig.java          # OpenAPI bean, JWT bearer scheme
│   │
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java # @RestControllerAdvice
│   │   ├── ResourceNotFoundException.java
│   │   ├── PredictionLockedException.java
│   │   ├── SeasonClosedException.java
│   │   └── ValidationException.java
│   │
│   ├── response/
│   │   ├── ApiResponse.java            # record ApiResponse<T>(boolean success,
│   │   │                               #   T data, String message, Instant timestamp)
│   │   ├── PagedResponse.java          # wraps Page<T> for list endpoints
│   │   └── ErrorResponse.java          # record with code + message + timestamp
│   │
│   └── security/
│       ├── SecurityConfig.java         # SecurityFilterChain, CORS, CSRF off
│       ├── JwtService.java             # issue / validate tokens (pure utility)
│       ├── JwtAuthFilter.java          # OncePerRequestFilter
│       └── CurrentUser.java            # @CurrentUser annotation for controller params
│
├── user/
│   ├── controller/
│   │   ├── AuthController.java         # POST /api/v1/auth/register, /login, /refresh
│   │   └── UserController.java         # GET/PATCH /api/v1/users/me
│   ├── service/
│   │   ├── UserService.java            # interface
│   │   ├── UserServiceImpl.java
│   │   └── UserDetailsServiceImpl.java # implements UserDetailsService — lives here,
│   │                                   # NOT in common/security, to avoid cross-coupling
│   ├── repository/
│   │   └── UserRepository.java
│   ├── entity/
│   │   ├── User.java                   # extends AuditableEntity
│   │   └── Role.java                   # enum: ROLE_USER, ROLE_ADMIN
│   ├── dto/
│   │   ├── RegisterRequest.java        # record
│   │   ├── LoginRequest.java           # record
│   │   ├── LoginResponse.java          # record (accessToken, refreshToken, expiresIn)
│   │   ├── UserResponse.java           # record
│   │   └── UpdateProfileRequest.java   # record
│   └── mapper/
│       └── UserMapper.java             # MapStruct
│
├── league/
│   ├── controller/
│   │   └── LeagueController.java       # /api/v1/leagues  (write = ADMIN only)
│   ├── service/
│   │   ├── LeagueService.java
│   │   └── LeagueServiceImpl.java
│   ├── repository/
│   │   └── LeagueRepository.java
│   ├── entity/
│   │   └── League.java                 # umbrella concept (e.g. "IPL")
│   ├── dto/
│   │   ├── CreateLeagueRequest.java
│   │   ├── LeagueResponse.java
│   │   └── LeagueSummaryResponse.java
│   └── mapper/
│       └── LeagueMapper.java
│
├── season/
│   ├── controller/
│   │   └── SeasonController.java       # /api/v1/seasons  (write = ADMIN only)
│   ├── service/
│   │   ├── SeasonService.java
│   │   └── SeasonServiceImpl.java
│   ├── repository/
│   │   └── SeasonRepository.java
│   ├── entity/
│   │   ├── Season.java                 # instance of a League; carries status enum
│   │   └── SeasonStatus.java           # enum: UPCOMING, ACTIVE, CLOSED
│   ├── dto/
│   │   ├── CreateSeasonRequest.java
│   │   ├── SeasonResponse.java
│   │   └── SeasonSummaryResponse.java
│   └── mapper/
│       └── SeasonMapper.java
│
├── team/
│   ├── controller/
│   │   └── TeamController.java         # /api/v1/teams  (write = ADMIN only)
│   ├── service/
│   │   ├── TeamService.java
│   │   └── TeamServiceImpl.java
│   ├── repository/
│   │   └── TeamRepository.java
│   ├── entity/
│   │   └── Team.java                   # independent of season
│   ├── dto/
│   │   ├── CreateTeamRequest.java
│   │   └── TeamResponse.java
│   └── mapper/
│       └── TeamMapper.java
│
├── player/
│   ├── controller/
│   │   └── PlayerController.java       # /api/v1/players  (write = ADMIN only)
│   ├── service/
│   │   ├── PlayerService.java
│   │   └── PlayerServiceImpl.java
│   ├── repository/
│   │   └── PlayerRepository.java
│   ├── entity/
│   │   └── Player.java                 # FK → Team
│   ├── dto/
│   │   ├── CreatePlayerRequest.java
│   │   └── PlayerResponse.java
│   └── mapper/
│       └── PlayerMapper.java
│
├── match/
│   ├── controller/
│   │   └── MatchController.java        # /api/v1/seasons/{seasonId}/matches
│   ├── service/
│   │   ├── MatchService.java
│   │   └── MatchServiceImpl.java
│   ├── repository/
│   │   └── MatchRepository.java
│   ├── entity/
│   │   └── Match.java                  # FK → Season, homeTeam, awayTeam
│   │                                   # startTime stored; lockTime derived at runtime
│   ├── dto/
│   │   ├── CreateMatchRequest.java
│   │   └── MatchResponse.java
│   └── mapper/
│       └── MatchMapper.java
│
├── prediction/
│   │   # Sub-packages for each prediction type — keeps each type's service/repo/dto
│   │   # independent so a new prediction type is an additive change, not a surgery.
│   │
│   ├── match/
│   │   ├── controller/
│   │   │   └── MatchPredictionController.java   # /api/v1/matches/{matchId}/predictions
│   │   ├── service/
│   │   │   ├── MatchPredictionService.java
│   │   │   └── MatchPredictionServiceImpl.java  # enforces 1-hour lock
│   │   ├── repository/
│   │   │   └── MatchPredictionRepository.java
│   │   ├── entity/
│   │   │   └── MatchPrediction.java    # winner, tossWinner, playerOfMatch; FK → Match, User
│   │   ├── dto/
│   │   │   ├── SubmitMatchPredictionRequest.java
│   │   │   ├── MatchPredictionResponse.java
│   │   │   └── HeadToHeadResponse.java          # visible only after lock
│   │   └── mapper/
│   │       └── MatchPredictionMapper.java
│   │
│   └── league/
│       ├── controller/
│       │   └── LeaguePredictionController.java  # /api/v1/seasons/{id}/predictions/league
│       ├── service/
│       │   ├── LeaguePredictionService.java
│       │   └── LeaguePredictionServiceImpl.java # enforces 4-hour lock before first match
│       ├── repository/
│       │   └── LeaguePredictionRepository.java
│       ├── entity/
│       │   └── LeaguePrediction.java   # stores full leaderboard positions; FK → Season, User
│       ├── dto/
│       │   ├── SubmitLeaguePredictionRequest.java
│       │   └── LeaguePredictionResponse.java
│       └── mapper/
│           └── LeaguePredictionMapper.java
│
├── result/
│   ├── controller/
│   │   └── ResultController.java       # /api/v1/admin/matches/{id}/result  (ADMIN only)
│   ├── service/
│   │   ├── ResultService.java
│   │   └── ResultServiceImpl.java      # persists result, then publishes ResultPublishedEvent
│   ├── repository/
│   │   ├── MatchResultRepository.java
│   │   └── LeagueResultRepository.java
│   ├── entity/
│   │   ├── MatchResult.java            # winner, tossWinner, playerOfMatch; FK → Match
│   │   └── LeagueResult.java           # final standings; FK → Season
│   ├── dto/
│   │   ├── PublishMatchResultRequest.java
│   │   ├── PublishLeagueResultRequest.java
│   │   └── ResultResponse.java
│   ├── mapper/
│   │   └── ResultMapper.java
│   └── event/
│       └── ResultPublishedEvent.java   # ApplicationEvent payload; carries matchId/seasonId
│
├── scoring/
│   │   # Isolated domain concern: takes a published result + predictions → point awards.
│   │   # Decoupled from both result/ and leaderboard/ — both use it but neither owns it.
│   │   # Adding new point rules (bonus points, streaks) only touches this slice.
│   │
│   ├── service/
│   │   ├── ScoringService.java         # interface: List<PointAward> calculate(MatchResult, List<MatchPrediction>)
│   │   └── ScoringServiceImpl.java     # current rules: 1 point per correct field; ties = 1 point
│   └── dto/
│       └── PointAward.java             # record(userId, matchId, points, reason)
│
├── leaderboard/
│   ├── controller/
│   │   └── LeaderboardController.java  # GET /api/v1/seasons/{id}/leaderboard
│   ├── service/
│   │   ├── LeaderboardService.java
│   │   └── LeaderboardServiceImpl.java # @Async; listens for ResultPublishedEvent;
│   │                                   # calls ScoringService, persists entries, emails admin
│   ├── repository/
│   │   └── LeaderboardEntryRepository.java
│   ├── entity/
│   │   └── LeaderboardEntry.java       # FK → Season, User; carries totalPoints, rank
│   ├── dto/
│   │   └── LeaderboardResponse.java
│   └── mapper/
│       └── LeaderboardMapper.java
│
└── notification/
    │   # Two distinct concerns kept explicit:
    │   #  - channel/  = delivery infrastructure (swap or add channels independently)
    │   #  - scheduler/ = when to send (business timing logic)
    │   #  - service/   = what to send to whom (orchestration)
    │
    ├── controller/
    │   └── NotificationController.java  # POST /api/v1/admin/notifications/bulk  (ADMIN)
    ├── service/
    │   ├── NotificationService.java     # interface: orchestrates who gets what
    │   └── NotificationServiceImpl.java # resolves users, builds messages, delegates to channel
    ├── channel/
    │   ├── NotificationChannel.java     # interface: void send(EmailMessage msg)
    │   └── EmailChannel.java            # implements NotificationChannel via JavaMailSender
    ├── scheduler/
    │   └── NotificationScheduler.java   # @Scheduled jobs: pre-match reminder, post-result alert
    ├── repository/
    │   └── EmailLogRepository.java
    ├── entity/
    │   └── EmailLog.java               # to, subject, body, eventType, status, sentAt
    └── dto/
        ├── BulkEmailRequest.java        # record(List<Long> userIds, String eventType, String body)
        └── EmailLogResponse.java
```

---

## Resources & Migrations

```
src/main/resources/
├── application.properties              # Base config (no secrets, no credentials)
├── application-local.properties        # Local overrides — git-ignored
├── application-test.properties         # Testcontainers / in-test overrides
├── logback-spring.xml                  # Console + rolling file appender
└── db/migration/
    ├── V1__create_users.sql
    ├── V2__create_leagues.sql
    ├── V3__create_seasons.sql          # Separated from leagues for rollback granularity
    ├── V4__create_teams.sql
    ├── V5__create_players.sql
    ├── V6__create_matches.sql
    ├── V7__create_match_predictions.sql
    ├── V8__create_league_predictions.sql
    ├── V9__create_results.sql
    ├── V10__create_leaderboard.sql
    └── V11__create_email_logs.sql
```

---

## Test Structure (mirrors src/)

```
src/test/java/com/rajashekar/familyleague/
├── common/
│   └── BaseIntegrationTest.java        # @SpringBootTest + Testcontainers PostgreSQL setup
├── user/
├── league/
├── season/
├── team/
├── player/
├── match/
├── prediction/
│   ├── match/
│   └── league/
├── result/
├── scoring/
├── leaderboard/
└── notification/
```

---

## Cross-Slice Communication Rules

Slices must not import each other's service classes. Use these patterns in order of preference:

1. **ApplicationEvent** (for fire-and-forget after a state change)
   - `ResultServiceImpl` publishes `ResultPublishedEvent`
   - `LeaderboardServiceImpl` listens with `@EventListener` / `@TransactionalEventListener`
   - `NotificationServiceImpl` also listens for `ResultPublishedEvent` to email admin

2. **Repository injection across slices** (acceptable for read-only lookups)
   - `LeaderboardServiceImpl` may inject `MatchPredictionRepository` to fetch predictions for scoring
   - `ScoringServiceImpl` receives result + predictions as method parameters — it imports no repositories itself

3. **Never** import a service from another slice directly. If you feel the urge, introduce an event or move the logic to `scoring/` or `common/`.

---

## Domain Rules (enforce in service layer — never circumvent)

1. **Soft delete everywhere** — every entity extends `AuditableEntity`. Never call hard-delete.
   Use `@SQLRestriction("deleted_at IS NULL")` on every entity (Hibernate 6 — not the deprecated `@Where`).

2. **Points are never accepted via API** — scoring always runs in `ScoringServiceImpl`, triggered via event.

3. **Prediction lock windows are configurable, not hardcoded**
   ```
   app.prediction.match-lock-hours=1
   app.prediction.league-lock-hours=4
   ```
   Bind via `@ConfigurationProperties(prefix = "app.prediction")` in `prediction/config/PredictionProperties.java`.

4. **Lock enforcement** — `MatchPredictionServiceImpl`: reject if `now >= match.startTime - lockHours`.
   `LeaguePredictionServiceImpl`: reject if `now >= season.firstMatchStartTime - lockHours`.

5. **Head-to-head visibility** — expose other users' predictions only if `now > predictionLockTime`.
   Check in `MatchPredictionServiceImpl.getHeadToHead()`.

6. **Closed seasons are fully immutable** — if `season.status == CLOSED`, reject all writes to matches,
   results, and predictions — even for `ROLE_ADMIN`. Check at the top of every relevant write method.

7. **Leaderboard recalculation is always async** — `LeaderboardServiceImpl.recalculate()` is annotated
   `@Async` and triggered by `ResultPublishedEvent`, never called synchronously from a web request.

8. **Ties are valid results** — a tied match gives 1 point to users who predicted a tie.

---

## API Design Rules

- Base path: `/api/v1`
- Admin-only endpoints live under `/api/v1/admin/**`
- All success responses use `ApiResponse<T>`:
  ```json
  { "success": true, "data": {}, "message": null, "timestamp": "2026-04-25T10:00:00Z" }
  ```
- All error responses use `ErrorResponse`:
  ```json
  { "success": false, "code": "PREDICTION_LOCKED", "message": "...", "timestamp": "..." }
  ```
- All list endpoints accept `page`, `size`, `sort`, `search` as query params (`Pageable`).
- `POST` = create → `201 Created` + `Location` header.
- `PUT` = full replace, `PATCH` = partial update, `DELETE` = soft delete, `GET` = read.
- Use `@Valid` on every `@RequestBody` parameter.
- Never expose entity classes in responses — DTOs only.

---

## Security Rules

- `JwtService` issues and validates tokens. `JwtAuthFilter` reads every request.
- `UserDetailsServiceImpl` lives in `user/service/` — it loads `User` entities, so it belongs in the user slice, not `common/security/`.
- Role enum values: `ROLE_USER`, `ROLE_ADMIN`. Granted via `@PreAuthorize` at method level.
- Passwords hashed with `BCryptPasswordEncoder`. Never log raw passwords.
- No hardcoded credentials — secrets via `application-local.properties` or environment variables.

---

## Persistence Rules

- All entities extend `AuditableEntity` — `createdAt`, `createdBy`, `updatedAt`, `updatedBy`, `deletedAt`.
- `AuditorAware<String>` reads the current username from `SecurityContextHolder`.
- Use `@SQLRestriction("deleted_at IS NULL")` for soft-delete filtering (Hibernate 6 standard).
- Schema changes through Flyway only — `spring.jpa.hibernate.ddl-auto=validate` in all non-local profiles.
- Migration files: one concern per file, named `V{n}__{snake_case_description}.sql`.
- All read-only service methods annotated `@Transactional(readOnly = true)`.
- All associations default to `LAZY` fetch — never `EAGER`.
- Use explicit `@Column(nullable = false)` — rely on DB constraints, not only Hibernate validation.

---

## Notification Rules

- Persist `EmailLog` with `status = PENDING` **before** attempting delivery. Update to `SENT` or `FAILED` after.
- All cron expressions live in `application.properties`:
  ```
  scheduler.match-reminder.cron=0 0 * * * *
  scheduler.result-alert.cron=0 * * * * *
  ```
- Adding a new delivery channel (SMS, push) = add a new `NotificationChannel` implementation. No changes to `NotificationService`.

---

## Code Style Rules

- Constructor injection only — no `@Autowired` on fields or setters.
- Controllers are thin: validate → call service → map to response. Zero business logic.
- Use Java records for all DTOs (request and response). Use classes only when mutation is needed.
- `@Slf4j` (Lombok) on every class that logs:
  - `DEBUG` — method entry/exit with key identifiers in service layer
  - `INFO` — meaningful state changes (prediction submitted, result published, email sent)
  - `WARN` — recoverable anomalies (lock window check boundary, retried send)
  - `ERROR` — caught exceptions with full stack trace
- `logback-spring.xml` configures both console and rolling file appenders (`logs/family-league.log`, daily rollover).

---

## Testing Rules

- Unit tests mock repositories — test service logic in isolation.
- Integration tests use `@SpringBootTest` + Testcontainers PostgreSQL. No H2 — schema must match prod.
- `BaseIntegrationTest` in `common/` holds the shared `@Container` and `@DynamicPropertySource`.
- Controller tests use `@WebMvcTest` + `MockMvc`. Mock the service layer.
- Test class name = class under test + `Test` suffix: `MatchPredictionServiceTest`.
- Test method naming: `methodName_condition_expectedBehavior`.
- Every lock rule requires a dedicated negative test: attempt after lock → expect `PredictionLockedException`.
- Every closed-season write requires a negative test: attempt on closed season → expect `SeasonClosedException`.

---

## What NOT to do

- Do not use `@Autowired` field injection.
- Do not use `@Where` — it is deprecated in Hibernate 6. Use `@SQLRestriction`.
- Do not expose `Entity` objects in API responses.
- Do not hardcode lock durations — bind from `PredictionProperties` (`@ConfigurationProperties`).
- Do not call `leaderboardService.recalculate()` synchronously from any web request.
- Do not import one slice's service class into another slice's service class — use events or repository injection.
- Do not add Swagger annotations on entity classes — annotate DTOs and controllers only.
- Do not use `Optional.get()` — use `.orElseThrow(() -> new ResourceNotFoundException(...))`.
- Do not add a catch-all service (`AppService`) — every slice owns its logic.
- Do not commit `application-local.properties`.
- Do not use `spring.jpa.hibernate.ddl-auto=update` outside of `application-local.properties`.

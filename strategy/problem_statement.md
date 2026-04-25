# Family League
## Business Requirements — Version 1.0

---

## 1. Purpose

This document describes the business needs of the intended platform.

### Platform Definition

Intended and limited to the **Service (Backend)** end of the platform. User Interface is not in scope.

The platform is intended for:
- Family and friends to predict the outcome of an actual league
- Gain points when the predictions match the actual result
- Support multiple such leagues

### League Structure

- A **League** consists of a series of matches; matches are played between two teams at a time
- Each team consists of *n* number of players
- League schedule is an exact replica of the actual league

### Prediction Types

**League-level predictions:**
- Who will be at 1st, 2nd, 3rd positions as winners
- Predictions are allowed for the full leaderboard (1 to n, n being the number of teams)

**Match-level predictions:**
- Who wins the match
- Who wins the toss
- Who will be the Player of the Match

### Prediction Windows

- **League predictions** must close 4 hours prior to the first match start time
- **Match predictions** must close 1 hour prior to the match start time

### Results and Leaderboard

- Results will be manually updated by the Admin persona after the actual result is declared
- Leaderboard calculations are performed in **async mode** and the Admin is informed via email

### Notifications

- Users must receive emails prior to match prediction closure if they haven't submitted predictions
- Admin must receive email alerts for result updates

### Admin Capabilities

- Admin APIs to create the entire set of League information (API collection and docs together should guide the Admin persona)
- Ability to bulk communicate by choosing users, event types, and custom messaging

### Access and Visibility

- APIs must be protected by persona-based access
- Users are allowed to see each other's predictions **only after** the prediction window is closed
- Prediction window is open from the moment the League is started until closed by the system (as configured, 1 hour prior to start time)

### Data and Audit

- All emails must be stored in the data store (when, to whom, for what, timestamp, status, etc.)
- All data changes must be captured as standard audit data

---

## 2. Technical Needs

- **Framework:** Spring Boot-based backend web application
- **Database:** PostgreSQL as data store
- **Email:** Email notification support with scheduled email alerts
- All standard practices of backend development

---

## 3. Core Workflows

### 3.1 User, RBAC and ACL Management

- Authentication and Authorization
- Profile Updates (avatar name and profile picture for the avatar)

### 3.2 Match Prediction Flow

1. Match is created with schedule and lock time
2. User submits predictions before lock
3. System enforces lock *n* hours before the match starts
4. After lock, no edits are allowed
5. Head-to-head (among users) becomes available only after lock

### 3.3 Result Processing Flow

1. Admin explicitly publishes the result
2. Points are recalculated server-side
3. Leaderboard rankings are updated and notifications sent

### 3.4 League Flow

1. Admin creates the league setup
2. League predictions are opened
3. League predictions are locked 4 hours prior to the first match start time
4. League-level leaderboard of team positions is adjusted by each match result (part of match result processing)
5. League-level results are updated by Admin
6. Final leaderboard is calculated and updated
7. Admin verifies the results and closes the league
8. Closed leagues are accessible but **without any amend capability** (not even for Admin)

### League vs Season Definition

- Teams are independent of league season (each team could play many leagues of the same name)
- This forces a definition of **League** as an umbrella and **Season** as an instance of a league

---

## 4. Backend Requirements

### 4.1 API Framework

- Use **Spring Boot (Java)** for all application APIs
- Use Dependency Injection
- Use **Spring Security** for authentication and role-based authorization
- Use **Spring Validation** for request validation

### 4.2 Persistence

- Use **PostgreSQL** (MySQL can also be chosen but must be justified in the decision log)
- Use **Spring Data JPA** with Hibernate as the ORM
- Use **Flyway** for database migrations (optional but recommended)
- Use Spring Data repositories for data access patterns

### 4.3 Data Model

- Derive the data model based on functional need
- **No records get permanently deleted** — soft delete only, unless a need is logged as a decision in the decision log

---

## 5. Notifications

- Admin should have the ability to bulk communicate by choosing users and event types with custom messaging

---

## 6. Scoring Rules

- One correct prediction adds **one point** to the user's score in the league
- Ranks are allocated based on total points

### 6.1 Match Scoring

- Correct predictions for: Match winner, Player of the Match, Toss winner
- Ties count as official results — either side gets 1 point

### 6.2 Leaderboard

- Total points determine rank
- Rankings recalculate after each confirmed result

---

## 7. Security and Integrity

- HTTPS is required (local certs are acceptable)
- JWT sessions are required
- Role-based authorization must protect admin routes
- Prediction lock must be enforced at the database level
- Points must **never** be accepted via API — the system calculates them (not even Admin can submit points)

---

## 8. Submission Checklist

### Repository Requirements

- Submission preferably as a GitHub repo allowing Gopal and Rama as moderators
- `README.md` exists at the root and links to all other docs
- Repo is assessed by the **main branch** only
- App starts successfully from a clean clone following the steps in `README.md`
- All documentation is within the repo only
- Composition of all AI prompts mentioning the AI tool very clearly
- No hardcoded credentials, API keys, or personal data in the codebase

### Must Have

- Clear logging to the right level and detail (Console and File are must)
- Inline documentation should be apt
- Exception handling
- Consistent API agreements (request and response data structures)
- Pagination, search, and sort handled in all applicable APIs
- Interface-driven development where applicable
- API collection in any chosen form including OpenAPI documentation
- Authentication and Authorization (Simple Auth; OAuth is also acceptable)
- JWT-based token management
- Clear data model (very detailed model must exist)
- Decision log with justifications
- Standards of Java, JEE, Spring, and general backend development good practices
- Time-driven notifications must be through scheduling
- Configurability to the maximum
- Modularity to the cleanest possible limits
- Normalisation to the best extent
- Any decision made must be logged with justification
- APIs must be apt to the business needs (handling all needs via one API is not acceptable)

### Good to Have

- Google or other OAuth-based authentication
- Abstracting common attributes in ORM
- Auto-population of audit fields (`created_at`, `created_by`, `updated_at`, etc.)
- Unit Testing
- End-to-end testing of APIs
- Batch processing (e.g. sending emails, prediction score calculations)
- Caching (only if safeguarded with a proper eviction policy)

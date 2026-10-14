# Mini Trading Desk

A small broker-style order system, plus the support tooling around it (health checks, fault diagnosis, regression tests). Built to learn Java and Spring Boot and to show application-support skills for trading systems.

**Status:** planning. Nothing is built yet. This file is the plan.

---

## Instructions for Claude Code

I am building this to learn. **I write all the code myself.** Your role is tutor and reviewer, not author.

**Do**

- Work one step at a time, following the phases below in order. Do not skip ahead.
- Before each step, explain what we are building and why, and name the concepts I need (for example: dependency injection, transactions, connection pooling).
- I know C# and ASP.NET Core well. Explain Java and Spring Boot by mapping to what I know (controller to controller, EF Core to JPA, `IHostedService` to `@Scheduled`, and so on), and point out where the two differ.
- Tell me which file to create and what it should be responsible for, then let me write it.
- When I am stuck, give a hint first. Give a second, more specific hint if I ask. Show code only if I say **"show me"**.
- Review my code after each step: bugs, naming, missing edge cases, and anything not idiomatic in Java. Explain the reason for each comment.
- Ask me one or two questions at the end of each step to check that I understood it.
- Point me to the official docs for anything new, so I learn to read them.
- Tick the checkbox in this file when I confirm a step is finished.

**Do not**

- Do not write or edit source files unless I say "show me" or "write this one". This covers Java, React, Bash, and the Python feed script.
- Do not generate whole classes, even as examples. Short snippets of three to five lines to illustrate syntax are fine.
- Do not fix my bugs for me. Tell me where to look and what to check.
- Do not add libraries, tools, or features that are not in this plan. If you think one is needed, ask first.
- Do not run commands that change the project (installs, migrations, git commits) without asking.

**You may write these without asking:** `.gitignore`, and corrections to this README's checkboxes.

---

## Stack

| Piece | Choice |
|---|---|
| Backend | Java 21, Spring Boot (Web, Data JPA, Security, Actuator) |
| Database | PostgreSQL, schema managed by Flyway SQL scripts |
| Frontend | React with Vite |
| Run | Docker Compose (API, database, frontend on nginx) |
| Scripting | Bash |
| Tests | JUnit 5, Playwright for the UI regression suite |
| CI | GitHub Actions |
| Optional live prices | A small Python script reading ticks from MetaTrader 5 (Windows only, demo account) |

**Out of scope on purpose:** WebSockets, Kafka, Redis, message queues, microservices, Kubernetes, cloud infrastructure, and sending orders to MetaTrader 5. The frontend polls the API every few seconds. Simulators run as scheduled methods inside the one Spring Boot app.

### Price source

Prices come from one of two sources, chosen by a config value `price.source`:

| Value | Source | Used for |
|---|---|---|
| `simulator` (default) | Random price moves inside the app | Docker Compose, CI, anyone cloning the repo |
| `mt5` | Live ticks pushed in by the Python feed script | My own machine and the live demo |

The simulator stays the default because the MetaTrader 5 Python package only runs on Windows next to a logged-in terminal, so it cannot run in Compose or CI. Orders are always filled by the app's own fill simulator, whichever price source is on.

---

## What the app does

- **Login** with two roles: trader and admin.
- **Market watch:** 10 to 20 instruments whose prices change every few seconds (simulated by default, live from MetaTrader 5 as an option).
- **Order entry:** buy or sell, limit or market, quantity and price.
- **Order blotter:** today's orders and their status.
- **Amend and cancel** for orders that are still open.
- **Fill simulator:** fills orders when the simulated price reaches them, sometimes partially.
- **Positions and cash:** holdings with average cost, and the account balance.
- **Admin:** halt trading on an instrument, view all users' orders.

### Order rules

- Quantity must be a multiple of the lot size (100).
- A buy needs enough available cash. A sell needs enough holdings (no short selling).
- Orders on a halted instrument are rejected.
- A market order fills at the current price. A limit buy fills when price is at or below the limit, a limit sell when price is at or above it.
- Statuses: `NEW`, `PARTIALLY_FILLED`, `FILLED`, `CANCELLED`, `REJECTED`.
- Only `NEW` and `PARTIALLY_FILLED` orders can be amended or cancelled.

---

## Data model

| Table | Holds |
|---|---|
| `users` | login, password hash, role |
| `accounts` | one per trader, cash balance |
| `instruments` | symbol, name, lot size, last price, last price time, halted flag |
| `orders` | account, instrument, side, type, quantity, price, filled quantity, status, timestamps, version |
| `executions` | order, quantity, price, time (one order has many) |
| `positions` | account, instrument, quantity, average cost |
| `audit_log` | order, old status, new status, reason, time |

---

## API

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/auth/login` | Log in |
| POST | `/api/auth/logout` | Log out |
| GET | `/api/instruments` | Instruments with current prices |
| POST | `/api/orders` | Place an order |
| GET | `/api/orders` | My orders (filter by status) |
| PUT | `/api/orders/{id}` | Amend quantity or price |
| DELETE | `/api/orders/{id}` | Cancel |
| GET | `/api/orders/{id}/executions` | Fills for one order |
| GET | `/api/positions` | My positions |
| GET | `/api/account` | My cash balance |
| GET | `/api/admin/orders` | All orders (admin) |
| POST | `/api/admin/instruments/{id}/halt` | Halt or resume (admin) |
| POST | `/api/prices` | Receive ticks from the MT5 feed script (API key, only when `price.source=mt5`) |
| GET | `/actuator/health` | Health, including database and price freshness |

---

## Repo layout

```text
/backend        Spring Boot app
/frontend       React app
/scripts        Bash: start, stop, health checks, log analysis
/feed           Python MT5 tick feed (optional, Windows only)
/tests-e2e      Playwright regression suite
/docs
   /rca         One write-up per fault scenario
docker-compose.yml
README.md
```

---

## Build plan

Each phase lists what I build, what I should learn, and how I know it is done.

### Part A: the app

- [ ] **Phase 0: Setup**
  - Install JDK 21, Maven, Node, Docker. Generate the project with Spring Initializr.
  - Compose file with PostgreSQL only. One `GET /api/hello` endpoint.
  - *Learn:* Maven project structure, `application.properties`, how Spring Boot starts.
  - *Done when:* the app starts, connects to Postgres, and `/actuator/health` returns `UP`.

- [ ] **Phase 1: Schema and seed data**
  - Flyway scripts for all tables. Seed instruments, two traders, one admin.
  - Seed instruments with symbols my MT5 broker offers (for example EURUSD, XAUUSD), so both price sources share one list. Store prices with five decimal places.
  - *Learn:* Flyway versioned migrations, JPA entities and relationships, repositories.
  - *Done when:* dropping the database and restarting rebuilds everything.

- [ ] **Phase 2: Market watch and price simulator**
  - `GET /api/instruments`. A scheduled method that moves each price by a small random amount.
  - Put the simulator behind a `PriceSource` interface, so a second source can be added later without touching the rest of the app.
  - *Learn:* `@RestController`, `@Service`, `@Scheduled`, DTOs versus entities, interfaces and conditional beans.
  - *Done when:* calling the endpoint twice a few seconds apart shows different prices.

- [ ] **Phase 3: Login and roles**
  - Session login with Spring Security. Password hashing. Trader and admin roles.
  - *Learn:* the Spring Security filter chain, sessions and cookies, method-level authorisation.
  - *Done when:* unauthenticated calls get 401, and a trader calling an admin endpoint gets 403.

- [ ] **Phase 4: Order entry**
  - `POST /api/orders` and `GET /api/orders` with all validation rules.
  - Audit log row on every status change. Order ID on every log line.
  - *Learn:* bean validation, `@Transactional`, exception handling with `@ControllerAdvice`, SLF4J and MDC.
  - *Done when:* valid orders are saved as `NEW`, invalid ones are `REJECTED` with a clear reason, and I can trace one order through the log by its ID.

- [ ] **Phase 5: Fills, positions, cash**
  - Scheduled fill simulator. Executions, position updates, cash updates in one transaction.
  - *Learn:* transaction boundaries, average cost calculation, `BigDecimal` for money.
  - *Done when:* cash plus position value stays consistent after many fills.

- [ ] **Phase 6: Amend, cancel, admin**
  - Amend and cancel with optimistic locking. Halt and resume. Admin order view.
  - *Learn:* `@Version`, handling a race between cancel and fill, HTTP status codes for conflicts.
  - *Done when:* cancelling an order while the simulator fills it never corrupts the data.

- [ ] **Phase 7: Frontend**
  - Pages: login, market watch, order ticket, blotter, positions, admin. Poll every three seconds.
  - *Learn:* calling a session-based API from React, CORS, handling loading and error states.
  - *Done when:* I can log in, place an order, watch it fill, and see my position change.

- [ ] **Phase 8: Docker and scripts**
  - Dockerfiles for backend and frontend. Compose runs all three containers.
  - `scripts/start.sh`, `scripts/stop.sh`, `scripts/status.sh`.
  - *Learn:* multi-stage Docker builds, Compose networking, environment variables, Bash basics.
  - *Done when:* a fresh clone runs with one command.

- [ ] **Phase 9: Tests and CI**
  - Unit tests for order validation and fill logic. A few API tests. GitHub Actions on every push.
  - *Learn:* JUnit 5, Mockito, Spring Boot test slices.
  - *Done when:* CI is green and a deliberately broken rule turns it red.

### Part B: the support tooling

- [ ] **Phase 10: Daily health checks**
  - `scripts/healthcheck.sh`: containers up, API port answering, database reachable, disk space, error count in the last hour of logs, latest price newer than 30 seconds.
  - Prints a pass or fail report, exits non-zero on failure, runs from cron.
  - *Learn:* `curl`, `grep`, `awk`, exit codes, cron.
  - *Done when:* stopping the database makes the script report the right failure.

- [ ] **Phase 11: Log analyser**
  - `scripts/log-summary.sh`: group exceptions by type, count them, show first and last occurrence, and print every line for a given order ID.
  - *Done when:* I can answer "what happened to order 1234" with one command.

- [ ] **Phase 12: Fault injection and root cause write-ups**
  - Break the app five ways, diagnose each, and write it up in `docs/rca/`.
    1. Connection pool exhausted (a leaked connection).
    2. Slow blotter query (missing index, large table).
    3. Blocked order update (a long transaction holding a row lock).
    4. Memory leak (an unbounded cache) leading to out-of-memory.
    5. Stale prices (the simulator thread dies silently).
  - Each write-up: symptoms, how I investigated, root cause, fix, prevention.
  - *Learn:* thread dumps, heap dumps, `EXPLAIN ANALYZE`, `pg_stat_activity` and `pg_locks`, HikariCP metrics.
  - *Done when:* each scenario can be reproduced from its write-up.

- [ ] **Phase 13: Regression suite**
  - Playwright tests: login, place order, amend, cancel, rejected order, admin halt.
  - Runs in CI against the Compose stack as a deployment-readiness check.
  - *Done when:* the suite passes on a clean stack and fails when I break a rule.

### Part C: live prices from MetaTrader 5 (optional)

Can be done any time after Phase 9. Skip it if time is short; nothing else depends on it.

- [ ] **Phase 14: MT5 price feed**
  - Backend: `POST /api/prices` protected by an API key, active only when `price.source=mt5`. It updates the last price and last price time for known symbols and ignores unknown ones.
  - `feed/mt5_feed.py`: connects to the MT5 terminal, reads the latest tick for each symbol every second or two, and posts the batch to the API. It logs and retries when the terminal or the API is unreachable.
  - Re-run fault scenario 5 (stale prices) against the real feed by closing the terminal, and add what differs to its write-up.
  - *Learn:* the `MetaTrader5` Python package, API-key authentication, Spring profiles and conditional beans, and what a quiet feed looks like when a market is closed versus when the connection is broken.
  - *Done when:* the market watch shows live prices with `price.source=mt5`, and switching back to `simulator` needs only a config change.
  - *Rules:* demo account only. MT5 login and the API key live in a gitignored `.env`. No orders are ever sent to MT5.

### Part D: finishing

- [ ] **Phase 15: Publish**
  - Replace this plan with a proper README: screenshot, architecture diagram, run command, demo logins, links to the RCA write-ups.
  - Deploy somewhere with a public link if a free host allows it.

---

## Notes to myself

- Finish Part A before starting Part B. A working small app beats a half-built large one.
- The MT5 feed is a bonus. The app must always work with the simulator alone.
- The weekend health check will flag forex prices as stale because the market is closed. Decide how the check should tell "market closed" from "feed broken".
- Commit after every phase with a message that says what the phase added.
- Keep a `docs/learning-log.md` with one or two lines per session on what I learned and what confused me.

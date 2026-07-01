# Aminah E-Learning Production Plan

This is the shared checklist for turning the current Spring Boot Thymeleaf project into a production-ready medical e-learning web app.

## Current Preparation Status

- Project unpacked into the workspace.
- Java 21 and Maven are installed locally.
- Default profile added: `dev`.
- Hardcoded mail, SendGrid, Paymob, AWS, and database secrets removed from properties files.
- Local uploads, build output, IntelliJ files, and local config are ignored by Git.
- Embedded Postgres and demo seed data are now controlled by properties.
- Spring method security is enabled.
- Doctor security route was aligned from `/doctor/**` to the actual `/dr/**` controller route.
- Admin and student method security role checks now use `hasRole(...)`.
- Phase 8 UI polish fixed live navigation, empty states, and key Thymeleaf rendering issues.
- Phase 9 deployment baseline is Docker-first for Render/Railway and keeps optional integrations environment-driven.
- Phase 10 added focused automated QA coverage for cloud database URL handling and Paymob callback HMAC verification.
- Phase 11 added Flyway as the migration tool with an opt-in baseline schema migration.
- Phase 12 added an explicit first-admin bootstrap path for new shared environments.
- Phase 13 added production readiness probes, proxy-header support, graceful shutdown, and deployment health-check alignment.
- Phase 14 added idempotent Paymob success handling and deterministic certificate issuance at the service layer.
- Phase 15 added production launch configuration validation for required deployment variables and enabled integrations.
- Phase 16 verified the Render deployment smoke baseline and documented the remaining redeploy/readiness checks.

## Current Blocker

No current local build blocker. Maven succeeds when run with the Windows root trust store:

```powershell
$env:MAVEN_OPTS='-Djavax.net.ssl.trustStoreType=Windows-ROOT'
mvn clean test
```

## Phase 1: Build And Runtime Baseline

1. Fix local Maven certificate trust or configure a trusted company/local Maven mirror.
2. Run `mvn -DskipTests package`.
3. Fix compile errors.
4. Run with dev profile:

```powershell
mvn spring-boot:run
```

5. Confirm the app starts cleanly.
6. Confirm demo accounts work:

- Admin: `admin`
- Doctor: `drsaber`
- Student: `student1`

## Phase 2: Database And Seed Strategy

1. Local dev can use embedded Postgres or a real local PostgreSQL database through profile properties.
2. Old MySQL-style `data.sql` has been replaced with a no-op note; Java demo seeding is the active dev seed path.
3. Demo seed data is controlled by `APP_SEED_ENABLED` and is disabled in production.
4. Production uses `spring.jpa.hibernate.ddl-auto=validate`.
5. Flyway or Liquibase should be introduced once the schema is intentionally stabilized.
6. A secure first-admin setup path is still needed before production launch.

## Phase 3: Authentication And Authorization

1. User role storage is aligned with Spring Security authorities: `ROLE_ADMIN`, `ROLE_DR`, and `ROLE_STUDENT`.
2. Disabled accounts are now enforced by Spring Security during login.
3. Main route access is configured for anonymous, admin, doctor, and student paths.
4. Controller method security is enabled with `@PreAuthorize`.
5. Password policy, admin write-action hardening, and deeper reset-token review remain future hardening work.

## Phase 4: Core Learning Flows

1. Admin and doctor course-management flows are preserved.
2. Student preview access remains available for preview tutorials.
3. Tutorial content JSON, completion, and quiz endpoints now enforce enrollment access.
4. Tutorial completion updates course enrollment progress.
5. Certificate issuance is limited to completed enrollments and records `certificateIssued`.
6. Certificate generation returns a stable certificate number for repeat calls.
7. Certificate controller/UX, PDF/download generation, and certificate email delivery remain future production work.
8. Functional tests around admin, doctor, and student journeys remain future hardening work.

## Phase 5: Payment Flow

1. `PaymentController` is active for checkout, Paymob iframe creation, callback, and webhook endpoints.
2. Paymob sandbox credentials are loaded through environment variables.
3. Paid courses create pending enrollment/payment records before redirecting students to Paymob.
4. Paymob callback/webhook requests require HMAC verification before marking enrollments paid.
5. Failed and pending callbacks do not unlock course access.
6. Successful callback/webhook processing is idempotent and covered by focused service tests.
7. Full Paymob sandbox/browser integration tests remain future hardening work.

## Phase 6: Files And Storage

1. Local file uploads remain available for dev.
2. S3 is still created only when `AWS_ENABLED=true`.
3. Uploads validate size, extension, and content type for PDF and MP4 tutorials.
4. Stored filenames now use UUID prefixes and sanitized base names to prevent traversal and collisions.
5. Local tutorial files are served through an authenticated controller that checks admin, doctor ownership, preview access, or paid enrollment before streaming.
6. Signed/private delivery for S3-hosted paid course files remains a future production hardening task.

## Phase 7: Email

1. SendGrid is the selected production provider for account confirmation and password reset emails.
2. SendGrid credentials and sender address are loaded through environment variables only.
3. Dev keeps email disabled by default so registration/reset flows do not fail when no provider is configured.
4. Account confirmation links now point to `/profile/confirm`.
5. Gmail SMTP wiring remains available but is not the active production sender.
6. Payment receipt and certificate emails remain future hardening work.

## Phase 8: UI And Medical Product Polish

1. Live Thymeleaf layout, auth/profile, admin, doctor, and student course templates were reviewed for broken links and rendering issues.
2. Header and floating action navigation now point to active routes for admin, doctor, student, profile, and contact workflows.
3. Student course catalog, student learning, doctor course management, and admin course review pages now show explicit empty states.
4. Student course detail tutorial buttons render valid HTML for the offcanvas tutorial viewer.
5. Profile/reset/login templates no longer expose broken account-action links, stale `/login` routing, mojibake labels, or a page-level Bootstrap Icons CDN dependency.

## Phase 9: Production Deployment

1. Dockerfile added for Java 21 Spring Boot deployment and copies the built JAR without depending on the snapshot filename.
2. Render and Railway descriptors added.
3. `/actuator/health` is exposed for platform health checks.
4. Cloud `DATABASE_URL` values in `postgres://...` format are converted to JDBC configuration at startup.
5. Production config accepts optional Paymob and email variables without failing startup when those integrations are not enabled.
6. Use managed PostgreSQL and set required environment variables in the provider dashboard.
7. Use `JPA_DDL_AUTO=update` only for first shared testing, then switch back to `validate` after migrations are introduced.
8. Object storage and production email remain optional environment-driven integrations.

## Phase 10: Functional QA Baseline

1. Automated tests now cover Render/Railway-style `postgres://...` database URL conversion.
2. Automated tests now verify explicit database credentials are not overridden by credentials embedded in `DATABASE_URL`.
3. Automated tests now verify Paymob callback HMAC validation accepts valid payloads and rejects missing or tampered signatures.
4. Automated tests now verify replayed Paymob success events do not duplicate payment/enrollment writes.
5. Automated tests now verify completed-only certificate issuance and repeat-generation behavior.
6. `mvn clean test` is the baseline QA command and should remain green before deployment.
7. Full browser/provider QA is still required for admin course review, doctor course authoring, student enrollment, tutorial completion, Paymob sandbox checkout, email delivery, file upload/download, and certificate UX.

## Phase 11: Schema Migration Baseline

1. Flyway dependencies are present for PostgreSQL migrations.
2. `V1__baseline_schema.sql` captures the current JPA table structure as a baseline migration.
3. Flyway is opt-in through `FLYWAY_ENABLED=true` so existing dev and shared databases are not changed unexpectedly.
4. New empty shared databases can be created with Flyway enabled and `JPA_DDL_AUTO=validate` after the baseline is verified.
5. Existing shared databases should be backed up and baselined intentionally before enabling Flyway.
6. Future schema changes should be made through new `V2__...` migration files instead of relying on Hibernate auto-update.

## Phase 12: Production Admin Setup

1. First admin creation is controlled by `APP_BOOTSTRAP_ADMIN_ENABLED`.
2. The bootstrap runner creates one enabled `ADMIN` user only when no admin exists.
3. Username, email, password, and full name are loaded from environment variables.
4. Existing admin accounts are never overwritten.
5. If the requested bootstrap username or email already belongs to a non-admin user, startup fails instead of silently promoting the account.
6. Disable `APP_BOOTSTRAP_ADMIN_ENABLED` and remove the bootstrap password after verifying the first admin login.

## Phase 13: Operations And Readiness

1. Production exposes only Actuator health endpoints.
2. `/actuator/health/**` is permitted so platform liveness and readiness probes do not require login.
3. Render and Railway health checks point to `/actuator/health/readiness`.
4. Production respects proxy forwarding headers for HTTPS-aware links and redirects behind cloud load balancers.
5. Graceful shutdown is enabled with a 30 second shutdown phase timeout.
6. Metrics, structured logs, alerts, and external uptime monitoring remain future production hardening work.

## Phase 15: Production Launch Guardrails

1. The production profile validates launch-critical configuration at startup.
2. `APP_URL` must be an absolute public URL and cannot point to localhost.
3. A PostgreSQL JDBC datasource URL must be resolved from `DATABASE_URL` or `JDBC_DATABASE_URL`.
4. Embedded PostgreSQL and demo seed data must remain disabled in production.
5. Enabled optional integrations fail fast when their required secrets are missing.
6. Provider-level smoke tests for Paymob, SendGrid, and S3 remain manual until live credentials are configured.

## Phase 16: Render Deployment Verification

1. `https://aminah-elearning-1.onrender.com/profile/login` returns `200`.
2. `https://aminah-elearning-1.onrender.com/actuator/health` returns `200` with `status=UP`.
3. Protected student routes redirect to `/profile/login`.
4. HTTPS, secure session cookie, HSTS, frame, and content-type headers are present.
5. Current live readiness and liveness subpaths redirect to login, which indicates the deployed Render artifact likely predates the Phase 13 `/actuator/health/**` security change.
6. Redeploy the latest repository commit, then re-check `/actuator/health/readiness` and `/actuator/health/liveness`.
7. Full browser QA for admin, doctor, student, payment, email, and file flows remains future work.

## Immediate Next Steps

1. Push the latest local Phase 13-16 changes to GitHub.
2. Redeploy the Render service from the latest commit.
3. Re-check `/actuator/health/readiness` and `/actuator/health/liveness`.
4. Verify first-admin login and disable bootstrap variables afterward.
5. Continue manual browser QA for course browsing and checkout callbacks.

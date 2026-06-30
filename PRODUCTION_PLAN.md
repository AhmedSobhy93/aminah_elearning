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
5. Certificate generation still needs a dedicated controller/UX pass before production.
6. Functional tests around admin, doctor, and student journeys remain future hardening work.

## Phase 5: Payment Flow

1. `PaymentController` is active for checkout, Paymob iframe creation, callback, and webhook endpoints.
2. Paymob sandbox credentials are loaded through environment variables.
3. Paid courses create pending enrollment/payment records before redirecting students to Paymob.
4. Paymob callback/webhook requests require HMAC verification before marking enrollments paid.
5. Failed and pending callbacks do not unlock course access.
6. Duplicate/replay callback handling and full Paymob payload integration tests remain future hardening work.

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

1. Review all Thymeleaf templates for broken links and missing fragments.
2. Make dashboards clear for admin, doctor, and student workflows.
3. Improve course pages for a medical learning audience.
4. Make mobile layouts reliable.
5. Clean unused CSS/JS and duplicate frontend assets.

## Phase 9: Production Deployment

1. Dockerfile added for Java 21 Spring Boot deployment.
2. Render and Railway descriptors added.
3. `/actuator/health` is exposed for platform health checks.
4. Cloud `DATABASE_URL` values in `postgres://...` format are converted to JDBC configuration at startup.
5. Use managed PostgreSQL and set required environment variables in the provider dashboard.
6. Use `JPA_DDL_AUTO=update` only for first shared testing, then switch back to `validate` after migrations are introduced.
7. Object storage and production email remain optional environment-driven integrations.

## Immediate Next Steps

1. Push the project to GitHub.
2. Create a Render or Railway service from the repository.
3. Add managed PostgreSQL.
4. Set the environment variables documented in `README.md`.
5. Deploy and verify `/actuator/health`, login, course browsing, and checkout callbacks.

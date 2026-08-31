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
- Phase 17 hardened public registration, confirmation resend, and forgot-password privacy.
- Phase 18 normalized email delivery behind a structured sender interface and added payment/certificate notifications.
- Release hardening now enforces administrator and doctor ownership boundaries, published-course student access, inert article/quiz rendering, answer-free student quiz DTOs, server-side grading, bounded authentication throttles, and webhook-only payment activation.
- Paymob events now bind signed order/amount/currency/integration/merchant values, serialize state changes with a database lock, preserve pending checkout retries, and make refund/void revocation monotonic.
- The Render blueprint now uses Flyway with JPA validation and mounts persistent upload storage at `/app/uploads`.

## Current Blocker

Current deploy blocker/risk is operational rather than code compile:

- Render now starts the app and connects to PostgreSQL.
- First admin bootstrap can fail if the configured username/email already exists as a non-admin user.
- Use a unique bootstrap username/email or intentionally fix the existing database row.
- Disable bootstrap after the first admin login succeeds.

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

1. SendGrid is the selected production provider for account confirmation, password reset, payment receipt, and certificate-issued emails.
2. SendGrid credentials and sender address are loaded through environment variables only.
3. Dev keeps email disabled by default so registration/reset flows do not fail when no provider is configured.
4. Account confirmation links now point to `/profile/confirm`.
5. Gmail SMTP wiring remains available but is not the active production sender.
6. Receipt page/PDF and provider delivery QA remain future hardening work.

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

## Feature Inventory

### Implemented Or Partially Implemented

- Public site: home, about, contact.
- Authentication: login, registration, email confirmation tokens, password reset tokens.
- Admin: user list/detail/edit/enable/delete, course list/detail review.
- Doctor: course CRUD, sections, tutorials, quiz questions, reorder, publish, student progress fragments.
- Student: browse/search courses, enroll, course detail, tutorial view, quiz submission, progress.
- Payment: Paymob iframe checkout, HMAC callback/webhook, idempotent success completion.
- Email: SendGrid production sender, Gmail/SMTP support kept as optional.
- File storage: local protected uploads and optional S3 upload configuration.
- Certificates: deterministic service-level certificate issuance for completed enrollments.
- Deployment: Docker, Render/Railway descriptors, health checks, production config validator.

### Not Yet Complete

- SMS provider for registration/OTP/password reset notifications.
- Production-grade registration UX with resend confirmation, expired token recovery, and duplicate account handling.
- Doctor/admin creation flow for production users with password setup/reset.
- Certificate download/PDF page.
- Payment invoice/receipt page.
- Paymob sandbox/live end-to-end test run with real IDs.
- S3 signed/private download behavior for paid course files.
- Structured audit logs for admin/doctor/payment actions.
- Auth rate limiting, password policy, CSRF mutation hardening, and admin GET-mutation cleanup.
- Browser end-to-end QA automation.
- Observability beyond health checks.

## Configuration Roadmap

### Core Production

- `SPRING_PROFILES_ACTIVE=production`
- `APP_URL=https://<active-render-url-or-custom-domain>`
- `DATABASE_URL` or `JDBC_DATABASE_URL`
- `JPA_DDL_AUTO=update` only for first shared schema creation, then `validate`
- `FLYWAY_ENABLED=true` after baseline strategy is confirmed

### First Admin

- `APP_BOOTSTRAP_ADMIN_ENABLED=true`
- `APP_BOOTSTRAP_ADMIN_USERNAME`
- `APP_BOOTSTRAP_ADMIN_EMAIL`
- `APP_BOOTSTRAP_ADMIN_PASSWORD` with at least 12 characters
- `APP_BOOTSTRAP_ADMIN_FULL_NAME`
- Set `APP_BOOTSTRAP_ADMIN_ENABLED=false` and remove the password after successful login.

### Email

- `APP_EMAIL_ENABLED=true`
- `APP_EMAIL_PROVIDER=sendgrid`
- `APP_EMAIL_FROM`
- `SENDGRID_API_KEY`
- Keep `MAIL_*` SMTP values optional unless Gmail/SMTP is intentionally selected later.

### SMS Future Provider

Add SMS behind a provider abstraction with startup disabled by default:

- `SMS_ENABLED=false`
- `SMS_PROVIDER=twilio` or selected regional provider
- `SMS_FROM`
- Provider-specific account ID/key/token variables
- Optional message templates for registration, password reset, and payment notification

Recommended first SMS use cases:

1. Registration welcome/verification notification.
2. Password reset notification.
3. Payment success notification.
4. Certificate issued notification.

### Payment

- `PAYMOB_API_KEY`
- `PAYMOB_INTEGRATION_ID`
- `PAYMOB_MERCHANT_ID`
- `PAYMOB_HMAC_SECRET`
- `PAYMOB_IFRAME_ID`
- `APP_URL` must match the live callback base URL.
- Add separate sandbox/live notes once Paymob live credentials are available.

### Storage

- `AWS_ENABLED=false` by default
- `AWS_S3_BUCKET`
- `AWS_S3_FOLDER`
- Future signed delivery will need S3 region/credentials or platform IAM strategy documented before implementation.

## Future Phases

## Phase 17: Production Account And Registration Hardening

1. Public registration now creates `STUDENT` accounts only.
2. Registration detects duplicate username/email before saving.
3. Inactive users can request a fresh confirmation link.
4. Forgot-password uses a generic response to avoid email enumeration.
5. Focused tests cover registration duplicates, role enforcement, resend confirmation, and reset privacy.
6. Admin/doctor invite or creation workflow remains future work.

## Phase 18: Email Notification Completion

1. Registration, password reset, payment receipts, and certificate notifications now use the `EmailService` abstraction.
2. SendGrid delivery now returns structured `EmailResult` values for sent, skipped, and failed outcomes.
3. Payment receipt email is sent only when a gateway payment first transitions to `SUCCESS`, so callback/webhook replays do not resend.
4. Certificate-issued email is sent only when a completed enrollment is first marked `certificateIssued`.
5. SendGrid sender/domain verification steps are documented in `README.md`.
6. A retry queue, receipt page, and certificate PDF/download UX remain future work.

## Phase 19: SMS Notification Foundation

1. Add `SmsService` interface and disabled-by-default no-op implementation.
2. Add provider configuration model for Twilio or chosen SMS provider.
3. Add production validator rules only when `SMS_ENABLED=true`.
4. Add SMS notifications for registration and password reset first.
5. Add docs for SMS env vars and provider setup.

## Phase 20: Paymob Sandbox And Receipt QA

1. Verify real Paymob sandbox iframe ID, integration ID, merchant ID, API key, and HMAC secret.
2. Run paid-course checkout end to end on Render.
3. Verify callback and webhook both mark payment once.
4. Add receipt page or payment history entry for students.
5. Document sandbox-to-live switch procedure.

## Phase 21: Certificate UX And PDF

1. Add student certificate route visible only for completed paid/free enrollments.
2. Generate certificate PDF or printable HTML with deterministic certificate number.
3. Add doctor/admin verification lookup by certificate number.
4. Send certificate email when issued.
5. Add tests for unauthorized and incomplete-course access.

## Phase 22: File Delivery And S3 Production Hardening

1. Verify local protected download authorization for admin, doctor owner, preview, and paid student.
2. Add S3 private object strategy and signed URLs or proxy streaming.
3. Document max file sizes and allowed content types.
4. Add malware scanning/manual moderation decision if required for public uploads.
5. Add storage integration tests where feasible without live AWS.

## Phase 23: Security Hardening

1. Add password complexity and breached/common password policy.
2. Add rate limiting for login, registration, forgot-password, and Paymob webhook.
3. Convert admin GET mutation routes to POST with CSRF.
4. Review CSRF ignores and keep only webhook-safe exceptions.
5. Add security tests for role access and mutation protection.

## Phase 24: Controller Cleanup And Maintainability

1. Remove legacy commented duplicate controller blocks after confirming active routes.
2. Split oversized `StudentController` and `DoctorController` only along existing feature boundaries.
3. Replace ad hoc runtime exceptions with user-safe errors where they affect UX.
4. Keep URLs stable unless redirects are added.
5. Add focused tests before and after cleanup.

## Phase 25: Browser End-To-End QA

1. Add smoke scripts for login, admin user page, doctor course creation, student browse/enroll, and health checks.
2. Add Render smoke verification commands.
3. Add manual QA checklist for Paymob, SendGrid, SMS, and S3 live integrations.
4. Consider Playwright only after key flows stabilize.

## Phase 26: Observability And Audit

1. Add request correlation ID logging.
2. Add audit events for admin user changes, doctor course changes, payment success/failure, and certificate issuance.
3. Add operational dashboard checklist using Render logs and uptime monitor.
4. Add alerting runbook for failed deploys, DB connection errors, payment callback failures, and email/SMS provider errors.

## Immediate Next Steps

1. Fix the existing Render admin bootstrap conflict by using a unique bootstrap username/email or intentionally updating the existing DB row.
2. Set `APP_URL` to the active Render URL or configured custom domain.
3. Verify `/profile/login`, `/actuator/health`, `/actuator/health/readiness`, and `/actuator/health/liveness`.
4. Login as admin, create or enable doctor/student accounts, then disable bootstrap.
5. Start Phase 19 before adding SMS or certificate PDF/download UX features.

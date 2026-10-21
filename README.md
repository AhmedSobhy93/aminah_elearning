# Aminah E-Learning

Medical e-learning web app built with Java 21, Spring Boot 3.5.6, Thymeleaf, Spring Security, Spring Data JPA, PostgreSQL, Paymob, email notifications, and optional AWS S3 storage.

## Completion plan and documentation map

The active application is `aminah_elearning-github`; preserve the separate
`aminah_elearning-master` copy. The goal is a polished, fully working application
with verified ADMIN, DR and STUDENT journeys and a reproducible production deployment.

- [PRODUCTION_PLAN.md](PRODUCTION_PLAN.md): active acceptance checklist, execution gates, evidence status and blockers.
- [DESIGN.md](DESIGN.md): current architecture, payment flow and UI/accessibility criteria.
- [AGENTS.md](AGENTS.md): project execution and preservation rules.
- This README: local setup, integration contracts and deployment operations.

Code/configuration presence and historical phase reports are not acceptance.
Keep automated, local-browser, provider-sandbox and live-production evidence separate.
Preserve Java 21, Spring Boot 3.5.6, Thymeleaf, PostgreSQL, existing URLs and roles;
dependency upgrades and optional new products are separate work.

## IntelliJ Local Setup

1. Open the folder that contains `pom.xml`.
2. Use JDK 21.
3. Let IntelliJ import the Maven project.
4. Run `com.aminah.elearning.ElearningApplication`.

The default profile is `dev`. It is configured for local development; demo seed data is opt-in.

## Local Commands

```powershell
mvn -DskipTests package
mvn spring-boot:run
```

On Windows hosts that block Java's default selector loopback socket, set
`TOMCAT_NIO2_ENABLED=true` before starting the application. This opt-in switch
only changes the embedded Tomcat connector used by that local run.

## Dev Accounts

These accounts are created by the dev seed runner when `APP_SEED_ENABLED=true` and `APP_SEED_DEFAULT_PASSWORD` is set:

- Admin: `admin`
- Doctor: `drsaber`
- Student: `student1`

Set a local demo password with:

```env
APP_SEED_ENABLED=true
APP_SEED_DEFAULT_PASSWORD=change-this-for-shared-testing
```

## Database Strategy

- Development can use embedded PostgreSQL with `EMBEDDED_POSTGRES_ENABLED=true`.
- Shared staging/production should use managed PostgreSQL through `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD`.
- SQL init is disabled; the old `data.sql` is intentionally a no-op.
- Demo seed data is controlled by `APP_SEED_ENABLED`.
- Production keeps `APP_SEED_ENABLED=false` and `spring.jpa.hibernate.ddl-auto=validate`.
- Flyway defaults to enabled in production; development can opt in with `FLYWAY_ENABLED=true`.
- The current baseline migration is `src/main/resources/db/migration/V1__baseline_schema.sql`.

## Authentication Strategy

- Application users authenticate through `UserService`.
- Roles are stored as the `Role` enum and exposed to Spring Security as `ROLE_ADMIN`, `ROLE_DR`, or `ROLE_STUDENT`.
- Disabled users cannot log in; registration keeps accounts disabled until verification.
- Public self-registration always creates `STUDENT` accounts; doctor/admin accounts should be created or promoted by an admin workflow.
- Registration checks duplicate username/email before saving and offers confirmation-link resend for inactive accounts.
- Forgot-password responses do not reveal whether an email address exists.
- Route access is enforced in `SecurityConfig` and controller-level `@PreAuthorize` checks.
- Set `APP_URL` in shared environments so verification and password reset links point to the deployed app.

## First Admin Bootstrap

For the first production/staging database, create one enabled admin through environment variables:

```env
APP_BOOTSTRAP_ADMIN_ENABLED=true
APP_BOOTSTRAP_ADMIN_USERNAME=admin
APP_BOOTSTRAP_ADMIN_EMAIL=admin@example.com
APP_BOOTSTRAP_ADMIN_PASSWORD=change-this-long-random-password
APP_BOOTSTRAP_ADMIN_FULL_NAME=Initial Administrator
```

The bootstrap password must be at least 12 characters. The bootstrap runs only when no `ADMIN` user exists. After the account is created and login is verified, set `APP_BOOTSTRAP_ADMIN_ENABLED=false` and remove the password variable from the cloud dashboard.

## Learning Flow Strategy

- Students can view preview tutorials without course access.
- Completing tutorials and submitting quizzes requires an active course enrollment with `paymentStatus=SUCCESS`.
- Tutorial completion updates the related course enrollment progress.
- Locked tutorial endpoints return forbidden responses instead of exposing content.

## UI Baseline

- Main navigation uses active role-based routes for admin users, doctors, and students.
- Course catalog, student learning, doctor course management, and admin course review pages include empty states for first-run or filtered results.
- Profile and reset-password templates avoid stale routes and unsupported profile actions.
- Shared templates use local static assets already bundled with the app.

## File Storage Strategy

- Local uploads remain available for development under `uploads/`.
- S3 upload support is active only when `AWS_ENABLED=true`.
- Tutorial uploads validate file extension, content type, and size before storage.
- Stored filenames are generated with UUID prefixes to prevent collisions and avoid trusting user-supplied paths.
- Local tutorial files are served through an authenticated `/uploads/...` controller that checks preview, doctor/admin ownership, or paid enrollment access.

## Paymob Sandbox Integration

Configure sandbox credentials through environment variables:

- `PAYMOB_ENABLED=true`
- `PAYMOB_API_KEY`
- `PAYMOB_INTEGRATION_ID`
- `PAYMOB_MERCHANT_ID`
- `PAYMOB_HMAC_SECRET`
- `PAYMOB_IFRAME_ID`

Payment flow baseline:

- Free courses activate enrollment immediately.
- Paid courses create a pending enrollment and payment before opening the Paymob iframe.
- The browser callback is display-only and never changes payment or enrollment state.
- The JSON webhook is the source of truth. It must pass constant-time HMAC verification and match the local order, amount in integer cents, `EGP` currency, integration ID, and merchant ID.
- Pending events do nothing. Failures mark pending payments failed; signed refund/void events revoke enrollment and cannot be reversed by replaying an older success event. Only a successful, non-pending, non-refunded, non-voided event activates enrollment.
- Webhook handling locks the payment row and is idempotent, so concurrent or replayed events do not duplicate state changes or receipts.
- Retrying an unchanged pending checkout reuses its Paymob order, so payment through an earlier-open iframe is not lost.
- Configure the Paymob callback URL as `${APP_URL}/payments/callback` and webhook URL as `${APP_URL}/payments/webhook`.

## Certificate Strategy

- Course completion is based on the enrollment `completed` flag updated by tutorial/quiz progress.
- Certificates can be generated only for completed enrollments.
- Certificate generation marks the enrollment as issued and returns a stable certificate number in the format `CERT-{userId}-{courseId}-{enrollmentId}`.
- Certificate issuance sends a notification email the first time the certificate is generated.
- Completed active enrollments expose a certificate link in My Learning. `/student/certificates/{courseId}` previews eligibility, POST `/issue` issues via the existing deterministic service, and GET `/download` returns printable HTML after issuance. Open the downloaded file and use Print → Save as PDF when a PDF is needed. Issuance is row-locked; pending/failed/refunded enrollments are denied.

## Email Strategy

- Application email is routed through the `EmailService` abstraction and returns a structured `EmailResult`.
- SendGrid is the selected production email provider for account confirmation, password reset, payment receipt, and certificate-issued emails.
- Development keeps email disabled by default with `APP_EMAIL_ENABLED=false`; registration and reset flows should not fail just because email is off.
- Production email requires `APP_EMAIL_ENABLED=true`, `APP_EMAIL_PROVIDER=sendgrid`, `APP_EMAIL_FROM`, and `SENDGRID_API_KEY`.
- In SendGrid, verify either the exact single sender address or authenticate the sending domain before setting `APP_EMAIL_FROM`.
- Use a verified `APP_EMAIL_FROM` address that matches the SendGrid sender/domain identity; otherwise SendGrid may reject or suppress delivery.
- Payment receipt emails are sent only when a gateway payment first transitions to `SUCCESS`, so callback/webhook replays do not resend receipts.
- Certificate emails are sent only on first certificate issuance; repeat certificate generation keeps the same certificate number without resending.
- SMTP/Gmail configuration remains available for future use, but it is not the active production sender.

## Cloud Deployment

The project includes deployment descriptors for Render and Railway:

- `Dockerfile` builds the Java 21 Spring Boot JAR and runs it with the `production` profile.
- `render.yaml` configures a Render web service and `/actuator/health/readiness` health check.
- Render mounts a 5 GB persistent disk at `/app/uploads` so local tutorial media survives deploys.
- `railway.json` configures Railway to deploy from the Dockerfile and use `/actuator/health/readiness`.

For Render manual setup, create the web service as:

- Runtime: `Docker`
- Dockerfile path: `Dockerfile`
- Health check path: `/actuator/health/readiness`
- Build command and start command: leave empty so Render uses the Dockerfile

If Render runs `yarn` or looks for `package.json`, the service is configured as a Node app instead of Docker.

Required environment variables:

- `SPRING_PROFILES_ACTIVE=production`
- `APP_URL`
- `DATABASE_URL` or `JDBC_DATABASE_URL`
- `FLYWAY_ENABLED=true`
- `JPA_DDL_AUTO=validate`

`BASIC_AUTH_USER` and `BASIC_AUTH_PASSWORD` are optional Spring Boot fallback credentials. They are not used for Aminah form login at `/profile/login`; real admin access is created with `APP_BOOTSTRAP_ADMIN_*`.

Production startup validates the launch configuration. The app fails fast when:

- `APP_URL` is missing, invalid, or still points to localhost.
- No PostgreSQL JDBC URL can be resolved from `DATABASE_URL` or `JDBC_DATABASE_URL`.
- Embedded PostgreSQL or demo seed data is enabled.
- Email is enabled without `APP_EMAIL_FROM`, or SendGrid email is enabled without `SENDGRID_API_KEY`.
- S3 is enabled without `AWS_S3_BUCKET`.
- Paymob is enabled without its API, integration, merchant, iframe, or HMAC settings.

The checked-in Render blueprint uses Flyway plus schema validation. For an existing database, take a backup and verify that duplicate payment gateway-order or user/course enrollment rows do not exist before the `V2__payment_event_constraints.sql` migration runs.

Production release requires `FLYWAY_ENABLED=true` and `JPA_DDL_AUTO=validate`.
Do not use Hibernate schema auto-update to deploy this completion plan.
For an existing database, back up and review schema/history compatibility before
running migrations. The production properties currently enable baseline-on-migrate
at version 0; this setting does not prove that an existing schema is compatible.
Existing migrations are V1 and V2. Add future changes with the next unused version;
do not alter applied migrations or blindly baseline a shared database.

Optional production integrations:

- Paymob: `PAYMOB_ENABLED=true`, `PAYMOB_API_KEY`, `PAYMOB_INTEGRATION_ID`, `PAYMOB_MERCHANT_ID`, `PAYMOB_HMAC_SECRET`, `PAYMOB_IFRAME_ID`
- Email: `APP_EMAIL_ENABLED=true`, `APP_EMAIL_PROVIDER=sendgrid`, `APP_EMAIL_FROM`, `SENDGRID_API_KEY`
- S3 uploads: `AWS_ENABLED=true`, `AWS_S3_BUCKET`, `AWS_S3_FOLDER`
- SMS is outside the completion scope unless required by an existing acceptance criterion; no SMS configuration is needed for this release.

Paymob, SendGrid, Gmail SMTP, and S3 variables can be left unset for initial infrastructure testing. The app will still start; those flows should be tested after their provider variables are configured.

The detailed feature roadmap, remaining phases, and integration configuration strategy live in `PRODUCTION_PLAN.md`.

## Operations Baseline

- Production exposes only Actuator health endpoints.
- `/actuator/health`, `/actuator/health/liveness`, and `/actuator/health/readiness` are public for platform probes.
- Production uses framework forward-header support so generated links and redirects work behind Render/Railway HTTPS proxies.
- Graceful shutdown is enabled with a 30 second shutdown phase timeout.

## Render Smoke Test

After each Render deploy, verify:

- Login page: `${APP_URL}/profile/login` returns `200`.
- Generic health: `${APP_URL}/actuator/health` returns `200` and `{"status":"UP"}`.
- Readiness: `${APP_URL}/actuator/health/readiness` returns `200`.
- Liveness: `${APP_URL}/actuator/health/liveness` returns `200`.
- A protected student URL such as `${APP_URL}/student/my-courses` redirects to `/profile/login`.

If readiness or liveness redirects to login while generic health is public, Render is likely running an older build that does not include the `/actuator/health/**` security rule. Redeploy the latest repository commit and re-run the smoke test.

## QA Baseline

Run the automated baseline before deploying:

```powershell
$env:MAVEN_OPTS='-Djavax.net.ssl.trustStoreType=Windows-ROOT'
mvn clean test
```

Current automated coverage includes:

- Cloud `DATABASE_URL` conversion for Render/Railway-style PostgreSQL URLs.
- Protection against overriding explicit database credentials with credentials embedded in `DATABASE_URL`.
- Production readiness configuration for health probes, graceful shutdown, and proxy headers.
- Production launch configuration validation.
- An explicit anonymous/STUDENT/DR/ADMIN route matrix for admin, user-management, doctor, upload, student, checkout, webhook, fallback-authentication, and CSRF behavior.
- Structured email sender disabled/configuration behavior.
- Flat callback and nested webhook HMAC acceptance/rejection checks.
- Payment binding checks for order, exact cents, currency, integration, and merchant.
- Payment success, pending, failure, refund/void, browser-callback refusal, mismatch, and replay/idempotency scenarios.
- Doctor create/update ownership, principal-bound profile updates, and registration/login throttle scenarios.
- Concurrent checkout protection through enrollment row locking and the database one-payment-per-enrollment constraint.
- Payment notification dispatch after transaction commit, outside the gateway row-lock scope.
- Payment receipt notification on first successful gateway completion.
- Certificate issuance and repeat-generation checks.
- Certificate-issued notification on first certificate generation.
- Baseline migration coverage plus uniqueness constraints for gateway orders and per-student course enrollments.

Historical reports recorded Render login and generic health smoke checks. They are not current production verification. Recheck the candidate artifact, all role journeys, sandbox Paymob, SendGrid delivery, upload persistence, certificates, readiness and liveness. No fresh build, browser, provider or deployment verification was performed for the 2026-10-07 documentation reconciliation.

## Notes

- Keep secrets in environment variables only.
- Production should use managed PostgreSQL and schema migrations.
- See `PRODUCTION_PLAN.md` for the shared rollout checklist.

## Release, backup and rollback procedure

1. Run read-only preflight, review the candidate diff and resolve ownership of pre-existing changes. Record branch/full commit, dirty state and exact deploy artifact identity. Use a task branch when safe; never absorb unrelated changes into a release.
2. Run focused tests, `mvn clean test`, `mvn -DskipTests package` and `git diff --check`. Start locally and execute the role-journey checklist in PRODUCTION_PLAN.md at mobile/tablet/desktop widths. Record failures and unexecuted checks honestly. Docker skips tests, so its build alone is insufficient.
3. Confirm the actual Render service, active HTTPS APP_URL, database, environment settings and persistent disk before deploying. Historical URL: `https://aminah-elearning-1.onrender.com`; current availability and deployed commit are unverified. Do not switch to Railway automatically.
4. Before migrations, take a provider database snapshot or PostgreSQL custom-format backup and back up local upload media from the persistent disk. Keep credentials in secure environment/provider settings. Record backup time/location and migration version without secrets. Rehearse restore into an isolated database and verify users, enrollments, payment records and media references before relying on it.
5. Review Flyway history, V1/V2 compatibility and uniqueness requirements. Keep production schema validation enabled. An existing schema or duplicate rows may require an explicitly approved remediation; do not drop data, rewrite migration history or blindly baseline.
6. Confirm `/app/uploads` disk mounting and effective upload directory. Check enabled Paymob, SendGrid and optional S3 configuration securely. Disabled providers allow infrastructure tests but do not satisfy paid checkout or email acceptance.
7. For first admin, inspect collision risk without exposing user data. If a non-admin owns the configured username/email, choose a unique approved bootstrap identity; never silently promote or overwrite the user. Verify ADMIN login, then disable bootstrap and remove the bootstrap password.
8. Deploy only after required gates pass, to the established target. The completion request permits reviewed task changes to be pushed if deployment requires it. Target ambiguity, unavailable credentials, paid provisioning or destructive migrations require user input; complete independent work first.
9. Record deployed URL, commit/image identity, deploy result and migration version. Run the post-deployment checks below before marking the release accepted.

For rollback, retain the prior known-good commit/image and provider configuration.
Check whether the prior application supports the migrated schema before redeploying it.
Do not automatically reverse migrations. If database restoration is necessary, obtain
explicit approval, stop writes in a controlled maintenance window, restore the verified
backup and matching uploads, and reconcile payments received since the backup before
resuming service. Recheck probes, login, role access and payment state after rollback.

## Post-deployment acceptance

- Health, readiness and liveness return success; public pages and login render over HTTPS; generated redirects/links respect proxy headers.
- ADMIN user/role and course-review journeys work; DR can author/publish owned content and view only authorized progress; STUDENT can enroll, learn, submit quizzes and retrieve eligible certificates.
- Negative checks deny anonymous/cross-role/cross-owner access and reject missing CSRF on state changes. Paid pending/failed enrollments remain locked; quiz responses contain no answers.
- Verify database persistence and migration history, upload access, and an upload still available after redeployment.
- Use Paymob sandbox credentials to prove bound signed webhook activation, failure/retry, replay and refund/void revocation. Browser callbacks remain display-only.
- Verify enabled SendGrid notifications and optional S3 private access separately from automated tests. Do not claim live payment/email delivery from local mocks.
- Record browser console, responsive/keyboard checks, invalid input and error/retry results by acceptance row.

If access blocks release, deliver the tested deployable JAR/configuration and precise
missing access or decision. An artifact prepared locally is not a deployed release.

## Current candidate verification (2026-10-21)

The active branch is `codex/finalize-elearning`, based on `01f3657f5fa84b81a34296c9fdabb9a6e4dda9ca`. Existing edits are preserved; no commit, push or deployment has occurred.

- `mvn clean test package`: 77 tests passed, zero failures/errors/skips. Edited JavaScript passes Node syntax checks; `git diff --check` is required before handoff.
- Chrome/Playwright exercised ADMIN name/email/role edits, duplicate-email recovery, disable/enable/login denial and disposable-user deletion.
- DR browser checks exercised course/section/tutorial creation and editing, quiz replacement/validation, PDF upload rejection/retry, real MP4 upload, student progress, and cancellation/deletion. Course edit values and tutorial preview/order are preserved.
- STUDENT checks covered preview-only access, free enrollment, protected PDF/video access, failed completion retry, quiz validation/wrong/right retries, answer-free quiz responses, 100% progress and certificate issuance. PDF/article completion now requires an explicit action; video can complete on playback end. Progress updates only after server success.
- Provider-disabled checkout retries and pending enrollment status work; unsigned browser callbacks do not unlock paid content. No payment-provider or email-provider calls were made.
- Thirty responsive checks covered ten primary pages at 375/768/1440 px. Login keyboard navigation, dialog/viewer focus restoration, viewer load-failure retry, and no unexpected JavaScript exceptions were verified. This is targeted accessibility QA, not a comprehensive WCAG certification.
- Checkout uses the existing local shared assets. Shared navigation/button contrast, narrow-layout containment, readable progress states, accessible controls and friendly error pages were improved without changing framework/dependency versions.

See `PRODUCTION_PLAN.md` for the final artifact checksum, packaged-app smoke result and exact evidence boundaries. Render deployment, provider sandbox/live verification, optional S3 delivery and upload survival across Render redeployment remain pending **at the user's request**. No new external access is required to use the tested local candidate.

On this host, system-drive space became exhausted during browser QA. Only conversation-created disposable QA data was removed or relocated to D:; repository files and unrelated user data were preserved. Test scratch should use a drive with adequate free space. The installed preflight helper still assumes a Maven parent POM and fails on this dependency-management POM; manual read-only Git/manifest checks were used instead.
## Free compute with durable external storage

The user selected a full launch with durable files rather than a disposable preview on 2026-10-21. `render-free.yaml` prepares an alternative to the existing disk-backed `render.yaml`. It uses free Docker compute, external managed PostgreSQL (for example Neon), and a private S3-compatible bucket (for example Cloudflare R2). It does not provision these external accounts or claim a completed deployment.

Render free compute sleeps after inactivity and has an ephemeral filesystem; Render advises against using free instances for production. Its free PostgreSQL expires after 30 days. R2 has a free Standard-storage allowance with billable overages. Check current account quotas, spending controls, and provider terms before activating anything; zero ongoing cost and production availability are not guaranteed.

Sources checked 2026-10-21: [Render free limits](https://render.com/docs/free), [Render Blueprint reference](https://render.com/docs/blueprint-spec), [Neon free storage](https://neon.com/blog/neon-free-plan-1-gb-per-project), [R2 pricing](https://developers.cloudflare.com/r2/pricing/), [R2 Java SDK configuration](https://developers.cloudflare.com/r2/examples/aws/aws-sdk-java/).

### Required secure settings

- Select `render-free.yaml` explicitly when creating a separate Render Blueprint. Both the compute plan and lack of persistent disk are deliberate; `render.yaml` retains the existing paid disk-backed option. Automatic deployments are disabled.
- Use a new isolated database for initial cloud verification. Set `JDBC_DATABASE_URL=jdbc:postgresql://<host>:5432/<database>?sslmode=require`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` in Render. Do not paste the provider's `postgresql://user:password@...` URL into a JDBC field or commit it. Use the provider's appropriate direct endpoint for Flyway, and confirm connection limits/backup procedures before launch.
- Set `APP_URL` to the actual HTTPS service URL. Keep `SPRING_PROFILES_ACTIVE=production`, `FLYWAY_ENABLED=true`, `JPA_DDL_AUTO=validate`, demo seeding off, and embedded PostgreSQL off.
- For R2 set `AWS_ENABLED=true`, `AWS_REGION=auto`, `AWS_S3_ENDPOINT=https://<account-id>.r2.cloudflarestorage.com`, `AWS_S3_BUCKET`, `AWS_S3_FOLDER=tutorials`, `AWS_ACCESS_KEY_ID`, and `AWS_SECRET_ACCESS_KEY`. Use a private bucket with narrowly scoped object read/write credentials. Do not enable a public bucket or public development URL. AWS S3 can omit the custom endpoint and use its actual AWS region and normal credential chain.
- Normal DR PDF/video uploads now use this backend when enabled. Database paths remain `/uploads/...`; object keys are `tutorials/uploads/...`. Authenticated file requests pass the same role/ownership/publication/enrollment checks before a five-minute signed download redirect. Redirects and object responses request no caching. A signed URL is a temporary bearer capability; already issued URLs can remain usable until expiry.
- Existing local upload bytes are not migrated automatically. Before enabling the backend for an existing database, copy and verify every referenced file under the matching object key, preserve the original bytes, and verify readback and rollback. Remote failures never silently fall back to the free instance's ephemeral disk. Changing storage mode does not change or delete database records/files.
- Configure `APP_EMAIL_ENABLED=true`, `APP_EMAIL_PROVIDER=sendgrid`, `APP_EMAIL_FROM` and `SENDGRID_API_KEY`. Use the HTTPS SendGrid API; Render free instances block common SMTP ports. Sender verification and delivery tests remain required.
- Paymob remains disabled until test merchant settings are added and the sandbox suite passes. Use the existing Paymob variables and callback/webhook instructions above; no real charges are authorized by this setup.
- On a new database, add the approved unique `APP_BOOTSTRAP_ADMIN_*` identity securely, enable bootstrap for the initial creation, verify login, then disable bootstrap and remove the bootstrap password. Never silently promote an existing identity.

### Cloud release gates

Local tests of storage routing/signing are not provider tests. Before launch verify actual bucket upload/readback, private anonymous denial, cross-role/unenrolled denial, MP4 playback/range seeking including signed-link expiry recovery, upload survival after Render redeployment, database migration/backup restoration, resource limits/cold start, SendGrid delivery, and Paymob sandbox success/failure/replay/refund handling. Paid provisioning or overages require explicit approval; do not select a paid fallback automatically.

### Strict $0 account setup

For the user's no-billing/no-upgrade requirement, use an account-confirmed free PostgreSQL project and a private S3-compatible bucket without payment activation. Backblaze B2 is the current storage candidate: its S3 signup advertises no credit card required and its published allowance includes the first 10 GB. Verify the actual account's upload/download/API caps before launch; stop if setup requires payment or exceeds free allowances. R2 remains an optional compatible backend, not an instruction to activate billing.

For B2 use `AWS_REGION=<bucket-region>` and `AWS_S3_ENDPOINT=https://s3.<bucket-region>.backblazeb2.com`, plus a private `AWS_S3_BUCKET` and restricted application-key ID/secret in the existing AWS credential variables. `render-free.yaml` now prompts for region instead of assuming R2's `auto`. The existing upload/signing code supports a custom HTTPS S3 origin. Real B2 readback, playback, expiry/range behavior and redeployment persistence still require account verification. Keep the bucket private; do not reuse public signed URLs as permanent tutorial paths.

Account creation, password entry, verification, and acceptance of provider terms are user steps. No payment method or paid upgrade should be added. References: [B2 S3 signup](https://www.backblaze.com/sign-up/s3), [B2 pricing](https://www.backblaze.com/cloud-storage/pricing), [B2 S3-compatible API](https://www.backblaze.com/docs/cloud-storage-s3-compatible-api).

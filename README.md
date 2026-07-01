# Aminah E-Learning

Medical e-learning web app built with Java 21, Spring Boot 3.5.6, Thymeleaf, Spring Security, Spring Data JPA, PostgreSQL, Paymob, email notifications, and optional AWS S3 storage.

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
- Flyway migrations are available but opt-in with `FLYWAY_ENABLED=true`.
- The current baseline migration is `src/main/resources/db/migration/V1__baseline_schema.sql`.

## Authentication Strategy

- Application users authenticate through `UserService`.
- Roles are stored as the `Role` enum and exposed to Spring Security as `ROLE_ADMIN`, `ROLE_DR`, or `ROLE_STUDENT`.
- Disabled users cannot log in; registration keeps accounts disabled until verification.
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

The bootstrap runs only when no `ADMIN` user exists. After the account is created and login is verified, set `APP_BOOTSTRAP_ADMIN_ENABLED=false` and remove the password variable from the cloud dashboard.

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

- `PAYMOB_API_KEY`
- `PAYMOB_INTEGRATION_ID`
- `PAYMOB_MERCHANT_ID`
- `PAYMOB_HMAC_SECRET`
- `PAYMOB_IFRAME_ID`

Payment flow baseline:

- Free courses activate enrollment immediately.
- Paid courses create a pending enrollment and payment before opening the Paymob iframe.
- Paymob callback/webhook requests must pass HMAC verification before the enrollment is marked `SUCCESS`.
- Successful Paymob callback/webhook handling is idempotent, so replayed success events do not duplicate state changes.
- Configure the Paymob callback URL as `${APP_URL}/payments/callback` and webhook URL as `${APP_URL}/payments/webhook`.

## Certificate Strategy

- Course completion is based on the enrollment `completed` flag updated by tutorial/quiz progress.
- Certificates can be generated only for completed enrollments.
- Certificate generation marks the enrollment as issued and returns a stable certificate number in the format `CERT-{userId}-{courseId}-{enrollmentId}`.
- A dedicated student certificate page/download/email workflow still needs a later UX pass before launch.

## Email Strategy

- SendGrid is the selected production email provider for account confirmation and password reset emails.
- Development keeps email disabled by default with `APP_EMAIL_ENABLED=false`; registration and reset flows should not fail just because email is off.
- Production email requires `APP_EMAIL_ENABLED=true`, `APP_EMAIL_PROVIDER=sendgrid`, `APP_EMAIL_FROM`, and `SENDGRID_API_KEY`.
- SMTP/Gmail configuration remains available for future use, but it is not the active production sender.

## Cloud Deployment

The project includes deployment descriptors for Render and Railway:

- `Dockerfile` builds the Java 21 Spring Boot JAR and runs it with the `production` profile.
- `render.yaml` configures a Render web service and `/actuator/health/readiness` health check.
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
- `BASIC_AUTH_PASSWORD`

Production startup validates the launch configuration. The app fails fast when:

- `APP_URL` is missing, invalid, or still points to localhost.
- No PostgreSQL JDBC URL can be resolved from `DATABASE_URL` or `JDBC_DATABASE_URL`.
- Embedded PostgreSQL or demo seed data is enabled.
- Email is enabled without `APP_EMAIL_FROM`, or SendGrid email is enabled without `SENDGRID_API_KEY`.
- S3 is enabled without `AWS_S3_BUCKET`.

For first shared testing on a new managed PostgreSQL database, set:

```env
JPA_DDL_AUTO=update
APP_SEED_ENABLED=false
AWS_ENABLED=false
APP_EMAIL_ENABLED=false
```

After the schema is stabilized, switch `JPA_DDL_AUTO` back to `validate` and keep future schema changes in Flyway migration files.

Flyway rollout:

```env
FLYWAY_ENABLED=true
JPA_DDL_AUTO=validate
```

Use Flyway first on a verified empty database, or back up and intentionally baseline an existing shared database before enabling it. Future schema changes should be added as new `V2__...` migration files.

Optional production integrations:

- Paymob: `PAYMOB_API_KEY`, `PAYMOB_INTEGRATION_ID`, `PAYMOB_MERCHANT_ID`, `PAYMOB_HMAC_SECRET`, `PAYMOB_IFRAME_ID`
- Email: `APP_EMAIL_ENABLED=true`, `APP_EMAIL_PROVIDER=sendgrid`, `APP_EMAIL_FROM`, `SENDGRID_API_KEY`
- S3 uploads: `AWS_ENABLED=true`, `AWS_S3_BUCKET`, `AWS_S3_FOLDER`

Paymob, SendGrid, Gmail SMTP, and S3 variables can be left unset for initial infrastructure testing. The app will still start; those flows should be tested after their provider variables are configured.

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
- Paymob callback HMAC acceptance/rejection checks.
- Paymob success callback replay/idempotency checks.
- Certificate issuance and repeat-generation checks.
- Baseline migration resource coverage for the current JPA table set.

Render smoke testing has verified the deployed login page and generic health endpoint. Manual QA still needs to cover first-admin login, admin user/course pages, doctor course authoring, student enrollment and tutorial completion, Paymob sandbox checkout, email delivery, file upload/download, and readiness/liveness after redeploying the latest Phase 13+ code.

## Notes

- Keep secrets in environment variables only.
- Production should use managed PostgreSQL and schema migrations.
- See `PRODUCTION_PLAN.md` for the shared rollout checklist.

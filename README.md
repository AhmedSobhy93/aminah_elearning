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

## Authentication Strategy

- Application users authenticate through `UserService`.
- Roles are stored as the `Role` enum and exposed to Spring Security as `ROLE_ADMIN`, `ROLE_DR`, or `ROLE_STUDENT`.
- Disabled users cannot log in; registration keeps accounts disabled until verification.
- Route access is enforced in `SecurityConfig` and controller-level `@PreAuthorize` checks.
- Set `APP_URL` in shared environments so verification and password reset links point to the deployed app.

## Learning Flow Strategy

- Students can view preview tutorials without course access.
- Completing tutorials and submitting quizzes requires an active course enrollment with `paymentStatus=SUCCESS`.
- Tutorial completion updates the related course enrollment progress.
- Locked tutorial endpoints return forbidden responses instead of exposing content.

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
- Configure the Paymob callback URL as `${APP_URL}/payments/callback` and webhook URL as `${APP_URL}/payments/webhook`.

## Email Strategy

- SendGrid is the selected production email provider for account confirmation and password reset emails.
- Development keeps email disabled by default with `APP_EMAIL_ENABLED=false`; registration and reset flows should not fail just because email is off.
- Production email requires `APP_EMAIL_ENABLED=true`, `APP_EMAIL_PROVIDER=sendgrid`, `APP_EMAIL_FROM`, and `SENDGRID_API_KEY`.
- SMTP/Gmail configuration remains available for future use, but it is not the active production sender.

## Cloud Deployment

The project includes deployment descriptors for Render and Railway:

- `Dockerfile` builds the Java 21 Spring Boot JAR and runs it with the `production` profile.
- `render.yaml` configures a Render web service and `/actuator/health` health check.
- `railway.json` configures Railway to deploy from the Dockerfile.

Required environment variables:

- `SPRING_PROFILES_ACTIVE=production`
- `APP_URL`
- `DATABASE_URL` or `JDBC_DATABASE_URL`
- `BASIC_AUTH_PASSWORD`

For first shared testing on a new managed PostgreSQL database, set:

```env
JPA_DDL_AUTO=update
APP_SEED_ENABLED=false
AWS_ENABLED=false
APP_EMAIL_ENABLED=false
```

After the schema is stabilized, switch `JPA_DDL_AUTO` back to `validate` and introduce Flyway or Liquibase migrations.

Optional production integrations:

- Paymob: `PAYMOB_API_KEY`, `PAYMOB_INTEGRATION_ID`, `PAYMOB_MERCHANT_ID`, `PAYMOB_HMAC_SECRET`, `PAYMOB_IFRAME_ID`
- Email: `APP_EMAIL_ENABLED=true`, `APP_EMAIL_PROVIDER=sendgrid`, `APP_EMAIL_FROM`, `SENDGRID_API_KEY`
- S3 uploads: `AWS_ENABLED=true`, `AWS_S3_BUCKET`, `AWS_S3_FOLDER`

## Notes

- Keep secrets in environment variables only.
- Production should use managed PostgreSQL and schema migrations.
- See `PRODUCTION_PLAN.md` for the shared rollout checklist.

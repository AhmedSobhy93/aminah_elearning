Java 21
Spring Boot 3.5
Thymeleaf
PostgreSQL
Constructor injection only
No hardcoded secrets
Run mvn clean test after changes
Do not rewrite the whole app

Do not scan unrelated files.
Do not rewrite templates unless needed.
Do not refactor working code.
Stop after this phase.
Ask before starting next phase.

Stop when:
- mvn clean test passes
- application starts
- no secrets remain in config
- README is updated

## Product Direction

Aminah E-Learning is a production medical e-learning app with three primary roles:
- ADMIN manages users, reviews courses, and owns production setup.
- DR creates and manages courses, sections, tutorials, quizzes, and student progress visibility.
- STUDENT registers, confirms account, enrolls, pays for paid courses, learns through tutorials/quizzes, tracks progress, and receives certificates.

Preserve this role model unless the user explicitly approves a product change.

## Phase Workflow

For every requested phase:
1. Read this file first.
2. Inspect only the files needed for the phase.
3. State the intended scope before editing.
4. Make minimal safe changes.
5. Preserve existing business logic and URLs unless a broken route is the task.
6. Add or update focused tests for changed service/config/security behavior.
7. Run `mvn clean test`.
8. If runtime/config/deployment behavior changed, perform a local startup smoke test when feasible.
9. Explain changed files and remaining risks.
10. Stop after the requested phase.

For documentation-only planning phases, update `PRODUCTION_PLAN.md` and/or `README.md`; tests are optional unless code/config changed.

## Integration Rules

- Payment: Paymob is the current provider. Keep the webhook HMAC-protected, payment-bound, idempotent, and authoritative. Do not unlock paid content on failed or pending states; refunds and voids must revoke access monotonically.
- Email: SendGrid is the production provider. Gmail/SMTP may remain available but should not be the default production path.
- SMS: Not implemented yet. Add through a provider abstraction first, likely Twilio or another region-appropriate SMS gateway, with `SMS_ENABLED=false` by default.
- Storage: Local upload remains dev/default. S3 is optional and must stay gated by `AWS_ENABLED=true`.
- Certificates: Current service records deterministic certificate issuance. Future UX/PDF/email work must build on that service instead of creating a second certificate flow.
- Database: Prefer Flyway migrations for schema changes. Avoid relying on `JPA_DDL_AUTO=update` beyond first shared testing.

## Configuration Rules

- All secrets must come from environment variables.
- Never commit provider keys, DB credentials, personal email passwords, or Paymob secrets.
- Any new integration must have:
  - enable/disable flag,
  - provider name when multiple providers are possible,
  - required environment variables documented in `README.md`,
  - production validator coverage when enabled,
  - local/dev behavior that does not block startup when disabled.

## Known Production Configs

- Core: `SPRING_PROFILES_ACTIVE`, `APP_URL`, `DATABASE_URL` or `JDBC_DATABASE_URL`, `JPA_DDL_AUTO`.
- First admin: `APP_BOOTSTRAP_ADMIN_ENABLED`, `APP_BOOTSTRAP_ADMIN_USERNAME`, `APP_BOOTSTRAP_ADMIN_EMAIL`, `APP_BOOTSTRAP_ADMIN_PASSWORD`, `APP_BOOTSTRAP_ADMIN_FULL_NAME`.
- Email: `APP_EMAIL_ENABLED`, `APP_EMAIL_PROVIDER`, `APP_EMAIL_FROM`, `SENDGRID_API_KEY`.
- Paymob: `PAYMOB_ENABLED`, `PAYMOB_API_KEY`, `PAYMOB_INTEGRATION_ID`, `PAYMOB_MERCHANT_ID`, `PAYMOB_HMAC_SECRET`, `PAYMOB_IFRAME_ID`.
- S3: `AWS_ENABLED`, `AWS_S3_BUCKET`, `AWS_S3_FOLDER`.
- Future SMS: `SMS_ENABLED`, `SMS_PROVIDER`, provider account SID/key/token/from number.

## Current Architectural Debt To Respect

- `StudentController` contains legacy commented duplicate code. Do not expand that duplication; clean it in a dedicated phase.
- Administrator state changes use CSRF-protected POST routes; do not reintroduce state-changing GET requests.
- Some integration services return strings instead of structured results. Improve only when touching that integration.
- UI templates contain older commented blocks. Do not rewrite broadly; remove only when the phase is specifically template cleanup.

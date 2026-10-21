# Aminah E-Learning Production Plan

## Goal and scope

Deliver a polished medical e-learning application with verified ADMIN, DR, and STUDENT journeys and a reproducible production deployment. This is the active completion plan; README.md supplies setup and operations guidance, DESIGN.md supplies architecture and UI criteria, and AGENTS.md supplies execution rules.

Preserve Java 21, pinned Spring Boot 3.5.6, Thymeleaf, PostgreSQL, constructor injection, existing architecture, URLs, and role model. Keep dependency upgrades separate. Do not rewrite the application or add SMS, invitations, invoice systems, certificate lookup, or observability products unless an existing acceptance criterion requires them.

## Evidence and starting point

Documentation reconciled on 2026-10-07. This update is documentation-only: no fresh Maven, runtime, browser, provider, or production verification has been performed.

- Active checkout: `aminah_elearning-github`; do not automatically merge or edit `aminah_elearning-master`.
- Recorded branch: `master`; HEAD: `01f3657f5fa84b81a34296c9fdabb9a6e4dda9ca`; upstream: `origin/master`; one application worktree at `D:/aminah elearning/aminah_elearning-github`.
- Existing changes at intake: AGENTS.md, README.md, application-dev.properties, and untracked TomcatNio2Config.java/TomcatNio2ConfigTest.java. Preserve them; confirm ownership before including code in a release.
- Source/configuration includes role authorization, registration/resend/reset, enrollment/progress, payment handling, deterministic certificate issuance, protected local uploads, optional S3, SendGrid, production validation, Docker, and deployment descriptors. Presence is not journey acceptance.
- Historical reports describe automated security/payment/configuration coverage and Render login/health smoke checks. They are evidence to rerun, not current pass results.
- Established deployment history points to Render. Railway configuration remains available; switching targets requires an explicit decision.
- Previously recorded Render URL: `https://aminah-elearning-1.onrender.com`. Current availability, deployed identity, bootstrap status, and probe behavior are unverified.

## Acceptance checklist

Use the active completion evidence below for partial results; unrecorded scenarios remain **not reverified**. Route families are existing entry points, not a promise that every desired workflow has a route. Resolve exact behavior from active controller methods and templates; ignore commented legacy code.

| ID | Journey and pages | Backend acceptance | Required evidence |
| --- | --- | --- | --- |
| A1 | Public home/catalog/details: `/`, `/student/courses`, `/student/course/{id}` | Verify anonymous access against current security contracts; expose only intended published content; search, links, and empty results work | Browser public/anonymous checks plus access tests |
| A2 | Register, confirm, resend: `/profile/register`, `/profile/confirm`, `/profile/resend-confirmation` | STUDENT-only registration, duplicates handled, disabled until confirmation, invalid/expired token recovery and resend feedback | Focused controller/service tests; browser inputs; SendGrid sandbox delivery |
| A3 | Login/logout/reset/profile: `/profile/login`, `/profile/logout`, `/forgot-password`, `/reset-password`, `/profile/profile` | Disabled users denied; reset responses preserve privacy; token expiry/reuse handled; profile principal-bound; logout conforms to security configuration | Tests and browser keyboard/error/retry checks |
| S1 | Catalog, previews, enrollment and `/student/my-courses` | Preview policy enforced; free enrollment activates once; paid enrollment stays locked while pending/failed | Student browser flow and anonymous/cross-user negative tests |
| S2 | Checkout/status: `/payments/buy/{courseId}`, `/payments/create/{courseId}`, `/payments/callback` | Callback is display-only; signed bound webhook activates once; retries preserve pending order; refund/void revocation is monotonic | Automated payment tests and Paymob sandbox checkout/webhook/replay evidence |
| S3 | Learning: `/student/tutorial/{id}`, tutorial JSON, completion and quiz actions | Enrollment/ownership enforced server-side; safe article rendering; no answers in student DTOs; server-side grading; progress consistent | Service/security tests plus actual tutorial/quiz browser completion |
| S4 | Certificate UX/download (route to be implemented from existing service contract) | Completed-only owner access; stable deterministic number; repeated downloads do not issue twice or resend notification | Unauthorized/incomplete/repeat tests; browser/download inspection; email evidence |
| D1 | DR authoring: `/dr/courses`, section/tutorial/quiz actions and fragments | Only owner/authorized admin can manage content; CRUD/reorder/uploads work with validation and CSRF | DR browser authoring plus cross-doctor/invalid upload tests |
| D2 | DR publication/progress: publish action and students fragment | Publication/review follows existing model; unauthorized progress visibility denied | DR/ADMIN workflow and access tests |
| M1 | ADMIN users: `/admin/users`, `/users` | Role/enable/update/delete controls work under current contracts; POST mutations require CSRF; never silently promote bootstrap collisions | ADMIN browser checks and role/CSRF negative tests |
| M2 | ADMIN review/operations: `/admin/courses`, probes | Review controls match backend; readiness/liveness public; no operational secrets exposed | Review journey, probe checks, production config tests |
| U1 | All templates and shared assets | Consistent medical UI, functioning visible actions, accessible forms/states/focus and responsive layouts | Mobile/tablet/desktop browser QA, console inspection and keyboard checks |
| O1 | Deployment, migrations, uploads, enabled integrations | Reproducible artifact, managed PostgreSQL/Flyway validation, HTTPS/proxy correctness, persistent protected media | Build identity, deploy logs, post-deploy role flows, DB and redeployment persistence evidence |

Record each result as passed, failed, skipped, blocked, or not executed. Evidence must identify command/scenario, environment, artifact identity, outcome, and limitation without secrets or personal data. Keep automated, local-browser, provider-sandbox, and live-production results separate.

## Execution order and gates

1. **Starting point:** read-only preflight, applicable instructions, complete Git status, branch/full HEAD/upstream/worktrees. Use a dedicated `codex/` branch when safe; no new worktree. Stop for ambiguous unrelated edits before a branch change. Preserve both copies.
2. **Launch blockers:** audit acceptance rows against source/config/tests; repair security, payment, bootstrap, migration, or storage blockers first. Do not mark existing code as verified.
3. **Functional completion:** complete auth, STUDENT, DR, and ADMIN journeys with the smallest coherent changes. Build certificate UI/download on the existing issuance service.
4. **UI polish:** apply DESIGN.md criteria to existing templates/assets; resolve broken controls, labels, states, and rendering defects without wholesale replacement.
5. **Verification:** focused tests, `mvn clean test`, `mvn -DskipTests package`, and `git diff --check`; start app and exercise actual browser journeys. Docker's skipped tests do not replace the test gate.
6. **Deployment readiness:** inspect Render service identity, environment, DB backup/migration state, persistent disk, probes, proxy behavior and bootstrap collision. Prepare reviewed artifact and rollback procedure.
7. **Deployment:** the completion request authorizes deployment to the established target after verification passes; push only reviewed task changes if required. Credentials, target ambiguity, paid provisioning, or destructive migrations require specific user input after independent work is complete.
8. **Close:** update README and this plan with exact results, deployed URL/artifact identity or precise blocker, every task-modified file, residual Git state, and one next action.

The completion request authorizes continuing between these phases without routine phase approval. A later narrower request, including documentation-only work, limits the current execution scope.

## Integration and security invariants

- Paymob is the current provider. Verify HMAC in constant time, bind order/exact integer cents/EGP/integration/merchant, serialize/idempotently process events, and use only webhook-authoritative activation. Pending/failed payments never unlock content. Browser redirects cannot activate enrollment. Refund/void revocation cannot be undone by older success events.
- SendGrid is the production sender. Verify confirmation/reset/payment/certificate delivery when enabled. Automated sender tests do not prove provider delivery. Payment/certificate replay must not duplicate notifications.
- Validate upload size/type/extension and enforce ownership/preview/enrollment access. Render local storage must persist at `/app/uploads`; prove survival across redeployment. Keep S3 gated by `AWS_ENABLED` and verify private paid-content delivery before enabling it.
- Server-side role/ownership authorization, CSRF on state changes, safe content rendering, and answer-free student quiz responses are required.
- Secrets remain environment-only and must never appear in logs, documentation, commits, or evidence.
- Production requires managed PostgreSQL, `FLYWAY_ENABLED=true`, `JPA_DDL_AUTO=validate`, and disabled embedded DB/demo seeding. Append new migrations after existing V1/V2; never rewrite applied migrations.
- Never silently promote, overwrite, or delete an existing user to fix bootstrap. Use a unique approved identity or an explicitly authorized data correction.

## Deployment runbook and external blockers

Use README.md's deployment, backup/restore, rollback, and post-deployment procedures. Before release, obtain access to the actual Render service and managed database, verify the active APP_URL and deployed commit/image, and supply sandbox credentials for enabled providers through secure environment configuration.

Known risks to verify: bootstrap username/email collision; older deployed readiness rules; migration compatibility/duplicate rows; persistent disk mounting; certificate download gap; S3 private delivery if enabled; incomplete real-browser/provider coverage. These are pending checks, not newly observed production failures.

If external access blocks deployment, finish independent implementation and tests, deliver the deployable artifact/configuration, and state the exact missing access or decision. Never describe an undeployed artifact as deployed.

## Historical baseline

Earlier phases introduced dev/config setup, role security, UI fixes, Docker/Render/Railway descriptors, Flyway V1/V2, admin bootstrap, probes/proxy support, registration/resend/reset, structured email, deterministic certificates, and payment/security hardening. Old build and Render smoke claims predate this acceptance checklist and must be rerun against the candidate artifact.

SMS foundation, provider switching, controller restructuring, invoice products, and expanded observability are deferred unless a current acceptance criterion requires them. Historical phase numbering is not a prerequisite for certificate or browser work.

## Current next action

Git baseline and task branch are established with existing changes preserved. Continue the acceptance checklist from the active evidence below; do not deploy before the remaining provider and production gates pass.

## Active completion work — 2026-10-21

- Git diagnosis: the physical checkout is healthy. The restricted sandbox reports contradictory worktree errors for the aliased C: path; approved execution resolves the checkout to D:. No Git metadata repair, reset, clean, stash, or existing-file relocation was necessary.
- Created `codex/finalize-elearning` at `01f3657f5fa84b81a34296c9fdabb9a6e4dda9ca`. Hash comparison verified all seven pre-existing changed/untracked files were preserved during branch creation. No commit or push yet.
- Bundled preflight failed because its strict-mode Maven parser assumes a parent POM exists. The application uses dependency management instead. Manual Git and manifest checks substitute for that broken script; do not change the POM to satisfy the helper.
- Audit repairs: production schema guard rejects disabled Flyway or non-validation JPA modes before Hibernate schema generation; invalid/reused reset tokens show recovery feedback; certificate page, CSRF-protected issuance and printable HTML download use the existing deterministic service and active/completed enrollment; issuance uses a pessimistic enrollment lock.
- Targeted UI changes: shared mobile viewport/language/focus/reduced-motion support, explicit login/reset labels, and eligible certificate links in My Learning. This is incremental polish, not acceptance of all templates.
- Automated candidate gate: `mvn clean test package` passed 69 tests, zero failures/errors/skips, and produced the executable JAR. The first earlier overlapping build attempt observed a newly added test against stale compiled code and failed; the subsequent focused gate and clean candidate gate passed.
- Runtime/browser and provider results will be recorded below. Render service access, production database backup/migration state, upload persistence and provider-sandbox delivery remain unverified. No deployment has occurred.
### Local browser evidence

Chrome via installed Playwright; Browser plugin unavailable. Candidate ran at `http://127.0.0.1:18080` with disposable embedded PostgreSQL, email/Paymob/S3 disabled, random local-only seeded account password. This does not prove production/provider behavior.

| Check | Outcome |
| --- | --- |
| Home, login, register, forgot/reset pages | Passed HTTP 200 with rendered content |
| ADMIN users/courses, DR courses, STUDENT catalog/My Learning | Passed login and page-render smoke |
| Cross-role routes | Passed expected 403 denials; resulting console HTTP errors are expected negative-test evidence |
| DR course/section/article creation | Passed HTTP requests within authenticated browser context; full authoring modal interaction remains unverified |
| STUDENT free enrollment and article completion | Passed local HTTP flow; progress reached 100% |
| Certificate issuance button and HTML download | Passed actual browser button flow; stable number CERT-3-7-19 and attachment content/header verified |
| Incomplete and other-student certificate access | Passed expected 403 |
| Responsive login and certificate | Passed at 375 px without horizontal overflow; desktop screenshots inspected; tablet and remaining pages not fully verified |
| Console/page errors | No JavaScript exceptions observed in smoke; deliberate forbidden requests produced expected HTTP errors |
| ADMIN review gap found during smoke | Added course-detail links/publication status, corrected first-page indexing; retest on final candidate pending |
| My Learning ignored filters | Removed unsupported controls; retained catalog navigation |

Screenshots are stored outside the repository in the chat visualization directory. Full quiz/upload/confirmation/reset-token delivery, authoring modal, all accessibility and role CRUD journeys remain acceptance work; do not infer them from page-render smoke or service tests.
### Production-profile local evidence and remaining release gates

- An isolated PostgreSQL 14.7 cluster was created from already-installed test binaries and bound to loopback. No existing local/shared/production database was altered.
- Production-profile startup applied Flyway V1/V2 successfully and passed JPA validation. Read-only JDBC verification confirmed both migration successes and exactly one enabled local QA admin.
- First-admin bootstrap created the local QA identity, login succeeded, then the test server was restarted with bootstrap disabled. This does not resolve or mutate the historical Render identity collision.
- Health, readiness and liveness returned HTTP 200 with status UP in production profile. ADMIN empty-state rendering passed at 375/768/1440 px without horizontal overflow.
- ADMIN review now has working detail/back links, publication status and a section/tutorial outline; it renders instructor full name instead of the entity object. Placeholder enrollment/content/previous/next actions were removed from this admin-only view.
- Password reset now reports invalid/reused links safely, preserves the token for error rendering, and supplies the correct success flag to the result template. Certificate POST issuance performs its first enrollment read under the row lock; ineligible issuance is forbidden.
- Intermediate verification failures were repaired: Windows could not clean a running JAR (test server stopped); the ADMIN empty-result fixture needed a paged empty result. Final candidate results supersede these attempts.
- Production deployment is blocked pending authenticated access to the actual Render service/database, verified backup/migration state, and secure Paymob/SendGrid sandbox configuration. Public fetch attempts could not establish current Render availability; they are not outage evidence. Docker image build was not executed because Docker tooling was unavailable.
- Upload survival across Render redeployment, enabled-provider delivery/checkout and full all-page browser/accessibility coverage remain open. Optional S3 stays disabled until private-content delivery is verified; SMS remains deferred.
### Earlier candidate identity

- `mvn clean test package`: passed 71 tests, failures=0, errors=0, skips=0. Configured `git diff --check` passed.
- Artifact: `target/elearning-0.0.1-SNAPSHOT.jar`.
- SHA-256: `6ACF6D214C79550763EF9F158166DEF621D5BCB58137F2757EAED7AD56D81C87`.
- Git: `codex/finalize-elearning`, base HEAD `01f3657f5fa84b81a34296c9fdabb9a6e4dda9ca`, uncommitted candidate plus preserved pre-existing edits, no upstream/push/deployment. The checksum identifies the built candidate; HEAD alone does not include these changes.
- Initial helper/sandbox failures were worked around without changing Git metadata or the installed skill. All seven pre-existing paths were hash-verified before/after branch creation; existing runtime workaround files remain preserved.
### Final browser and cleanup result

- Final production-profile JAR: registration, confirmation and STUDENT login passed using a local QA confirmation token (email provider disabled).
- Forgot/reset browser POST, success rendering, reused-token recovery and login with the new password passed. Token lookup waited for the actual POST response; the initial browser harness queried too early and was corrected.
- Populated ADMIN course review, safe instructor rendering and back navigation passed on the final candidate.
- The previously recorded free-course/article/certificate flow passed; issuance-controller locking was subsequently tightened and all 71 regression tests passed. Full provider/payment/upload/all-page acceptance is still open as listed above.
- Disposable application/database services were stopped, and temporary QA password/token files were removed. Existing repository uploads and user changes were preserved.
- Next external action: provide authenticated access to the existing Render service/database and configure provider sandbox settings securely. No production data changes or deployment were performed.
## UI and role journey continuation — 2026-10-21

Deployment is explicitly deferred by the user until Render access and sandbox settings are supplied. This pass is local UI/functionality QA; it does not authorize or claim a production rollout.

### Repairs and current local evidence

| Area | Verified result |
| --- | --- |
| ADMIN accounts | Full-name/email/role edits persist; duplicate email returns a usable form before mutation; disable denies login, enable restores it; disposable account deletion works |
| DR courses | Native form submission preserves validation/flash feedback; edit JSON includes course name/publication/video URL; edit/cancel preserves values; empty-course deletion cancel/confirm works |
| DR sections | Add/edit works with the owning course ID; deletion redirect repaired to the existing route; final bundled redirect check recorded below |
| DR tutorials | Article/quiz/PDF/VIDEO authoring works; edits preserve article/preview/order; existing tutorial edits do not append a duplicate section association; display order follows orderIndex |
| DR quiz edits | Question replacement prevents duplicates; empty/invalid questions/options/correct indexes reject before persistence; failed transactions roll back |
| Uploads | Invalid PDF upload rejected with retry feedback; valid PDF uploaded/served; public MDN CC0 flower.mp4 fixture uploaded and played successfully through the protected local endpoint |
| STUDENT access | Preview text remains inert; previews cannot save progress; enrolled users can read/complete; other students/doctors cannot fetch protected files/quizzes or another doctor's student progress |
| STUDENT quiz | Answers absent from JSON; blank answers stay local; wrong/right retry works; reopening does not accumulate submit handlers; each submit sends one request |
| Completion | Article/PDF explicit completion and video-ended completion are persisted before Read feedback; simulated HTTP 503 does not falsely mark Read; retry reaches 100%; eligible certificate issuance works |
| Payments (provider disabled) | Checkout retry and PENDING labels work; content stays locked; unsigned callback remains display-only. This is not Paymob sandbox or live payment proof |
| Profile | Update feedback is visible; duplicate email recovers on the current principal's form; blank password preserves login; role/id cannot be injected into error rendering |
| Responsive/keyboard | 30 page/viewport checks at 375/768/1440 passed; login Tab sequence, mobile modal bounds, Escape focus restoration, viewer fetch failure/retry and zero unexpected JS exceptions passed |

Primary responsive pages: login, ADMIN users, ADMIN course review, DR courses, STUDENT catalog, My Learning, course learning, profile, checkout and certificate. Screenshots were inspected outside the repository. Expected HTTP 403/503 from deliberate denial/failure tests are separate from JavaScript errors.

### Scope and verification boundaries

- Kept Java 21, Spring Boot 3.5.6, Thymeleaf, PostgreSQL, URLs, roles, provider authority and existing migrations. No dependency upgrades, new worktree, commits, pushes or deployments.
- Removed unsupported tutorial-status/catalog-level controls; retained working search and tutorial ordering rather than presenting inert controls.
- Fixed common narrow-layout containment and off-screen tooltip overflow, placed overlays outside animated containers, improved contrast/labels, and retained keyboard focus after closing overlays.
- Browser plugin unavailable; used installed Playwright and Chrome. No browser dependencies were installed.
- Full code gate passed 77 tests with zero failures/errors/skips. Subsequent resource-only adjustments are packaged and smoke-checked separately; no claim of provider delivery follows from mocks or local tokens.
- Environmental/harness issues were resolved: system drive full (own stopped QA data removed and current scratch moved to D: after copy verification); unbounded body-finish waits on negative responses replaced by status plus UI assertions; competing reload in the harness corrected. These attempts are not counted as application acceptance results.
- Render deployment, Paymob/SendGrid sandbox delivery, S3 private delivery if enabled, and Render redeployment persistence remain deferred. Local file persistence across app restart is checked separately below. Comprehensive screen-reader/a11y certification remains outside the targeted keyboard/layout checks.
### Packaged candidate closeout

- Final JAR SHA-256: `4D3A6C33AA92CD48B47F2430D0D2F79BCBF04B5B9A6209980C90B5FED5949003`.
- Bundled-resource Chrome smoke passed: DR section create/delete with canonical redirect, student course page, and authenticated PDF/video delivery after stopping/restarting the app against the same isolated PostgreSQL database and local upload directory.
- Local persistence passed; this does not establish Render filesystem persistence. Deployment remains pending user-provided Render access and sandbox settings.

## Free-cloud launch preparation — 2026-10-21

- User now authorizes work toward deployment on free cloud compute and explicitly requires durable files. This supersedes the earlier deployment deferral, but does not waive account access, provider tests, private-content protection, or authorize charges.
- Render dashboard was signed out. Automatic browser approval rejected GitHub OAuth sign-in due to the separate account identity/access involved. User chose to sign in directly; do not retry that OAuth action without explicit authorization. No cloud resources or paid plans were created.
- Research confirmed Render Free has no persistent disk and its own free PostgreSQL expires after 30 days. Proposed free-allowance stack: Render Docker compute, external PostgreSQL such as Neon, and private S3-compatible storage such as R2. Provider quotas, overages and availability must be reviewed in the authenticated accounts; this is not a guarantee of a zero-cost production service.
- Found and repaired a prerequisite: the normal DR upload flow previously always wrote to local disk even with S3 enabled. The optional private backend now stores the same protected tutorial paths in the bucket. Authorization precedes time-limited, uncacheable signed redirects; missing/denied access cannot issue a download URL, and remote failures do not fall back to ephemeral disk.
- Added optional HTTPS object endpoint/region support using the pinned AWS SDK. No framework/dependency/schema upgrades. Existing local mode remains supported; existing file migration requires verified copies before switching an existing deployment.
- Added `render-free.yaml` with free compute, external secure credentials, private storage required, production schema guards, SendGrid configured, Paymob off until sandbox setup, and auto-deployment disabled. The existing disk-backed descriptor remains available.
- Remaining external gates: authenticated Render/source access, a new external PostgreSQL database, a private bucket and restricted credentials, sender verification, sandbox merchant settings, initial approved admin, Docker/cloud build, actual provider tests, long-video seeking/link expiry, redeployment persistence, backup restoration and post-deploy role smoke. No public launch URL exists for this candidate yet.

### Verification of the free-cloud preparation

- `mvn clean test package` passed 90 tests, failures=0, errors=0, skipped=0. New coverage checks private uploads, signed-download expiry/path, invalid path/endpoint rejection, no ephemeral fallback on remote failure, and authorization before issuing links. Existing local-file delivery remains covered.
- The first restricted-shell run could not attach Mockito's Java agent through Windows pipes and was stopped. The next gate exposed test-fixture mistakes (an unauthenticated token constructor and an over-exact expiry assertion); fixtures were corrected before the final clean passing gate. These failures are not counted as acceptance passes.
- Final JAR SHA-256: `1C368161EFA5F740BB921F4C35896953347DCB87600258CA9E261CA75A495514`.
- Packaged production-profile startup passed with a separate loopback PostgreSQL 14.7 database, Flyway V1/V2, JPA validation, a 256 MiB Java heap, and private-storage beans enabled using unused synthetic QA credentials. Readiness/liveness returned HTTP 200 UP and login rendered HTTP 200. No storage-provider API calls, actual credentials, shared database changes or cloud resources were involved. This does not establish the full 512 MiB container memory budget or cold-start behavior.
- YAML parsed with the installed SnakeYAML library; local checks confirmed exactly one free compute service, no persistent disk/Render database provisioning, external storage required, auto-deploy off, and unique string environment settings. Authenticated Render Blueprint validation and Docker/cloud build were not executed. Docker/Podman/Render CLIs are unavailable locally.
- `git diff --check` passed. Existing changes remain on `codex/finalize-elearning` at base HEAD `01f3657f5fa84b81a34296c9fdabb9a6e4dda9ca`, no upstream, no staging/commit/push/deployment. Manual preflight again substituted for the installed helper's known missing-parent-POM failure.
- User was asked whether external PostgreSQL and private storage accounts are already available. No real Render/database/object-store/SendGrid/Paymob credentials were present in this execution session. Actual provider delivery, durable cloud readback/redeployment and post-deploy checks remain blocked on secure setup/access. Do not treat the configured descriptor or locally signed URL as cloud acceptance.

## Authenticated Render inspection — 2026-10-21

- User completed GitHub authentication; Render workspace is accessible. Existing target `aminah-medical-elearning` (`srv-d91teqi8qa3s73b73ujg`) is already Docker Free at `https://aminah-elearning-1.onrender.com`. Last dashboard deployment references `69efe666adaa6c402a3bdb869c3950b05ae72f82`; this is the older service, not the newly verified candidate. Automatic deployment is disabled.
- Existing masked environment contains database, Paymob, SendGrid and bootstrap settings. Presence is not proof of valid credentials, test-merchant mode, or delivery. No provider secret values were printed or copied into repository artifacts.
- Found schema mode `update` and bootstrap enabled. Saved `JPA_DDL_AUTO=validate` and `APP_BOOTSTRAP_ADMIN_ENABLED=false` with Render's **Save only** action. No deployment was requested by that action; retain bootstrap identity values until an approved account-recovery plan exists.
- The datasource points to a Render internal PostgreSQL host, but workspace search/project inspection did not locate that database. Its availability, contents and recoverability are unresolved. Do not overwrite/delete the old connection, silently replace production data, or infer a backup from local QA.
- Source repository master still resolves to local base `01f3657f5fa84b81a34296c9fdabb9a6e4dda9ca`. Git push dry-run authenticated successfully for a new `codex/finalize-elearning` branch. GitHub CLI's separate stored token is invalid; it is not required for Git credential-manager pushes. No force-push or merge is planned.
- To avoid activating usage billing, the current external storage candidate is a private Backblaze B2 account rather than R2 billing activation. User explicitly chose to complete both free Neon and B2 account setups. Separate provider account creation/terms/verification remain user steps; do not activate paid resources. Region in the free descriptor is now supplied per bucket provider.

### Candidate publication preparation

- Rebuilt the same application source with `mvn clean test package`: 90 passed, zero failures/errors/skips. New JAR SHA-256: `CDAA1E98C2169855683764F4E2EF624290C4C45D24DA503BAC164E150FC0A491` (build artifact identity changes with rebuild metadata).
- Staged the 61 related candidate paths explicitly after whitespace and credential-pattern checks; original `AGENTS.md` and `DESIGN.md` edits are excluded from publication and remain preserved locally. No source changes were needed during this authenticated inspection; the new repository edits are the provider-neutral region prompt and current launch documentation.
- Environment correction readback initially still showed the old values. The corrections were re-entered and saved without deploying; persistence must be confirmed before rollout. A successful button click alone is not recorded as a passing configuration check.
- User is completing free Neon and Backblaze B2 account setup. Keep the existing Render service's database references unchanged until a new isolated cloud verification environment is ready and the old database's recovery status is understood. Do not deploy the candidate against an unverified database or ephemeral tutorial storage.

# Aminah E-Learning - Full Source (IntelliJ-ready)

**Stack:** Java 21, Spring Boot 3.5.6, Thymeleaf, Spring Data JPA, MySQL, Paymob (SANDBOX)

## Quick start (IntelliJ local)
1. Ensure Java 21 JDK and Maven are installed.
2. Create MySQL database and user (example):
   ```sql
   CREATE DATABASE elearning CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'root'@'localhost' IDENTIFIED BY 'root';
   GRANT ALL PRIVILEGES ON elearning.* TO 'root'@'localhost';
   ```
3. Update `src/main/resources/application.properties` with your MySQL credentials and Paymob SANDBOX keys.
4. Import `src/main/resources/data.sql` into your MySQL database to seed sample data and admin user.
5. Open project in IntelliJ (File → Open → select project root with pom.xml).
6. Run `com.aminah.elearning.ElearningApplication` from IDE or use `mvn spring-boot:run`.

## Paymob (SANDBOX) integration
- Configure SANDBOX API keys in `application.properties`.
- Webhook endpoint: `POST /payments/webhook` — configure this URL in Paymob sandbox dashboard.
- Use Paymob sandbox endpoints; payment iframe will load using SANDBOX tokens.

## Notes
- Admin seeded user: `admin@aminah.local` with password `Admin@123` (the DB seed contains a BCrypt hash). If you have trouble logging in, update the password hash or create a new admin via DB.
- For production: move secrets to environment variables, enable CSRF, secure cookies, use managed DB, and enable HTTPS.

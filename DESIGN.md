# DESIGN.md - Aminah E-Learning (Short)

## Architecture
- Spring Boot application with Thymeleaf server-side rendering.
- MySQL database for persistence.
- Paymob SANDBOX integration for payments (iframe flow).
- Cloudinary optional for video storage and signed URLs.

## Payment sequence (SANDBOX)
1. Get auth token using API key
2. Create ecommerce order (merchant_order_id)
3. Request payment key (integration_id + order)
4. Build iframe URL and render to user
5. Paymob sends webhook -> verify HMAC -> mark payment/enrollment SUCCESS

## Next steps to production
- Harden security, switch to env vars or Vault for secrets
- Implement idempotent webhook handling & update Payment/Enrollment records
- Add email notifications and user dashboard for enrolled courses
- Integrate Cloudinary signed uploads for secure video hosting

CREATE UNIQUE INDEX IF NOT EXISTS uq_payments_gateway_order
    ON payments (gateway, gateway_order_id)
    WHERE gateway_order_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_course_enrollment_user_course
    ON course_enrollments (user_id, course_id);

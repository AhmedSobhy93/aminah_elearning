-- Baseline schema for the current Aminah E-Learning JPA model.
-- Enable with FLYWAY_ENABLED=true after verifying the target database is ready.

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255),
    phone_number VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS courses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    course_name VARCHAR(255) NOT NULL,
    description TEXT,
    price DOUBLE PRECISION NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    video_url VARCHAR(255),
    created_at TIMESTAMP(6) NOT NULL,
    level VARCHAR(255),
    author_id BIGINT,
    CONSTRAINT fk_courses_author FOREIGN KEY (author_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS contact (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255),
    subject VARCHAR(255),
    message VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS section (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255),
    description TEXT,
    duration_minutes INTEGER NOT NULL DEFAULT 0,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    unlock_on_finish_previous BOOLEAN NOT NULL DEFAULT FALSE,
    order_index INTEGER NOT NULL DEFAULT 0,
    course_id BIGINT,
    CONSTRAINT fk_section_course FOREIGN KEY (course_id) REFERENCES courses (id)
);

CREATE TABLE IF NOT EXISTS tutorial (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255),
    type VARCHAR(255),
    file_path VARCHAR(255),
    order_index INTEGER NOT NULL DEFAULT 0,
    uploaded_at TIMESTAMP(6),
    section_id BIGINT,
    user_id BIGINT,
    status VARCHAR(255),
    article_content TEXT,
    preview BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_tutorial_section FOREIGN KEY (section_id) REFERENCES section (id),
    CONSTRAINT fk_tutorial_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS quiz_question (
    id BIGSERIAL PRIMARY KEY,
    question VARCHAR(255),
    correct_option_index INTEGER NOT NULL DEFAULT 0,
    tutorial_id BIGINT,
    CONSTRAINT fk_quiz_question_tutorial FOREIGN KEY (tutorial_id) REFERENCES tutorial (id)
);

CREATE TABLE IF NOT EXISTS quiz_options (
    question_id BIGINT NOT NULL,
    option_text VARCHAR(255),
    CONSTRAINT fk_quiz_options_question FOREIGN KEY (question_id) REFERENCES quiz_question (id)
);

CREATE TABLE IF NOT EXISTS course_enrollments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT,
    enrollment_date TIMESTAMP(6) NOT NULL,
    payment_status VARCHAR(255),
    progress_percentage DOUBLE PRECISION,
    completed BOOLEAN,
    certificate_issued BOOLEAN,
    CONSTRAINT fk_course_enrollments_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_course_enrollments_course FOREIGN KEY (course_id) REFERENCES courses (id)
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    enrollment_id BIGINT NOT NULL UNIQUE,
    amount DOUBLE PRECISION,
    status VARCHAR(255),
    gateway VARCHAR(255),
    gateway_order_id VARCHAR(255),
    course_id VARCHAR(255),
    CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_payments_enrollment FOREIGN KEY (enrollment_id) REFERENCES course_enrollments (id)
);

CREATE TABLE IF NOT EXISTS password_reset_token (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP(6),
    CONSTRAINT fk_password_reset_token_course FOREIGN KEY (course_id) REFERENCES courses (id),
    CONSTRAINT fk_password_reset_token_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS tutorial_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    tutorial_id BIGINT,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP(6),
    CONSTRAINT fk_tutorial_progress_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_tutorial_progress_tutorial FOREIGN KEY (tutorial_id) REFERENCES tutorial (id)
);

CREATE TABLE IF NOT EXISTS verification_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255),
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP(6),
    CONSTRAINT fk_verification_token_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS videos (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT,
    title VARCHAR(255),
    description VARCHAR(255),
    video_url VARCHAR(255),
    CONSTRAINT fk_videos_course FOREIGN KEY (course_id) REFERENCES courses (id)
);

CREATE INDEX IF NOT EXISTS idx_courses_author_id ON courses (author_id);
CREATE INDEX IF NOT EXISTS idx_section_course_id ON section (course_id);
CREATE INDEX IF NOT EXISTS idx_tutorial_section_id ON tutorial (section_id);
CREATE INDEX IF NOT EXISTS idx_tutorial_user_id ON tutorial (user_id);
CREATE INDEX IF NOT EXISTS idx_course_enrollments_user_id ON course_enrollments (user_id);
CREATE INDEX IF NOT EXISTS idx_course_enrollments_course_id ON course_enrollments (course_id);
CREATE INDEX IF NOT EXISTS idx_tutorial_progress_user_tutorial ON tutorial_progress (user_id, tutorial_id);

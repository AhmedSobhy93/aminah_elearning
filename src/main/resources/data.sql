INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN') ON DUPLICATE KEY UPDATE name=name;
INSERT INTO users (id, email, password, full_name, enabled, created_at) VALUES (1, 'admin@aminah.local', '$2a$10$Q9zqJYVq9xYHqk5u9K1/eeO2a1pSgJ8q9m3Z2y/8pQ2z1qXwJ8j0e', 'Admin User', true, NOW()) ON DUPLICATE KEY UPDATE email=email;
INSERT INTO user_roles (user_id, role_id) VALUES (1,1) ON DUPLICATE KEY UPDATE user_id=user_id;

INSERT INTO courses (id, title, description, price, published, thumbnail_url, video_url, created_at) VALUES
(1, 'Intro to Digital Banking', 'Overview of digital banking concepts.', 0.00, true, '', '', NOW()),
(2, 'Spring Boot for Enterprises', 'Build scalable applications with Spring Boot.', 500.00, true, '', '', NOW()),
(3, 'Contact Center Automation', 'Cisco PCCE and monitoring best practices.', 750.00, true, '', '', NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS device_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE device_management;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    nickname VARCHAR(100),
    avatar VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN DEFAULT TRUE,
    last_online_time DATETIME,
    is_online BOOLEAN DEFAULT FALSE,
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Devices table
CREATE TABLE IF NOT EXISTS devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    model VARCHAR(200),
    category VARCHAR(100),
    brand VARCHAR(100),
    status VARCHAR(50) DEFAULT 'NORMAL',
    description VARCHAR(500),
    location VARCHAR(200),
    purchase_date DATETIME,
    warranty_expiry DATETIME,
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_status (status),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Friends table
CREATE TABLE IF NOT EXISTS friends (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at DATETIME,
    INDEX idx_user_id (user_id),
    INDEX idx_friend_id (friend_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Friend requests table
CREATE TABLE IF NOT EXISTS friend_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    remark VARCHAR(200),
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at DATETIME,
    INDEX idx_sender (sender_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Blacklists table
CREATE TABLE IF NOT EXISTS blacklists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    blocked_user_id BIGINT NOT NULL,
    created_at DATETIME,
    INDEX idx_user_id (user_id),
    INDEX idx_blocked_user (blocked_user_id),
    UNIQUE KEY uk_user_blocked (user_id, blocked_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Chat messages table
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(20) DEFAULT 'TEXT',
    is_read BOOLEAN DEFAULT FALSE,
    read_at DATETIME,
    send_time DATETIME,
    created_at DATETIME,
    INDEX idx_sender (sender_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_send_time (send_time),
    INDEX idx_is_read (is_read),
    INDEX idx_conversation (sender_id, receiver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default admin account (password: admin123)
INSERT INTO users (username, password, email, nickname, role, enabled, is_online, created_at, updated_at)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@example.com', '系统管理员', 'ADMIN', TRUE, FALSE, NOW(), NOW())
ON DUPLICATE KEY UPDATE username=username;

-- Insert demo user (password: user123)
INSERT INTO users (username, password, email, nickname, role, enabled, is_online, created_at, updated_at)
VALUES ('user1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'user1@example.com', '测试用户', 'USER', TRUE, FALSE, NOW(), NOW())
ON DUPLICATE KEY UPDATE username=username;

-- Insert demo device data
INSERT INTO devices (name, model, category, brand, status, description, location, purchase_date, warranty_expiry, created_at, updated_at)
VALUES
('ThinkPad X1 Carbon', 'X1C Gen 11', '办公设备', 'Lenovo', 'NORMAL', '笔记本电脑', 'A栋301', '2024-01-15 00:00:00', '2027-01-15 00:00:00', NOW(), NOW()),
('Dell U2723QE', 'U2723QE', '办公设备', 'Dell', 'NORMAL', '4K显示器', 'A栋302', '2024-02-20 00:00:00', '2027-02-20 00:00:00', NOW(), NOW()),
('HP LaserJet Pro', 'M404dn', '办公设备', 'HP', 'FAULT', '激光打印机', 'B栋101', '2023-06-10 00:00:00', '2026-06-10 00:00:00', NOW(), NOW()),
('Cisco Catalyst 9200', 'C9200-24T', '网络设备', 'Cisco', 'NORMAL', '核心交换机', '机房A', '2024-03-01 00:00:00', '2029-03-01 00:00:00', NOW(), NOW()),
('APC Smart-UPS 1500', 'SMT1500IC', '网络设备', 'APC', 'MAINTENANCE', 'UPS电源', '机房A', '2023-01-20 00:00:00', '2026-01-20 00:00:00', NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

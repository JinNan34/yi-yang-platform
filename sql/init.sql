CREATE DATABASE IF NOT EXISTS doctor_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE doctor_platform;

DROP TABLE IF EXISTS followup_intervention_record;
DROP TABLE IF EXISTS health_assessment;
DROP TABLE IF EXISTS key_population_followup;
DROP TABLE IF EXISTS health_alert;
DROP TABLE IF EXISTS health_record;
DROP TABLE IF EXISTS elder_account;
DROP TABLE IF EXISTS elder;
DROP TABLE IF EXISTS doctor;

CREATE TABLE doctor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    real_name VARCHAR(64),
    title VARCHAR(64),
    department VARCHAR(128),
    phone VARCHAR(32),
    avatar VARCHAR(512),
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE elder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    id_card VARCHAR(32),
    gender TINYINT,
    birth_date DATE,
    phone VARCHAR(32),
    address VARCHAR(512),
    emergency_contact VARCHAR(64),
    emergency_phone VARCHAR(32),
    remark VARCHAR(512),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE health_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    elder_id BIGINT NOT NULL,
    doctor_id BIGINT,
    systolic_bp INT,
    diastolic_bp INT,
    blood_sugar DECIMAL(6,2),
    heart_rate INT,
    temperature DECIMAL(4,1),
    weight DECIMAL(5,2),
    record_time DATETIME,
    remark VARCHAR(1024),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_elder (elder_id)
);

CREATE TABLE health_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    elder_id BIGINT NOT NULL,
    alert_type VARCHAR(64),
    alert_level VARCHAR(16),
    message VARCHAR(1024),
    status TINYINT DEFAULT 0,
    handle_time DATETIME,
    handle_remark VARCHAR(512),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_elder (elder_id)
);

CREATE TABLE key_population_followup (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    elder_id BIGINT NOT NULL,
    doctor_id BIGINT,
    risk_type VARCHAR(64),
    followup_cycle_days INT,
    next_followup_date DATE,
    status TINYINT DEFAULT 0,
    remark VARCHAR(512),
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_elder (elder_id)
);

CREATE TABLE followup_intervention_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    followup_id BIGINT,
    elder_id BIGINT NOT NULL,
    doctor_id BIGINT,
    intervention_type VARCHAR(64),
    content TEXT,
    intervention_time DATETIME,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_elder (elder_id)
);

CREATE TABLE health_assessment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    elder_id BIGINT NOT NULL,
    assessor_id BIGINT,
    score INT,
    conclusion VARCHAR(2048),
    assessment_time DATETIME,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_elder (elder_id)
);

CREATE TABLE elder_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    elder_id BIGINT NOT NULL UNIQUE,
    account_no VARCHAR(64),
    balance DECIMAL(12,2) DEFAULT 0,
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

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
    role VARCHAR(32),
    status TINYINT DEFAULT 1,
    deleted TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE elder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creator_doctor_id BIGINT,
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
    doctor_id BIGINT,
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

-- 插入老人数据 (creator_doctor_id=1 绑定演示医生)
INSERT INTO elder (creator_doctor_id, name, id_card, gender, birth_date, phone, address, emergency_contact, emergency_phone) VALUES
(1, '张建国', '110101195001011234', 1, '1950-01-01', '13900001111', '北京市朝阳区示例街道', '张小明', '13900002222'),
(1, '李秀英', '110102195203152345', 0, '1952-03-15', '13900003333', '北京市海淀区幸福小区', '李小红', '13900004444'),
(1, '王德福', '110103194807203456', 1, '1948-07-20', '13900005555', '北京市西城区平安里', '王小伟', '13900006666'),
(1, '陈桂兰', '110104195511084567', 0, '1955-11-08', '13900007777', '北京市东城区王府井', '陈大明', '13900008888'),
(1, '刘振邦', '110105194905155678', 1, '1949-05-15', '13900009999', '北京市丰台区科技园', '刘伟', '13900000001');

-- 插入老人账户数据
INSERT INTO elder_account (elder_id, account_no, balance, status) VALUES
(1, 'EA1', 0.00, 1),
(2, 'EA2', 0.00, 1),
(3, 'EA3', 0.00, 1),
(4, 'EA4', 0.00, 1),
(5, 'EA5', 0.00, 1);

-- 插入健康记录数据
INSERT INTO health_record (elder_id, doctor_id, systolic_bp, diastolic_bp, blood_sugar, heart_rate, temperature, weight, record_time) VALUES
(1, 1, 135, 85, 6.20, 75, 36.5, 68.50, '2026-04-15 09:00:00'),
(1, 1, 140, 90, 7.10, 80, 36.8, 69.00, '2026-04-25 09:00:00'),
(1, 1, 138, 88, 6.80, 78, 36.3, 68.80, '2026-05-05 09:00:00'),
(2, 1, 128, 82, 5.80, 72, 36.2, 62.00, '2026-04-18 10:00:00'),
(2, 1, 132, 84, 6.00, 74, 36.4, 62.50, '2026-04-28 10:00:00'),
(2, 1, 130, 83, 5.90, 73, 36.1, 62.30, '2026-05-08 10:00:00'),
(3, 1, 145, 92, 7.80, 85, 36.6, 72.00, '2026-04-12 08:30:00'),
(3, 1, 142, 91, 7.50, 82, 36.7, 71.80, '2026-04-22 08:30:00'),
(3, 1, 148, 94, 8.10, 88, 36.9, 72.20, '2026-05-02 08:30:00');

-- 插入健康评估数据
INSERT INTO health_assessment (elder_id, assessor_id, score, conclusion, assessment_time) VALUES
(1, 1, 85, '老人身体状况良好，建议继续保持规律作息和适当运动。', '2026-05-05 14:00:00'),
(1, 1, 78, '老人血压略高，建议定期监测并遵医嘱服药。', '2026-05-11 14:00:00'),
(2, 1, 90, '老人身体状况优秀，各项指标正常，继续保持。', '2026-05-06 15:00:00'),
(2, 1, 88, '老人身体状况良好，建议适当增加户外活动。', '2026-05-10 15:00:00');

-- 插入重点人群随访数据
INSERT INTO key_population_followup (elder_id, doctor_id, risk_type, followup_cycle_days, next_followup_date, status, remark) VALUES
(1, 1, '高血压', 7, '2026-05-15', 1, '重点关注高血压指标控制情况'),
(3, 1, '糖尿病', 7, '2026-05-18', 1, '重点关注血糖指标控制情况'),
(4, 1, '心脏病', 14, '2026-05-25', 1, '重点关注心脏功能监测');

-- 插入健康预警数据
INSERT INTO health_alert (elder_id, doctor_id, alert_type, alert_level, message, status, handle_time, handle_remark) VALUES
(1, 1, '血压异常', '中等', '老人最近一次测量血压偏高，建议及时关注', 1, NULL, NULL),
(1, 1, '血糖异常', '高等', '老人血糖值超过正常范围，请尽快安排复查', 0, '2026-05-10 10:00:00', '已通知家属，安排次日复查'),
(3, 1, '血糖异常', '高等', '老人血糖持续偏高，需要调整用药方案', 1, NULL, NULL),
(3, 1, '血压异常', '中等', '老人血压波动较大，建议加强监测', 0, '2026-05-09 11:00:00', '已电话随访，提醒按时服药');

-- 插入随访干预记录数据
INSERT INTO followup_intervention_record (elder_id, doctor_id, intervention_type, content, intervention_time) VALUES
(1, 1, '电话随访', '提醒老人按时服药，注意饮食清淡', '2026-05-06 09:00:00'),
(1, 1, '上门服务', '为老人测量血压血糖，指导健康生活方式', '2026-05-09 10:00:00'),
(3, 1, '电话随访', '提醒老人注意饮食控制，定期监测血糖', '2026-05-07 14:00:00'),
(3, 1, '上门服务', '为老人进行全面体检，调整健康管理方案', '2026-05-10 09:30:00');

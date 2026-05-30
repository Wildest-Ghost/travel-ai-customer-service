-- ====== 用户表（表名用 sys_user，因为 user 是 PostgreSQL 保留字）======
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ====== 产品表 ======
DROP TABLE IF EXISTS sys_product;
CREATE TABLE sys_product (
    id           BIGSERIAL PRIMARY KEY,
    type         VARCHAR(20)  NOT NULL,                 -- FLIGHT / HOTEL
    name         VARCHAR(200) NOT NULL,
    origin       VARCHAR(100),
    destination  VARCHAR(100),
    price        NUMERIC(10, 2) NOT NULL,
    stock        INTEGER      NOT NULL DEFAULT 0,
    start_time   TIMESTAMP,
    end_time     TIMESTAMP,
    description  TEXT,
    created_at   TIMESTAMP DEFAULT NOW(),
    updated_at   TIMESTAMP DEFAULT NOW()
);

INSERT INTO sys_product (type, name, origin, destination, price, stock, start_time, end_time, description) VALUES
('FLIGHT', 'CA1501', '北京', '上海', 1200.00, 100, '2026-06-01 08:00:00', '2026-06-01 10:30:00', '国航直飞，经济舱'),
('FLIGHT', 'MU5102', '上海', '广州',  980.00,  80, '2026-06-01 14:00:00', '2026-06-01 16:30:00', '东航直飞，经济舱'),
('FLIGHT', 'CZ3001', '广州', '北京', 1450.00,  60, '2026-06-02 09:00:00', '2026-06-02 12:00:00', '南航直飞，经济舱'),
('FLIGHT', 'CA1502', '上海', '北京', 1180.00, 100, '2026-06-03 18:00:00', '2026-06-03 20:30:00', '国航返程，经济舱'),
('HOTEL',  '北京王府井希尔顿',     '北京', NULL,  880.00,  20, '2026-06-01 14:00:00', '2026-06-02 12:00:00', '五星商务酒店，含早'),
('HOTEL',  '上海外滩华尔道夫',     '上海', NULL, 1500.00,  10, '2026-06-01 15:00:00', '2026-06-02 12:00:00', '外滩黄金地段豪华套房'),
('HOTEL',  '广州花园酒店',         '广州', NULL,  760.00,  30, '2026-06-02 14:00:00', '2026-06-03 12:00:00', '五星级园林酒店');

-- ====== 订单表 ======
DROP TABLE IF EXISTS sys_order;
CREATE TABLE sys_order (
    id                  BIGSERIAL PRIMARY KEY,
    order_no            VARCHAR(64) NOT NULL UNIQUE,
    user_id             BIGINT      NOT NULL,                   -- 应用层关联 sys_user.id，无 FK（微服务解耦）
    product_id          BIGINT      NOT NULL,                   -- 应用层关联 sys_product.id，无 FK
    status              VARCHAR(20) NOT NULL,                   -- PENDING/PAID/CANCELLED/CHANGED/REFUNDED
    amount              NUMERIC(10, 2) NOT NULL,
    quantity            INTEGER     NOT NULL DEFAULT 1,
    start_time          TIMESTAMP,
    end_time            TIMESTAMP,
    contact_name        VARCHAR(100),
    contact_phone       VARCHAR(30),
    original_order_id   BIGINT,                                 -- 改签订单指向原订单，无 FK
    change_type         VARCHAR(20),                            -- 'CHANGE' / 'REFUND' / NULL
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW()
);
CREATE INDEX idx_order_user    ON sys_order(user_id);
CREATE INDEX idx_order_product ON sys_order(product_id);

-- 种子订单（user_id=1 对应测试用户，需先注册）
INSERT INTO sys_order (order_no, user_id, product_id, status, amount, quantity, start_time, end_time, contact_name, contact_phone) VALUES
('ORDSEED0000001', 1, 1, 'PAID',     1200.00, 1, '2026-06-01 08:00:00', '2026-06-01 10:30:00', '张三', '13800000001'),
('ORDSEED0000002', 1, 5, 'PAID',      880.00, 1, '2026-06-01 14:00:00', '2026-06-02 12:00:00', '张三', '13800000001'),
('ORDSEED0000003', 1, 2, 'PENDING',   980.00, 1, '2026-06-01 14:00:00', '2026-06-01 16:30:00', '张三', '13800000001');

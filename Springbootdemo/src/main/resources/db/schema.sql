-- 数据库表结构示例

CREATE TABLE IF NOT EXISTS conversation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  title VARCHAR(255),
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS message_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  conversation_id BIGINT,
  role VARCHAR(16),
  content TEXT,
  created_at DATETIME
);

CREATE TABLE IF NOT EXISTS article (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  author VARCHAR(64),
  category VARCHAR(64),
  position INT,
  create_time DATETIME,
  update_time DATETIME
);

CREATE TABLE IF NOT EXISTS product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL COMMENT '商品名称',
  price DECIMAL(10,2) NOT NULL COMMENT '商品价格',
  stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '商品状态：0=下架，1=上架',
  create_time DATETIME COMMENT '创建时间'
);

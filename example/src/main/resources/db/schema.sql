-- 每次启动都执行，必须幂等

CREATE TABLE IF NOT EXISTS app_user
(
    id            VARCHAR(36) PRIMARY KEY,  -- xbatis 的 UUID_V7 生成器，带连字符
    email         VARCHAR(128) NOT NULL UNIQUE,
    nickname      VARCHAR(64)  NOT NULL,
    password_hash VARCHAR(100),
    role          VARCHAR(16)  NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS note
(
    id          VARCHAR(36)  PRIMARY KEY,
    owner_email VARCHAR(128) NOT NULL,
    title       VARCHAR(120) NOT NULL,
    content     TEXT         NOT NULL,
    tags        VARCHAR(200),
    trace_id    VARCHAR(32),
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_note_owner ON note (owner_email, updated_at DESC);

CREATE TABLE IF NOT EXISTS note_attachment
(
    id           VARCHAR(36) PRIMARY KEY,
    note_id      VARCHAR(32)  NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    object_key   VARCHAR(255) NOT NULL,
    size         BIGINT       NOT NULL,
    content_type VARCHAR(128),
    created_at   TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_attachment_note ON note_attachment (note_id);

CREATE TABLE IF NOT EXISTS note_share
(
    -- token 是 72 位随机串的 Base64URL（12 字符），库里这一行就是凭据
    token      VARCHAR(32) PRIMARY KEY,
    note_id    VARCHAR(32) NOT NULL,
    expires_at TIMESTAMP   NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_share_note ON note_share (note_id);

CREATE TABLE IF NOT EXISTS login_log
(
    id         VARCHAR(36) PRIMARY KEY,
    email      VARCHAR(128),
    ip         VARCHAR(64),
    user_agent VARCHAR(255),
    browser    VARCHAR(32),
    os         VARCHAR(32),
    device     VARCHAR(32),
    success    BOOLEAN     NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS audit_log
(
    id         VARCHAR(32) PRIMARY KEY,  -- 审计由 AuditStore 直接插入，id 用 IdUtil 的 32 位十六进制
    operator   VARCHAR(128),
    module     VARCHAR(64),
    type       VARCHAR(16),
    description VARCHAR(255),
    args       TEXT,
    success    BOOLEAN     NOT NULL,
    error      VARCHAR(500),
    duration_ms BIGINT,
    created_at TIMESTAMP   NOT NULL DEFAULT now()
);

-- 初始数据：管理员账号（密码 admin123，首次登录可用密码方式，不需要收邮件）
INSERT INTO app_user (id, email, nickname, password_hash, role)
VALUES ('0192654a0b2c7def8a1b2c3d4e5f6071', 'admin@example.com', '管理员',
        '{bcrypt}$2a$10$Y1UO4nfsftCwzJPUyeegTO5cHXmWzjSRJM8/tjSGJoyEPmqZL0rWS', 'ADMIN')
ON CONFLICT (email) DO NOTHING;

INSERT INTO note (id, owner_email, title, content, tags, created_at, updated_at)
VALUES ('0192654a0b2d7abc9def4567890abcde', 'admin@example.com', '欢迎使用轻笔记',
        '这个示例把 ext-library 的 28 个模块都串进了真实业务：验证码登录、笔记增删改、附件缩略图、分享链接、后台看板。',
        'demo', now(), now())
ON CONFLICT (id) DO NOTHING;

CREATE TABLE point_reward (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL, -- FK to user(id) 주석 처리
    order_id BIGINT,         -- FK to order(id) 주석 처리
    activity_record_id BIGINT, -- FK to activity_record(id) 주석 처리
    point_type VARCHAR(20) NOT NULL,
    points_delta INTEGER NOT NULL,
    balance_after INTEGER NOT NULL DEFAULT 0,
    reason_text VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

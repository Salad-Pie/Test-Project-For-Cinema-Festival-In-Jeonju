CREATE TABLE i18n_messages (
    id BIGSERIAL PRIMARY KEY,
    message_key VARCHAR(200) NOT NULL,
    locale VARCHAR(10) NOT NULL,
    content TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE (message_key, locale)
);

CREATE INDEX idx_i18n_messages_locale ON i18n_messages(locale);

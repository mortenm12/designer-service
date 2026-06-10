CREATE TABLE IF NOT EXISTS survey_definitions
(
    id         UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    owner_id   VARCHAR(255) NOT NULL,
    title      VARCHAR(500) NOT NULL,
    status     VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    structure  TEXT         NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version    BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT chk_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX idx_survey_definitions_owner_id ON survey_definitions (owner_id);
CREATE INDEX idx_survey_definitions_status ON survey_definitions (status);

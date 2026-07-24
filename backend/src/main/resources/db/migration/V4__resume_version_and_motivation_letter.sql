ALTER TABLE resume_generation
    ADD COLUMN version VARCHAR(20) NOT NULL DEFAULT 'WORK';

CREATE TABLE motivation_letter (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES job_application(id) ON DELETE CASCADE,
    generation_id UUID NOT NULL REFERENCES resume_generation(id) ON DELETE CASCADE,
    language VARCHAR(10) NOT NULL,
    personal_info TEXT,
    subject TEXT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

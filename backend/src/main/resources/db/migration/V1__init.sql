CREATE TABLE portfolio_project (
    id UUID PRIMARY KEY,
    slug VARCHAR(100) NOT NULL UNIQUE,
    name_zh VARCHAR(200) NOT NULL,
    name_de VARCHAR(200),
    project_type VARCHAR(50) NOT NULL,
    role_text VARCHAR(200) NOT NULL,
    summary TEXT NOT NULL,
    technologies TEXT NOT NULL,
    responsibilities TEXT NOT NULL,
    outcomes TEXT NOT NULL,
    facts TEXT NOT NULL,
    keywords TEXT NOT NULL,
    sensitive BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE job_application (
    id UUID PRIMARY KEY,
    job_title VARCHAR(250) NOT NULL,
    company_name VARCHAR(250),
    job_description TEXT NOT NULL,
    candidate_summary TEXT,
    status VARCHAR(30) NOT NULL,
    analysis_json TEXT,
    analysis_edited_json TEXT,
    selected_project_ids TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE project_draft (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES job_application(id) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES portfolio_project(id),
    position SMALLINT NOT NULL,
    generated_prompt TEXT,
    pasted_latex TEXT,
    parsed_json TEXT,
    validation_errors TEXT,
    approved BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE(application_id, position)
);

CREATE TABLE resume_generation (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES job_application(id) ON DELETE CASCADE,
    tex_path TEXT NOT NULL,
    pdf_path TEXT,
    status VARCHAR(30) NOT NULL,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS kb_docs (
                                       id           varchar(64) PRIMARY KEY,
    category     varchar(64) NOT NULL,
    tags         text,
    last_updated varchar(32),
    blob_text    text NOT NULL,
    snippet      text,
    embedding    vector(1536) NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_kb_category ON kb_docs(category);

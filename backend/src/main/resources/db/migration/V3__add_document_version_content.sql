ALTER TABLE document_versions
    ADD COLUMN title VARCHAR(150) NOT NULL DEFAULT 'Untitled',
    ADD COLUMN content TEXT NOT NULL DEFAULT '';

CREATE
    EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE
    TABLE
        IF NOT EXISTS posts(
            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
            title VARCHAR(255),
            content VARCHAR(255),
            metadata JSON DEFAULT '{}', -- In this sample, use Varchar to store enum(name), Spring Data R2dbc can convert Java Enum to pg VARCHAR, and reverse.
            status VARCHAR(255) DEFAULT 'DRAFT',
            created_at TIMESTAMP, --NOT NULL DEFAULT LOCALTIMESTAMP,
            updated_at TIMESTAMP,
            version INTEGER
        );

CREATE
    TABLE
        IF NOT EXISTS comments(
            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
            content VARCHAR(255),
            created_at TIMESTAMP,
            updated_at TIMESTAMP,
            version INTEGER,
            post_id UUID REFERENCES posts ON
            DELETE
                CASCADE
        );

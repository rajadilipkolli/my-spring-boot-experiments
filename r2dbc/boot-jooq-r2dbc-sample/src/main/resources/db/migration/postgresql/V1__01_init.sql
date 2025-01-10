CREATE
    EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE
    TABLE
        posts(
            id uuid NOT NULL DEFAULT uuid_generate_v4(),
            title text NOT NULL,
            content text NOT NULL,
            status text NOT NULL,
            created_at timestamptz DEFAULT NOW(),
            created_by text,
            updated_at timestamptz,
            version INT DEFAULT 0,
            PRIMARY KEY(id)
        );

CREATE
    TABLE
        post_comments(
            id uuid NOT NULL DEFAULT uuid_generate_v4(),
            content text,
            created_at timestamptz DEFAULT NOW(),
            post_id uuid,
            PRIMARY KEY(id),
            CONSTRAINT FK_POST_COMMENTS FOREIGN KEY(post_id) REFERENCES POSTS(id)
        );

CREATE
    TABLE
        tags(
            id uuid NOT NULL DEFAULT uuid_generate_v4(),
            name text UNIQUE,
            created_at timestamptz DEFAULT NOW(),
            PRIMARY KEY(id)
        );

CREATE
    TABLE
        posts_tags(
            post_id UUID NOT NULL,
            tag_id UUID NOT NULL,
            CONSTRAINT FK_POST_TAGS_PID FOREIGN KEY(post_id) REFERENCES posts(id),
            CONSTRAINT FK_POST_TAGS_TID FOREIGN KEY(tag_id) REFERENCES tags(id),
            CONSTRAINT UK_POST_TAGS UNIQUE(
                post_id,
                tag_id
            )
        );

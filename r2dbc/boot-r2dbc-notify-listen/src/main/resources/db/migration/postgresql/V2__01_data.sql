DELETE
FROM
    comments;

DELETE
FROM
    posts;

INSERT
    INTO
        posts(
            title,
            content,
            metadata
        )
    VALUES(
        'R2dbc is refined',
        'R2dbc is now part of Spring framework core',
        '{"tags":["spring", "r2dbc"]}'
    );

INSERT
    INTO
        posts(
            title,
            content
        )
    VALUES(
        'Spring Data Relational is refined',
        'Spring Data R2dbc is now a subproject of Spring Data Relational'
    );

INSERT
    INTO
        comments(
            content,
            post_id
        ) SELECT
            content_data,
            post_id
        FROM
            (
            VALUES('I Love R2dbc'),
            ('I like Reactive Postgresql'),
            ('Spring makes Reactive programing easy'),
            ('flyway makes db management easy')
            ) AS bulk(content_data),
            (
                SELECT
                    id AS post_id
                FROM
                    posts LIMIT 1
            ) AS post;

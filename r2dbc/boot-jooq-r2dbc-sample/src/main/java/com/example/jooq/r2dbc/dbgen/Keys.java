/*
 * This file is generated by jOOQ.
 */
package com.example.jooq.r2dbc.dbgen;

import com.example.jooq.r2dbc.dbgen.tables.PostComments;
import com.example.jooq.r2dbc.dbgen.tables.Posts;
import com.example.jooq.r2dbc.dbgen.tables.PostsTags;
import com.example.jooq.r2dbc.dbgen.tables.Tags;
import com.example.jooq.r2dbc.dbgen.tables.records.PostCommentsRecord;
import com.example.jooq.r2dbc.dbgen.tables.records.PostsRecord;
import com.example.jooq.r2dbc.dbgen.tables.records.PostsTagsRecord;
import com.example.jooq.r2dbc.dbgen.tables.records.TagsRecord;
import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;

/** A class modelling foreign key relationships and constraints of tables in public. */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<PostCommentsRecord> POST_COMMENTS_PKEY =
            Internal.createUniqueKey(
                    PostComments.POST_COMMENTS,
                    DSL.name("post_comments_pkey"),
                    new TableField[] {PostComments.POST_COMMENTS.ID},
                    true);
    public static final UniqueKey<PostsRecord> POSTS_PKEY =
            Internal.createUniqueKey(
                    Posts.POSTS, DSL.name("posts_pkey"), new TableField[] {Posts.POSTS.ID}, true);
    public static final UniqueKey<PostsTagsRecord> UK_POST_TAGS =
            Internal.createUniqueKey(
                    PostsTags.POSTS_TAGS,
                    DSL.name("uk_post_tags"),
                    new TableField[] {PostsTags.POSTS_TAGS.POST_ID, PostsTags.POSTS_TAGS.TAG_ID},
                    true);
    public static final UniqueKey<TagsRecord> TAGS_NAME_KEY =
            Internal.createUniqueKey(
                    Tags.TAGS, DSL.name("tags_name_key"), new TableField[] {Tags.TAGS.NAME}, true);
    public static final UniqueKey<TagsRecord> TAGS_PKEY =
            Internal.createUniqueKey(
                    Tags.TAGS, DSL.name("tags_pkey"), new TableField[] {Tags.TAGS.ID}, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<PostCommentsRecord, PostsRecord>
            POST_COMMENTS__FK_POST_COMMENTS =
                    Internal.createForeignKey(
                            PostComments.POST_COMMENTS,
                            DSL.name("fk_post_comments"),
                            new TableField[] {PostComments.POST_COMMENTS.POST_ID},
                            Keys.POSTS_PKEY,
                            new TableField[] {Posts.POSTS.ID},
                            true);
    public static final ForeignKey<PostsTagsRecord, PostsRecord> POSTS_TAGS__FK_POST_TAGS_PID =
            Internal.createForeignKey(
                    PostsTags.POSTS_TAGS,
                    DSL.name("fk_post_tags_pid"),
                    new TableField[] {PostsTags.POSTS_TAGS.POST_ID},
                    Keys.POSTS_PKEY,
                    new TableField[] {Posts.POSTS.ID},
                    true);
    public static final ForeignKey<PostsTagsRecord, TagsRecord> POSTS_TAGS__FK_POST_TAGS_TID =
            Internal.createForeignKey(
                    PostsTags.POSTS_TAGS,
                    DSL.name("fk_post_tags_tid"),
                    new TableField[] {PostsTags.POSTS_TAGS.TAG_ID},
                    Keys.TAGS_PKEY,
                    new TableField[] {Tags.TAGS.ID},
                    true);
}

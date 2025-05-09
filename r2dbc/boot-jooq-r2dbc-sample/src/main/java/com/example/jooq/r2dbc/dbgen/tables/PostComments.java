/*
 * This file is generated by jOOQ.
 */
package com.example.jooq.r2dbc.dbgen.tables;

import com.example.jooq.r2dbc.dbgen.Keys;
import com.example.jooq.r2dbc.dbgen.Public;
import com.example.jooq.r2dbc.dbgen.tables.records.PostCommentsRecord;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function4;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row4;
import org.jooq.Schema;
import org.jooq.SelectField;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

/** This class is generated by jOOQ. */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class PostComments extends TableImpl<PostCommentsRecord> {

    private static final long serialVersionUID = 1L;

    /** The reference instance of <code>public.post_comments</code> */
    public static final PostComments POST_COMMENTS = new PostComments();

    /** The class holding records for this type */
    @Override
    public Class<PostCommentsRecord> getRecordType() {
        return PostCommentsRecord.class;
    }

    /** The column <code>public.post_comments.id</code>. */
    public final TableField<PostCommentsRecord, UUID> ID =
            createField(
                    DSL.name("id"),
                    SQLDataType.UUID
                            .nullable(false)
                            .defaultValue(
                                    DSL.field(DSL.raw("uuid_generate_v4()"), SQLDataType.UUID)),
                    this,
                    "");

    /** The column <code>public.post_comments.content</code>. */
    public final TableField<PostCommentsRecord, String> CONTENT =
            createField(DSL.name("content"), SQLDataType.CLOB, this, "");

    /** The column <code>public.post_comments.created_at</code>. */
    public final TableField<PostCommentsRecord, OffsetDateTime> CREATED_AT =
            createField(
                    DSL.name("created_at"),
                    SQLDataType.TIMESTAMPWITHTIMEZONE(6)
                            .defaultValue(
                                    DSL.field(DSL.raw("now()"), SQLDataType.TIMESTAMPWITHTIMEZONE)),
                    this,
                    "");

    /** The column <code>public.post_comments.post_id</code>. */
    public final TableField<PostCommentsRecord, UUID> POST_ID =
            createField(DSL.name("post_id"), SQLDataType.UUID, this, "");

    private PostComments(Name alias, Table<PostCommentsRecord> aliased) {
        this(alias, aliased, null);
    }

    private PostComments(Name alias, Table<PostCommentsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /** Create an aliased <code>public.post_comments</code> table reference */
    public PostComments(String alias) {
        this(DSL.name(alias), POST_COMMENTS);
    }

    /** Create an aliased <code>public.post_comments</code> table reference */
    public PostComments(Name alias) {
        this(alias, POST_COMMENTS);
    }

    /** Create a <code>public.post_comments</code> table reference */
    public PostComments() {
        this(DSL.name("post_comments"), null);
    }

    public <O extends Record> PostComments(Table<O> child, ForeignKey<O, PostCommentsRecord> key) {
        super(child, key, POST_COMMENTS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public UniqueKey<PostCommentsRecord> getPrimaryKey() {
        return Keys.POST_COMMENTS_PKEY;
    }

    @Override
    public List<ForeignKey<PostCommentsRecord, ?>> getReferences() {
        return Arrays.asList(Keys.POST_COMMENTS__FK_POST_COMMENTS);
    }

    private transient Posts _posts;

    /** Get the implicit join path to the <code>public.posts</code> table. */
    public Posts posts() {
        if (_posts == null) _posts = new Posts(this, Keys.POST_COMMENTS__FK_POST_COMMENTS);

        return _posts;
    }

    @Override
    public PostComments as(String alias) {
        return new PostComments(DSL.name(alias), this);
    }

    @Override
    public PostComments as(Name alias) {
        return new PostComments(alias, this);
    }

    @Override
    public PostComments as(Table<?> alias) {
        return new PostComments(alias.getQualifiedName(), this);
    }

    /** Rename this table */
    @Override
    public PostComments rename(String name) {
        return new PostComments(DSL.name(name), null);
    }

    /** Rename this table */
    @Override
    public PostComments rename(Name name) {
        return new PostComments(name, null);
    }

    /** Rename this table */
    @Override
    public PostComments rename(Table<?> name) {
        return new PostComments(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<UUID, String, OffsetDateTime, UUID> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    /** Convenience mapping calling {@link SelectField#convertFrom(Function)}. */
    public <U> SelectField<U> mapping(
            Function4<
                            ? super UUID,
                            ? super String,
                            ? super OffsetDateTime,
                            ? super UUID,
                            ? extends U>
                    from) {
        return convertFrom(Records.mapping(from));
    }

    /** Convenience mapping calling {@link SelectField#convertFrom(Class, Function)}. */
    public <U> SelectField<U> mapping(
            Class<U> toType,
            Function4<
                            ? super UUID,
                            ? super String,
                            ? super OffsetDateTime,
                            ? super UUID,
                            ? extends U>
                    from) {
        return convertFrom(toType, Records.mapping(from));
    }
}

/*
 * This file is generated by jOOQ.
 */
package com.example.jooq.r2dbc.dbgen.routines;

import com.example.jooq.r2dbc.dbgen.Public;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;

/** This class is generated by jOOQ. */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class UuidGenerateV3 extends AbstractRoutine<UUID> {

    private static final long serialVersionUID = 1L;

    /** The parameter <code>public.uuid_generate_v3.RETURN_VALUE</code>. */
    public static final Parameter<UUID> RETURN_VALUE =
            Internal.createParameter("RETURN_VALUE", SQLDataType.UUID, false, false);

    /** The parameter <code>public.uuid_generate_v3.namespace</code>. */
    public static final Parameter<UUID> NAMESPACE =
            Internal.createParameter("namespace", SQLDataType.UUID, false, false);

    /** The parameter <code>public.uuid_generate_v3.name</code>. */
    public static final Parameter<String> NAME =
            Internal.createParameter("name", SQLDataType.CLOB, false, false);

    /** Create a new routine call instance */
    public UuidGenerateV3() {
        super("uuid_generate_v3", Public.PUBLIC, SQLDataType.UUID);

        setReturnParameter(RETURN_VALUE);
        addInParameter(NAMESPACE);
        addInParameter(NAME);
    }

    /** Set the <code>namespace</code> parameter IN value to the routine */
    public void setNamespace(UUID value) {
        setValue(NAMESPACE, value);
    }

    /**
     * Set the <code>namespace</code> parameter to the function to be used with a {@link
     * org.jooq.Select} statement
     */
    public void setNamespace(Field<UUID> field) {
        setField(NAMESPACE, field);
    }

    /** Set the <code>name</code> parameter IN value to the routine */
    public void setName_(String value) {
        setValue(NAME, value);
    }

    /**
     * Set the <code>name</code> parameter to the function to be used with a {@link org.jooq.Select}
     * statement
     */
    public void setName_(Field<String> field) {
        setField(NAME, field);
    }
}

package com.example.custom.sequence.config.db;

import java.lang.reflect.Member;
import java.util.EnumSet;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.GeneratorCreationContext;

public class StringPrefixedNumberFormattedSequenceIdGenerator implements BeforeExecutionGenerator {

    private final String valuePrefix;
    private final String numberFormat;
    private final String sequenceName;
    private final String sequenceNextValSql;

    public StringPrefixedNumberFormattedSequenceIdGenerator(
            StringPrefixedSequence annotation, Member member, GeneratorCreationContext context) {

        this.valuePrefix = annotation.valuePrefix();
        this.numberFormat = annotation.numberFormat();
        // Derive sequence name from the declaring class (use lowercase + 's' + '_seq')
        String simpleName = member.getDeclaringClass().getSimpleName();
        this.sequenceName = simpleName.toLowerCase() + "s_seq";

        var dialect = context.getDatabase().getDialect();
        this.sequenceNextValSql = dialect.getSequenceSupport().getSequenceNextValString(sequenceName);
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EnumSet.of(EventType.INSERT);
    }

    @Override
    public Object generate(
            SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        // Fetch the next value from the database sequence
        Long nextValue = session.doReturningWork(connection -> {
            try (var stmt = connection.createStatement();
                    var rs = stmt.executeQuery(sequenceNextValSql)) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new HibernateException("Unable to fetch sequence value for " + sequenceName);
            }
        });

        // Format the Long value as a String with the prefix
        return valuePrefix + numberFormat.formatted(nextValue);
    }
}

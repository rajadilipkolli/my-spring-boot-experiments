package com.example.custom.sequence.config.db;

import java.io.Serializable;
import java.lang.reflect.Member;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

public class StringPrefixedNumberFormattedSequenceIdGenerator extends SequenceStyleGenerator {

    private final String valuePrefix;
    private final String numberFormat;

    public StringPrefixedNumberFormattedSequenceIdGenerator(
            StringPrefixedSequence annotation, Member annotatedMember, GeneratorCreationContext context) {
        valuePrefix = annotation.valuePrefix();
        numberFormat = annotation.numberFormat();
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return valuePrefix + numberFormat.formatted(super.generate(session, object));
    }
}

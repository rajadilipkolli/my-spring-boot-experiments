package com.example.custom.sequence.config.db;

import java.io.Serializable;
import java.lang.reflect.Member;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.hibernate.type.spi.TypeConfiguration;

public class StringPrefixedNumberFormattedSequenceIdGenerator extends SequenceStyleGenerator {

    private final String valuePrefix;
    private final String numberFormat;

    public StringPrefixedNumberFormattedSequenceIdGenerator(
            StringPrefixedSequence config, Member annotatedMember, GeneratorCreationContext context) {
        valuePrefix = config.valuePrefix();
        numberFormat = config.numberFormat();
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        return valuePrefix + numberFormat.formatted(super.generate(session, object));
    }

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        super.configure(
                new TypeConfiguration().getBasicTypeRegistry().getRegisteredType(Long.class), params, serviceRegistry);
    }
}

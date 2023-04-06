package com.example.custom.sequence.config;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.JavaObjectType;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.AdjustableJdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.hibernate.type.descriptor.jdbc.JdbcTypeJavaClassMappings;
import org.hibernate.type.descriptor.jdbc.LongVarcharJdbcType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Properties;

public class StringPrefixedSequenceIdGenerator extends SequenceStyleGenerator {

    public static final String VALUE_PREFIX_PARAMETER = "valuePrefix";
    public static final String VALUE_PREFIX_DEFAULT = "";
    private String valuePrefix;

    public static final String NUMBER_FORMAT_PARAMETER = "numberFormat";
    public static final String NUMBER_FORMAT_DEFAULT = "%d";
    private String numberFormat;

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object)
            throws HibernateException {
        return valuePrefix + String.format(numberFormat, super.generate(session, object));
    }

    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry)
            throws MappingException {
        super.configure(
                new JavaObjectType(LongVarcharJdbcType.INSTANCE, new CustomLongJavaType<>()),
                params,
                serviceRegistry);
        valuePrefix =
                ConfigurationHelper.getString(VALUE_PREFIX_PARAMETER, params, VALUE_PREFIX_DEFAULT);
        numberFormat =
                ConfigurationHelper.getString(
                        NUMBER_FORMAT_PARAMETER, params, NUMBER_FORMAT_DEFAULT);
    }

    private class CustomLongJavaType<O> implements JavaType<Object> {

        @Override
        public JdbcType getRecommendedJdbcType(JdbcTypeIndicators indicators) {
            // match legacy behavior
            final JdbcType descriptor =
                    indicators
                            .getTypeConfiguration()
                            .getJdbcTypeRegistry()
                            .getDescriptor(
                                    JdbcTypeJavaClassMappings.INSTANCE
                                            .determineJdbcTypeCodeForJavaClass(getJavaTypeClass()));
            if (descriptor instanceof AdjustableJdbcType) {
                return ((AdjustableJdbcType) descriptor).resolveIndicatedType(indicators, this);
            }
            return descriptor;
        }

        @Override
        public Object fromString(CharSequence string) {
            return Long.valueOf(string.toString());
        }

        @Override
        public Class<Long> getJavaType() {
            return Long.class;
        }

        @Override
        public <X> X unwrap(Object value, Class<X> type, WrapperOptions options) {
            if (value == null) {
                return null;
            }
            if (Long.class.isAssignableFrom(type)) {
                return (X) value;
            }
            if (Byte.class.isAssignableFrom(type)) {
                return (X) Byte.valueOf(((Long) value).byteValue());
            }
            if (Short.class.isAssignableFrom(type)) {
                return (X) Short.valueOf(((Long) value).shortValue());
            }
            if (Integer.class.isAssignableFrom(type)) {
                return (X) Integer.valueOf(((Long) value).intValue());
            }
            if (Double.class.isAssignableFrom(type)) {
                return (X) Double.valueOf(((Long) value).doubleValue());
            }
            if (Float.class.isAssignableFrom(type)) {
                return (X) Float.valueOf(((Long) value).floatValue());
            }
            if (BigInteger.class.isAssignableFrom(type)) {
                return (X) BigInteger.valueOf((Long) value);
            }
            if (BigDecimal.class.isAssignableFrom(type)) {
                return (X) BigDecimal.valueOf((Long) value);
            }
            if (String.class.isAssignableFrom(type)) {
                return (X) value.toString();
            }
            throw unknownUnwrap(type);
        }

        private <X> HibernateException unknownUnwrap(Class<X> sourceType) {
            throw new HibernateException(
                    "Unknown unwrap conversion requested: "
                            + sourceType.getName()
                            + " to "
                            + Long.class.getName());
        }

        @Override
        public <X> Long wrap(X value, WrapperOptions options) {
            if (value == null) {
                return null;
            }
            if (value instanceof Long) {
                return (Long) value;
            }
            if (value instanceof Number) {
                return ((Number) value).longValue();
            } else if (value instanceof String) {
                return Long.valueOf(((String) value));
            }
            throw unknownWrap(value.getClass());
        }

        private <X> HibernateException unknownWrap(Class<X> valueType) {
            throw new HibernateException(
                    "Unknown wrap conversion requested: "
                            + valueType.getName()
                            + " to "
                            + Long.class.getName());
        }
    }
}

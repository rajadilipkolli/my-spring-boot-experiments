package com.example.custom.sequence.config.db;

import java.lang.reflect.Member;
import java.util.EnumSet;
import java.util.Locale;
import org.hibernate.HibernateException;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.ExportableProducer;
import org.hibernate.boot.model.relational.QualifiedName;
import org.hibernate.boot.model.relational.QualifiedNameParser;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.Configurable;
import org.hibernate.id.enhanced.DatabaseStructure;
import org.hibernate.id.enhanced.Optimizer;
import org.hibernate.id.enhanced.OptimizerFactory;
import org.hibernate.id.enhanced.SequenceStructure;
import org.hibernate.id.enhanced.StandardOptimizerDescriptor;
import org.hibernate.mapping.Table;

public class StringPrefixedNumberFormattedSequenceIdGenerator
        implements BeforeExecutionGenerator, ExportableProducer, Configurable {

    private final Table table;
    private final String valuePrefix;
    private final String numberFormat;
    private final DatabaseStructure structure;
    private final Optimizer optimizer;

    /**
     * Creates a string identifier generator backed by a pooled-low database sequence.
     *
     * @param config sequence name, allocation settings, prefix, and numeric format
     * @param member annotated entity member supplied by Hibernate
     * @param context mapping and database metadata used to configure the sequence
     * @throws HibernateException if the sequence name is blank, the allocation size is not positive,
     *     or the database does not support sequences
     */
    public StringPrefixedNumberFormattedSequenceIdGenerator(
            StringPrefixedSequence config, Member member, GeneratorCreationContext context) {

        if (config.sequenceName().isBlank()) {
            throw new HibernateException("sequenceName must not be blank");
        }

        if (config.allocationSize() < 1) {
            throw new HibernateException("allocationSize must be positive");
        }

        var jdbcEnvironment = context.getDatabase().getJdbcEnvironment();

        if (!jdbcEnvironment.getDialect().getSequenceSupport().supportsSequences()) {
            throw new HibernateException("This generator requires a database with sequence support");
        }

        this.valuePrefix = config.valuePrefix();
        this.numberFormat = config.numberFormat();
        this.table = context.getValue().getTable();

        QualifiedName sequenceName = QualifiedNameParser.INSTANCE.parse(config.sequenceName());

        this.structure =
                new SequenceStructure("orm", sequenceName, config.initialValue(), config.allocationSize(), Long.class);

        this.optimizer = OptimizerFactory.buildOptimizer(
                StandardOptimizerDescriptor.POOLED_LO, Long.class, config.allocationSize(), config.initialValue());

        this.structure.configure(optimizer);
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EnumSet.of(EventType.INSERT);
    }

    /**
     * Obtains the next pooled sequence value and formats it with the configured prefix.
     *
     * @param session session used when the optimizer needs another sequence allocation
     * @param owner entity receiving the identifier
     * @param currentValue current identifier value, unused by this generator
     * @param eventType persistence event requesting the identifier
     * @return the prefixed identifier formatted using the root locale
     * @throws HibernateException if the optimizer returns a nonnumeric value
     */
    @Override
    public Object generate(
            SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {

        Object value = optimizer.generate(structure.buildCallback(session));

        if (!(value instanceof Number number)) {
            throw new HibernateException("Sequence optimizer did not return a numeric value");
        }

        return valuePrefix + String.format(Locale.ROOT, numberFormat, number.longValue());
    }

    /**
     * Registers the sequence and any optimizer-specific schema objects with Hibernate.
     *
     * @param database database model that receives the exported objects
     */
    @Override
    public void registerExportables(Database database) {
        structure.registerExportables(database);
        structure.registerExtraExportables(table, optimizer);
    }

    /**
     * Initializes the sequence SQL using Hibernate's SQL generation context.
     *
     * @param context dialect and naming context for generating SQL
     */
    @Override
    public void initialize(SqlStringGenerationContext context) {
        structure.initialize(context);
    }
}

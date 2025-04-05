package com.example.jooq.r2dbc.repository.custom.impl;

import static com.example.jooq.r2dbc.dbgen.Tables.TAGS;

import com.example.jooq.r2dbc.entities.Tags;
import com.example.jooq.r2dbc.repository.custom.CustomTagRepository;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CustomTagRepositoryImpl extends JooqSorting implements CustomTagRepository {

    private final DSLContext dslContext;

    public CustomTagRepositoryImpl(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public Mono<Page<Tags>> findAll(Pageable pageable) {
        var dataSql =
                dslContext
                        .select(TAGS.ID, TAGS.NAME)
                        .from(TAGS)
                        .orderBy(getSortFields(pageable.getSort(), TAGS))
                        .limit(pageable.getPageSize())
                        .offset(pageable.getOffset());
        var countSql = dslContext.selectCount().from(TAGS);
        return Mono.zip(
                        Flux.from(dataSql)
                                .map(r -> new Tags().setId(r.value1()).setName(r.value2()))
                                .collectList(),
                        Mono.from(countSql).map(Record1::value1))
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }
}

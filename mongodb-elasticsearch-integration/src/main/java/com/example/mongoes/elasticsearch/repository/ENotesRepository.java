package com.example.mongoes.elasticsearch.repository;

import com.example.mongoes.elasticsearch.domain.ENotes;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;

public interface ENotesRepository extends ReactiveElasticsearchRepository<ENotes, String> {
}

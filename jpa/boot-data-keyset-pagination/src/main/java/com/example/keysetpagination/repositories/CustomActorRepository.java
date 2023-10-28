package com.example.keysetpagination.repositories;

import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PagedList;
import com.example.keysetpagination.entities.Actor;
import org.springframework.data.domain.Sort;

public interface CustomActorRepository {

    PagedList<Actor> findTopN(Sort sortBy, int pageSize);

    PagedList<Actor> findNextN(Sort orderBy, KeysetPage keysetPage, int firstResult, int maxResult);
}

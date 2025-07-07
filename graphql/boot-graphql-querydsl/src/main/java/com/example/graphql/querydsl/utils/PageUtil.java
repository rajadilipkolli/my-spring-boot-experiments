package com.example.graphql.querydsl.utils;

import com.example.graphql.querydsl.model.query.FindQuery;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageUtil {

    public static Pageable createPageable(FindQuery findQuery) {
        int pageNo = Math.max(findQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findQuery.sortBy())
                        : Sort.Order.desc(findQuery.sortBy()));
        return PageRequest.of(pageNo, findQuery.pageSize(), sort);
    }
}

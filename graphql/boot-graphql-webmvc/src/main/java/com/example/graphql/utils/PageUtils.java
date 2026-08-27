package com.example.graphql.utils;

import com.example.graphql.model.query.FindQuery;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageUtils {

    public static Pageable createPageable(FindQuery findQuery) {
        int pageNo = Math.max(findQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findQuery.sortBy())
                        : Sort.Order.desc(findQuery.sortBy()));
        return PageRequest.of(pageNo, findQuery.pageSize(), sort);
    }

    private PageUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

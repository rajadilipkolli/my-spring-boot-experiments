package com.example.archunit.utils;

import com.example.archunit.model.query.FindClientsQuery;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageUtils {

    public static Pageable createPageable(FindClientsQuery findClientsQuery) {
        int pageNo = Math.max(findClientsQuery.pageNo() - 1, 0);
        Sort sort = Sort.by(
                findClientsQuery.sortDir().equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.Order.asc(findClientsQuery.sortBy())
                        : Sort.Order.desc(findClientsQuery.sortBy()));
        return PageRequest.of(pageNo, findClientsQuery.pageSize(), sort);
    }

    private PageUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

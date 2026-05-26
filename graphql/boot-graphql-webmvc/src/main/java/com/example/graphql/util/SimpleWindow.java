package com.example.graphql.util;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.util.Assert;

public class SimpleWindow<T> implements Window<T> {

    private final List<T> content;
    private final ScrollPosition position;
    private final Boolean hasNext;

    public SimpleWindow(List<T> content, ScrollPosition position, Boolean hasNext) {
        this.content = content;
        this.position = position;
        this.hasNext = hasNext;
    }

    @Override
    public int size() {
        return content.size();
    }

    @Override
    public boolean isEmpty() {
        return content.isEmpty();
    }

    @Override
    public List<T> getContent() {
        return content;
    }

    @Override
    public boolean hasNext() {
        return Boolean.TRUE.equals(hasNext);
    }

    @Override
    public ScrollPosition positionAt(int index) {
        return position;
    }

    @Override
    public <U> Window<U> map(Function<? super T, ? extends U> converter) {
        Assert.notNull(converter, "Function must not be null");
        return new SimpleWindow<>(content.stream().map(converter).collect(Collectors.toList()), position, hasNext);
    }

    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }
}

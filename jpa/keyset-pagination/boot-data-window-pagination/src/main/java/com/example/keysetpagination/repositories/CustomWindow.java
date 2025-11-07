package com.example.keysetpagination.repositories;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.util.Assert;

public class CustomWindow<T> implements Window<T> {

    private List<T> content;
    private ScrollPosition position;
    private Boolean hasNext;

    public CustomWindow(List<T> content, ScrollPosition position, Boolean hasNext) {
        this.content = content;
        this.position = position;
        this.hasNext = hasNext;
    }

    public CustomWindow() {}

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
        if (index < 0 || index >= content.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds for content size: " + content.size());
        }
        // You can customize this if you have additional scroll position data per item
        return position;
    }

    @Override
    public <U> Window<U> map(Function<? super T, ? extends U> converter) {
        Assert.notNull(converter, "Function must not be null");

        return new CustomWindow<>(stream().map(converter).collect(Collectors.toList()), position, hasNext);
    }

    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }
}

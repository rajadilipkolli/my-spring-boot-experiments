package com.example.highrps.shared;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * A queue that serializes aggregate operations by key using CompletableFutures.
 * Used to ensure that operations for a specific aggregate ID execute sequentially.
 */
public class AggregateOperationQueue {

    private final ConcurrentHashMap<String, CompletableFuture<Void>> futures = new ConcurrentHashMap<>();
    private final Executor executor;

    public AggregateOperationQueue() {
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public CompletableFuture<Void> enqueue(String aggregateKey, Supplier<CompletableFuture<Void>> operation) {
        AtomicReference<CompletableFuture<Void>> currentRef = new AtomicReference<>();

        futures.compute(aggregateKey, (key, previous) -> {
            CompletableFuture<Void> tail = (previous == null) ? CompletableFuture.completedFuture(null) : previous;

            CompletableFuture<Void> next =
                    tail.exceptionally(ex -> null).thenComposeAsync(ignored -> operation.get(), executor);

            currentRef.set(next);
            return next;
        });

        CompletableFuture<Void> current = currentRef.get();

        current.whenComplete((ignored, throwable) -> {
            futures.computeIfPresent(aggregateKey, (key, tail) -> tail == current ? null : tail);
        });

        return current;
    }
}

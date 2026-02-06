package com.example.highrps.utility;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class RequestCoalescer<T> {
    Map<String, CompletableFuture<T>> inFlightRequests = new ConcurrentHashMap<>();

    public T subscribe(String key, Supplier<T> supplier) {
        CompletableFuture<T> future = getOrCreateFuture(key, supplier);
        return future.join();
    }

    private CompletableFuture<T> getOrCreateFuture(String key, Supplier<T> supplier) {
        CompletableFuture<T> future = inFlightRequests.get(key);
        if (future != null) {
            return future;
        }
        CompletableFuture<T> newFuture = new CompletableFuture<>();
        CompletableFuture<T> oldFuture = inFlightRequests.putIfAbsent(key, newFuture);
        if (oldFuture != null) {
            return oldFuture;
        } else {
            CompletableFuture.supplyAsync(() -> {
                try {
                    T result = supplier.get();
                    newFuture.complete(result);
                    inFlightRequests.remove(key, newFuture);
                    return result;
                } catch (Exception e) {
                    newFuture.completeExceptionally(e);
                    inFlightRequests.remove(key, newFuture);
                    // return value is unused - newFuture is actually used.
                    return null;
                }
            });
            return newFuture;
        }
    }
}

package com.example;

import org.example.Person;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class PersonBenchmark {

    @Param({"100"})
    private long size;

    private List<Person> persons;

    @Setup
    public void setup() {
        persons = new ArrayList<>();
        for (long i = 0; i < size; i++) {
            persons.add(new Person("John" + i, 30));
        }
    }

    @Benchmark
    @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    public void testNormalApproach() {
        List<Person> personList = new ArrayList<>();
        for (Person person : persons) {
            person.setName(person.getName() + "a");
            person.setAge(person.getAge() + 1);
            personList.add(person);
        }
    }

    @Benchmark
    @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    public void testParallelUsingCompletableFuture() {
        List<CompletableFuture<Person>> completableFutureList = persons.stream()
                .map(person -> CompletableFuture.supplyAsync(() -> {
                    person.setName(person.getName() + "a");
                    person.setAge(person.getAge() + 1);
                    return person;
                })).toList();
        List<Person> updatedPersons = completableFutureList.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    @Benchmark
    @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    public void testParallelUsingParallelStream() {
        var newPersonList = persons.parallelStream()
                .map(person -> {
                    Person p = new Person();
                    p.setName(person.getName() + "a");
                    p.setAge(person.getAge() + 1);
                    return p;
                }).toList();
    }

    @Benchmark
    @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    public void testParallelUsingStreamParallel() {
        var newPersonList = persons.stream().parallel()
                .map(person -> {
                    Person p = new Person();
                    p.setName(person.getName() + "a");
                    p.setAge(person.getAge() + 1);
                    return p;
                }).toList();
    }
}


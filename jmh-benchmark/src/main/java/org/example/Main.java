package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {
        List<Person> persons = new ArrayList<>();
        for (long i = 0; i < 10000000L; i++) {
            persons.add(new Person("John" + i, 30));
        }
        //1. normal approach - without parallel
        long time1 = System.currentTimeMillis();
        List<Person> personList = new ArrayList<>();
        for (Person person : persons) {
            person.setName(person.getName() + "a");
            person.setAge(person.getAge() + 1);
            personList.add(person);
        }
        System.out.println("Without parallel - Total time taken: " + (System.currentTimeMillis() - time1) + " ms");
        //2. completable future approach - parallel
        long time2 = System.currentTimeMillis();
        List<CompletableFuture<Person>> completableFutureList = persons.stream()
                .map(person -> CompletableFuture.supplyAsync(() -> {
                    person.setName(person.getName() + "a");
                    person.setAge(person.getAge() + 1);
                    return person;
                })).toList();
        List<Person> updatedPersons = completableFutureList.stream()
                .map(CompletableFuture::join)
                .toList();
        System.out.println("With parallel - Total time taken: " + (System.currentTimeMillis() - time2) + " ms");
    }
}
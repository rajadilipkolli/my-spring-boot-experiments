package com.example.ultimateredis.model;

import java.util.StringJoiner;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("actor")
public class Actor {

    @Id
    String id;

    @Indexed
    String name;

    @Indexed
    Integer age;

    public Actor() {}

    public String getId() {
        return id;
    }

    public Actor setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Actor setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getAge() {
        return age;
    }

    public Actor setAge(Integer age) {
        this.age = age;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Actor.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("name='" + name + "'")
                .add("age=" + age)
                .toString();
    }
}

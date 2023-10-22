package com.example.ultimateredis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("actor")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Actor {

    @Id String id;

    @Indexed String name;

    @Indexed Integer age;
}

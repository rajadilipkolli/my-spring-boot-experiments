package com.example.choasmonkey;

import com.example.choasmonkey.common.ContainerConfig;
import org.springframework.boot.SpringApplication;

public class TestChoasMonkeyApplication {

    public static void main(String[] args) {
        SpringApplication.from(ChoasMonkeyApplication::main).with(ContainerConfig.class).run(args);
    }
}

package com.locallink;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.locallink.mapper")
public class LocallinkSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocallinkSystemApplication.class, args);
    }

}

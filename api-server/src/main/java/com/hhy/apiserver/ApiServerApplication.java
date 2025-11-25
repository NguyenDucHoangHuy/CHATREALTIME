package com.hhy.apiserver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiServerApplication {
    @Value("${application.jwt.secretKey}")
    public String secretKey;

    public static void main(String[] args) {
        SpringApplication.run(ApiServerApplication.class, args);
    }
}

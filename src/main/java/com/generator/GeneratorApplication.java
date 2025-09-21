package com.generator;

import module spring.boot;
import module io.github.cdimascio.dotenv.java;
import module spring.boot.autoconfigure;

@SpringBootApplication
public class GeneratorApplication {
    
    
    // Dotenv load
    static {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
        
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
    }
    
    static void main(String[] args) {
        SpringApplication.run(GeneratorApplication.class, args);
    }
    
}

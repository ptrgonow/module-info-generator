package com.generator.service;

import module spring.web;
import module spring.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SampleService {
    
    public int findAge(String name) {
        int age = 0;
        if(name.equals("John Doe")) {
            log.info("Name is John Doe, returning age 30");
            age = 30;
        } else {
            log.info("Name not found, returning age 0");
        }
        log.info("Finding age: {}", name);
        return age;
    }
}

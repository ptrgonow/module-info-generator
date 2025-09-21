package com.generator.controller;

import module spring.web;

import com.generator.service.SampleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SampleController {
    
    private final SampleService sample;
    
    @GetMapping("/age")
    public int getSample() {
        String name = "John Doe";
        return sample.findAge(name);
    }
}

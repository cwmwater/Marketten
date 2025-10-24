package com.example.Marketten.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TestHomeController {

    // 루트 경로 접속 시 index.html 보여주기
    @GetMapping("/")
    public String index() {
        return "index";
    }

}

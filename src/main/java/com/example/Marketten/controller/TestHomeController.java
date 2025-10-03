package com.example.Marketten.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/*@Controller
@RequiredArgsConstructor
@Slf4j*/
public class TestHomeController {

    // 루트 경로 접속 시 index.html 보여주기
    @GetMapping("/")
    public String index() {
        // src/main/resources/templates/index.html 또는 static/index.html 기준
        return "index";
        // templates/index.html 사용 시 Thymeleaf가 필요
        // static/index.html 사용 시 그냥 파일 경로 자동 매핑 가능
    }

}

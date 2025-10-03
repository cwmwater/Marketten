package com.example.Marketten.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
@Slf4j
public class KeyWordController {

    public String getKeywords() {
        log.info("KeyWordController의 getKeywords 메소드가 호출되었습니다.");
        return "키워드 목록을 반환합니다.";
    }
}

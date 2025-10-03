package com.example.Marketten.service;

import com.example.Marketten.dto.login.LoginResponse;
import com.example.Marketten.dto.user.UserRequest;

public interface RegisterService {

    // 회원가입 후 자동 로그인 처리를 위해 LoginResponse를 반환
    LoginResponse registerNewUserAndLogin(UserRequest request);
}
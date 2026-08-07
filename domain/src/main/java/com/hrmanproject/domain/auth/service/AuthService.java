package com.hrmanproject.domain.auth.service;

import com.hrmanproject.domain.auth.dto.LoginRequestDto;
import com.hrmanproject.domain.auth.dto.LoginResponseDto;

public interface AuthService {

    LoginResponseDto login(LoginRequestDto request);

    boolean authenticateMail(String mail);
}

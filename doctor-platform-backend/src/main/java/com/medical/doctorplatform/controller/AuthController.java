package com.medical.doctorplatform.controller;

import com.medical.doctorplatform.common.ApiResult;
import com.medical.doctorplatform.dto.LoginRequest;
import com.medical.doctorplatform.dto.LoginResponse;
import com.medical.doctorplatform.security.JwtUtil;
import com.medical.doctorplatform.security.LoginUser;
import com.medical.doctorplatform.service.LoginRateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final LoginRateLimiter loginRateLimiter;
    private final CaptchaController captchaController;

    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        if (!captchaController.verify(request.getCaptchaKey(), request.getCaptchaCode())) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }
        loginRateLimiter.checkBlockedOrThrow(request.getUsername());
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            LoginUser lu = (LoginUser) auth.getPrincipal();
            loginRateLimiter.clearOnSuccess(request.getUsername());
            String token = jwtUtil.createToken(lu.getDoctor());
            return ApiResult.ok(LoginResponse.of(token, lu.getDoctor()));
        } catch (BadCredentialsException e) {
            loginRateLimiter.recordFailure(request.getUsername());
            throw e;
        }
    }
}

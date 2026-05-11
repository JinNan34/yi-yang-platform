package com.medical.doctorplatform.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.medical.doctorplatform.common.ApiResult;
import com.medical.doctorplatform.dto.LoginResponse;
import com.medical.doctorplatform.dto.PasswordUpdateRequest;
import com.medical.doctorplatform.entity.Doctor;
import com.medical.doctorplatform.mapper.DoctorMapper;
import com.medical.doctorplatform.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class ProfileController {

    private final DoctorMapper doctorMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ApiResult<LoginResponse.DoctorProfile> profile() {
        Doctor d = doctorMapper.selectById(SecurityUtils.currentDoctorId());
        return ApiResult.ok(LoginResponse.DoctorProfile.from(d));
    }

    @PutMapping
    public ApiResult<Void> update(@RequestBody Doctor body) {
        Long id = SecurityUtils.currentDoctorId();
        doctorMapper.update(null, new LambdaUpdateWrapper<Doctor>()
                .eq(Doctor::getId, id)
                .set(body.getRealName() != null, Doctor::getRealName, body.getRealName())
                .set(body.getTitle() != null, Doctor::getTitle, body.getTitle())
                .set(body.getDepartment() != null, Doctor::getDepartment, body.getDepartment())
                .set(body.getPhone() != null, Doctor::getPhone, body.getPhone())
                .set(body.getAvatar() != null, Doctor::getAvatar, body.getAvatar()));
        return ApiResult.ok();
    }

    @PutMapping("/password")
    public ApiResult<Void> password(@Valid @RequestBody PasswordUpdateRequest request) {
        Doctor d = doctorMapper.selectById(SecurityUtils.currentDoctorId());
        if (!passwordEncoder.matches(request.getOldPassword(), d.getPassword())) {
            throw new IllegalArgumentException("原密码错误");
        }
        d.setPassword(passwordEncoder.encode(request.getNewPassword()));
        doctorMapper.updateById(d);
        return ApiResult.ok();
    }
}

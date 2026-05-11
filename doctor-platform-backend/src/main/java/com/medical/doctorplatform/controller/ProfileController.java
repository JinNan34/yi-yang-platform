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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class ProfileController {

    private final DoctorMapper doctorMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

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

    @PostMapping("/avatar")
    public ApiResult<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的图片");
        }
        
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (extension == null || !extension.matches("(?i)jpg|jpeg|png|gif|webp")) {
            throw new IllegalArgumentException("只支持 jpg、jpeg、png、gif、webp 格式的图片");
        }
        
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String newFilename = "avatar_" + SecurityUtils.currentDoctorId() + "_" + timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;
            Path filePath = uploadPath.resolve(newFilename);
            
            Files.copy(file.getInputStream(), filePath);
            
            String avatarUrl = contextPath + "/files/" + newFilename;
            
            doctorMapper.update(null, new LambdaUpdateWrapper<Doctor>()
                    .eq(Doctor::getId, SecurityUtils.currentDoctorId())
                    .set(Doctor::getAvatar, avatarUrl));
            
            Map<String, String> result = new HashMap<>();
            result.put("url", avatarUrl);
            return ApiResult.ok(result);
            
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
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
package com.medical.doctorplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medical.doctorplatform.common.ApiResult;
import com.medical.doctorplatform.entity.HealthAssessment;
import com.medical.doctorplatform.service.HealthAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health-assessments")
@RequiredArgsConstructor
public class HealthAssessmentController {

    private final HealthAssessmentService healthAssessmentService;

    @GetMapping
    public ApiResult<IPage<HealthAssessment>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long elderId) {
        return ApiResult.ok(healthAssessmentService.page(page, size, elderId));
    }

    @GetMapping("/{id}")
    public ApiResult<HealthAssessment> get(@PathVariable Long id) {
        return ApiResult.ok(healthAssessmentService.getById(id));
    }

    @PostMapping
    public ApiResult<HealthAssessment> create(@RequestBody HealthAssessment entity) {
        return ApiResult.ok(healthAssessmentService.create(entity));
    }

    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody HealthAssessment entity) {
        healthAssessmentService.update(id, entity);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        healthAssessmentService.delete(id);
        return ApiResult.ok();
    }
}
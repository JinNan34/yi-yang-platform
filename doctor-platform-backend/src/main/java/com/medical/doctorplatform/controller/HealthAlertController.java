package com.medical.doctorplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medical.doctorplatform.common.ApiResult;
import com.medical.doctorplatform.entity.HealthAlert;
import com.medical.doctorplatform.service.HealthAlertService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/health-alerts")
@RequiredArgsConstructor
public class HealthAlertController {

    private final HealthAlertService healthAlertService;

    @GetMapping
    public ApiResult<IPage<HealthAlert>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long elderId,
            @RequestParam(required = false) Integer status) {
        return ApiResult.ok(healthAlertService.page(page, size, elderId, status));
    }

    @GetMapping("/{id}")
    public ApiResult<HealthAlert> get(@PathVariable Long id) {
        return ApiResult.ok(healthAlertService.getById(id));
    }

    @PostMapping
    public ApiResult<HealthAlert> create(@RequestBody HealthAlert alert) {
        return ApiResult.ok(healthAlertService.create(alert));
    }

    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody HealthAlert alert) {
        healthAlertService.update(id, alert);
        return ApiResult.ok();
    }

    @PutMapping("/{id}/handle")
    public ApiResult<Void> handle(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String handleRemark = body != null ? body.get("handleRemark") : null;
        healthAlertService.handle(id, handleRemark);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        healthAlertService.delete(id);
        return ApiResult.ok();
    }
}
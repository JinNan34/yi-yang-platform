package com.medical.doctorplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medical.doctorplatform.common.ApiResult;
import com.medical.doctorplatform.entity.HealthRecord;
import com.medical.doctorplatform.service.HealthRecordService;
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
@RequestMapping("/api/health-records")
@RequiredArgsConstructor
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    @GetMapping
    public ApiResult<IPage<HealthRecord>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long elderId,
            @RequestParam(required = false) String elderName) {
        return ApiResult.ok(healthRecordService.page(page, size, elderId, elderName));
    }

    @GetMapping("/{id}")
    public ApiResult<HealthRecord> get(@PathVariable Long id) {
        return ApiResult.ok(healthRecordService.getById(id));
    }

    @GetMapping("/elder/{elderId}")
    public ApiResult<IPage<HealthRecord>> getByElder(
            @PathVariable Long elderId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return ApiResult.ok(healthRecordService.findByElderId(elderId, page, size));
    }

    @PostMapping
    public ApiResult<HealthRecord> create(@RequestBody HealthRecord record) {
        return ApiResult.ok(healthRecordService.create(record));
    }

    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody HealthRecord record) {
        healthRecordService.update(id, record);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        healthRecordService.delete(id);
        return ApiResult.ok();
    }
}
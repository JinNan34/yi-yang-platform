package com.medical.doctorplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medical.doctorplatform.common.ApiResult;
import com.medical.doctorplatform.entity.KeyPopulationFollowup;
import com.medical.doctorplatform.service.KeyPopulationFollowupService;
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
@RequestMapping("/api/key-followups")
@RequiredArgsConstructor
public class KeyPopulationFollowupController {

    private final KeyPopulationFollowupService keyPopulationFollowupService;

    @GetMapping
    public ApiResult<IPage<KeyPopulationFollowup>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long elderId,
            @RequestParam(required = false) String elderName) {
        return ApiResult.ok(keyPopulationFollowupService.page(page, size, elderId, elderName));
    }

    @GetMapping("/{id}")
    public ApiResult<KeyPopulationFollowup> get(@PathVariable Long id) {
        return ApiResult.ok(keyPopulationFollowupService.getById(id));
    }

    @PostMapping
    public ApiResult<KeyPopulationFollowup> create(@RequestBody KeyPopulationFollowup entity) {
        return ApiResult.ok(keyPopulationFollowupService.create(entity));
    }

    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody KeyPopulationFollowup entity) {
        keyPopulationFollowupService.update(id, entity);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        keyPopulationFollowupService.delete(id);
        return ApiResult.ok();
    }
}
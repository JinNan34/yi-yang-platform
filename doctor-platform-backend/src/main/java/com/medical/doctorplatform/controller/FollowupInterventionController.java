package com.medical.doctorplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medical.doctorplatform.common.ApiResult;
import com.medical.doctorplatform.entity.FollowupInterventionRecord;
import com.medical.doctorplatform.service.FollowupInterventionService;
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
@RequestMapping("/api/followup-interventions")
@RequiredArgsConstructor
public class FollowupInterventionController {

    private final FollowupInterventionService followupInterventionService;

    @GetMapping
    public ApiResult<IPage<FollowupInterventionRecord>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long elderId,
            @RequestParam(required = false) String elderName,
            @RequestParam(required = false) Long followupId) {
        return ApiResult.ok(followupInterventionService.page(page, size, elderId, elderName, followupId));
    }

    @GetMapping("/{id}")
    public ApiResult<FollowupInterventionRecord> get(@PathVariable Long id) {
        return ApiResult.ok(followupInterventionService.getById(id));
    }

    @PostMapping
    public ApiResult<FollowupInterventionRecord> create(@RequestBody FollowupInterventionRecord entity) {
        return ApiResult.ok(followupInterventionService.create(entity));
    }

    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody FollowupInterventionRecord entity) {
        followupInterventionService.update(id, entity);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        followupInterventionService.delete(id);
        return ApiResult.ok();
    }
}
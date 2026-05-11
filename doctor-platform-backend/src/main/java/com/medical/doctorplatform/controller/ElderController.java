package com.medical.doctorplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medical.doctorplatform.common.ApiResult;
import com.medical.doctorplatform.entity.Elder;
import com.medical.doctorplatform.service.ElderService;
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
@RequestMapping("/api/elders")
@RequiredArgsConstructor
public class ElderController {

    private final ElderService elderService;

    @GetMapping
    public ApiResult<IPage<Elder>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name) {
        return ApiResult.ok(elderService.page(page, size, name));
    }

    @GetMapping("/{id}")
    public ApiResult<Elder> get(@PathVariable Long id) {
        return ApiResult.ok(elderService.getById(id));
    }

    @PostMapping
    public ApiResult<Elder> create(@RequestBody Elder elder) {
        return ApiResult.ok(elderService.create(elder));
    }

    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Elder elder) {
        elderService.update(id, elder);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        elderService.delete(id);
        return ApiResult.ok();
    }
}
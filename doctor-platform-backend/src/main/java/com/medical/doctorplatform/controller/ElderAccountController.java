package com.medical.doctorplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medical.doctorplatform.common.ApiResult;
import com.medical.doctorplatform.entity.ElderAccount;
import com.medical.doctorplatform.service.ElderAccountService;
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
@RequestMapping("/api/elder-accounts")
@RequiredArgsConstructor
public class ElderAccountController {

    private final ElderAccountService elderAccountService;

    @GetMapping
    public ApiResult<IPage<ElderAccount>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long elderId,
            @RequestParam(required = false) String elderName) {
        return ApiResult.ok(elderAccountService.page(page, size, elderId, elderName));
    }

    @GetMapping("/{id}")
    public ApiResult<ElderAccount> get(@PathVariable Long id) {
        return ApiResult.ok(elderAccountService.getById(id));
    }

    @PostMapping
    public ApiResult<ElderAccount> create(@RequestBody ElderAccount account) {
        return ApiResult.ok(elderAccountService.create(account));
    }

    @PutMapping("/{id}")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody ElderAccount account) {
        elderAccountService.update(id, account);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        elderAccountService.delete(id);
        return ApiResult.ok();
    }
}
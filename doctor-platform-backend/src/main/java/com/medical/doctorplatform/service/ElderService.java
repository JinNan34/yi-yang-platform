package com.medical.doctorplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.doctorplatform.entity.Elder;
import com.medical.doctorplatform.entity.ElderAccount;
import com.medical.doctorplatform.mapper.ElderAccountMapper;
import com.medical.doctorplatform.mapper.ElderMapper;
import com.medical.doctorplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElderService {

    private final ElderMapper elderMapper;
    private final ElderAccountMapper elderAccountMapper;
    private final RecordPermissionService permissionService;

    public IPage<Elder> page(long page, long size, String name) {
        Page<Elder> p = new Page<>(page, size);
        LambdaQueryWrapper<Elder> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            w.like(Elder::getName, name);
        }
        w.orderByAsc(Elder::getId);
        return elderMapper.selectPage(p, w);
    }

    public Elder getById(Long id) {
        Elder elder = elderMapper.selectById(id);
        if (elder == null) {
            throw new IllegalArgumentException("老人不存在");
        }
        return elder;
    }

    @Transactional
    public Elder create(Elder elder) {
        elder.setCreatorDoctorId(SecurityUtils.currentDoctorId());
        elderMapper.insert(elder);
        log.info("创建老人档案: id={}, name={}, creator={}", 
                elder.getId(), elder.getName(), elder.getCreatorDoctorId());
        
        ElderAccount acc = new ElderAccount();
        acc.setElderId(elder.getId());
        acc.setAccountNo("EA" + elder.getId());
        acc.setBalance(BigDecimal.ZERO);
        acc.setStatus(1);
        elderAccountMapper.insert(acc);
        log.info("创建老人账户: elderId={}, accountNo={}", elder.getId(), acc.getAccountNo());
        
        return elder;
    }

    public void update(Long id, Elder elder) {
        Elder existing = getById(id);
        permissionService.assertCanModifyElder(existing);
        
        elder.setId(id);
        elder.setCreatorDoctorId(existing.getCreatorDoctorId());
        elderMapper.updateById(elder);
        log.info("更新老人档案: id={}, name={}", id, elder.getName());
    }

    public void delete(Long id) {
        Elder existing = getById(id);
        permissionService.assertCanModifyElder(existing);
        
        elderMapper.deleteById(id);
        log.info("删除老人档案: id={}", id);
    }
}
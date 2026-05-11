package com.medical.doctorplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.doctorplatform.entity.KeyPopulationFollowup;
import com.medical.doctorplatform.mapper.KeyPopulationFollowupMapper;
import com.medical.doctorplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeyPopulationFollowupService {

    private final KeyPopulationFollowupMapper keyPopulationFollowupMapper;
    private final RecordPermissionService permissionService;

    public IPage<KeyPopulationFollowup> page(long page, long size, Long elderId) {
        Page<KeyPopulationFollowup> p = new Page<>(page, size);
        LambdaQueryWrapper<KeyPopulationFollowup> w = new LambdaQueryWrapper<>();
        if (elderId != null) {
            w.eq(KeyPopulationFollowup::getElderId, elderId);
        }
        w.orderByDesc(KeyPopulationFollowup::getCreateTime);
        return keyPopulationFollowupMapper.selectPage(p, w);
    }

    public KeyPopulationFollowup getById(Long id) {
        KeyPopulationFollowup entity = keyPopulationFollowupMapper.selectById(id);
        if (entity == null) {
            throw new IllegalArgumentException("记录不存在");
        }
        return entity;
    }

    public KeyPopulationFollowup create(KeyPopulationFollowup entity) {
        entity.setDoctorId(SecurityUtils.currentDoctorId());
        if (entity.getStatus() == null) {
            entity.setStatus(0);
        }
        keyPopulationFollowupMapper.insert(entity);
        log.info("创建重点人群随访: id={}, elderId={}, riskType={}", 
                entity.getId(), entity.getElderId(), entity.getRiskType());
        return entity;
    }

    public void update(Long id, KeyPopulationFollowup entity) {
        KeyPopulationFollowup existing = getById(id);
        permissionService.assertCanModifyClinicalRecord(existing.getDoctorId());
        
        entity.setId(id);
        entity.setDoctorId(existing.getDoctorId());
        keyPopulationFollowupMapper.updateById(entity);
        log.info("更新重点人群随访: id={}, riskType={}", id, entity.getRiskType());
    }

    public void delete(Long id) {
        KeyPopulationFollowup existing = getById(id);
        permissionService.assertCanModifyClinicalRecord(existing.getDoctorId());
        
        keyPopulationFollowupMapper.deleteById(id);
        log.info("删除重点人群随访: id={}", id);
    }
}
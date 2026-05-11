package com.medical.doctorplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.doctorplatform.entity.HealthAssessment;
import com.medical.doctorplatform.mapper.HealthAssessmentMapper;
import com.medical.doctorplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthAssessmentService {

    private final HealthAssessmentMapper healthAssessmentMapper;
    private final ElderLookupService elderLookup;
    private final RecordPermissionService permissionService;

    public IPage<HealthAssessment> page(long page, long size, Long elderId, String elderName) {
        Page<HealthAssessment> p = new Page<>(page, size);
        LambdaQueryWrapper<HealthAssessment> w = new LambdaQueryWrapper<>();
        Optional<List<Long>> nameFilter = elderLookup.resolveElderIdsForNameQuery(elderName);
        if (nameFilter.isPresent() && nameFilter.get().isEmpty()) {
            return elderLookup.emptyPage(page, size);
        }
        nameFilter.ifPresent(ids -> w.in(HealthAssessment::getElderId, ids));
        if (elderId != null) {
            w.eq(HealthAssessment::getElderId, elderId);
        }
        w.orderByDesc(HealthAssessment::getAssessmentTime);
        IPage<HealthAssessment> result = healthAssessmentMapper.selectPage(p, w);
        elderLookup.fillElderNames(result.getRecords(), HealthAssessment::getElderId, HealthAssessment::setElderName);
        return result;
    }

    public HealthAssessment getById(Long id) {
        HealthAssessment entity = healthAssessmentMapper.selectById(id);
        if (entity == null) {
            throw new IllegalArgumentException("记录不存在");
        }
        return entity;
    }

    public HealthAssessment create(HealthAssessment entity) {
        entity.setAssessorId(SecurityUtils.currentDoctorId());
        if (entity.getAssessmentTime() == null) {
            entity.setAssessmentTime(LocalDateTime.now());
        }
        healthAssessmentMapper.insert(entity);
        log.info("创建健康评估: id={}, elderId={}, score={}", 
                entity.getId(), entity.getElderId(), entity.getScore());
        return entity;
    }

    public void update(Long id, HealthAssessment entity) {
        HealthAssessment existing = getById(id);
        permissionService.assertCanModifyClinicalRecord(existing.getAssessorId());
        
        entity.setId(id);
        entity.setAssessorId(existing.getAssessorId());
        healthAssessmentMapper.updateById(entity);
        log.info("更新健康评估: id={}, score={}", id, entity.getScore());
    }

    public void delete(Long id) {
        HealthAssessment existing = getById(id);
        permissionService.assertCanModifyClinicalRecord(existing.getAssessorId());
        
        healthAssessmentMapper.deleteById(id);
        log.info("删除健康评估: id={}", id);
    }
}
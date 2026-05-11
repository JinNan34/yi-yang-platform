package com.medical.doctorplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.doctorplatform.entity.HealthAlert;
import com.medical.doctorplatform.mapper.HealthAlertMapper;
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
public class HealthAlertService {

    private final HealthAlertMapper healthAlertMapper;
    private final ElderLookupService elderLookup;
    private final RecordPermissionService permissionService;

    public IPage<HealthAlert> page(long page, long size, Long elderId, String elderName, Integer status) {
        Page<HealthAlert> p = new Page<>(page, size);
        LambdaQueryWrapper<HealthAlert> w = new LambdaQueryWrapper<>();
        Optional<List<Long>> nameFilter = elderLookup.resolveElderIdsForNameQuery(elderName);
        if (nameFilter.isPresent() && nameFilter.get().isEmpty()) {
            return elderLookup.emptyPage(page, size);
        }
        nameFilter.ifPresent(ids -> w.in(HealthAlert::getElderId, ids));
        if (elderId != null) {
            w.eq(HealthAlert::getElderId, elderId);
        }
        if (status != null) {
            w.eq(HealthAlert::getStatus, status);
        }
        w.orderByAsc(HealthAlert::getId);
        IPage<HealthAlert> result = healthAlertMapper.selectPage(p, w);
        elderLookup.fillElderNames(result.getRecords(), HealthAlert::getElderId, HealthAlert::setElderName);
        return result;
    }

    public HealthAlert getById(Long id) {
        HealthAlert alert = healthAlertMapper.selectById(id);
        if (alert == null) {
            throw new IllegalArgumentException("记录不存在");
        }
        return alert;
    }

    public HealthAlert create(HealthAlert alert) {
        alert.setDoctorId(SecurityUtils.currentDoctorId());
        if (alert.getStatus() == null) {
            alert.setStatus(0);
        }
        healthAlertMapper.insert(alert);
        log.info("创建健康预警: id={}, elderId={}, level={}", 
                alert.getId(), alert.getElderId(), alert.getAlertLevel());
        return alert;
    }

    public void update(Long id, HealthAlert alert) {
        HealthAlert existing = getById(id);
        permissionService.assertCanModifyClinicalRecord(existing.getDoctorId());
        
        alert.setId(id);
        alert.setDoctorId(existing.getDoctorId());
        healthAlertMapper.updateById(alert);
        log.info("更新健康预警: id={}, status={}", id, alert.getStatus());
    }

    public void handle(Long id, String handleRemark) {
        HealthAlert alert = getById(id);
        permissionService.assertCanModifyClinicalRecord(alert.getDoctorId());
        
        alert.setStatus(1);
        alert.setHandleTime(LocalDateTime.now());
        alert.setHandleRemark(handleRemark);
        healthAlertMapper.updateById(alert);
        log.info("处理健康预警: id={}, handleRemark={}", id, handleRemark);
    }

    public void delete(Long id) {
        HealthAlert existing = getById(id);
        permissionService.assertCanModifyClinicalRecord(existing.getDoctorId());
        
        healthAlertMapper.deleteById(id);
        log.info("删除健康预警: id={}", id);
    }
}
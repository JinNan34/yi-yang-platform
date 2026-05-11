package com.medical.doctorplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.doctorplatform.entity.HealthRecord;
import com.medical.doctorplatform.mapper.HealthRecordMapper;
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
public class HealthRecordService {

    private final HealthRecordMapper healthRecordMapper;
    private final ElderLookupService elderLookup;
    private final RecordPermissionService permissionService;

    public IPage<HealthRecord> page(long page, long size, Long elderId, String elderName) {
        Page<HealthRecord> p = new Page<>(page, size);
        LambdaQueryWrapper<HealthRecord> w = new LambdaQueryWrapper<>();
        Optional<List<Long>> nameFilter = elderLookup.resolveElderIdsForNameQuery(elderName);
        if (nameFilter.isPresent() && nameFilter.get().isEmpty()) {
            return elderLookup.emptyPage(page, size);
        }
        nameFilter.ifPresent(ids -> w.in(HealthRecord::getElderId, ids));
        if (elderId != null) {
            w.eq(HealthRecord::getElderId, elderId);
        }
        w.orderByAsc(HealthRecord::getId);
        IPage<HealthRecord> result = healthRecordMapper.selectPage(p, w);
        elderLookup.fillElderNames(result.getRecords(), HealthRecord::getElderId, HealthRecord::setElderName);
        return result;
    }

    public HealthRecord getById(Long id) {
        HealthRecord record = healthRecordMapper.selectById(id);
        if (record == null) {
            throw new IllegalArgumentException("记录不存在");
        }
        return record;
    }

    public HealthRecord create(HealthRecord record) {
        record.setDoctorId(SecurityUtils.currentDoctorId());
        if (record.getRecordTime() == null) {
            record.setRecordTime(LocalDateTime.now());
        }
        healthRecordMapper.insert(record);
        log.info("创建健康记录: id={}, elderId={}, doctorId={}",
                record.getId(), record.getElderId(), record.getDoctorId());
        return record;
    }

    public void update(Long id, HealthRecord record) {
        HealthRecord existing = getById(id);
        permissionService.assertCanModifyClinicalRecord(existing.getDoctorId());

        record.setId(id);
        record.setDoctorId(existing.getDoctorId());
        healthRecordMapper.updateById(record);
        log.info("更新健康记录: id={}, elderId={}", id, record.getElderId());
    }

    public void delete(Long id) {
        HealthRecord existing = getById(id);
        permissionService.assertCanModifyClinicalRecord(existing.getDoctorId());

        healthRecordMapper.deleteById(id);
        log.info("删除健康记录: id={}", id);
    }

    public IPage<HealthRecord> findByElderId(Long elderId, long page, long size) {
        Page<HealthRecord> p = new Page<>(page, size);
        LambdaQueryWrapper<HealthRecord> w = new LambdaQueryWrapper<>();
        w.eq(HealthRecord::getElderId, elderId);
        w.orderByAsc(HealthRecord::getId);
        IPage<HealthRecord> result = healthRecordMapper.selectPage(p, w);
        elderLookup.fillElderNames(result.getRecords(), HealthRecord::getElderId, HealthRecord::setElderName);
        return result;
    }
}

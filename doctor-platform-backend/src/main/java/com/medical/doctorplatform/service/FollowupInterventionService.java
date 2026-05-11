package com.medical.doctorplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.doctorplatform.entity.FollowupInterventionRecord;
import com.medical.doctorplatform.mapper.FollowupInterventionRecordMapper;
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
public class FollowupInterventionService {

    private final FollowupInterventionRecordMapper followupInterventionRecordMapper;
    private final ElderLookupService elderLookup;
    private final RecordPermissionService permissionService;

    public IPage<FollowupInterventionRecord> page(long page, long size, Long elderId, String elderName, Long followupId) {
        Page<FollowupInterventionRecord> p = new Page<>(page, size);
        LambdaQueryWrapper<FollowupInterventionRecord> w = new LambdaQueryWrapper<>();
        Optional<List<Long>> nameFilter = elderLookup.resolveElderIdsForNameQuery(elderName);
        if (nameFilter.isPresent() && nameFilter.get().isEmpty()) {
            return elderLookup.emptyPage(page, size);
        }
        nameFilter.ifPresent(ids -> w.in(FollowupInterventionRecord::getElderId, ids));
        if (elderId != null) {
            w.eq(FollowupInterventionRecord::getElderId, elderId);
        }
        if (followupId != null) {
            w.eq(FollowupInterventionRecord::getFollowupId, followupId);
        }
        w.orderByAsc(FollowupInterventionRecord::getId);
        IPage<FollowupInterventionRecord> result = followupInterventionRecordMapper.selectPage(p, w);
        elderLookup.fillElderNames(result.getRecords(), FollowupInterventionRecord::getElderId, FollowupInterventionRecord::setElderName);
        return result;
    }

    public FollowupInterventionRecord getById(Long id) {
        FollowupInterventionRecord entity = followupInterventionRecordMapper.selectById(id);
        if (entity == null) {
            throw new IllegalArgumentException("记录不存在");
        }
        return entity;
    }

    public FollowupInterventionRecord create(FollowupInterventionRecord entity) {
        entity.setDoctorId(SecurityUtils.currentDoctorId());
        if (entity.getInterventionTime() == null) {
            entity.setInterventionTime(LocalDateTime.now());
        }
        followupInterventionRecordMapper.insert(entity);
        log.info("创建随访干预记录: id={}, elderId={}, type={}", 
                entity.getId(), entity.getElderId(), entity.getInterventionType());
        return entity;
    }

    public void update(Long id, FollowupInterventionRecord entity) {
        FollowupInterventionRecord existing = getById(id);
        permissionService.assertCanModifyClinicalRecord(existing.getDoctorId());
        
        entity.setId(id);
        entity.setDoctorId(existing.getDoctorId());
        followupInterventionRecordMapper.updateById(entity);
        log.info("更新随访干预记录: id={}, type={}", id, entity.getInterventionType());
    }

    public void delete(Long id) {
        FollowupInterventionRecord existing = getById(id);
        permissionService.assertCanModifyClinicalRecord(existing.getDoctorId());
        
        followupInterventionRecordMapper.deleteById(id);
        log.info("删除随访干预记录: id={}", id);
    }
}
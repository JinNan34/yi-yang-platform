package com.medical.doctorplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Optional;
import com.medical.doctorplatform.entity.Elder;
import com.medical.doctorplatform.entity.ElderAccount;
import com.medical.doctorplatform.mapper.ElderAccountMapper;
import com.medical.doctorplatform.mapper.ElderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElderAccountService {

    private final ElderAccountMapper elderAccountMapper;
    private final ElderMapper elderMapper;
    private final ElderLookupService elderLookup;
    private final RecordPermissionService permissionService;

    public IPage<ElderAccount> page(long page, long size, Long elderId, String elderName) {
        Page<ElderAccount> p = new Page<>(page, size);
        LambdaQueryWrapper<ElderAccount> w = new LambdaQueryWrapper<>();
        java.util.Optional<java.util.List<Long>> nameFilter = elderLookup.resolveElderIdsForNameQuery(elderName);
        if (nameFilter.isPresent() && nameFilter.get().isEmpty()) {
            return elderLookup.emptyPage(page, size);
        }
        nameFilter.ifPresent(ids -> w.in(ElderAccount::getElderId, ids));
        if (elderId != null) {
            w.eq(ElderAccount::getElderId, elderId);
        }
        w.orderByDesc(ElderAccount::getCreateTime);
        com.baomidou.mybatisplus.core.metadata.IPage<ElderAccount> result = elderAccountMapper.selectPage(p, w);
        elderLookup.fillElderNames(result.getRecords(), ElderAccount::getElderId, ElderAccount::setElderName);
        return result;
    }

    public ElderAccount getById(Long id) {
        ElderAccount account = elderAccountMapper.selectById(id);
        if (account == null) {
            throw new IllegalArgumentException("账户不存在");
        }
        return account;
    }

    public ElderAccount getByElderId(Long elderId) {
        LambdaQueryWrapper<ElderAccount> w = new LambdaQueryWrapper<>();
        w.eq(ElderAccount::getElderId, elderId);
        ElderAccount account = elderAccountMapper.selectOne(w);
        if (account == null) {
            throw new IllegalArgumentException("账户不存在");
        }
        return account;
    }

    public ElderAccount create(ElderAccount account) {
        Elder elder = elderMapper.selectById(account.getElderId());
        if (elder == null) {
            throw new IllegalArgumentException("老人不存在");
        }
        permissionService.assertCanModifyElder(elder);
        elderAccountMapper.insert(account);
        log.info("创建老人账户: id={}, elderId={}", account.getId(), account.getElderId());
        return account;
    }

    public void update(Long id, ElderAccount account) {
        ElderAccount existing = getById(id);
        Elder elder = elderMapper.selectById(existing.getElderId());
        if (elder == null) {
            throw new IllegalArgumentException("关联老人不存在");
        }
        permissionService.assertCanModifyElder(elder);
        
        account.setId(id);
        elderAccountMapper.updateById(account);
        log.info("更新老人账户: id={}, balance={}", id, account.getBalance());
    }

    public void delete(Long id) {
        ElderAccount existing = getById(id);
        Elder elder = elderMapper.selectById(existing.getElderId());
        if (elder == null) {
            throw new IllegalArgumentException("关联老人不存在");
        }
        permissionService.assertCanModifyElder(elder);
        
        elderAccountMapper.deleteById(id);
        log.info("删除老人账户: id={}", id);
    }
}
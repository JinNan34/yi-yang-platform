package com.medical.doctorplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.doctorplatform.entity.Elder;
import com.medical.doctorplatform.mapper.ElderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 按老人姓名缩小 elderId 范围，以及列表回填老人姓名。
 */
@Service
@RequiredArgsConstructor
public class ElderLookupService {

    private final ElderMapper elderMapper;

    /**
     * Optional.empty() — 未按姓名筛选；
     * Optional.of(空列表) — 输入了姓名但无匹配老人，调用方应直接返回空分页；
     * Optional.of(非空) — health_* .elder_id IN (...)
     */
    public Optional<List<Long>> resolveElderIdsForNameQuery(String elderName) {
        if (!StringUtils.hasText(elderName)) {
            return Optional.empty();
        }
        List<Long> ids = elderMapper.selectList(new LambdaQueryWrapper<Elder>()
                        .select(Elder::getId)
                        .like(Elder::getName, elderName.trim()))
                .stream()
                .map(Elder::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return Optional.of(ids);
    }

    public <T> void fillElderNames(List<T> rows, Function<T, Long> idGetter, BiConsumer<T, String> nameSetter) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        List<Long> ids = rows.stream()
                .map(idGetter)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, String> map = elderMapper.selectList(new LambdaQueryWrapper<Elder>().in(Elder::getId, ids))
                .stream()
                .collect(Collectors.toMap(Elder::getId, e -> e.getName() != null ? e.getName() : "", (a, b) -> a));
        for (T row : rows) {
            Long id = idGetter.apply(row);
            if (id != null) {
                nameSetter.accept(row, map.getOrDefault(id, ""));
            }
        }
    }

    public <T> Page<T> emptyPage(long page, long size) {
        Page<T> empty = new Page<>(page, size, 0);
        empty.setRecords(Collections.emptyList());
        return empty;
    }
}

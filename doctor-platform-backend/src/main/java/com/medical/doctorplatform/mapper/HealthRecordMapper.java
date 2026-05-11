package com.medical.doctorplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.doctorplatform.entity.HealthRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HealthRecordMapper extends BaseMapper<HealthRecord> {
}

package com.medical.doctorplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.doctorplatform.entity.HealthAssessment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HealthAssessmentMapper extends BaseMapper<HealthAssessment> {
}

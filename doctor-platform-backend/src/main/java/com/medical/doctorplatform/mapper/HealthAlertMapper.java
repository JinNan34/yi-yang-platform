package com.medical.doctorplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.doctorplatform.entity.HealthAlert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HealthAlertMapper extends BaseMapper<HealthAlert> {
}

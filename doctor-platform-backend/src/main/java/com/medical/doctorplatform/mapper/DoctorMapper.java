package com.medical.doctorplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.doctorplatform.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {
}

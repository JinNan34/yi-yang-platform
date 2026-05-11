package com.medical.doctorplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("health_record")
public class HealthRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long elderId;
    private Long doctorId;
    private Integer systolicBp;
    private Integer diastolicBp;
    private BigDecimal bloodSugar;
    private Integer heartRate;
    private BigDecimal temperature;
    private BigDecimal weight;
    private LocalDateTime recordTime;
    private String remark;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
}

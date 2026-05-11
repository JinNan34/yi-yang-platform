package com.medical.doctorplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("health_assessment")
public class HealthAssessment {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long elderId;
    @TableField(exist = false)
    private String elderName;
    private Long assessorId;
    private Integer score;
    private String conclusion;
    private LocalDateTime assessmentTime;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
}

package com.medical.doctorplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("key_population_followup")
public class KeyPopulationFollowup {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long elderId;
    private Long doctorId;
    private String riskType;
    private Integer followupCycleDays;
    private LocalDate nextFollowupDate;
    private Integer status;
    private String remark;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
}

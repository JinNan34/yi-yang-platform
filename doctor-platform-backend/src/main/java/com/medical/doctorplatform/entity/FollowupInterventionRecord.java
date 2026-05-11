package com.medical.doctorplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("followup_intervention_record")
public class FollowupInterventionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long followupId;
    private Long elderId;
    private Long doctorId;
    private String interventionType;
    private String content;
    private LocalDateTime interventionTime;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
}

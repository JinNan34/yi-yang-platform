package com.medical.doctorplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("health_alert")
public class HealthAlert {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 登记/负责医生，用于数据权限 */
    private Long doctorId;
    private Long elderId;
    @TableField(exist = false)
    private String elderName;
    private String alertType;
    private String alertLevel;
    private String message;
    private Integer status;
    private LocalDateTime handleTime;
    private String handleRemark;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
}

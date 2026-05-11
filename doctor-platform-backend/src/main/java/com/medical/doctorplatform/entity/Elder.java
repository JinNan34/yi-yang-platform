package com.medical.doctorplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("elder")
public class Elder {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 建档医生，用于数据权限 */
    private Long creatorDoctorId;
    private String name;
    private String idCard;
    private Integer gender;
    private LocalDate birthDate;
    private String phone;
    private String address;
    private String emergencyContact;
    private String emergencyPhone;
    private String remark;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

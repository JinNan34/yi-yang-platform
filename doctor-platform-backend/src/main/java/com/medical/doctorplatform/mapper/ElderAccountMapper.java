package com.medical.doctorplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.doctorplatform.entity.ElderAccount;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ElderAccountMapper extends BaseMapper<ElderAccount> {

    /** 物理删除，释放 elder_id 唯一约束，供管理员删除后重新建档内账户 */
    @Delete("DELETE FROM elder_account WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);
}

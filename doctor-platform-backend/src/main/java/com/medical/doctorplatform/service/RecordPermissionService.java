package com.medical.doctorplatform.service;

import com.medical.doctorplatform.common.ForbiddenOperationException;
import com.medical.doctorplatform.domain.DoctorRole;
import com.medical.doctorplatform.entity.Doctor;
import com.medical.doctorplatform.entity.Elder;
import com.medical.doctorplatform.mapper.DoctorMapper;
import com.medical.doctorplatform.security.LoginUser;
import com.medical.doctorplatform.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RecordPermissionService {

    private final DoctorMapper doctorMapper;

    /**
     * 临床类记录：负责人为某医生 ID（如建档医生、评估人）。负责人为空时仅管理员与科主任可改（院内无主数据）。
     */
    public void assertCanModifyClinicalRecord(Long ownerDoctorId) {
        LoginUser me = SecurityUtils.currentUser();
        if (canModifyByOwnerOrDeptOrAdmin(me, ownerDoctorId)) {
            return;
        }
        throw new ForbiddenOperationException();
    }

    public void assertCanModifyElder(Elder elder) {
        if (elder == null) {
            throw new IllegalArgumentException("老人不存在");
        }
        LoginUser me = SecurityUtils.currentUser();
        if (DoctorRole.isAdmin(me.getDoctor().getRole())) {
            return;
        }
        Long creatorId = elder.getCreatorDoctorId();
        if (creatorId == null) {
            if (DoctorRole.isDeptHead(me.getDoctor().getRole())) {
                return;
            }
            throw new ForbiddenOperationException("仅管理员或科室负责人可维护无主建档记录");
        }
        if (me.getDoctorId().equals(creatorId)) {
            return;
        }
        if (DoctorRole.isDeptHead(me.getDoctor().getRole())) {
            Doctor creator = doctorMapper.selectById(creatorId);
            if (creator != null && sameDepartment(me.getDoctor(), creator)) {
                return;
            }
        }
        throw new ForbiddenOperationException();
    }

    private boolean canModifyByOwnerOrDeptOrAdmin(LoginUser me, Long ownerDoctorId) {
        if (DoctorRole.isAdmin(me.getDoctor().getRole())) {
            return true;
        }
        if (ownerDoctorId == null) {
            return DoctorRole.isDeptHead(me.getDoctor().getRole());
        }
        if (me.getDoctorId().equals(ownerDoctorId)) {
            return true;
        }
        if (DoctorRole.isDeptHead(me.getDoctor().getRole())) {
            Doctor owner = doctorMapper.selectById(ownerDoctorId);
            return owner != null && sameDepartment(me.getDoctor(), owner);
        }
        return false;
    }

    private static boolean sameDepartment(Doctor a, Doctor b) {
        String da = a.getDepartment();
        String db = b.getDepartment();
        if (!StringUtils.hasText(da) || !StringUtils.hasText(db)) {
            return false;
        }
        return da.trim().equalsIgnoreCase(db.trim());
    }
}

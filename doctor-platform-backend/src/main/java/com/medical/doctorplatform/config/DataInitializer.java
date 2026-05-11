package com.medical.doctorplatform.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.medical.doctorplatform.domain.DoctorRole;
import com.medical.doctorplatform.entity.Doctor;
import com.medical.doctorplatform.entity.Elder;
import com.medical.doctorplatform.entity.ElderAccount;
import com.medical.doctorplatform.mapper.DoctorMapper;
import com.medical.doctorplatform.mapper.ElderAccountMapper;
import com.medical.doctorplatform.mapper.ElderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Order(100)
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final DoctorMapper doctorMapper;
    private final ElderMapper elderMapper;
    private final ElderAccountMapper elderAccountMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        ensureDoctor("doctor", "123456", "演示医生", DoctorRole.DOCTOR, "主治医师", "医养结合科");
        ensureDoctor("depthead", "123456", "李主任", DoctorRole.DEPT_HEAD, "主任医师", "医养结合科");
        ensureDoctor("admin", "123456", "系统管理员", DoctorRole.ADMIN, "信息科", "信息中心");

        Doctor demo = doctorMapper.selectOne(new LambdaQueryWrapper<Doctor>().eq(Doctor::getUsername, "doctor"));
        if (demo != null && elderMapper.selectCount(new QueryWrapper<>()) == 0) {
            Elder e = new Elder();
            e.setCreatorDoctorId(demo.getId());
            e.setName("张建国");
            e.setIdCard("110101195001011234");
            e.setGender(1);
            e.setBirthDate(LocalDate.of(1950, 1, 1));
            e.setPhone("13900001111");
            e.setAddress("北京市朝阳区示例街道");
            e.setEmergencyContact("张小明");
            e.setEmergencyPhone("13900002222");
            elderMapper.insert(e);
            ElderAccount acc = new ElderAccount();
            acc.setElderId(e.getId());
            acc.setAccountNo("EA" + e.getId());
            acc.setBalance(BigDecimal.ZERO);
            acc.setStatus(1);
            elderAccountMapper.insert(acc);
        }
    }

    private void ensureDoctor(String username, String rawPassword, String realName, String role,
                              String title, String department) {
        Doctor existing = doctorMapper.selectOne(new LambdaQueryWrapper<Doctor>().eq(Doctor::getUsername, username));
        if (existing == null) {
            Doctor d = new Doctor();
            d.setUsername(username);
            d.setPassword(passwordEncoder.encode(rawPassword));
            d.setRealName(realName);
            d.setTitle(title);
            d.setDepartment(department);
            d.setPhone("13800138000");
            d.setStatus(1);
            d.setRole(role);
            doctorMapper.insert(d);
        } else if (existing.getRole() == null || existing.getRole().isBlank()) {
            doctorMapper.update(null, new LambdaUpdateWrapper<Doctor>()
                    .eq(Doctor::getId, existing.getId())
                    .set(Doctor::getRole, role));
        }
    }
}

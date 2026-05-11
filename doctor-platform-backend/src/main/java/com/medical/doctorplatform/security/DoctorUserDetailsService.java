package com.medical.doctorplatform.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.medical.doctorplatform.entity.Doctor;
import com.medical.doctorplatform.mapper.DoctorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoctorUserDetailsService implements UserDetailsService {

    private final DoctorMapper doctorMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUsername, username));
        if (doctor == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return new LoginUser(doctor);
    }
}

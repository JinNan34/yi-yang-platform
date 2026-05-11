package com.medical.doctorplatform.security;

import com.medical.doctorplatform.domain.DoctorRole;
import com.medical.doctorplatform.entity.Doctor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class LoginUser implements UserDetails {

    private final Doctor doctor;

    public LoginUser(Doctor doctor) {
        this.doctor = doctor;
    }

    public Long getDoctorId() {
        return doctor.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String r = DoctorRole.normalize(doctor.getRole());
        if (DoctorRole.ADMIN.equals(r)) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        if (DoctorRole.DEPT_HEAD.equals(r)) {
            return List.of(new SimpleGrantedAuthority("ROLE_DEPT_HEAD"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"));
    }

    @Override
    public String getPassword() {
        return doctor.getPassword();
    }

    @Override
    public String getUsername() {
        return doctor.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return doctor.getStatus() != null && doctor.getStatus() == 1;
    }
}

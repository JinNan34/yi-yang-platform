package com.medical.doctorplatform.dto;

import com.medical.doctorplatform.entity.Doctor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private DoctorProfile doctor;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorProfile {
        private Long id;
        private String username;
        private String realName;
        private String title;
        private String department;
        private String phone;
        private String avatar;
        /** DOCTOR | DEPT_HEAD | ADMIN */
        private String role;

        public static DoctorProfile from(Doctor d) {
            return new DoctorProfile(
                    d.getId(),
                    d.getUsername(),
                    d.getRealName(),
                    d.getTitle(),
                    d.getDepartment(),
                    d.getPhone(),
                    d.getAvatar(),
                    d.getRole());
        }
    }

    public static LoginResponse of(String token, Doctor doctor) {
        return new LoginResponse(token, DoctorProfile.from(doctor));
    }
}

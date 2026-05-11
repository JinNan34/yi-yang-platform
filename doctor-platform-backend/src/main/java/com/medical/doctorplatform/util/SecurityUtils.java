package com.medical.doctorplatform.util;

import com.medical.doctorplatform.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser lu)) {
            throw new IllegalStateException("未登录");
        }
        return lu;
    }

    public static Long currentDoctorId() {
        return currentUser().getDoctorId();
    }
}

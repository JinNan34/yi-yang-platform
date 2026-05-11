package com.medical.doctorplatform.domain;

public final class DoctorRole {

    public static final String DOCTOR = "DOCTOR";
    public static final String DEPT_HEAD = "DEPT_HEAD";
    public static final String ADMIN = "ADMIN";

    private DoctorRole() {
    }

    public static String normalize(String role) {
        if (role == null || role.isBlank()) {
            return DOCTOR;
        }
        return role.trim().toUpperCase();
    }

    public static boolean isAdmin(String role) {
        return ADMIN.equalsIgnoreCase(normalize(role));
    }

    public static boolean isDeptHead(String role) {
        return DEPT_HEAD.equalsIgnoreCase(normalize(role));
    }
}

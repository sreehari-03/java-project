package com.srms.util;

import java.util.regex.Pattern;

/**
 * Common validation utilities for inputs, marks, and registration numbers.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$");
    private static final Pattern REG_PATTERN =
            Pattern.compile("^[A-Za-z0-9_-]{3,20}$");

    private ValidationUtil() {}

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidRegNo(String reg) {
        return reg != null && REG_PATTERN.matcher(reg.trim()).matches();
    }

    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2 && name.trim().length() <= 100;
    }

    public static boolean isValidMark(int mark, int maxMarks) {
        return mark >= 0 && mark <= maxMarks;
    }
}

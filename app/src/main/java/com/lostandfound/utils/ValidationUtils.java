package com.lostandfound.utils;

import android.text.TextUtils;
import android.util.Patterns;

/**
 * Stateless validation helpers used across Login, SignUp, and AddPost screens.
 */
public class ValidationUtils {

    private ValidationUtils() {}

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        if (TextUtils.isEmpty(password) || password.length() < 8) return false;
        boolean hasUpper   = false;
        boolean hasDigit   = false;
        boolean hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isDigit(c))     hasDigit = true;
            if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }
        return hasUpper && hasDigit && hasSpecial;
    }

    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && name.trim().length() >= 2;
    }

    public static boolean isValidPhone(String phone) {
        if (TextUtils.isEmpty(phone)) return false;
        String cleaned = phone.replaceAll("[\\s\\-]", "");
        return cleaned.matches("\\d{10,13}");
    }
}

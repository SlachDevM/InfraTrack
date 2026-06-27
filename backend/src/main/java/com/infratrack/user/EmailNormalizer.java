package com.infratrack.user;

import java.util.Locale;

/**
 * Normalizes email addresses to a canonical lowercase form for storage and lookup.
 */
public final class EmailNormalizer {

    private EmailNormalizer() {
    }

    public static String normalize(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

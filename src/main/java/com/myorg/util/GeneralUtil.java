package com.myorg.util;

import java.security.SecureRandom;
import java.util.UUID;

public class GeneralUtil {

    public static String generateRandomString(int length){

        String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
         String ALPHANUMERIC = ALPHABET + "0123456789";
         SecureRandom SECURE_RANDOM = new SecureRandom();
        if (length < 1) {
            throw new IllegalArgumentException("Length must be at least 1");
        }

        StringBuilder sb = new StringBuilder(length);

        // Ensure the first character is alphabetic
        sb.append(ALPHABET.charAt(SECURE_RANDOM.nextInt(ALPHABET.length())));

        // Fill the remaining characters with alphanumeric characters
        for (int i = 1; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(SECURE_RANDOM.nextInt(ALPHANUMERIC.length())));
        }

        return sb.toString();
    }

    public static boolean validString(String s){
        return null != s && !s.isEmpty();
    }
}

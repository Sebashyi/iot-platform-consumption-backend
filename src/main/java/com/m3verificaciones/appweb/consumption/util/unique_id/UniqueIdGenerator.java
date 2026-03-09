package com.m3verificaciones.appweb.consumption.util.unique_id;

import java.util.concurrent.ThreadLocalRandom;

public class UniqueIdGenerator {

    // Private constructor to prevent instantiation
    private UniqueIdGenerator() {
    }
    
    public static String generateUniqueKey() {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < 3; i++) {
            int index = ThreadLocalRandom.current().nextInt(letters.length());
            key.append(letters.charAt(index));
        }

        for (int i = 0; i < 7; i++) {
            int digit = ThreadLocalRandom.current().nextInt(10);
            key.append(digit);
        }

        return key.toString();
    }
}

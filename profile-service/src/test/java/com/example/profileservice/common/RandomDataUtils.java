package com.example.profileservice.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.ThreadLocalRandom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RandomDataUtils {
    public static String randomString(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

    public static String randomEmail() {
        return randomString(8) + "@gmail.com";
    }

    public static Integer randomAge() {
        return ThreadLocalRandom.current().nextInt(18, 100);
    }

    public static Long randomLong() {
        return ThreadLocalRandom.current().nextLong(1, 10000);
    }
}


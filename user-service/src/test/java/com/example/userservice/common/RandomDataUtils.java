package com.example.userservice.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RandomDataUtils {
    public static String randomString(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

    public static String randomEmail() {
        return randomString(8) + "@gmail.com";
    }
}

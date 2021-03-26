package com.hankki.fooddeal.data.security;

import com.migcomponents.migbase64.Base64;

import java.nio.charset.StandardCharsets;

public class AES256Util {
    public static String aesEncode(String str) {
        return Base64.encodeToString(str.getBytes(StandardCharsets.UTF_8), false);
    }

    public static String aesDecode(String str) {
        return new String(Base64.decode(str), StandardCharsets.UTF_8);
    }
}

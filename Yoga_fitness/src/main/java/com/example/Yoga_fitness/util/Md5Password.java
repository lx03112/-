package com.example.Yoga_fitness.util;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Password {

    // 独一无二的盐值
    private static final String SALT = "WW|";


    public static String generateMD5(String input) {
        try {
            // 在原始输入前后加盐
            String saltedInput = SALT + input + SALT;

            // 创建一个 MessageDigest 实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(saltedInput.getBytes());

            // 转换成十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
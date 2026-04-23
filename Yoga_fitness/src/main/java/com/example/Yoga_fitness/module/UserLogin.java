package com.example.Yoga_fitness.module;

import lombok.Data;

@Data
public class UserLogin {
    private Integer userId;

    private String userName;

    private String userEmail;

    private String userPasswordHash;

    private Integer roleId;

    // ================== 个人信息 ==================
    private String userBio;

    private String userPhone;
}

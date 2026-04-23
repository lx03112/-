package com.example.Yoga_fitness.module;

import lombok.Data;

@Data
public class UserRegister {
    private String userName;
    private String userPasswordHash;
    private String userEmail;
}

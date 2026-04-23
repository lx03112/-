package com.example.Yoga_fitness.module;

import lombok.Data;

@Data
public class UserFound {
    private String userName;
    private String userPasswordHash;
    private String userEmail;
    private String userPhone;
}

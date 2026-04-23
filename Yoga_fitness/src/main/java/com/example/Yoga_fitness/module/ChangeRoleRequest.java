package com.example.Yoga_fitness.module;

import lombok.Data;

@Data
public class ChangeRoleRequest {
    private Long userId;      // 用户ID
    private Integer newRoleId; // 新的角色ID
}
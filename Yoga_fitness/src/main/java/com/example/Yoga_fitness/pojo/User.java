package com.example.Yoga_fitness.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "yjx_user")
public class User {
    @TableId(type = IdType.AUTO)  // 主键自增
    private Integer userId;

    private String userName;

    private String userEmail;

    private String userPasswordHash;

    private Integer roleId;

    // ================== 个人信息 ==================
    private String userBio;

    private String userPhone;

    private String userGender;  // "male", "female", "unknown"

    private LocalDateTime userLastActive;

    private LocalDateTime userCreatedAt;

    private String userStatus;  // "active", "inactive", "banned", "pending"

    private String realName;

    private LocalDate birthDate;

    private String location;
}

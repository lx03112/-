package com.example.Yoga_fitness.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "yjx_message")
public class YjxMessage {
    private Long id;

    private String verificationCode;
    private Integer userId;

    private Boolean used = false;

    private LocalDateTime usedTime;

    private LocalDateTime createdTime = LocalDateTime.now();

    private LocalDateTime expiredTime;

    private String purpose; // 验证码用途，如："UPGRADE_GOLD"


    // 判断验证码是否过期
    public boolean isExpired() {
        return expiredTime != null && LocalDateTime.now().isAfter(expiredTime);
    }
}
package com.example.Yoga_fitness.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("yjx_user")
public class Role {
    private Integer roleId;
    private String userName;
    private String userBio;
}

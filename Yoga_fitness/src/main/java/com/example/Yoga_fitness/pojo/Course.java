package com.example.Yoga_fitness.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName(value = "yjx_course")
@AllArgsConstructor
@NoArgsConstructor

public class Course {
    private Integer courseId;
    private String courseModel;
    private String courseDescription;
    private Integer coachId;      // 新增：教练ID，对应数据库中的coach_id
    private Integer userId;       // 新增：创建者ID
    private String expectStatus;
    private String timeSchedule;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String coach;
}

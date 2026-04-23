package com.example.Yoga_fitness.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@TableName(value = "yjx_repair")
@AllArgsConstructor
@NoArgsConstructor
public class Repair {
    @TableId(value = "repair_id", type = IdType.AUTO)
    private Integer repairId;
    private String repairRequestId;

    private String repairNotes;
    private String repairStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH")
    private Date createdAt;

    private Integer userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date repairStartTime;

    // 维修完成时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date repairEndTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date estimatedCompletion;

    private String equipName;

    // 逻辑删除标志
    @TableLogic
    private Integer isDeleted = 0;

    // 用于前端展示的非数据库字段
    @TableField(exist = false)
    private List<RepairImage> images; // 图片列表

    @TableField(exist = false)
    private Integer imageCount; // 图片数量（用于列表展示）
}

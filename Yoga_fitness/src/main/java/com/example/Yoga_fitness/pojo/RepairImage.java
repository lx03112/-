// RepairImage.java
package com.example.Yoga_fitness.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@TableName(value = "yjx_repair_images")
@AllArgsConstructor
@NoArgsConstructor
public class RepairImage {
    @TableId(value = "image_id", type = IdType.AUTO)
    private Integer imageId;

    private Integer repairId;

    private String imageUrl;      // 图片URL
    private String imageName;     // 图片原始文件名
    private String filePath;      // 服务器存储路径
    private Long fileSize;        // 文件大小（字节）
    private String fileType;      // 文件类型（image/jpeg, image/png等）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    @TableLogic
    private Integer isDeleted = 0; // 逻辑删除标志
}
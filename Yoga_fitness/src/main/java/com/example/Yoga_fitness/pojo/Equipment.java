package com.example.Yoga_fitness.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName(value = "yjx_equipment")
@AllArgsConstructor
@NoArgsConstructor
public class Equipment {

    @TableId(value = "equip_id", type = IdType.AUTO)
    private Long equipId;

    private String equipName;

    private BigDecimal equipPrice;

    private String picture;
    private String equipDescription;

    // 新增字段，对应数据库中的equip_category
    private String equipCategory;   // 器材分类

    private String equipModel;        // 设备型号

    private String equipBrand;        // 设备品牌

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date purchaseDate;        // 购买日期

    private String equipStatus;       // 设备状态（正常、维修中、报废）

    @JsonFormat(pattern = "yyyy-MM-dd HH")
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;
}
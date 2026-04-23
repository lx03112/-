package com.example.Yoga_fitness.module;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class EquipmentVO {
    private Long equipId;
    private String equipName;
    private String equipDescription;
    private BigDecimal equipPrice;
    private String picture;

    // 分类信息
    private String equipCategory;
    private String categoryCode;  // 用于前端分类筛选

    // 品牌和型号
    private String equipBrand;
    private String equipModel;

    // 日期信息
    private Date purchaseDate;
    private String formattedPurchaseDate;  // 格式化后的日期

    // 状态信息
    private String equipStatus;
    private String statusText;  // 状态文本（带样式）

    // 用于前端展示的字段
    private String formattedPrice;  // 格式化后的价格（如：¥12,500.00）
    private String equipmentType;   // 器材类型（有氧、力量等）
    private String trainingParts;   // 训练部位（多个部位用逗号分隔）

    // 点击链接
    private String detailUrl;       // 详情页链接
}
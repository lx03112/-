package com.example.Yoga_fitness.service.Impl;

import com.example.Yoga_fitness.mapper.EquipmentMapper;
import com.example.Yoga_fitness.module.EquipmentVO;
import com.example.Yoga_fitness.pojo.Equipment;
import com.example.Yoga_fitness.service.EquipmentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Override
    public List<EquipmentVO> getAllEquipments() {
        QueryWrapper<Equipment> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_at");
        List<Equipment> equipments = equipmentMapper.selectList(queryWrapper);
        return convertToVOList(equipments);
    }

    @Override
    public List<EquipmentVO> getEquipmentsByCategory(String category) {
        QueryWrapper<Equipment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("equip_category", category)
                .orderByDesc("created_at");
        List<Equipment> equipments = equipmentMapper.selectList(queryWrapper);
        return convertToVOList(equipments);
    }

    @Override
    public List<EquipmentVO> getHotEquipments() {
        // 热门器材逻辑：价格较高、较新购买的
        QueryWrapper<Equipment> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("equip_price", "purchase_date")
                .last("LIMIT 8");
        List<Equipment> equipments = equipmentMapper.selectList(queryWrapper);
        return convertToVOList(equipments);
    }

    @Override
    public List<String> getAllCategories() {
        // 查询所有不重复的分类，过滤掉null
        QueryWrapper<Equipment> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT equip_category")
                .isNotNull("equip_category")
                .ne("equip_category", "");

        List<Equipment> equipments = equipmentMapper.selectList(queryWrapper);
        return equipments.stream()
                .map(Equipment::getEquipCategory)
                .filter(category -> category != null && !category.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<EquipmentVO> getNewEquipments() {
        // 新品推荐：最近3个月购买的器材
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -3); // 3个月内
        Date threeMonthsAgo = calendar.getTime();

        QueryWrapper<Equipment> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("purchase_date", threeMonthsAgo) // 大于等于3个月前
                .orderByDesc("purchase_date")
                .last("LIMIT 8");

        List<Equipment> equipments = equipmentMapper.selectList(queryWrapper);
        return convertToVOList(equipments);
    }

    @Override
    public EquipmentVO getEquipmentDetail(Long id) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) {
            return null;
        }
        return convertToVO(equipment);
    }

    @Override
    public List<EquipmentVO> searchEquipments(String keyword) {
        QueryWrapper<Equipment> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("equip_name", keyword)
                .or().like("equip_description", keyword)
                .or().like("equip_brand", keyword)
                .or().like("equip_model", keyword)
                .orderByDesc("created_at");

        List<Equipment> equipments = equipmentMapper.selectList(queryWrapper);
        return convertToVOList(equipments);
    }

    @Override
    public Map<String, String> getCategoryMap() {
        Map<String, String> categoryMap = new HashMap<>();
        categoryMap.put("臀部器械", "hip");
        categoryMap.put("胸部器械", "chest");
        categoryMap.put("背部器械", "back");
        categoryMap.put("肩部器械", "shoulder");
        return categoryMap;
    }

    // 转换单个Equipment到EquipmentVO
    private EquipmentVO convertToVO(Equipment equipment) {
        if (equipment == null) {
            return null;
        }

        EquipmentVO vo = new EquipmentVO();
        BeanUtils.copyProperties(equipment, vo);

        // 设置分类编码
        vo.setCategoryCode(getCategoryCode(equipment.getEquipCategory()));

        // 格式化价格
        if (equipment.getEquipPrice() != null) {
            DecimalFormat df = new DecimalFormat("¥#,###.00");
            vo.setFormattedPrice(df.format(equipment.getEquipPrice()));
        }

        // 格式化购买日期
        if (equipment.getPurchaseDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            vo.setFormattedPurchaseDate(sdf.format(equipment.getPurchaseDate()));
        }

        // 设置状态文本
        vo.setStatusText(getStatusText(equipment.getEquipStatus()));

        // 设置器材类型
        vo.setEquipmentType(getEquipmentType(equipment.getEquipCategory()));

        // 根据描述提取训练部位
        vo.setTrainingParts(extractTrainingParts(equipment.getEquipDescription()));

        return vo;
    }

    // 转换列表
    private List<EquipmentVO> convertToVOList(List<Equipment> equipments) {
        return equipments.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    // 分类编码映射
    private String getCategoryCode(String category) {
        if (category == null) return "other";
        switch(category) {
            case "臀部器械": return "hip";
            case "胸部器械": return "chest";
            case "背部器械": return "back";
            case "肩部器械": return "shoulder";
            default: return "other";
        }
    }

    // 状态文本
    private String getStatusText(String status) {
        if (status == null) return "未知";
        switch(status) {
            case "正常": return "正常使用";
            case "维修中": return "正在维修";
            case "报废": return "已报废";
            default: return status;
        }
    }

    // 器材类型
    private String getEquipmentType(String category) {
        if (category == null) return "其他";
        switch(category) {
            case "臀部器械":
            case "胸部器械":
            case "背部器械":
            case "肩部器械":
                return "力量训练器械";
            default:
                return "有氧训练器械";
        }
    }

    // 从描述中提取训练部位
    private String extractTrainingParts(String description) {
        if (description == null) return "";

        List<String> parts = new ArrayList<>();
        if (description.contains("胸")) parts.add("胸部");
        if (description.contains("背")) parts.add("背部");
        if (description.contains("肩")) parts.add("肩部");
        if (description.contains("腿") || description.contains("臀")) parts.add("腿部");
        if (description.contains("臂")) parts.add("手臂");
        if (description.contains("核心") || description.contains("腹部")) parts.add("核心");

        return parts.isEmpty() ? "全身" : String.join("、", parts);
    }
}
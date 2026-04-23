package com.example.Yoga_fitness.service;

import com.example.Yoga_fitness.pojo.Equipment;
import com.example.Yoga_fitness.module.EquipmentVO;

import java.util.List;
import java.util.Map;

public interface EquipmentService {

    // 获取所有器材
    List<EquipmentVO> getAllEquipments();

    // 根据分类获取器材
    List<EquipmentVO> getEquipmentsByCategory(String category);

    // 获取热门器材（用于明星器材展示）
    List<EquipmentVO> getHotEquipments();

    // 获取所有分类
    List<String> getAllCategories();

    // 获取器材详情
    EquipmentVO getEquipmentDetail(Long id);

    // 搜索器材
    List<EquipmentVO> searchEquipments(String keyword);

    // 获取分类映射（用于前端分类菜单）
    Map<String, String> getCategoryMap();

    // 获取新品推荐（最近30天购买的器材）
    List<EquipmentVO> getNewEquipments();
}
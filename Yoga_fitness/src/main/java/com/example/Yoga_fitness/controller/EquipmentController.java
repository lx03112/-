package com.example.Yoga_fitness.controller;

import com.example.Yoga_fitness.module.EquipmentVO;
import com.example.Yoga_fitness.pojo.Equipment;
import com.example.Yoga_fitness.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    // 获取所有器材
    @GetMapping("/list")
    public List<EquipmentVO> list() {
        return equipmentService.getAllEquipments();
    }

    // 根据分类获取器材
    @GetMapping("/category/{category}")
    @ResponseBody
    public Map<String, Object> getByCategory(@PathVariable String category) {
        Map<String, Object> result = new HashMap<>();

        List<EquipmentVO> equipments;
        if ("热门".equals(category) || "0".equals(category)) {
            equipments = equipmentService.getHotEquipments();
        } else if ("新品推荐".equals(category)) {
            equipments = equipmentService.getNewEquipments();
        } else {
            equipments = equipmentService.getEquipmentsByCategory(category);
        }

        result.put("code", 200);
        result.put("data", equipments);
        result.put("message", "success");

        return result;
    }

    // 获取所有分类
    @GetMapping("/categories")
    public List<String> getCategories() {
        return equipmentService.getAllCategories();
    }

    // 获取热门器材（这里暂时返回所有，或者你可以根据业务逻辑返回指定数量的器材）
    @GetMapping("/hot")
    public List<EquipmentVO> getHotEquipments() {
        // 假设热门器材是前6个
        return equipmentService.getAllEquipments().subList(0, Math.min(6, equipmentService.getAllEquipments().size()));
    }

    // 器材详情接口
    @GetMapping("/detail/{id}")
    @ResponseBody
    public Map<String, Object> getEquipmentDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        try {
            EquipmentVO equipment = equipmentService.getEquipmentDetail(id);
            if (equipment != null) {
                result.put("code", 200);
                result.put("data", equipment);
                result.put("message", "success");
            } else {
                result.put("code", 404);
                result.put("message", "器材不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "服务器错误: " + e.getMessage());
        }

        return result;
    }
}
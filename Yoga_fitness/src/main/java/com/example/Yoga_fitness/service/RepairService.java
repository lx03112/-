package com.example.Yoga_fitness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.Yoga_fitness.module.EquipmentVO;
import com.example.Yoga_fitness.module.QueryModule;
import com.example.Yoga_fitness.pojo.Repair;
import com.example.Yoga_fitness.util.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface RepairService extends IService<Repair> {

    Result<Map<String, Object>> queryRepair(QueryModule queryModule);

    List<EquipmentVO> getAllEquipment();

    Result<String> createRepair(Repair repair);

    boolean deleteRepair(Integer repairId, Integer userId, String password);

    boolean update(Integer repairId, String repairNotes,Integer userId);

    // 新增：创建维修单（支持图片）
    Result<Map<String, Object>> createRepairWithImages(Repair repair, List<MultipartFile> imageFiles);

    // 新增：为维修单添加图片
    Result<String> addImagesToRepair(Integer repairId, MultipartFile[] files);

    // 新增：获取维修单图片
    Result<List<Object>> getRepairImages(Integer repairId);

    // 新增：删除图片
    Result<String> deleteImage(Long imageId, Integer userId);
}

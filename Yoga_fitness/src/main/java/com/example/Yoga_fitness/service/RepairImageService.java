package com.example.Yoga_fitness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.Yoga_fitness.pojo.RepairImage;
import com.example.Yoga_fitness.util.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface RepairImageService extends IService<RepairImage> {

    // 修改返回类型为 Map<String, Object>
    Result<Map<String, Object>> uploadImage(Long repairId, MultipartFile file);

    Result<List<RepairImage>> uploadMultipleImages(Long repairId, MultipartFile[] files);

    Result<List<RepairImage>> getImagesByRepairId(Long repairId);

    Result<String> deleteImage(Long imageId, Integer userId);
}
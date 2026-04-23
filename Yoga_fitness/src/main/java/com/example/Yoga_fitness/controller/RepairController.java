package com.example.Yoga_fitness.controller;

import com.example.Yoga_fitness.module.EquipmentVO;
import com.example.Yoga_fitness.module.QueryModule;
import com.example.Yoga_fitness.pojo.Repair;
import com.example.Yoga_fitness.service.RepairService;
import com.example.Yoga_fitness.service.UserService;
import com.example.Yoga_fitness.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/repair")
public class RepairController {
    @Autowired
    private RepairService repairService;
    @Autowired
    private UserService userService;

    @RequestMapping("/getAllRepairManagement")
    public Result<Map<String, Object>> queryCourse(QueryModule queryModule) {
        return repairService.queryRepair(queryModule);
    }

    @GetMapping("/getAllEquipment")
    public Result<List<EquipmentVO>> getAllEquipment() {
        List<EquipmentVO> list = repairService.getAllEquipment();
        return Result.success(list);
    }

    @PostMapping("/createRepairManagement")
    public Result<String> createRepairManagement(@RequestBody Repair repair) {
        return repairService.createRepair(repair);
    }

    // 新增：创建维修单（带图片）
    @PostMapping("/createRepairWithImages")
    public Result<Map<String, Object>> createRepairWithImages(
            @RequestParam("repairNotes") String repairNotes,
            @RequestParam("equipmentName") String equipmentName,
            @RequestParam("userId") Integer userId,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {

        Repair repair = new Repair();
        repair.setRepairNotes(repairNotes);
        repair.setEquipName(equipmentName);
        repair.setUserId(userId);

        return repairService.createRepairWithImages(repair, images);
    }

    @PostMapping("/deleteRepairManagement")
    public Result<String> deleteRepairManagement(
            @RequestParam("repairId") Integer repairId,
            @RequestParam("userId") Integer userId,
            @RequestParam("password") String password) {
        boolean isDeleted = repairService.deleteRepair(repairId, userId, password);
        if (isDeleted) {
            return Result.success("删除成功");
        } else {
            return Result.fail("订单不存在或无删除权限", 403);
        }
    }

    @PostMapping("/updateRepairManagement")
    public Result<String> updateRepairManagement(
            @RequestParam("repairId") Integer repairId,
            @RequestParam("repairNotes") String repairNotes,
            @RequestParam("userId") Integer userId) {

        boolean isUpdated = repairService.update(repairId, repairNotes, userId);
        if (isUpdated) {
            return Result.success("修改成功");
        } else {
            return Result.fail("订单不存在或无修改权限", 403);
        }
    }

    // 新增：上传图片接口
    @PostMapping("/uploadImage")
    public Result<String> uploadImage(
            @RequestParam("repairId") Integer repairId,
            @RequestParam("file") MultipartFile file) {
        return repairService.addImagesToRepair(repairId, new MultipartFile[]{file});
    }

    // 新增：批量上传图片接口
    @PostMapping("/uploadImages")
    public Result<String> uploadImages(
            @RequestParam("repairId") Integer repairId,
            @RequestParam("files") MultipartFile[] files) {
        return repairService.addImagesToRepair(repairId, files);
    }

    // 新增：获取维修单的所有图片
    @GetMapping("/images/{repairId}")
    public Result<List<Object>> getRepairImages(@PathVariable Integer repairId) {
        return repairService.getRepairImages(repairId);
    }

    // 新增：删除图片
    @DeleteMapping("/image/{imageId}")
    public Result<String> deleteImage(
            @PathVariable Long imageId,
            @RequestParam("userId") Integer userId) {
        return repairService.deleteImage(imageId, userId);
    }
}
package com.example.Yoga_fitness.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Yoga_fitness.mapper.RepairMapper;
import com.example.Yoga_fitness.module.EquipmentVO;
import com.example.Yoga_fitness.module.QueryModule;
import com.example.Yoga_fitness.pojo.Repair;
import com.example.Yoga_fitness.pojo.RepairImage;
import com.example.Yoga_fitness.pojo.User;
import com.example.Yoga_fitness.service.RepairService;
import com.example.Yoga_fitness.service.UserService;
import com.example.Yoga_fitness.util.Md5Password;
import com.example.Yoga_fitness.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class RepairServiceImpl extends ServiceImpl<RepairMapper, Repair> implements RepairService {

    @Autowired
    private RepairMapper repairMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private RepairImageServiceImpl repairImageService;

    @Override
    public Result<Map<String, Object>> queryRepair(QueryModule queryModule) {
        int pageNum = queryModule.getPageNum() == null ? 1 : queryModule.getPageNum();
        int pageSize = queryModule.getPageSize() == null ? 10 : queryModule.getPageSize();
        Page<Repair> page = new Page<>(pageNum, pageSize);
        String searchKeyWord = queryModule.getSearchKeyword() == null ? "" : queryModule.getSearchKeyword();
        String sortField = queryModule.getSortField() == null ? "created_at" : queryModule.getSortField();
        String sortOrder = queryModule.getSortOrder() == null ? "desc" : queryModule.getSortOrder();

        IPage<Repair> resultPage = repairMapper.queryRepairByCondition(page, queryModule.getUserId(),
                searchKeyWord, sortField, sortOrder);

        // 为每个维修单设置图片数量
        for (Repair repair : resultPage.getRecords()) {
            // 调用repairImageService的方法，而不是直接访问baseMapper
            int imageCount = 0;
            Result<List<com.example.Yoga_fitness.pojo.RepairImage>> imagesResult =
                    repairImageService.getImagesByRepairId(repair.getRepairId().longValue());
            if (imagesResult.getCode() == 200 && imagesResult.getData() != null) {
                imageCount = imagesResult.getData().size();
            }
            repair.setImageCount(imageCount);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("repairRequest", resultPage.getRecords());
        map.put("count", resultPage.getTotal());
        return Result.success(map);
    }

    @Override
    public List<EquipmentVO> getAllEquipment() {
        return repairMapper.getAllEquipment();
    }

    @Override
    public Result<String> createRepair(Repair repair) {
        // 移除设备存在判断，允许相同设备多次报修
        // if(this.getOne(new QueryWrapper<Repair>().eq("equip_name",repair.getEquipName())) != null){
        //     return Result.fail("设备已存在",400);
        // }

        Repair repair1 = new Repair();
        repair1.setRepairRequestId(generateRepairRequestId());
        repair1.setEquipName(repair.getEquipName());
        repair1.setRepairNotes(repair.getRepairNotes());
        repair1.setRepairStatus("待处理");
        repair1.setUserId(repair.getUserId());
        repair1.setCreatedAt(new Date());

        boolean save = this.save(repair1);
        return save ? Result.success("申报成功") : Result.fail("申报失败", 400);
    }

    @Override
    public Result<Map<String, Object>> createRepairWithImages(Repair repair, List<MultipartFile> imageFiles) {
        // 创建维修单
        Result<String> createResult = createRepair(repair);
        if (createResult.getCode() != 200) {
            // 修复：使用getMsg()而不是getMessage()
            return Result.fail(createResult.getMsg(), createResult.getCode());
        }

        // 获取最后插入的维修单ID
        LambdaQueryWrapper<Repair> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Repair::getRepairId).last("LIMIT 1");
        Repair createdRepair = this.getOne(queryWrapper);

        if (createdRepair == null) {
            return Result.fail("创建维修单失败", 500);
        }

        Integer repairId = createdRepair.getRepairId();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("repair", createdRepair);

        // 上传图片
        if (imageFiles != null && !imageFiles.isEmpty()) {
            MultipartFile[] files = imageFiles.toArray(new MultipartFile[0]);
            Result<List<com.example.Yoga_fitness.pojo.RepairImage>> uploadResult =
                    repairImageService.uploadMultipleImages(repairId.longValue(), files);

            if (uploadResult.getCode() == 200) {
                resultMap.put("images", uploadResult.getData());
            }
        }

        return Result.success("申报成功", resultMap);
    }

    @Override
    public Result<String> addImagesToRepair(Integer repairId, MultipartFile[] files) {
        Result<List<RepairImage>> result = repairImageService.uploadMultipleImages(repairId.longValue(), files);

        if (result.getCode() == 200) {
            return Result.success("图片上传成功");
        } else {
            return Result.fail(result.getMsg(), result.getCode());
        }
    }

    @Override
    public Result<List<Object>> getRepairImages(Integer repairId) {
        Result<List<com.example.Yoga_fitness.pojo.RepairImage>> result =
                repairImageService.getImagesByRepairId(repairId.longValue());

        if (result.getCode() == 200) {
            return Result.success("获取成功", new ArrayList<>(result.getData()));
        } else {
            // 修复：使用getMsg()而不是getMessage()
            return Result.fail(result.getMsg(), result.getCode());
        }
    }

    @Override
    public Result<String> deleteImage(Long imageId, Integer userId) {
        return repairImageService.deleteImage(imageId, userId);
    }

    private String generateRepairRequestId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());

        LambdaQueryWrapper<Repair> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(Repair::getRepairRequestId, "RQ" + dateStr);
        int count = Math.toIntExact(this.count(queryWrapper));

        int serialNumber = count + 1;
        String serialStr = String.format("%04d", serialNumber);

        return "RQ" + dateStr + serialStr;
    }

    @Override
    public boolean deleteRepair(Integer repairId, Integer userId, String password) {
        if (repairId == null || userId == null || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("删除参数不能为空");
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new RuntimeException("当前用户不存在");
        }

        if (!Md5Password.generateMD5(password).equals(user.getUserPasswordHash())) {
            throw new RuntimeException("密码错误，删除失败");
        }

        // 修复：使用repairMapper而不是baseMapper
        int affectedRows = repairMapper.deleteRepairByIdAndUserId(repairId, userId);
        System.out.println("----------------affectedRows: " + affectedRows);
        return affectedRows > 0;
    }

    @Override
    public boolean update(Integer repairId, String repairNotes, Integer userId) {
        if (repairId == null || userId == null) {
            throw new IllegalArgumentException("修改参数不能为空");
        }

        // 修复：使用repairMapper而不是baseMapper
        int affectedRows = repairMapper.updateRepairByIdAndUserId(repairId, repairNotes, userId);
        System.out.println("----------------affectedRows: " + affectedRows);
        return affectedRows > 0;
    }
}
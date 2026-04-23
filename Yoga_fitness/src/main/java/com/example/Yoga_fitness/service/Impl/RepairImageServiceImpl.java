package com.example.Yoga_fitness.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Yoga_fitness.mapper.RepairImageMapper;
import com.example.Yoga_fitness.mapper.RepairMapper;
import com.example.Yoga_fitness.pojo.Repair;
import com.example.Yoga_fitness.pojo.RepairImage;
import com.example.Yoga_fitness.service.RepairImageService;
import com.example.Yoga_fitness.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class RepairImageServiceImpl extends ServiceImpl<RepairImageMapper, RepairImage> implements RepairImageService {

    @Autowired
    private RepairImageMapper repairImageMapper;

    @Autowired
    private RepairMapper repairMapper;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.access.url:http://localhost:8081/LX/uploads}")
    private String accessUrl;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @PostConstruct
    public void init() {
        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                System.out.println("创建上传目录: " + uploadDir.toAbsolutePath());
            }

            // 创建子目录
            Path repairDir = uploadDir.resolve("repair");
            if (!Files.exists(repairDir)) {
                Files.createDirectories(repairDir);
                System.out.println("创建维修图片目录: " + repairDir.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("创建上传目录失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<Map<String, Object>> uploadImage(Long repairId, MultipartFile file) {
        System.out.println("===== 开始上传图片 =====");
        System.out.println("当前工作目录: " + System.getProperty("user.dir"));
        System.out.println("uploadPath配置值: " + uploadPath);
        System.out.println("accessUrl配置值: " + accessUrl);

        try {
            // 验证维修单是否存在
            Repair repair = repairMapper.selectById(repairId);
            if (repair == null) {
                System.out.println("维修单不存在: " + repairId);
                return Result.fail("维修单不存在", 404);
            }
            System.out.println("维修单存在: " + repairId);

            // 验证文件
            if (file.isEmpty()) {
                System.out.println("文件为空");
                return Result.fail("文件不能为空", 400);
            }

            String originalFilename = file.getOriginalFilename();
            System.out.println("原始文件名: " + originalFilename);

            String contentType = file.getContentType();
            System.out.println("文件类型: " + contentType);

            if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
                System.out.println("不支持的文件类型: " + contentType);
                return Result.fail("不支持的文件类型，仅支持: jpg, png, gif, bmp, webp", 400);
            }

            long fileSize = file.getSize();
            System.out.println("文件大小: " + fileSize + " bytes");

            if (fileSize > MAX_FILE_SIZE) {
                System.out.println("文件过大: " + fileSize + " > " + MAX_FILE_SIZE);
                return Result.fail("文件大小不能超过5MB", 400);
            }

            // 生成文件名和路径
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            System.out.println("文件扩展名: " + fileExtension);

            String uuid = UUID.randomUUID().toString().replace("-", "");
            String filename = uuid + fileExtension;
            System.out.println("生成文件名: " + filename);

            // 按日期创建目录
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            String datePath = dateFormat.format(new Date());
            String relativePath = "repair/" + datePath + "/" + filename;

            System.out.println("日期路径: " + datePath);
            System.out.println("相对路径: " + relativePath);

            // 完整存储路径
            String fullPath = uploadPath + "/" + relativePath;
            Path destPath = Paths.get(fullPath);

            System.out.println("完整存储路径: " + fullPath);
            System.out.println("目标路径绝对路径: " + destPath.toAbsolutePath());

            // 检查父目录是否存在
            Path parentDir = destPath.getParent();
            System.out.println("父目录: " + parentDir.toAbsolutePath());
            System.out.println("父目录是否存在: " + Files.exists(parentDir));

            // 创建目录
            try {
                System.out.println("开始创建目录...");
                Files.createDirectories(parentDir);
                System.out.println("目录创建成功");
            } catch (IOException e) {
                System.err.println("创建目录失败: " + e.getMessage());
                return Result.fail("创建目录失败: " + e.getMessage(), 500);
            }

            // 保存文件
            try {
                System.out.println("开始保存文件到: " + destPath.toAbsolutePath());
                file.transferTo(destPath.toFile());
                System.out.println("文件保存成功");

                // 验证文件是否真的保存
                boolean fileExists = Files.exists(destPath);
                System.out.println("文件是否存在: " + fileExists);

                if (fileExists) {
                    long savedFileSize = Files.size(destPath);
                    System.out.println("保存后的文件大小: " + savedFileSize + " bytes");
                    try {
                        System.out.println("保存后的文件权限: " + Files.getPosixFilePermissions(destPath));
                    } catch (UnsupportedOperationException e) {
                        System.out.println("保存后的文件权限: Windows系统不支持POSIX权限检查");
                        // 或者使用Windows兼容的方式
                        System.out.println("文件可读: " + Files.isReadable(destPath));
                        System.out.println("文件可写: " + Files.isWritable(destPath));
                        System.out.println("文件可执行: " + Files.isExecutable(destPath));
                    }
                }
            } catch (IOException e) {
                System.err.println("保存文件失败: " + e.getMessage());
                e.printStackTrace();
                return Result.fail("文件上传失败: " + e.getMessage(), 500);
            }

            // 构造访问URL
            String imageUrl = accessUrl + "/" + relativePath;
            System.out.println("图片访问URL: " + imageUrl);

            // 测试URL是否可以访问（去掉上下文路径测试）
            String testUrl = imageUrl.replace("/LX/", "/");
            System.out.println("测试URL: " + testUrl);

            // 保存到数据库
            RepairImage repairImage = new RepairImage();
            repairImage.setRepairId(repairId.intValue());
            repairImage.setImageUrl(imageUrl);
            repairImage.setFilePath(relativePath);
            repairImage.setImageName(originalFilename);
            repairImage.setFileSize(fileSize);
            repairImage.setFileType(contentType);
            repairImage.setCreatedAt(new Date());

            System.out.println("开始保存到数据库...");
            System.out.println("RepairImage对象: " + repairImage);

            boolean saved = this.save(repairImage);
            System.out.println("数据库保存结果: " + saved);

            if (!saved) {
                System.out.println("数据库保存失败，删除已保存的文件...");
                try {
                    Files.deleteIfExists(destPath);
                    System.out.println("文件已删除");
                } catch (IOException e) {
                    System.err.println("删除文件失败: " + e.getMessage());
                }
                return Result.fail("保存图片信息失败", 500);
            }

            System.out.println("数据库保存成功，imageId: " + repairImage.getImageId());

            // 立即查询验证
            System.out.println("立即查询验证...");
            LambdaQueryWrapper<RepairImage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RepairImage::getRepairId, repairId.intValue())
                    .eq(RepairImage::getIsDeleted, 0);

            List<RepairImage> testImages = this.list(queryWrapper);
            System.out.println("查询到的记录数: " + testImages.size());

            for (RepairImage img : testImages) {
                System.out.println("查询到的图片: id=" + img.getImageId() +
                        ", repairId=" + img.getRepairId() +
                        ", url=" + img.getImageUrl());
            }

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("url", repairImage.getImageUrl());
            result.put("id", repairImage.getImageId());
            result.put("name", repairImage.getImageName());
            result.put("repairId", repairImage.getRepairId());

            System.out.println("返回结果: " + result);
            System.out.println("===== 上传完成 =====");

            return Result.success("上传成功", result);

        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            return Result.fail("上传失败: " + e.getMessage(), 500);
        }
    }

    @Override
    public Result<List<RepairImage>> uploadMultipleImages(Long repairId, MultipartFile[] files) {
        List<RepairImage> uploadedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            Result<Map<String, Object>> result = uploadImage(repairId, file);
            if (result.getCode() == 200) {
                // 重新查询数据库获取完整的RepairImage对象
                if (result.getData() != null && result.getData().containsKey("id")) {
                    Object idObj = result.getData().get("id");
                    Integer imageId = null;
                    if (idObj instanceof Integer) {
                        imageId = (Integer) idObj;
                    } else if (idObj instanceof Long) {
                        imageId = ((Long) idObj).intValue();
                    }

                    if (imageId != null) {
                        RepairImage image = this.getById(imageId);
                        if (image != null) {
                            uploadedImages.add(image);
                        }
                    }
                }
            }
        }

        return Result.success("批量上传完成", uploadedImages);
    }

    @Override
    public Result<List<RepairImage>> getImagesByRepairId(Long repairId) {
        try {
            List<RepairImage> images = repairImageMapper.selectByRepairId(repairId.intValue());
            return Result.success("获取成功", images);
        } catch (Exception e) {
            return Result.fail("获取图片失败: " + e.getMessage(), 500);
        }
    }

    @Override
    public Result<String> deleteImage(Long imageId, Integer userId) {
        try {
            RepairImage image = this.getById(imageId);
            if (image == null) {
                return Result.fail("图片不存在", 404);
            }

            // 验证权限
            Repair repair = repairMapper.selectById(Long.valueOf(image.getRepairId()));
            if (repair == null) {
                return Result.fail("维修单不存在", 404);
            }

            boolean hasPermission = false;
            if (userId == 1 || userId == 12) { // 管理员
                hasPermission = true;
            } else if (repair.getUserId() != null && repair.getUserId().equals(userId)) {
                hasPermission = true;
            }

            if (!hasPermission) {
                return Result.fail("没有删除权限", 403);
            }

            // 逻辑删除
            int deleted = repairImageMapper.logicDelete(imageId.intValue());
            if (deleted > 0) {
                return Result.success("删除成功");
            } else {
                return Result.fail("删除失败", 500);
            }

        } catch (Exception e) {
            return Result.fail("删除失败: " + e.getMessage(), 500);
        }
    }
}
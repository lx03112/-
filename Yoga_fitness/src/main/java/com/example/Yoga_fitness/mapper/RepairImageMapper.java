// RepairImageMapper.java
package com.example.Yoga_fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.Yoga_fitness.pojo.RepairImage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RepairImageMapper extends BaseMapper<RepairImage> {

    @Select("SELECT * FROM yjx_repair_images WHERE repair_id = #{repairId} AND is_deleted = 0 ORDER BY created_at DESC")
    @Results({
            @Result(property = "imageId", column = "image_id"),
            @Result(property = "repairId", column = "repair_id"),
            @Result(property = "imageUrl", column = "image_url"),
            @Result(property = "imageName", column = "image_name"),
            @Result(property = "filePath", column = "file_path"),
            @Result(property = "fileSize", column = "file_size"),
            @Result(property = "fileType", column = "file_type"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "isDeleted", column = "is_deleted")
    })
    List<RepairImage> selectByRepairId(@Param("repairId") Integer repairId);

    @Update("UPDATE yjx_repair_images SET is_deleted = 1 WHERE image_id = #{imageId}")
    int logicDelete(@Param("imageId") Integer imageId);

    @Select("SELECT COUNT(*) FROM yjx_repair_images WHERE repair_id = #{repairId} AND is_deleted = 0")
    int countByRepairId(@Param("repairId") Integer repairId);
}
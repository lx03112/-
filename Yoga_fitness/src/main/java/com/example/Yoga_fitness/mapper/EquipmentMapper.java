package com.example.Yoga_fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.Yoga_fitness.pojo.Equipment;
import com.example.Yoga_fitness.module.EquipmentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {

    // 查询所有可用的器材
    @Select("SELECT * FROM yjx_equipment WHERE equip_status != '报废' ORDER BY created_at DESC")
    List<Equipment> findAllAvailable();

    // 根据分类查询器材
    @Select("SELECT * FROM yjx_equipment WHERE equip_category = #{category} AND equip_status != '报废' ORDER BY created_at DESC")
    List<Equipment> findByCategory(@Param("category") String category);

    // 查询热门器材（按价格或创建时间排序）
    @Select("SELECT * FROM yjx_equipment WHERE equip_status != '报废' ORDER BY equip_price DESC, created_at DESC LIMIT 8")
    List<Equipment> findHotEquipments();

    // 根据训练部位查询（模糊查询）
    @Select("SELECT * FROM yjx_equipment WHERE equip_description LIKE CONCAT('%', #{part}, '%') AND equip_status != '报废'")
    List<Equipment> findByTrainingPart(@Param("part") String part);

    // 查询所有不重复的分类
    @Select("SELECT DISTINCT equip_category FROM yjx_equipment WHERE equip_status != '报废'")
    List<String> findAllCategories();

    // 根据ID查询器材详情
    @Select("SELECT * FROM yjx_equipment WHERE equip_id = #{id}")
    Equipment findById(@Param("id") Long id);

    // 搜索器材
    @Select("SELECT * FROM yjx_equipment WHERE (equip_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR equip_description LIKE CONCAT('%', #{keyword}, '%') " +
            "OR equip_brand LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND equip_status != '报废'")
    List<Equipment> search(@Param("keyword") String keyword);
}
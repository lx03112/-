package com.example.Yoga_fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Yoga_fitness.module.EquipmentVO;
import com.example.Yoga_fitness.pojo.Repair;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RepairMapper extends BaseMapper<Repair> {
    @Select("""
    SELECT
        r.repair_id AS repairId,
        r.repair_request_id AS repairRequestId,
        r.equip_name AS equipName,
        r.repair_notes AS repairNotes,
        r.repair_status AS repairStatus,
        r.user_id AS userId,
        r.created_at AS createdAt
    FROM yjx_repair r
    LEFT JOIN yjx_user cu ON cu.user_id = #{userId}
    WHERE 1=1
        AND r.is_deleted = 0
        AND ( 
            r.repair_request_id LIKE CONCAT('%', IFNULL(#{searchKeyword}, ''), '%')
            OR r.equip_name LIKE CONCAT('%', IFNULL(#{searchKeyword}, ''), '%')
            OR r.repair_notes LIKE CONCAT('%', IFNULL(#{searchKeyword}, ''), '%')
            OR r.repair_status LIKE CONCAT('%', IFNULL(#{searchKeyword}, ''), '%')
        )
    ORDER BY
        CASE 
            WHEN #{sortField} = 'repairId' THEN r.repair_id
            WHEN #{sortField} = 'repairRequestId' THEN r.repair_request_id
            WHEN #{sortField} = 'createdAt' THEN r.created_at
            ELSE r.created_at
        END
    ${sortOrder == 'desc' ? 'DESC' : 'ASC'}
    """)
    IPage<Repair> queryRepairByCondition(Page<Repair> page,
                                         @Param("userId") Integer userId,
                                         @Param("searchKeyword") String searchKeyword,
                                         @Param("sortField") String sortField,
                                         @Param("sortOrder") String sortOrder);

    @Select("""
    SELECT 
        equip_id as equipId,
        equip_name as equipName
    FROM yjx_equipment
    ORDER BY equip_name
    """)
    List<EquipmentVO> getAllEquipment();

    @Delete("""
    DELETE FROM yjx_repair
    WHERE repair_id = #{repairId} 
    AND ( #{userId} IN (1,12) OR user_id = #{userId} )
    """)
    int deleteRepairByIdAndUserId(Integer repairId, Integer userId);

    @Update("""
    UPDATE yjx_repair
    SET repair_notes = #{repairNotes},
            updated_at = NOW()
    WHERE repair_id = #{repairId}
    AND ( #{userId} IN (1,12) OR user_id = #{userId} )
    """)
    int updateRepairByIdAndUserId(Integer repairId, String repairNotes, Integer userId);

}
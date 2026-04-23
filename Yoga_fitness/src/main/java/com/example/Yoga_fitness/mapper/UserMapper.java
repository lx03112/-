package com.example.Yoga_fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Yoga_fitness.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名、邮箱、手机号查询用户信息
     */
    @Select("""
        SELECT 
            user_id AS userId,
            user_name AS userName,
            user_email AS userEmail,
            user_password_hash AS userPasswordHash,
            user_phone AS userPhone,
            user_status AS userStatus
        FROM yjx_user 
        WHERE user_name = #{userName} 
            AND user_email = #{userEmail} 
            AND user_phone = #{userPhone}
    """)
    Map<String, Object> selectUserByInfo(
            @Param("userName") String userName,
            @Param("userEmail") String userEmail,
            @Param("userPhone") String userPhone
    );

    /**
     * 根据用户名、邮箱、手机号更新密码
     */
    @Update("""
        UPDATE yjx_user 
        SET user_password_hash = #{passwordHash},
            user_last_active = NOW()
        WHERE user_name = #{userName} 
            AND user_email = #{userEmail} 
            AND user_phone = #{userPhone}
    """)
    int updatePasswordByInfo(
            @Param("userName") String userName,
            @Param("userEmail") String userEmail,
            @Param("userPhone") String userPhone,
            @Param("passwordHash") String passwordHash
    );

    @Select("""
        SELECT
            user_id AS userId,
            user_name AS userName,
            user_email AS userEmail,
            user_password_hash AS userPasswordHash,
            role_id AS roleId,
            user_bio AS userBio,
            user_phone AS userPhone,
            user_gender AS userGender,
            user_last_active AS userLastActive,
            user_created_at AS userCreatedAt,
            user_status AS userStatus
        FROM yjx_user
        WHERE 1=1
            -- 用户ID筛选，仅当userId不为null时添加条件
            AND (#{userId} IS NULL OR user_id = #{userId})
            -- 关键词搜索，仅当searchKeyword不为空时添加模糊匹配条件
            AND (
                #{searchKeyword} IS NULL OR #{searchKeyword} = '' OR
                user_name LIKE CONCAT('%', #{searchKeyword}, '%') OR
                user_email LIKE CONCAT('%', #{searchKeyword}, '%') OR
                user_phone LIKE CONCAT('%', #{searchKeyword}, '%')
            )
            -- 排序，与维修管理查询逻辑完全一致
        ORDER BY
            CASE WHEN #{sortField} IS NOT NULL AND #{sortField} != ''
                THEN CASE #{sortField}
                    WHEN 'userName' THEN user_name
                    WHEN 'userId' THEN user_id
                    WHEN 'userCreatedAt' THEN user_created_at
                    WHEN 'userLastActive' THEN user_last_active
                    WHEN 'userStatus' THEN user_status
                    ELSE user_created_at END
                ELSE user_created_at END
        ${sortOrder != null && "asc".equals(sortOrder.toLowerCase()) ? 'ASC' : 'DESC'}
    """)
    IPage<User> queryUserByCondition(Page<User> page,
                                     @Param("userId") Integer userId,
                                     @Param("searchKeyword") String searchKeyword,
                                     @Param("sortField") String sortField,
                                     @Param("sortOrder") String sortOrder);

    @Select("""
        SELECT
            user_name AS userName,
            role_id AS roleId,
            user_bio AS userBio
        FROM yjx_user
    """)
    java.util.List<com.example.Yoga_fitness.pojo.Role> listAllRoles();

    /**
     * 获取用户信息（除密码外）
     * 修正SQL语句，去掉错误的单引号
     */
    @Select("""
        SELECT 
            user_id AS userId,
            user_name AS userName,
            user_email AS userEmail,
            user_phone AS userPhone,
            role_id AS roleId,
            user_bio AS userBio,
            user_status AS userStatus,
            user_created_at AS userCreatedAt
        FROM yjx_user 
        WHERE user_id = #{userId}
    """)
    Map<String, Object> selectUserInfoWithoutPassword(@Param("userId") Integer userId);

    /**
     * 更新用户角色
     */
    @Update("UPDATE yjx_user SET role_id = #{newRoleId} WHERE user_id = #{userId}")
    int updateUserRole(@Param("userId") Integer userId, @Param("newRoleId") Integer newRoleId);
}
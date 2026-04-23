package com.example.Yoga_fitness.mapper;

import com.example.Yoga_fitness.pojo.YjxMessage;
import org.apache.ibatis.annotations.*;

@Mapper
public interface YjxMessageMapper {

    // 根据验证码查询
    @Select("SELECT * FROM yjx_message WHERE verification_code = #{code}")
    YjxMessage findByCode(@Param("code") String code);

    // 插入验证码
    @Insert("INSERT INTO yjx_message(verification_code, user_id, used, used_time, created_time, expired_time, purpose) " +
            "VALUES(#{verificationCode}, #{userId}, #{used}, #{usedTime}, #{createdTime}, #{expiredTime}, #{purpose})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(YjxMessage message);

    // 更新验证码使用状态
    @Update("UPDATE yjx_message SET used = #{used}, user_id = #{userId}, used_time = #{usedTime} WHERE id = #{id}")
    int update(YjxMessage message);

    // 根据验证码和用途查询未使用的
    @Select("SELECT * FROM yjx_message WHERE verification_code = #{code} AND purpose = #{purpose} AND used = false")
    YjxMessage findValidByCodeAndPurpose(@Param("code") String code, @Param("purpose") String purpose);
}

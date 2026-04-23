package com.example.Yoga_fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Yoga_fitness.module.CoachVo;
import com.example.Yoga_fitness.pojo.Course;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    @Select("""
    SELECT
        c.*,
        u.user_name AS coach
    FROM yjx_course c
    LEFT JOIN yjx_user u ON c.coach_id = u.user_id
    LEFT JOIN yjx_user cu ON cu.user_id = #{userId}
    WHERE 1=1
        
        AND ( 
            c.course_model LIKE CONCAT('%', #{searchKeyword}, '%')
            OR c.course_description LIKE CONCAT('%', #{searchKeyword}, '%')
            OR c.expect_status LIKE CONCAT('%', #{searchKeyword}, '%')
        )
    ORDER BY
        CASE WHEN #{sortField} IS NOT NULL AND #{sortOrder} IS NOT NULL
            THEN CASE #{sortField}
                WHEN 'createdAt' THEN c.created_at
                WHEN 'courseId' THEN c.course_id
                WHEN 'timeSchedule' THEN c.time_schedule
                ELSE c.created_at
            END
        ELSE c.created_at
        END
    ${sortOrder == 'desc' ? 'DESC' : 'ASC'}
    """)
    IPage<Course> queryCourseByCondition(Page<Course> page,
                                         @Param("userId") Integer userId,
                                         @Param("searchKeyword") String searchKeyword,
                                         @Param("sortField") String sortField,
                                         @Param("sortOrder") String sortOrder);

    @Select("""
    SELECT 
        user_id as userId,
        user_name as userName
    FROM yjx_user
    WHERE role_id = 3
    GROUP BY user_id, user_name
    ORDER BY user_name
    """)
    List<CoachVo> getAllCoaches();

    @Delete("""
    DELETE FROM yjx_course
    WHERE course_id = #{courseId} 
    AND ( #{userId} IN (1,12) OR user_id = #{userId} )
    """)
    int deleteCourseByIdAndUserId(@Param("courseId") Integer courseId,
                                  @Param("userId") Integer userId);

    @Select("""
    SELECT
        c.*,
        u.user_name as coach
    FROM yjx_course c
    LEFT JOIN yjx_user u ON c.coach_id = u.user_id
    WHERE c.course_id = #{courseId}
    """)
    Course getCourseById(@Param("courseId") Integer courseId);

    @Insert("""
    INSERT INTO yjx_course(
        course_model,
        course_description,
        expect_status,
        coach_id,
        time_schedule,
        user_id,
        created_at,
        updated_at
    ) VALUES (
        #{courseModel},
        #{courseDescription},
        #{expectStatus},
        #{coachId},
        #{timeSchedule},
        #{userId},
        NOW(),
        NOW()
    )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "courseId")
    int insertCourse(Course course);

//    @Update("""
//    UPDATE yjx_course
//    SET
//        course_model = #{courseModel},
//        course_description = #{courseDescription},
//        expect_status = #{expectStatus},
//        coach_id = #{coachId},
//        time_schedule = #{timeSchedule},
//        updated_at = NOW()
//    WHERE course_id = #{courseId}
//    AND ( #{userId} IN (1, 3) OR user_id = #{userId} )
//    """)
//    int updateCourse(Course course);
}
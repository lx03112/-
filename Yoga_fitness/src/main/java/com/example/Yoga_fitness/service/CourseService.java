package com.example.Yoga_fitness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.Yoga_fitness.module.CoachVo;
import com.example.Yoga_fitness.module.QueryModule;
import com.example.Yoga_fitness.pojo.Course;
import com.example.Yoga_fitness.util.Result;

import java.util.List;
import java.util.Map;


public interface CourseService extends IService<Course> {

    Result<Map<String, Object>> queryCourse(QueryModule queryModule);

    List<CoachVo> getAllCoach();

    boolean deleteCourse(Integer courseId, Integer userId, String password);
}

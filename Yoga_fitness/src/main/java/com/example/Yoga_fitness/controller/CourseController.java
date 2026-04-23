package com.example.Yoga_fitness.controller;

import com.example.Yoga_fitness.mapper.CourseMapper;
import com.example.Yoga_fitness.module.CoachVo;
import com.example.Yoga_fitness.module.QueryModule;
import com.example.Yoga_fitness.pojo.Course;
import com.example.Yoga_fitness.service.CourseService;
import com.example.Yoga_fitness.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/management")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @RequestMapping("/getAllCourseManagement")
    public Result<Map<String,Object>> queryCourse(QueryModule queryModule){
        return courseService.queryCourse(queryModule);
    }

    @GetMapping("/getAllCoach")
    public Result<List<CoachVo>> getAllCoach() {
        List<CoachVo> list = courseService.getAllCoach();
        return Result.success(list);
    }

    @PostMapping("/createCourseManagement")
    public Result<String> createCourseManagement(@RequestBody Course course) {
        boolean result = courseService.save(course);

        return result ? Result.success("创建成功") : Result.fail("创建失败",500);
    }

    @PostMapping("/deleteCourseManagement")
    public Result<Object> deleteCourse(
            @RequestParam("courseId") Integer courseId,
            @RequestParam("userId") Integer userId,
            @RequestParam("password") String password) {
        // 调用Service删除
        boolean isDeleted = courseService.deleteCourse(courseId, userId, password);
        if (isDeleted) {
            return Result.success(null); // 成功响应（code=200）
        } else {
            return Result.fail("订单不存在或无删除权限", 403); // 无权限/无数据（code=403）
        }
    }
}

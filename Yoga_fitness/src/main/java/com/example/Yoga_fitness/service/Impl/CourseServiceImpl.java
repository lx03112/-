package com.example.Yoga_fitness.service.Impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Yoga_fitness.mapper.CourseMapper;
import com.example.Yoga_fitness.module.CoachVo;
import com.example.Yoga_fitness.module.QueryModule;
import com.example.Yoga_fitness.pojo.Course;
import com.example.Yoga_fitness.pojo.User;
import com.example.Yoga_fitness.service.CourseService;
import com.example.Yoga_fitness.service.UserService;
import com.example.Yoga_fitness.util.Md5Password;
import com.example.Yoga_fitness.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    UserService userService;

    @Override
    public Result<Map<String, Object>> queryCourse(QueryModule queryModule) {
        int pageNum = queryModule.getPageNum()==null?1:queryModule.getPageNum();
        int pageSize = queryModule.getPageSize()==null?10:queryModule.getPageSize();
        Page<Course> page = new Page<>(pageNum,pageSize);
        //处理关键词,不可为空
        String searchKeyWord = queryModule.getSearchKeyword()==null?"":queryModule.getSearchKeyword();
        //排序参数
        String sortField = queryModule.getSortField()==null?"created_at":queryModule.getSortField();
        String sortOrder = queryModule.getSortOrder()==null?"desc":queryModule.getSortOrder();
        //调用mapper层方法
        IPage<Course> Page = courseMapper.queryCourseByCondition(page, queryModule.getUserId(), searchKeyWord,sortField,sortOrder);
        //返回结果，mapper层存放查询结果，分页参数
        Map<String,Object> map = new HashMap<>();
        System.out.println(Page.getRecords());
        map.put("courseRequest",Page.getRecords());
        map.put("count",Page.getTotal());
        return Result.success(map);
    }

    @Override
    public List<CoachVo> getAllCoach() {
        return courseMapper.getAllCoaches();
    }

    @Override
    public boolean deleteCourse(Integer courseId, Integer userId, String password) {
        // 1. 校验参数：避免null导致异常
        if (courseId == null || userId == null || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("删除参数不能为空");
        }

        // 2. 验证密码正确性：从数据库查询用户真实密码，与前端传递的密码比对

        User user = userService.getById(userId); // User是用户实体类，password
        if (user == null) {
            throw new RuntimeException("当前用户不存在");
        }

        if (!Md5Password.generateMD5(password).equals(user.getUserPasswordHash())) { // 实际项目需替换为加密比对逻辑
            throw new RuntimeException("密码错误，删除失败");
        }

        // 3. 调用Mapper删除：返回影响行数（1=成功，0=无数据/无权限）
        int affectedRows = baseMapper.deleteCourseByIdAndUserId(courseId, userId);
        System.out.println("----------------affectedRows: " + affectedRows);
        return affectedRows > 0; // 影响行数>0表示删除成功
    }
}

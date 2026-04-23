package com.example.Yoga_fitness.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Yoga_fitness.mapper.UserMapper;
import com.example.Yoga_fitness.module.QueryModule;
import com.example.Yoga_fitness.module.UserFound;
import com.example.Yoga_fitness.module.UserLogin;
import com.example.Yoga_fitness.module.UserRegister;
import com.example.Yoga_fitness.pojo.Repair;
import com.example.Yoga_fitness.pojo.Role;
import com.example.Yoga_fitness.service.UserService;
import com.example.Yoga_fitness.util.Md5Password;
import com.example.Yoga_fitness.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.Yoga_fitness.pojo.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserLogin login(String usernameOrEmail,String password) {
        User user = this.getOne(new QueryWrapper<User>().eq("user_name",usernameOrEmail).or().eq("user_email",usernameOrEmail));
        if(user==null){
            return null;
        }

        if (!user.getUserPasswordHash().equals(Md5Password.generateMD5(password))){
            return null;
        }
        if ("banned".equals(user.getUserStatus())) {
            // 这里可以抛出自定义异常或返回特定对象，我建议抛异常让Controller处理
            throw new RuntimeException("你已被封禁");
        }

        UserLogin userLogin = new UserLogin();
        userLogin.setUserId(user.getUserId());
        userLogin.setUserName(user.getUserName());
        userLogin.setUserEmail(user.getUserEmail());
        userLogin.setUserPhone(user.getUserPhone());
        userLogin.setUserBio(user.getUserBio());
        userLogin.setRoleId(user.getRoleId());
        return userLogin;
    }

    @Override
    public Result<String> register(UserRegister userRegister){
        //判断用户名是否存在
        if(this.getOne(new QueryWrapper<User>().eq("user_name",userRegister.getUserName())) != null){
            return Result.fail("用户名已存在",400);
        }
        //判断邮箱是否存在
        if(this.getOne(new QueryWrapper<User>().eq("user_email",userRegister.getUserEmail()))!= null){
            return Result.fail("邮箱已存在",400);
        }
        //新建一个用户
        User user = new User();
        user.setUserName(userRegister.getUserName());
        user.setUserEmail(userRegister.getUserEmail());
        user.setUserPasswordHash(Md5Password.generateMD5(userRegister.getUserPasswordHash()));
        //设置默认角色Id
        user.setRoleId(4);
        //返回Result
        boolean save = this.save(user);
        return save ? Result.success("注册成功"):Result.fail("注册失败",400);
    }

    @Override
    public Result<String> foundPassword(UserFound userFound) {
        Map<String, Object> user = userMapper.selectUserByInfo(
                userFound.getUserName(),
                userFound.getUserEmail(),
                userFound.getUserPhone()
        );
        if (user == null) {
            return Result.fail("用户信息验证失败，请检查用户名、邮箱和手机号", 400);
        }
        String passwordHash = Md5Password.generateMD5(userFound.getUserPasswordHash());
        int rows = userMapper.updatePasswordByInfo(
                userFound.getUserName(),
                userFound.getUserEmail(),
                userFound.getUserPhone(),
                passwordHash // 前端已加密的密码
        );
        if (rows > 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("message", "密码修改成功");
            result.put("userName", userFound.getUserName());
            return Result.success("密码修改成功");
        } else {
            return Result.fail("密码修改失败，请稍后再试", 500);
        }
    }

    @Override
    public Map<String, Object> queryUser(QueryModule queryModule) {
        //判断分页参数
        int pageNum = queryModule.getPageNum()==null?1:queryModule.getPageNum();
        int pageSize = queryModule.getPageSize()==null?10:queryModule.getPageSize();
        Page<User> page = new Page<>(pageNum,pageSize);
        //处理关键词,不可为空
        String searchKeyWord = queryModule.getSearchKeyword()==null?"":queryModule.getSearchKeyword();
        //排序参数
        String sortField = queryModule.getSortField()==null?"created_at":queryModule.getSortField();
        String sortOrder = queryModule.getSortOrder()==null?"desc":queryModule.getSortOrder();
        //调用mapper层方法
        IPage<User> Page = userMapper.queryUserByCondition(page, queryModule.getUserId(), searchKeyWord,sortField,sortOrder);
        //返回结果，mapper层存放查询结果，分页参数
        Map<String,Object> map = new HashMap<>();
        map.put("userList",Page.getRecords());
        map.put("count",Page.getTotal());
        return map;

    }

    @Override
    public Result<List<Role>> listAllRoles() {
        List<Role> roles = userMapper.listAllRoles();
        return Result.success(roles);
    }
}

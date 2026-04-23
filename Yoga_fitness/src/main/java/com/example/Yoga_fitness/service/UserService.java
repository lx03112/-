package com.example.Yoga_fitness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.Yoga_fitness.module.QueryModule;
import com.example.Yoga_fitness.module.UserFound;
import com.example.Yoga_fitness.module.UserLogin;
import com.example.Yoga_fitness.module.UserRegister;
import com.example.Yoga_fitness.pojo.Role;
import com.example.Yoga_fitness.pojo.User;
import com.example.Yoga_fitness.util.Result;

import java.util.List;
import java.util.Map;

public interface UserService extends IService<User> {

    UserLogin login(String usernameOrEmail, String password);

    Result<String> register(UserRegister userRegister);

    Result<String> foundPassword(UserFound userFound);

    Map<String, Object> queryUser(QueryModule queryModule);

    Result<List<Role>> listAllRoles();
}

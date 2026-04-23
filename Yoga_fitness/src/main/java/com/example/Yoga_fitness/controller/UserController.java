package com.example.Yoga_fitness.controller;

import com.example.Yoga_fitness.mapper.UserMapper;
import com.example.Yoga_fitness.mapper.YjxMessageMapper;
import com.example.Yoga_fitness.module.*;
import com.example.Yoga_fitness.pojo.Role;
import com.example.Yoga_fitness.pojo.User;
import com.example.Yoga_fitness.pojo.YjxMessage;
import com.example.Yoga_fitness.service.UserService;
import com.example.Yoga_fitness.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private YjxMessageMapper yjxMessageMapper;

    @RequestMapping("/login")
    public Result<UserLogin> login(@RequestParam String usernameOrEmail, @RequestParam String password){
        try {
            UserLogin userLogin = userService.login(usernameOrEmail, password);
            if (userLogin == null) {
                return Result.fail("用户名或密码错误", 400);
            }
            return Result.success(userLogin);
        } catch (RuntimeException e) {
            // 捕获封禁异常
            if ("你已被封禁".equals(e.getMessage())) {
                return Result.fail("你已被封禁", 403); // 使用403状态码表示禁止访问
            }
            // 其他异常
            return Result.fail("登录失败", 500);
        }
    }

    //注册
    @PostMapping("/createUser")
    public Result<String> createUser(@RequestBody UserRegister userRegister){
        //调用service层方法
       return userService.register(userRegister);

    }

    @PostMapping("/foundPassword")
    public Result<String> foundPassword(@RequestBody UserFound userFound) {
        String userName = userFound.getUserName();
        String userEmail = userFound.getUserEmail();
        String userPhone = userFound.getUserPhone();
        String userPassword = userFound.getUserPasswordHash();

        if (userName == null || userEmail == null || userPhone == null|| userPassword == null) {
            return Result.fail("请输入完整",400);
        }
        return userService.foundPassword(userFound);
    }

    @GetMapping("/getAllUsers")
    public Result<Map<String, Object>> queryUsers(QueryModule queryModule) {
        Map<String, Object> map = userService.queryUser(queryModule);
        return Result.success(map);
    }

    @RequestMapping("/list")
    public Result<List<Role>> list() {
        return userService.listAllRoles();
    }

    @PostMapping("/changeRole")
    public Result<?> changeRole(@RequestBody ChangeRoleRequest request) {
        try {
            // 1. 参数校验
            if (request.getUserId() == null || request.getUserId() <= 0) {
                return Result.fail("用户ID不能为空且必须大于0", 400);
            }

            if (request.getNewRoleId() == null) {
                return Result.fail("新角色ID不能为空", 400);
            }

            // 3. 查询用户是否存在
            User user = userService.getById(request.getUserId());
            if (user == null) {
                return Result.fail("用户不存在", 400);
            }

            // 4. 检查用户是否被封禁
            if ("banned".equals(user.getUserStatus())) {
                return Result.fail("该用户已被封禁，无法修改角色", 403);
            }

            // 5. 检查是否已经是目标角色（避免重复操作）
            if (request.getNewRoleId().equals(user.getRoleId())) {
                String currentRole = user.getRoleId() == 4 ? "普通用户" : "会员";
                return Result.fail("用户已经是" + currentRole + "，无需重复操作", 400);
            }

            // 6. 更新用户角色
            User updateUser = new User();
            updateUser.setUserId(Math.toIntExact(request.getUserId()));
            updateUser.setRoleId(request.getNewRoleId());

            boolean success = userService.updateById(updateUser);
            if (success) {
                // 记录操作日志（可选）
                String action = request.getNewRoleId() == 5 ? "升级为会员" : "降级为普通用户";
                String oldRole = user.getRoleId() == 4 ? "普通用户" : "会员";
                String newRole = request.getNewRoleId() == 4 ? "普通用户" : "会员";

                // 构造返回数据（可选）
                Map<String, Object> data = new HashMap<>();
                data.put("userId", request.getUserId());
                data.put("oldRole", oldRole);
                data.put("newRole", newRole);
                data.put("action", action);

                return Result.success(action + "成功", data);
            } else {
                return Result.fail("角色变更失败，请稍后重试", 500);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // 记录错误日志
            System.err.println("角色变更失败，用户ID: " + request.getUserId() + ", 错误: " + e.getMessage());
            return Result.fail("服务器内部错误: " + e.getMessage(), 500);
        }
    }



    /**
     * 封禁用户接口
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/ban/{userId}")
    public Result<?> banUser(@PathVariable Long userId) {
        try {
            // 1. 参数校验
            if (userId == null || userId <= 0) {
                return Result.fail("用户ID不能为空且必须大于0", 400);
            }

            // 2. 查询用户是否存在
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在", 400);
            }

            // 3. 检查用户是否已经被封禁
            if ("banned".equals(user.getUserStatus())) {
                return Result.fail("该用户已被封禁，无需重复操作", 400);
            }

            // 4. 更新用户状态为封禁
            User updateUser = new User();
            updateUser.setUserId(Math.toIntExact(userId));
            updateUser.setUserStatus("banned");

            boolean success = userService.updateById(updateUser);
            if (success) {
                return Result.success("用户封禁成功");
            } else {
                return Result.fail("用户封禁失败，请稍后重试", 500);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("服务器内部错误: " + e.getMessage(), 500);
        }
    }

    /**
     * 解禁用户接口
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/unban/{userId}")
    public Result<?> unbanUser(@PathVariable Long userId) {
        try {
            // 1. 参数校验
            if (userId == null || userId <= 0) {
                return Result.fail("用户ID不能为空且必须大于0", 400);
            }

            // 2. 查询用户是否存在
            User user = userService.getById(userId);
            if (user == null) {
                return Result.fail("用户不存在", 400);
            }

            // 3. 检查用户是否已经是活跃状态
            if ("active".equals(user.getUserStatus())) {
                return Result.fail("该用户已经是活跃状态，无需重复操作", 400);
            }

            // 4. 更新用户状态为活跃
            User updateUser = new User();
            updateUser.setUserId(Math.toIntExact(userId));
            updateUser.setUserStatus("active");

            boolean success = userService.updateById(updateUser);
            if (success) {
                return Result.success("用户解禁成功");
            } else {
                return Result.fail("用户解禁失败，请稍后重试", 500);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("服务器内部错误: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/getUserInfo")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestParam Integer userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 注意：这里通过注入的userMapper实例调用方法，不是静态调用
            // 错误的写法：UserMapper.selectUserInfoWithoutPassword(userId)
            // 正确的写法：userMapper.selectUserInfoWithoutPassword(userId)

            // 1. 验证用户是否存在且为普通用户
            Map<String, Object> userInfo = userMapper.selectUserInfoWithoutPassword(userId);

            if (userInfo == null || userInfo.isEmpty()) {
                response.put("code", 404);
                response.put("msg", "用户不存在");
                return ResponseEntity.ok(response);
            }

            // 2. 获取当前角色ID
            Integer currentRoleId = (Integer) userInfo.get("roleId");
            if (currentRoleId == null) {
                response.put("code", 400);
                response.put("msg", "用户角色信息异常");
                return ResponseEntity.ok(response);
            }

            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", userInfo);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("msg", "服务器错误: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 升级为黄金用户
     * POST http://127.0.0.1:8081/LX/user/upgradeToGold
     * 请求体：{"userId": 1, "verificationCode": "ABCD1234"}
     */
    @PostMapping("/upgradeToGold")
    public ResponseEntity<Map<String, Object>> upgradeToGold(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Integer userId = (Integer) request.get("userId");
            String verificationCode = (String) request.get("verificationCode");

            if (userId == null || verificationCode == null || verificationCode.trim().isEmpty()) {
                response.put("code", 400);
                response.put("msg", "参数错误：用户ID和验证码不能为空");
                return ResponseEntity.ok(response);
            }

            // 1. 验证用户是否存在且为普通用户
            Map<String, Object> userInfo = userMapper.selectUserInfoWithoutPassword(userId);
            if (userInfo == null || userInfo.isEmpty()) {
                response.put("code", 404);
                response.put("msg", "用户不存在");
                return ResponseEntity.ok(response);
            }

            Integer currentRoleId = (Integer) userInfo.get("roleId");
            if (currentRoleId == null || currentRoleId != 4) {
                response.put("code", 400);
                response.put("msg", "只有普通用户(角色ID=4)可以升级为黄金用户，当前用户角色ID为：" + currentRoleId);
                return ResponseEntity.ok(response);
            }

            // 2. 验证验证码
            // 首先检查验证码是否存在
            com.example.Yoga_fitness.pojo.YjxMessage message =
                    yjxMessageMapper.findByCode(verificationCode);

            if (message == null) {
                response.put("code", 400);
                response.put("msg", "验证码不存在");
                return ResponseEntity.ok(response);
            }

            // 检查验证码是否已使用
            if (Boolean.TRUE.equals(message.getUsed())) {
                response.put("code", 400);
                response.put("msg", "验证码已被使用");
                return ResponseEntity.ok(response);
            }

            // 检查验证码是否过期
            if (message.getExpiredTime() != null &&
                    LocalDateTime.now().isAfter(message.getExpiredTime())) {
                response.put("code", 400);
                response.put("msg", "验证码已过期");
                return ResponseEntity.ok(response);
            }

            // 检查验证码用途是否正确（可选）
            if (!"UPGRADE_GOLD".equals(message.getPurpose())) {
                response.put("code", 400);
                response.put("msg", "验证码用途不正确");
                return ResponseEntity.ok(response);
            }

            // 3. 更新用户角色为黄金用户(5)
            int updateResult = userMapper.updateUserRole(userId, 5);
            if (updateResult <= 0) {
                response.put("code", 500);
                response.put("msg", "更新用户角色失败");
                return ResponseEntity.ok(response);
            }

            // 4. 标记验证码为已使用
            message.setUsed(true);
            message.setUserId(userId);
            message.setUsedTime(LocalDateTime.now());
            yjxMessageMapper.update(message);

            response.put("code", 200);
            response.put("msg", "升级成功！您现在是黄金用户了");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);
            response.put("msg", "服务器错误: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 生成验证码（管理员使用）
     * POST http://127.0.0.1:8081/LX/user/generateVerificationCode
     */
    @PostMapping("/generateVerificationCode")
    public ResponseEntity<Map<String, Object>> generateVerificationCode(
            @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String purpose = (String) request.get("purpose");
            Integer expireHours = (Integer) request.get("expireHours");

            if (purpose == null || purpose.trim().isEmpty()) {
                purpose = "UPGRADE_GOLD";
            }
            if (expireHours == null) {
                expireHours = 24;
            }

            // 生成随机验证码
            String verificationCode = generateRandomCode(8);

            // 创建验证码记录 - 注意这里需要使用实例
            com.example.Yoga_fitness.pojo.YjxMessage message =
                    new com.example.Yoga_fitness.pojo.YjxMessage();
            message.setVerificationCode(verificationCode);
            message.setPurpose(purpose);
            message.setExpiredTime(LocalDateTime.now().plusHours(expireHours));
            message.setUsed(false);
            message.setCreatedTime(LocalDateTime.now());

            // 使用mapper实例插入数据
            int result = yjxMessageMapper.insert(message);

            if (result > 0) {
                response.put("code", 200);
                response.put("msg", "验证码生成成功");
                response.put("data", verificationCode);
            } else {
                response.put("code", 500);
                response.put("msg", "验证码生成失败");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("msg", "服务器错误: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 生成随机验证码
     */
    private String generateRandomCode(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }
}

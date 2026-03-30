package com.xmon.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.xmon.shortlink.admin.common.convention.result.Result;
import com.xmon.shortlink.admin.common.convention.result.Results;
import com.xmon.shortlink.admin.dto.req.UserLoginReqDTO;
import com.xmon.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.xmon.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.xmon.shortlink.admin.dto.resp.UserActualRespDTO;
import com.xmon.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.xmon.shortlink.admin.dto.resp.UserRespDTO;
import com.xmon.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制层
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable String username) {
        return Results.success(userService.getUserByUsername(username));
    }

    /**
     * 根据用户名获取无脱敏用户信息
     */
    @GetMapping("/api/shortlink/v1/actual/user/{username}")
    public Result<UserActualRespDTO> getActualUserByUsername(@PathVariable String username) {
        return Results.success(BeanUtil.toBean(userService.getUserByUsername(username), UserActualRespDTO.class));
    }

    /**
     * 判断用户名是否可用
     */
    @GetMapping("/api/shortlink/v1/user/available")
    public Result<Boolean> isUsernameAvailable(@RequestParam("username") String username) {
        return Results.success(userService.isUsernameAvailable(username));
    }

    /**
     * 用户注册
     */
    @PostMapping("/api/shortlink/v1/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParam) {
        userService.register(requestParam);
        return Results.success();
    }

    /**
     * 修改用户
     */
    @PutMapping("/api/shortlink/v1/user")
    public Result<Void> update(@RequestBody UserUpdateReqDTO requestParam) {
        userService.update(requestParam);
        return Results.success();
    }

    /**
     * 用户登陆
     */
    @PostMapping("/api/shortlink/v1/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParam) {
        return Results.success(userService.login(requestParam));
    }

    /**
     * 用户是否登陆
     */
    @GetMapping("/api/shortlink/v1/user/check-login")
    public Result<Boolean> checkLogin(@RequestParam("username") String username, @RequestParam("token") String token) {
        return Results.success(userService.checkLogin(username, token));
    }
}

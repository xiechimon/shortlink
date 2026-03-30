package com.xmon.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xmon.shortlink.admin.dao.entity.UserDO;
import com.xmon.shortlink.admin.dto.req.UserLoginReqDTO;
import com.xmon.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.xmon.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.xmon.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.xmon.shortlink.admin.dto.resp.UserRespDTO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户返回实体
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 判断用户名是否可用
     *
     * @param username 用户名
     * @return 可用返回true，已存在返回false
     */
    Boolean isUsernameAvailable(String username);

    /**
     * 用户注册
     *
     * @param requestParam 注册请求参数
     */
    void register(UserRegisterReqDTO requestParam);

    /**
     * 修改用户
     *
     * @param requestParam 修改用户请求参数
     */
    void update(UserUpdateReqDTO requestParam);

    /**
     * 用户登陆
     *
     * @param requestParam 用户登录请求参数
     * @return 用户登陆返回参数
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    /**
     * 检查用户是否登录
     *
     * @param token 用户登录token
     * @return 已登录返回true，未登录返回false
     */
    Boolean checkLogin(String username, String token);

    /**
     * 退出登陆
     *
     * @param username 用户名
     * @param token    用户登陆token
     */
    void logout(String username, String token);
}

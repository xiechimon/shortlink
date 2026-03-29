package com.xmon.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xmon.shortlink.admin.dao.entity.UserDO;
import com.xmon.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.xmon.shortlink.admin.dto.req.UserUpdateReqDTO;
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
}

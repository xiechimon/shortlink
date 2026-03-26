package com.xmon.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmon.shortlink.admin.common.convention.exception.ClientException;
import com.xmon.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.xmon.shortlink.admin.dao.entity.UserDO;
import com.xmon.shortlink.admin.dao.mapper.UserMapper;
import com.xmon.shortlink.admin.dto.resp.UserRespDTO;
import com.xmon.shortlink.admin.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 用户接口实现层
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Override
    public UserRespDTO getUserByUsername(String username) {
        // select * from t_user where username = ?
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        // 从数据库拿一条数据
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NOT_FOUND);
        }
        UserRespDTO result = new UserRespDTO();

        // 将do转dto
        BeanUtils.copyProperties(userDO, result);
        return result;
    }
}

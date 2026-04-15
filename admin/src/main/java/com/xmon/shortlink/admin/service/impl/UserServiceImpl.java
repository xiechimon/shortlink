package com.xmon.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmon.shortlink.admin.common.convention.exception.ClientException;
import com.xmon.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.xmon.shortlink.admin.dao.entity.UserDO;
import com.xmon.shortlink.admin.dao.mapper.UserMapper;
import com.xmon.shortlink.admin.dto.req.UserLoginReqDTO;
import com.xmon.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.xmon.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.xmon.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.xmon.shortlink.admin.dto.resp.UserRespDTO;
import com.xmon.shortlink.admin.service.GroupService;
import com.xmon.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.xmon.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.xmon.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;
import static com.xmon.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_TIMEOUT;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private static final String DEFAULT_GROUP_NAME = "默认分组";

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final GroupService groupService;

    @Override
    public UserRespDTO getUserByUsername(String username) {
        // select * from t_user where username = ?
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getUsername, username);
        // 从数据库拿一条数据
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        UserRespDTO result = new UserRespDTO();

        // 将do转dto
        BeanUtils.copyProperties(userDO, result);
        return result;
    }

    @Override
    public Boolean isUsernameAvailable(String username) {
        return !userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterReqDTO requestParam) {
        String username = requestParam.getUsername();

        // 1. 先通过布隆过滤器判断用户名是否存在
        if (!isUsernameAvailable(username)) {
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + username);
        // 2. 获取分布式锁，防止并发注册同一用户名
        if (!lock.tryLock()) {
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }

        try {
            // 3. 获取到锁后，再次检查用户名是否存在，防止并发注册同一用户名
            if (!isUsernameAvailable(username)) {
                throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
            }

            // 4. 插入数据库
            int inserted = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
            if (inserted < 1) {
                throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
            }

            // 5. 初始化默认分组，保证新用户注册后可直接创建短链接
            groupService.saveGroup(username, DEFAULT_GROUP_NAME);

            // 6. 注册成功后，将用户名添加到布隆过滤器中，防止缓存穿透
            userRegisterCachePenetrationBloomFilter.add(username);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
        // TODO 验证当前用户名是否为登陆用户（Gateway）
        String username = requestParam.getUsername();
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, username);
        baseMapper.update(BeanUtil.toBean(requestParam, UserDO.class), updateWrapper);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        String username = requestParam.getUsername();
        String password = requestParam.getPassword();

        // 检查是否已登录
        String pattern = USER_LOGIN_KEY + username + ":*";
        Set<String> existingKeys = stringRedisTemplate.keys(pattern);
        if (!existingKeys.isEmpty()) {
            throw new ClientException("用户已登录");
        }

        // 校验用户名密码
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username)
                .eq(UserDO::getPassword, password)
                .eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException("用户不存在");
        }

        // 生成新 token
        String token = UUID.randomUUID().toString();
        String key = USER_LOGIN_KEY + username + ":" + token;
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(userDO), USER_LOGIN_TIMEOUT, TimeUnit.MINUTES);
        return new UserLoginRespDTO(token);
    }

    @Override
    public Boolean checkLogin(String username, String token) {
        return stringRedisTemplate.hasKey(USER_LOGIN_KEY + username + ":" + token);
    }

    @Override
    public void logout(String username, String token) {
        if (checkLogin(username, token)) {
            stringRedisTemplate.delete(USER_LOGIN_KEY + username + ":" + token);
            return;
        }

        throw new ClientException("用户Token不存在或用户未登录");
    }
}

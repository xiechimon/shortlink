package com.xmon.shortlink.admin.config;

import com.xmon.shortlink.admin.dao.entity.UserDO;
import com.xmon.shortlink.admin.dao.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 布隆过滤器初始化
 */
@Component
@RequiredArgsConstructor
public class BloomFilterInitRunner implements ApplicationRunner {

    private final UserMapper userMapper;
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    @Override
    public void run(ApplicationArguments args) {
        userMapper.selectList(null).stream()
                .map(UserDO::getUsername)
                .forEach(userRegisterCachePenetrationBloomFilter::add);
    }
}

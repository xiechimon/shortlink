package com.xmon.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmon.shortlink.admin.dao.entity.GroupDO;
import com.xmon.shortlink.admin.dao.mapper.GroupMapper;
import com.xmon.shortlink.admin.service.GroupService;
import com.xmon.shortlink.admin.toolkit.RandomStringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短链接分组接口实现层
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    @Override
    public void saveGroup(String groupName) {
        String gid;
        do {
            gid = RandomStringUtil.generate();
        } while (!hasGid(gid));

        GroupDO groupDO = GroupDO.builder()
                .gid(gid)
                .sortOrder(0)
                .name(groupName)
                .build();
        baseMapper.insert(groupDO);
    }

    /**
     * 判断 gid 是否已存在
     */
    private boolean hasGid(String gid) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                // TODO 设置用户名
                .eq(GroupDO::getName, null);
        GroupDO hasGroupFlag = baseMapper.selectOne(queryWrapper);

        return hasGroupFlag == null;
    }
}

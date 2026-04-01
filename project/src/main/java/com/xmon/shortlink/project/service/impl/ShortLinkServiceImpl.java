package com.xmon.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmon.shortlink.project.dao.entity.ShortLinkDO;
import com.xmon.shortlink.project.dao.mapper.ShortLinkMapper;
import com.xmon.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 短链接接口实现层
 */
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

}

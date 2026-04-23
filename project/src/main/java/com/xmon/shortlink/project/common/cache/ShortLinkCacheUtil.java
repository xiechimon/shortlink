package com.xmon.shortlink.project.common.cache;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.xmon.shortlink.project.common.constant.ShortLinkCacheConstant;
import com.xmon.shortlink.project.common.enums.ValidDateTypeEnum;
import com.xmon.shortlink.project.dao.entity.ShortLinkDO;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * 短链接缓存工具类
 */
public final class ShortLinkCacheUtil {

    private ShortLinkCacheUtil() {
    }

    public static boolean isExpired(ShortLinkDO shortLinkDO) {
        return Objects.equals(shortLinkDO.getValidDateType(), ValidDateTypeEnum.CUSTOM.getType())
                && shortLinkDO.getValidDate() != null
                && shortLinkDO.getValidDate().before(new Date());
    }

    /**
     * 获取短链接缓存有效期（毫秒）。
     * 传入过期时间为 null 时，使用默认缓存时长。
     */
    public static long getLinkCacheValidTime(Date validDate) {
        return Optional.ofNullable(validDate)
                .map(each -> Math.max(DateUtil.between(new Date(), each, DateUnit.MS), 0L))
                .orElse(ShortLinkCacheConstant.DEFAULT_CACHE_VALID_TIME);
    }
}

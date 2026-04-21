package com.xmon.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmon.shortlink.project.dto.req.RecycleBinPageReqDTO;
import com.xmon.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.xmon.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.xmon.shortlink.project.dto.resp.ShortLinkPageRespDTO;

/**
 * 回收站接口层
 */
public interface RecycleBinService {

    /**
     * 将短链接移至回收站
     *
     * @param requestParam 请求参数
     */
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);

    /**
     * 分页查询回收站短链接
     *
     * @param requestParam 分页查询请求参数（gidList 由 admin 层自动填充）
     * @return 返回回收站内短链接分页数据
     */
    IPage<ShortLinkPageRespDTO> pageRecycleBin(RecycleBinPageReqDTO requestParam);

    /**
     * 从回收站恢复短链接
     *
     * @param requestParam 恢复请求参数
     */
    void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam);
}

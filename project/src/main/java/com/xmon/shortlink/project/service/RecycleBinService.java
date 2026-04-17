package com.xmon.shortlink.project.service;

import com.xmon.shortlink.project.dto.req.RecycleBinSaveReqDTO;

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
}

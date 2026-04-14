package com.xmon.shortlink.admin.remote;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmon.shortlink.admin.common.convention.result.Result;
import com.xmon.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.xmon.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.xmon.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.xmon.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;

import java.util.HashMap;

/**
 * 管理端调用中台短链接能力的远程服务接口。
 */
public interface ShortLinkRemoteService {

    /**
     * 调用中台的短链接创建接口。
     */
    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO requestParam) {
        String resultBodyStr = HttpUtil.post("http://localhost:8001/api/shortlink/v1/create", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 调用中台分页接口查询短链接列表。
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("gid", requestParam.getGid());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        String resultPageStr = HttpUtil.get("http://localhost:8001/api/shortlink/v1/page", requestMap);

        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }
}

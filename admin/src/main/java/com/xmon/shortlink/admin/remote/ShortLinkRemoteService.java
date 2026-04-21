package com.xmon.shortlink.admin.remote;

import cn.hutool.http.HttpUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmon.shortlink.admin.common.convention.result.Result;
import com.xmon.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.xmon.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.xmon.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import com.xmon.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.xmon.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.xmon.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.xmon.shortlink.admin.remote.dto.req.RecycleBinPageReqDTO;
import com.xmon.shortlink.admin.remote.dto.req.RecycleBinRecoverReqDTO;
import com.xmon.shortlink.admin.remote.dto.req.RecycleBinSaveReqDTO;

import java.util.HashMap;
import java.util.List;

/**
 * 管理端调用中台短链接能力的远程服务接口。
 */
public interface ShortLinkRemoteService {

    String SHORT_LINK_SERVICE_BASE_URL = "http://localhost:8001/api/shortlink/v1";

    /**
     * 调用中台的短链接创建接口。
     */
    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO requestParam) {
        String resultBodyStr = HttpUtil.post(SHORT_LINK_SERVICE_BASE_URL + "/create", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 调用中台的短链接修改接口。
     */
    default Result<Void> updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        String resultBodyStr = HttpRequest.put(SHORT_LINK_SERVICE_BASE_URL + "/update")
                .body(JSON.toJSONString(requestParam))
                .execute()
                .body();
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
        String resultPageStr = HttpUtil.get(SHORT_LINK_SERVICE_BASE_URL + "/page", requestMap);

        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    /**
     * 查询短链接分组总量
     */
    default Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(List<String> requestParam) {
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestParam", requestParam);
        String resultPageStr = HttpUtil.get(SHORT_LINK_SERVICE_BASE_URL + "/count", requestMap);

        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    /**
     * 根据 URL 获取目标网站的标题
     */
    default Result<String> getTitleByUrl(String url) {
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("url", url);
        String resultStr = HttpUtil.get(SHORT_LINK_SERVICE_BASE_URL + "/title", requestMap);

        return JSON.parseObject(resultStr, new TypeReference<>() {
        });
    }

    /**
     * 根据 URL 获取目标网站的网页图标
     */
    default Result<String> getFaviconByUrl(String url) {
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("url", url);
        String resultStr = HttpUtil.get(SHORT_LINK_SERVICE_BASE_URL + "/favicon", requestMap);

        return JSON.parseObject(resultStr, new TypeReference<>() {
        });
    }

    /**
     * 将短链接移至回收站
     */
    default void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
        HttpUtil.post(SHORT_LINK_SERVICE_BASE_URL + "/recycle-bin/save", JSON.toJSONString(requestParam));
    }

    /**
     * 分页查询回收站短链接
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageRecycleBin(RecycleBinPageReqDTO requestParam) {
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("gidList", requestParam.getGidList());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        String resultPageStr = HttpUtil.get(SHORT_LINK_SERVICE_BASE_URL + "/recycle-bin/page", requestMap);

        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    /**
     * 从回收站恢复短链接
     */
    default void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam) {
        HttpUtil.post(SHORT_LINK_SERVICE_BASE_URL + "/recycle-bin/recover", JSON.toJSONString(requestParam));
    }
}

package com.xmon.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xmon.shortlink.project.dao.entity.ShortLinkDO;
import com.xmon.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.xmon.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.xmon.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.xmon.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.xmon.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.xmon.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLinkDO> {

    /**
     * 创建短链接
     *
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建信息
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);

    /**
     * 短链接跳转模板
     *
     * @param domain 请求域名
     * @param shortUri 短链接后缀
     * @param request Http 请求
     * @param response Http 响应
     */
    void restoreUrl(String domain, String shortUri, HttpServletRequest request, HttpServletResponse response);

    /**
     * 修改短链接
     *
     * @param requestParam 修改短链接请求参数
     */
    void updateShortLink(ShortLinkUpdateReqDTO requestParam);

    /**
     * 分页查询短链接
     * @param requestParam 分页查询短链接请求参数
     * @return 短链接分页返回结果
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);

    /**
     * 查询短链接内分组数量
     * @param requestParam 查询短链接内分组数量请求参数
     * @return 查询短链接内分组数量相应
     */
    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);

    /**
     * 获取目标网站的网页标题
     *
     * @param url 目标链接
     * @return 网页标题；若无法获取则返回 null
     */
    String getTitleByUrl(String url);

    /**
     * 获取目标网站的网页图标
     *
     * @param url 目标链接
     * @return 网页图标绝对地址；若无法获取则返回回退地址
     */
    String getFaviconByUrl(String url);
}

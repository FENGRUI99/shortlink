package com.fengrui.shortlink.admin.remote;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fengrui.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.fengrui.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.fengrui.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.fengrui.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.fengrui.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.fengrui.shortlink.common.convention.result.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短链接中台远程调用服务\
 */
public interface ShortLinkRemoteService {
    /**
     * 创建短链接
     *
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建响应
     */
    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO requestParam) {
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/project/links", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 分页查询短链接
     *
     * @param requestParam 分页短链接请求参数
     * @return 查询短链接响应
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("gid", requestParam.getGid());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        String requestJson = JSON.toJSONString(requestMap);
        HttpResponse httpResponse = HttpUtil.createPost("http://127.0.0.1:8001/api/short-link/project/links/page")
                .contentType(ContentType.JSON.toString())
                .body(requestJson)
                .execute();
        String resultPageStr = httpResponse.body();
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    default Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(List<String> gids) {
        String gidsParam = String.join(",", gids);
        String url = String.format("http://127.0.0.1:8001/api/short-link/project/links/count?gids=%s", gidsParam);
        HttpResponse httpResponse = HttpUtil.createGet(url)
                .contentType(ContentType.JSON.toString())
                .execute();
        String resultPageStr = httpResponse.body();
        return JSON.parseObject(resultPageStr, new TypeReference<>() {});
    }

}

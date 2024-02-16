package com.fengrui.shortlink.admin.remote;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengrui.shortlink.admin.dto.req.RecycleBinRecoverReqDTO;
import com.fengrui.shortlink.admin.remote.dto.req.*;
import com.fengrui.shortlink.admin.remote.dto.resp.*;
import com.fengrui.shortlink.common.convention.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 短链接中台远程调用服务
 */
@FeignClient(value = "short-link-project", url = "${aggregation.remote-url:}")
public interface ShortLinkActualRemoteService {

    /**
     * 创建短链接
     *
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建响应
     */
    @PostMapping("/api/short-link/project/links")
    Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam);

    /**
     * 批量创建短链接
     *
     * @param requestParam 批量创建短链接请求参数
     * @return 短链接批量创建响应
     */
    @PostMapping("/api/short-link/project/links/batch")
    Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam);

    /**
     * 修改短链接
     *
     * @param requestParam 修改短链接请求参数
     */
    @PostMapping("/api/short-link/project/links/update")
    void updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam);

    /**
     * 分页查询短链接
     *
     * @param requestParam 分页短链接请求参数
     * @return 查询短链接响应
     */
    @PostMapping("/api/short-link/project/links/page")
    Result<Page<ShortLinkPageRespDTO>> pageShortLink(@RequestBody ShortLinkPageReqDTO requestParam);

    /**
     * 查询分组短链接总量
     *
     * @param gids 分组短链接总量请求参数
     * @return 查询分组短链接总量响应
     */
    @GetMapping("/api/short-link/project/links/count")
    Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("gids") List<String> gids);

    /**
     * 根据 URL 获取标题
     *
     * @param url 目标网站地址
     * @return 网站标题
     */
    @GetMapping("/api/short-link/project/title?url=")
    Result<String> getTitleByUrl(@RequestParam("url") String url);

    /**
     * 保存回收站
     *
     * @param requestParam 请求参数
     */
    @PostMapping("/api/short-link/project/recycle-bin/save")
    void saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam);

    /**
     * 分页查询回收站短链接
     *
     * @param requestParam 分页短链接请求参数
     * @return 查询短链接响应
     */
    @PostMapping("/api/short-link/project/recycle-bin/page")
    Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink(@RequestBody ShortLinkRecycleBinPageReqDTO requestParam);

    /**
     * 恢复短链接
     *
     * @param requestParam 短链接恢复请求参数
     */
    @PostMapping("/api/short-link/project/recycle-bin/recover")
    void recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam);

    /**
     * 移除短链接
     *
     * @param requestParam 短链接移除请求参数
     */
    @PostMapping("/api/short-link/project/recycle-bin/remove")
    void removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam);


    /**
     * 访问单个短链接指定时间内监控数据
     *
     * @param requestParam 访问短链接监控请求参数
     * @return 短链接监控信息
     */
    @PostMapping("/api/short-link/project/stats")
    Result<ShortLinkStatsRespDTO> oneShortLinkStats(@RequestBody ShortLinkStatsReqDTO requestParam);

    /**
     * 访问分组短链接指定时间内监控数据
     *
     * @param requestParam 访分组问短链接监控请求参数
     * @return 分组短链接监控信息
     */
    @PostMapping("/api/short-link/project/stats/group")
    Result<ShortLinkStatsRespDTO> groupShortLinkStats(@RequestBody ShortLinkGroupStatsReqDTO requestParam);

    /**
     * 分页访问单个短链接指定时间内监控日志数据
     *
     * @param requestParam 访问短链接监控访问记录请求参数
     * @return 短链接监控访问记录信息
     */
    @PostMapping("/api/short-link/project/stats/access-record")
    Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(@RequestBody ShortLinkStatsAccessRecordReqDTO requestParam);

    /**
     * 分页访问分组短链接指定时间内监控访问记录数据
     *
     * @param requestParam 访问分组短链接监控访问记录请求参数
     * @return 分组短链接监控访问记录信息
     */
    @PostMapping("/api/short-link/project/stats/access-record/group")
    Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(@RequestBody ShortLinkGroupStatsAccessRecordReqDTO requestParam);
}

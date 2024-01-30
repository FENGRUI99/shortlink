package com.fengrui.shortlink.admin.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fengrui.shortlink.admin.remote.ShortLinkRemoteService;
import com.fengrui.shortlink.admin.remote.dto.req.ShortLinkGroupStatsReqDTO;
import com.fengrui.shortlink.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.fengrui.shortlink.admin.remote.dto.req.ShortLinkStatsReqDTO;
import com.fengrui.shortlink.admin.remote.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.fengrui.shortlink.admin.remote.dto.resp.ShortLinkStatsRespDTO;
import com.fengrui.shortlink.common.convention.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@Tag(name = "短链接监控管理")
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    /**
     * 后续重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @PostMapping("/api/short-link/admin/stats")
    @Operation(summary = "单个短链接指定时间内监控数据")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(@RequestBody ShortLinkStatsReqDTO requestParam) {
        return shortLinkRemoteService.oneShortLinkStats(requestParam);
    }

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @PostMapping("/api/short-link/admin/admin/stats/group")
    @Operation(summary = "访问分组短链接指定时间内监控数据")
    public Result<ShortLinkStatsRespDTO> groupShortLinkStats(@RequestBody ShortLinkGroupStatsReqDTO requestParam) {
        return shortLinkRemoteService.groupShortLinkStats(requestParam);
    }

    /**
     * 分页访问单个短链接指定时间内监控日志数据
     */
    @PostMapping("/api/short-link/admin/stats/access-record")
    @Operation(summary = "分页访问单个短链接指定时间内监控日志数据")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(@RequestBody ShortLinkStatsAccessRecordReqDTO requestParam) {
        return shortLinkRemoteService.shortLinkStatsAccessRecord(requestParam);
    }

}

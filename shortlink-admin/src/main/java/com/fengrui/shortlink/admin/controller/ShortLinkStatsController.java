package com.fengrui.shortlink.admin.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengrui.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.fengrui.shortlink.admin.remote.ShortLinkRemoteService_bak;
import com.fengrui.shortlink.admin.remote.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.fengrui.shortlink.admin.remote.dto.req.ShortLinkGroupStatsReqDTO;
import com.fengrui.shortlink.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.fengrui.shortlink.admin.remote.dto.req.ShortLinkStatsReqDTO;
import com.fengrui.shortlink.admin.remote.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.fengrui.shortlink.admin.remote.dto.resp.ShortLinkStatsRespDTO;
import com.fengrui.shortlink.common.convention.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @PostMapping("/api/short-link/admin/stats")
    @Operation(summary = "单个短链接指定时间内监控数据")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(@RequestBody ShortLinkStatsReqDTO requestParam) {
        return shortLinkActualRemoteService.oneShortLinkStats(requestParam);
    }

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @PostMapping("/api/short-link/admin/admin/stats/group")
    @Operation(summary = "访问分组短链接指定时间内监控数据")
    public Result<ShortLinkStatsRespDTO> groupShortLinkStats(@RequestBody ShortLinkGroupStatsReqDTO requestParam) {
        return shortLinkActualRemoteService.groupShortLinkStats(requestParam);
    }

    /**
     * 分页访问单个短链接指定时间内监控日志数据
     */
    @PostMapping("/api/short-link/admin/stats/access-record")
    @Operation(summary = "分页访问单个短链接指定时间内监控日志数据")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(@RequestBody ShortLinkStatsAccessRecordReqDTO requestParam) {
        return shortLinkActualRemoteService.shortLinkStatsAccessRecord(requestParam);
    }

    /**
     * 分页访问分组短链接指定时间内访问记录监控数据
     */
    @PostMapping("/api/short-link/admin/stats/access-record/group")
    @Operation(summary = "分页访问分组短链接指定时间内访问监控日志数据")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(@RequestBody ShortLinkGroupStatsAccessRecordReqDTO requestParam) {
        return shortLinkActualRemoteService.groupShortLinkStatsAccessRecord(requestParam);
    }

}

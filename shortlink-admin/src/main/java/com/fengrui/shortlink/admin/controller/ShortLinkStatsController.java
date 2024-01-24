package com.fengrui.shortlink.admin.controller;


import com.fengrui.shortlink.admin.remote.ShortLinkRemoteService;
import com.fengrui.shortlink.admin.remote.dto.req.ShortLinkStatsReqDTO;
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

}

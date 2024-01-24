package com.fengrui.shortlink.project.controller;

import com.fengrui.shortlink.common.convention.result.Result;
import com.fengrui.shortlink.common.convention.result.Results;
import com.fengrui.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.fengrui.shortlink.project.dto.resp.ShortLinkStatsRespDTO;
import com.fengrui.shortlink.project.service.ShortLinkStatsService;
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
@Tag(name = "短链接统计管理")
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkStatsService shortLinkStatsService;

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @PostMapping("/api/short-link/project/stats")
    @Operation(summary = "单个短链接指定时间内监控数据")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(@RequestBody ShortLinkStatsReqDTO requestParam) {
        return Results.success(shortLinkStatsService.oneShortLinkStats(requestParam));
    }
}

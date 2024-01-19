package com.fengrui.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fengrui.shortlink.common.convention.result.Result;
import com.fengrui.shortlink.common.convention.result.Results;
import com.fengrui.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.fengrui.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.fengrui.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.fengrui.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.fengrui.shortlink.project.service.RecycleBinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站管理控制层
 */
@Tag(name = "回收站管理")
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    /**
     * 保存回收站
     */
    @PostMapping("/api/short-link/project/recycle-bin/save")
    @Operation(summary = "移到回收站")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        recycleBinService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询回收站短链接
     */
    @GetMapping("/api/short-link/project/recycle-bin/page")
    @Operation(summary = "分页查询回收站")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        return Results.success(recycleBinService.pageShortLink(requestParam));
    }

    /**
     * 恢复短链接
     */
    @PostMapping("/api/short-link/project/recycle-bin/recover")
    @Operation(summary = "恢复短链接")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam) {
        recycleBinService.recoverRecycleBin(requestParam);
        return Results.success();
    }

}

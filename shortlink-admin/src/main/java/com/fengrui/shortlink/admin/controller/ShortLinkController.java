package com.fengrui.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengrui.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.fengrui.shortlink.admin.remote.ShortLinkRemoteService_bak;
import com.fengrui.shortlink.admin.remote.dto.req.ShortLinkBatchCreateReqDTO;
import com.fengrui.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.fengrui.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.fengrui.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import com.fengrui.shortlink.admin.remote.dto.resp.*;
import com.fengrui.shortlink.admin.toolkit.EasyExcelWebUtil;
import com.fengrui.shortlink.common.convention.result.Result;
import com.fengrui.shortlink.common.convention.result.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "短链接链接管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin")
public class ShortLinkController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 创建短链接
     */
    @PostMapping("/links")
    @Operation(summary = "创建短链接")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return shortLinkActualRemoteService.createShortLink(requestParam);
    }

    /**
     * 批量创建短链接
     */
    @SneakyThrows
    @PostMapping("/links/batch")
    public void batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam, HttpServletResponse response) {
        Result<ShortLinkBatchCreateRespDTO> shortLinkBatchCreateRespDTOResult = shortLinkActualRemoteService.batchCreateShortLink(requestParam);
        if (shortLinkBatchCreateRespDTOResult.isSuccess()) {
            List<ShortLinkBaseInfoRespDTO> baseLinkInfos = shortLinkBatchCreateRespDTOResult.getData().getBaseLinkInfos();
            EasyExcelWebUtil.write(response, "批量创建短链接-SaaS短链接系统", ShortLinkBaseInfoRespDTO.class, baseLinkInfos);
        }
    }

    /**
     * 分页查询短链接
     */
    @PostMapping("/links/page")
    @Operation(summary = "短链接分页查询")
    public Result<Page<ShortLinkPageRespDTO>> pageShortLink(@RequestBody ShortLinkPageReqDTO requestParam) {
        return shortLinkActualRemoteService.pageShortLink(requestParam);
    }

    /**
     * 修改短链接
     */
    @PostMapping("/links/update")
    @Operation(summary = "短链接修改")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO shortLinkUpdateReqDTO) {
        shortLinkActualRemoteService.updateShortLink(shortLinkUpdateReqDTO);
        return Results.success();
    }

}

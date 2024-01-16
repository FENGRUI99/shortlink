package com.fengrui.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fengrui.shortlink.admin.remote.ShortLinkRemoteService;
import com.fengrui.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.fengrui.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.fengrui.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import com.fengrui.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.fengrui.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.fengrui.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.fengrui.shortlink.common.convention.result.Result;
import com.fengrui.shortlink.common.convention.result.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "短链接链接管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin")
public class ShortLinkController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 创建短链接
     */
    @PostMapping("/links")
    @Operation(summary = "创建短链接")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return shortLinkRemoteService.createShortLink(requestParam);
    }

    /**
     * 分页查询短链接
     */
    @PostMapping("/links/page")
    @Operation(summary = "短链接分页查询")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(@RequestBody ShortLinkPageReqDTO requestParam) {
        return shortLinkRemoteService.pageShortLink(requestParam);
    }

    /**
     * 修改短链接
     */
    @PostMapping("/links/update")
    @Operation(summary = "短链接修改")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO shortLinkUpdateReqDTO) {
        shortLinkRemoteService.updateShortLink(shortLinkUpdateReqDTO);
        return Results.success();
    }

}

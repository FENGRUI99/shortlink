package com.fengrui.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fengrui.shortlink.common.convention.result.Result;
import com.fengrui.shortlink.common.convention.result.Results;
import com.fengrui.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.fengrui.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.fengrui.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.fengrui.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.fengrui.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.fengrui.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.fengrui.shortlink.project.service.ShortLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接控制层
 */
@Tag(name = "短链接项目管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/project")
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 创建短链接
     */
    @PostMapping("/links")
    @Operation(summary = "新增短链接")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        return Results.success(shortLinkService.createShortLink(shortLinkCreateReqDTO));
    }

    @PostMapping("/links/page")
    @Operation(summary = "分页查询短链接")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(@RequestBody ShortLinkPageReqDTO shortLinkPageReqDTO){
        return Results.success(shortLinkService.pageShortLink(shortLinkPageReqDTO));
    }

    /**
     * 查询短链接分组内数量
     */
    @GetMapping("/links/count")
    @Operation(summary = "查询分组内短链接数量")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("gids") List<String> gids) {
        return Results.success(shortLinkService.listGroupShortLinkCount(gids));
    }

    /**
     * 修改短链接
     */
    @PostMapping("/links/update")
    @Operation(summary = "修改短链接")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO shortLinkUpdateReqDTO) {
        shortLinkService.updateShortLink(shortLinkUpdateReqDTO);
        return Results.success();
    }
}

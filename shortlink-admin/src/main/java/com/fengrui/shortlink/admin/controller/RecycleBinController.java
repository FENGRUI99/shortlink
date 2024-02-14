package com.fengrui.shortlink.admin.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fengrui.shortlink.admin.dto.req.RecycleBinRecoverReqDTO;
import com.fengrui.shortlink.admin.remote.ShortLinkRemoteService;
import com.fengrui.shortlink.admin.remote.dto.req.RecycleBinRemoveReqDTO;
import com.fengrui.shortlink.admin.remote.dto.req.RecycleBinSaveReqDTO;
import com.fengrui.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.fengrui.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.fengrui.shortlink.admin.service.GroupService;
import com.fengrui.shortlink.common.convention.result.Result;
import com.fengrui.shortlink.common.convention.result.Results;
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
@RestController
@RequiredArgsConstructor
@Tag(name = "回收站管理")
public class RecycleBinController {

    /**
     * 后续重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    private final GroupService groupService;

    /**
     * 保存回收站
     */
    @PostMapping("/api/short-link/admin/recycle-bin/save")
    @Operation(summary = "移到回收站")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        shortLinkRemoteService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询回收站短链接
     */
    @PostMapping("/api/short-link/admin/recycle-bin/page")
    @Operation(summary = "分页查询回收站")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(@RequestBody ShortLinkRecycleBinPageReqDTO requestParam) {
        requestParam.setGidList(groupService.getGids());
        return shortLinkRemoteService.pageRecycleBinShortLink(requestParam);
    }

    /**
     * 恢复短链接
     */
    @PostMapping("/api/short-link/admin/recycle-bin/recover")
    @Operation(summary = "恢复短链接")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam) {
        shortLinkRemoteService.recoverRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 移除短链接
     */
    @PostMapping("/api/short-link/admin/recycle-bin/remove")
    @Operation(summary = "彻底删除短链接")
    public Result<Void> removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam) {
        shortLinkRemoteService.removeRecycleBin(requestParam);
        return Results.success();
    }

}

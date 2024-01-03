package com.fengrui.shortlink.admin.controller;

import com.fengrui.shortlink.admin.common.convention.result.Result;
import com.fengrui.shortlink.admin.common.convention.result.Results;
import com.fengrui.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.fengrui.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.fengrui.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.fengrui.shortlink.admin.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接分组控制层
 */
@Tag(name = "短链接分组管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin")
public class GroupController {

    private final GroupService groupService;

    /**
     * 新增短链接分组
     * @param shortLinkGroupSaveReqDTO
     * @return
     */
    @PostMapping("/groups")
    @Operation(summary = "新增分组")
    public Result<Void> save(@RequestBody ShortLinkGroupSaveReqDTO shortLinkGroupSaveReqDTO){
        groupService.saveGroup(shortLinkGroupSaveReqDTO.getName());
        return Results.success();
    }

    /**
     * 查询短链接分组集合
     */
    @GetMapping("/groups")
    @Operation(summary = "查询分组")
    public Result<List<ShortLinkGroupRespDTO>> listGroup() {
        return Results.success(groupService.listGroup());
    }

    /**
     * 修改短链接分组
     * @param shortLinkGroupUpdateReqDTO
     * @return
     */
    @PutMapping("/groups")
    @Operation(summary = "修改分组")
    public Result<Void>  update(@RequestBody ShortLinkGroupUpdateReqDTO shortLinkGroupUpdateReqDTO){
        groupService.updateGroup(shortLinkGroupUpdateReqDTO);
        return Results.success();
    }

    /**
     * 删除短链接分组
     * @param gid
     * @return
     */
    @DeleteMapping("/groups")
    @Operation(summary = "删除分组")
    public Result<Void> delete(@RequestParam String gid){
        groupService.deleteGroup(gid);
        return Results.success();
    }

}

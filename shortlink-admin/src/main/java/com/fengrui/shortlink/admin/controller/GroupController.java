package com.fengrui.shortlink.admin.controller;

import com.fengrui.shortlink.admin.common.convention.result.Result;
import com.fengrui.shortlink.admin.common.convention.result.Results;
import com.fengrui.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.fengrui.shortlink.admin.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接分组控制层
 */
@Tag(name = "短链接分组管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin")
public class GroupController {

    private final GroupService groupService;
    @PostMapping("/groups")
    @Operation(summary = "新增分组")
    public Result<Void> save(@RequestBody ShortLinkGroupSaveReqDTO shortLinkGroupSaveReqDTO){
        groupService.saveGroup(shortLinkGroupSaveReqDTO.getName());
        return Results.success();
    }
}

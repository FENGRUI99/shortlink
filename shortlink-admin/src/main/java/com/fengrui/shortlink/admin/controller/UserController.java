package com.fengrui.shortlink.admin.controller;

import com.fengrui.shortlink.admin.common.convention.result.Result;
import com.fengrui.shortlink.admin.common.convention.result.Results;
import com.fengrui.shortlink.admin.dto.resp.UserRespDTO;
import com.fengrui.shortlink.admin.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@Tag(name = "用户管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin/users")
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名查询用户信息
     */
    @GetMapping
    @Operation(summary = "根据用户名查找")
    public Result<UserRespDTO> getUserByUsername(@RequestParam String username){
        UserRespDTO result = userService.getUserByUsername(username);
        return Results.success(result);
    }
}

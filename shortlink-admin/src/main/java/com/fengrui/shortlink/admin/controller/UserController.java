package com.fengrui.shortlink.admin.controller;

import com.fengrui.shortlink.admin.common.convention.result.Result;
import com.fengrui.shortlink.admin.common.convention.result.Results;
import com.fengrui.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.fengrui.shortlink.admin.dto.resp.UserActualRespDTO;
import com.fengrui.shortlink.admin.dto.resp.UserRespDTO;
import com.fengrui.shortlink.admin.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin")
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("/users")
    @Operation(summary = "根据用户名查询脱敏用户信息")
    public Result<UserRespDTO> getUserByUsername(@RequestParam("username") String username){
        return Results.success(userService.getUserByUsername(username));
    }

    /**
     * 根据用户名查询无脱敏用户信息
     */
    @GetMapping("/actual/users")
    @Operation(summary = "根据用户名查询无脱敏用户信息")
    public Result<UserActualRespDTO> getActualUserByUsername(@RequestParam("username") String username){
        return Results.success(userService.getActualUserByUsername(username));
    }

    /**
     * 判断用户名是否存在
     */
    @GetMapping("/users/has-username")
    @Operation(summary = "判断用户是否存在")
    public Result<Boolean> hasUsername(@RequestParam("username") String username){
        return Results.success(userService.hasUsername(username));
    }

    /**
     * 注册用户
     */
    @PostMapping("/users")
    @Operation(summary = "注册用户")
    public Result<Void> register(@RequestBody UserRegisterReqDTO registerReqDTO){
        userService.register(registerReqDTO);
        return Results.success();
    }
}

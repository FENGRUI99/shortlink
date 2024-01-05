package com.fengrui.shortlink.admin.controller;

import com.fengrui.shortlink.common.convention.result.Result;
import com.fengrui.shortlink.common.convention.result.Results;
import com.fengrui.shortlink.admin.service.BloomFilterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin/clear")
public class test {

    private final BloomFilterService bloomFilterService;

    @GetMapping
    @Operation(summary = "清空布隆过滤器缓存")
    public Result<Void> getUserByUsername(){
        bloomFilterService.filterClear();
        return Results.success();
    }
}

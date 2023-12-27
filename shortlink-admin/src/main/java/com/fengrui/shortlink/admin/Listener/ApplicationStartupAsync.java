package com.fengrui.shortlink.admin.Listener;

import com.fengrui.shortlink.admin.service.impl.BloomFilterServiceImpl;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 用于初始化布隆过滤器数据
 * 打印了应用的访问URL和Swagger文档的访问地址到日志中
 */

@Component
public class ApplicationStartupAsync implements ApplicationListener<ApplicationReadyEvent> {
    private final BloomFilterServiceImpl bloomFilterService;

    public ApplicationStartupAsync(BloomFilterServiceImpl bloomFilterService) {
        this.bloomFilterService = bloomFilterService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        bloomFilterService.importDataToBloomFilter();
    }
}

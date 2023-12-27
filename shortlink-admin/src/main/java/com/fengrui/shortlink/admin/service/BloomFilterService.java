package com.fengrui.shortlink.admin.service;

/**
 * 布隆过滤器异步导入数据
 */
public interface BloomFilterService {
    public void importDataToBloomFilter();

    /**
     * 仅测试使用
     */
    public void filterClear();
}

package com.fengrui.shortlink.project.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务配置类
 */
@Configuration
@EnableScheduling
@Slf4j
public class ScheduleTaskConfig {

    @Autowired
    private RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    @Autowired
    private RedissonClient redissonClient;

    //TODO 监控布隆过滤器并扩容 思路：https://nageoffer.com/pages/6575aa/#%E9%97%AE%E9%A2%98%E8%AF%A6%E8%A7%A3
//    @Scheduled(fixedRate = 5000)
//    public void copyRedisBloomFilter1(){
//        double currentFpp = shortUriCreateCachePenetrationBloomFilter.getFalseProbability();
//        long currentCapacity = shortUriCreateCachePenetrationBloomFilter.count();
//        long size = shortUriCreateCachePenetrationBloomFilter.getSize();
//        String name = shortUriCreateCachePenetrationBloomFilter.getName();
//
//        if (currentFpp >= 0.001 && currentCapacity >= size * 0.9) {
//            long newCapacity = size * 2 + 1;
//            RBloomFilter<String> newBloomFilter = redissonClient.getBloomFilter("newBloomFilter");
//            newBloomFilter.tryInit(newCapacity, 0.001);
//            shortUriCreateCachePenetrationBloomFilter.delete();
//            newBloomFilter.rename(name);
//        }
//    }
}

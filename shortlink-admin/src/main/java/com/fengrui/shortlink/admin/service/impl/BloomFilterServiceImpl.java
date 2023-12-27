package com.fengrui.shortlink.admin.service.impl;

import com.fengrui.shortlink.admin.common.convention.errorcode.BaseErrorCode;
import com.fengrui.shortlink.admin.common.convention.exception.ServiceException;
import com.fengrui.shortlink.admin.dao.entity.UserDO;
import com.fengrui.shortlink.admin.service.BloomFilterService;
import com.fengrui.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BloomFilterServiceImpl implements BloomFilterService {

    private final UserService userService;

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    @Async
    public void importDataToBloomFilter() {
        try {
            long pageNum = 1;
            long pageSize = 1000;
            while (true){
                List<UserDO> userDOS = userService.queryUsernameByPage(pageNum, pageSize);
                log.info("Processed page {}, pageSize {}. Retrieved {} users.", pageNum, pageSize, userDOS.size());
                if (userDOS.isEmpty()) {
                    break;
                }
                for (UserDO user : userDOS) {
                    userRegisterCachePenetrationBloomFilter.add(user.getUsername());
                }
                pageNum++;
            }
        } catch (Exception e) {
            throw new ServiceException(BaseErrorCode.SERVICE_BLOOM_FILTER_IMPORT_ERROR);
        }
    }
}

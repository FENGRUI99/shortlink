package com.fengrui.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengrui.shortlink.admin.common.biz.user.UserContext;
import com.fengrui.shortlink.admin.common.convention.exception.ServiceException;
import com.fengrui.shortlink.admin.dao.entity.GroupDO;
import com.fengrui.shortlink.admin.dao.mapper.GroupMapper;
import com.fengrui.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.fengrui.shortlink.admin.service.GroupService;
import com.fengrui.shortlink.admin.toolkit.DataConverter;
import com.fengrui.shortlink.admin.toolkit.RandomGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.fengrui.shortlink.admin.common.constant.RedisCacheConstant.LOCK_GROUP_CREATE_KEY;
import static com.fengrui.shortlink.admin.common.convention.errorcode.BaseErrorCode.SERVICE_MAX_GROUP_NUM;

/**
 * 短链接分组接口实现层
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
    @Value("${short-link.group.max-num}")
    private Integer groupMaxNum;

    private final RedissonClient redissonClient;

    @Override
    public void saveGroup(String username, String groupName) {
        RLock lock = redissonClient.getLock(LOCK_GROUP_CREATE_KEY + username);
        lock.lock();
        try{
            LambdaQueryWrapper<GroupDO> lambdaQueryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getUsername, username)
                    .eq(GroupDO::getDelFlag, 0);
            List<GroupDO> groupDOList = baseMapper.selectList(lambdaQueryWrapper);
            if (CollUtil.isNotEmpty(groupDOList) && groupDOList.size() >= groupMaxNum){
                throw new ServiceException(SERVICE_MAX_GROUP_NUM + ": " + groupMaxNum);
            }
            String gid;
            do {
               gid = RandomGenerator.generateRandom();
            } while (hasGid(username, gid));
            GroupDO groupDO = GroupDO.builder()
                    .gid(gid)
                    .username(username)
                    .sortOrder(0)
                    .name(groupName)
                    .build();
            baseMapper.insert(groupDO);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void saveGroup(String groupName) {
        saveGroup(UserContext.getUsername(), groupName);
    }

    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0)
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
        List<ShortLinkGroupRespDTO> shortLinkGroupRespDTOS = DataConverter.INSTANCE.toGroupRespDTOList(baseMapper.selectList(queryWrapper));
        // TODO 分组下短链接数量
        return shortLinkGroupRespDTOS;
    }

    private boolean hasGid(String username, String gid) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, Optional.ofNullable(username).orElse(UserContext.getUsername()));
        GroupDO hasGroupFlag = baseMapper.selectOne(queryWrapper);
        return hasGroupFlag != null;
    }
}

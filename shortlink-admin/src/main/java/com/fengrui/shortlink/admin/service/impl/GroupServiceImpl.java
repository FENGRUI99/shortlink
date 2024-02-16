package com.fengrui.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengrui.shortlink.admin.common.biz.user.UserContext;
import com.fengrui.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.fengrui.shortlink.admin.remote.ShortLinkRemoteService_bak;
import com.fengrui.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.fengrui.shortlink.common.convention.exception.ServiceException;
import com.fengrui.shortlink.admin.dao.entity.GroupDO;
import com.fengrui.shortlink.admin.dao.mapper.GroupMapper;
import com.fengrui.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.fengrui.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.fengrui.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.fengrui.shortlink.admin.service.GroupService;
import com.fengrui.shortlink.admin.toolkit.DataConverter;
import com.fengrui.shortlink.admin.toolkit.RandomGenerator;
import com.fengrui.shortlink.common.convention.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.fengrui.shortlink.common.constant.RedisCacheConstant.LOCK_GROUP_CREATE_KEY;
import static com.fengrui.shortlink.common.convention.errorcode.BaseErrorCode.SERVICE_MAX_GROUP_NUM;
import static com.fengrui.shortlink.common.convention.errorcode.BaseErrorCode.SERVICE_NO_GROUP;

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

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

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
        List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
        // 远程调用获取分组下短链接数量
        List<String> gids = groupDOList.stream().map(GroupDO::getGid).toList();
        Result<List<ShortLinkGroupCountQueryRespDTO>> listResult = shortLinkActualRemoteService.listGroupShortLinkCount(gids);
        List<ShortLinkGroupRespDTO> shortLinkGroupRespDTOList = DataConverter.INSTANCE.toGroupRespDTOList(groupDOList);
        shortLinkGroupRespDTOList.forEach(each -> {
            Optional<ShortLinkGroupCountQueryRespDTO> first = listResult.getData().stream()
                    .filter(item -> Objects.equals(item.getGid(), each.getGid()))
                    .findFirst();
            first.ifPresent(item -> each.setShortLinkCount(first.get().getShortLinkCount()));
        });

        return shortLinkGroupRespDTOList;
    }

    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO shortLinkGroupRespDTO) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getGid, shortLinkGroupRespDTO.getGid())
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        GroupDO groupDO = GroupDO.builder()
                .name(shortLinkGroupRespDTO.getName())
                .build();
        baseMapper.update(groupDO, updateWrapper);
    }

    @Override
    public void deleteGroup(String gid) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        GroupDO groupDO = new GroupDO();
        groupDO.setDelFlag(1);
        baseMapper.update(groupDO, updateWrapper);
    }

    @Override
    public void sortGroup(List<ShortLinkGroupSortReqDTO> linkGroupSortReqDTOS) {
        linkGroupSortReqDTOS.forEach(each -> {
            GroupDO groupDO = GroupDO.builder()
                    .sortOrder(each.getSortOrder())
                    .build();
            LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                    .eq(GroupDO::getGid, each.getGid())
                    .eq(GroupDO::getUsername, UserContext.getUsername())
                    .eq(GroupDO::getDelFlag, 0);
            baseMapper.update(groupDO, updateWrapper);
        });
    }

    @Override
    public List<String> getGids() {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(groupDOList)){
            throw new ServiceException(SERVICE_NO_GROUP);
        }
        return groupDOList.stream().map(GroupDO::getGid).toList();
    }

    private boolean hasGid(String username, String gid) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, Optional.ofNullable(username).orElse(UserContext.getUsername()));
        GroupDO hasGroupFlag = baseMapper.selectOne(queryWrapper);
        return hasGroupFlag != null;
    }
}

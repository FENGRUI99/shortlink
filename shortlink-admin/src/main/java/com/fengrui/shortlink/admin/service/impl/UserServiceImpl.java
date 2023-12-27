package com.fengrui.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengrui.shortlink.admin.common.convention.exception.ServiceException;
import com.fengrui.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.fengrui.shortlink.admin.dao.entity.UserDO;
import com.fengrui.shortlink.admin.dao.mapper.UserMapper;
import com.fengrui.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.fengrui.shortlink.admin.dto.resp.UserActualRespDTO;
import com.fengrui.shortlink.admin.dto.resp.UserRespDTO;
import com.fengrui.shortlink.admin.service.UserService;
import com.fengrui.shortlink.admin.toolkit.DataConverter;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.fengrui.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.fengrui.shortlink.admin.common.enums.UserErrorCodeEnum.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;

    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null){
            throw new ServiceException(UserErrorCodeEnum.USER_NULL);
        }
        return DataConverter.INSTANCE.toUserDTO(userDO);
    }

    @Override
    public UserActualRespDTO getActualUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null){
            throw new ServiceException(UserErrorCodeEnum.USER_NULL);
        }
        UserActualRespDTO userActualRespDTO = DataConverter.INSTANCE.toUserActualDTO(userDO);

        return userActualRespDTO;
    }

    @Override
    public Boolean hasUsername(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO userRegisterReqDTO) {
        if (hasUsername(userRegisterReqDTO.getUsername())){
            throw new ServiceException(USER_NAME_EXIST);
        }

        // 通过redis分布式锁确保某一时刻只有一个线程在执行特定用户名的注册
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + userRegisterReqDTO.getUsername());
        try{
            if (lock.tryLock()) {
                try{
                    int inserted = baseMapper.insert(DataConverter.INSTANCE.toUserDo(userRegisterReqDTO));
                    if (inserted < 1) {
                        throw new ServiceException(USER_SAVE_ERROR);
                    }
                }
                /**
                 * DuplicateKeyException存在的意义
                 * 假设线程A，线程B都要注册username且username还没未被注册
                 * 线程A率先进入并成功插入了数据库记录，随后释放了分布式锁
                 * 此时线程B刚好执行trylock，随后执行insert
                 * 会抛出DuplicateKeyException
                 */
                catch (DuplicateKeyException ex){
                    throw new ServiceException(USER_EXIST);
                }
                userRegisterCachePenetrationBloomFilter.add(userRegisterReqDTO.getUsername());
                return;
            }
            throw new ServiceException(USER_NAME_EXIST);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<UserDO> queryUsernameByPage(long pageNum, long pageSize) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .select(UserDO::getUsername);
        Page<UserDO> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPage(page, queryWrapper).getRecords();
    }

}

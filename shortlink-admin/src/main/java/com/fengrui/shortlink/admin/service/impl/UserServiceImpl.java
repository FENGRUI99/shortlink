package com.fengrui.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengrui.shortlink.common.convention.exception.ClientException;
import com.fengrui.shortlink.common.convention.exception.ServiceException;
import com.fengrui.shortlink.admin.dao.entity.UserDO;
import com.fengrui.shortlink.admin.dao.mapper.UserMapper;
import com.fengrui.shortlink.admin.dto.req.UserLoginReqDTO;
import com.fengrui.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.fengrui.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.fengrui.shortlink.admin.dto.resp.UserActualRespDTO;
import com.fengrui.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.fengrui.shortlink.admin.dto.resp.UserRespDTO;
import com.fengrui.shortlink.admin.service.GroupService;
import com.fengrui.shortlink.admin.service.UserService;
import com.fengrui.shortlink.admin.toolkit.DataConverter;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.fengrui.shortlink.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.fengrui.shortlink.common.convention.errorcode.BaseErrorCode.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final GroupService groupService;

    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null){
            throw new ServiceException(USER_NULL);
        }
        return DataConverter.INSTANCE.toUserDTO(userDO);
    }

    @Override
    public UserActualRespDTO getActualUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null){
            throw new ServiceException(USER_NULL);
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
                    int inserted = baseMapper.insert(DataConverter.INSTANCE.toUserDO(userRegisterReqDTO));
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
                groupService.saveGroup(userRegisterReqDTO.getUsername(), "默认分组");
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

    @Override
    public void update(UserUpdateReqDTO userUpdateReqDTO) {
        // TODO 验证当前用户名是否为登录用户
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, userUpdateReqDTO.getUsername());
        baseMapper.update(DataConverter.INSTANCE.toUserDO(userUpdateReqDTO), updateWrapper);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO userLoginReqDTO) {
        LambdaQueryWrapper<UserDO> lambdaQueryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, userLoginReqDTO.getUsername())
                .eq(UserDO::getPassword, userLoginReqDTO.getPassword())
                .eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(lambdaQueryWrapper);
        if (userDO == null){
            throw new ClientException(USER_PASSWORD_WRONG);
        }
        Map<Object, Object> hasLoginMap = stringRedisTemplate.opsForHash().entries("login_" + userDO.getUsername());

        // 用户重复登陆返回token
        if(CollUtil.isNotEmpty(hasLoginMap)){
            String token = hasLoginMap.keySet().stream()
                    .findFirst().map(Object::toString)
                    .orElseThrow(() -> new ClientException(USER_LOGIN_ERROR));
            return new UserLoginRespDTO(token);
        }
        /**
         * redis  hash结构
         * Key：  login_用户名
         * Value:
         *  Key： token
         *  Val： JSON 字符串（用户信息）
         *  类似于Map<Object, Map<Object, Object>>
         */
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put("login_" + userDO.getUsername(), uuid, JSON.toJSONString(userDO));
        stringRedisTemplate.expire("login_" + userDO.getUsername(), 30L, TimeUnit.MINUTES);
        return new UserLoginRespDTO(uuid);
    }

    @Override
    public Boolean checkLogin(String username, String token) {
        return stringRedisTemplate.opsForHash().get("login_" + username, token) != null;
    }

    @Override
    public void logout(String username, String token) {
        if (checkLogin(username, token)){
           stringRedisTemplate.delete("login_" + username);
           return;
        }
        throw new ClientException(USER_TOKEN_FAIL);
    }
}

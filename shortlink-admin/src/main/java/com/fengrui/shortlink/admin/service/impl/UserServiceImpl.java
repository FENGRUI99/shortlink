package com.fengrui.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengrui.shortlink.admin.dao.entity.UserDO;
import com.fengrui.shortlink.admin.dao.mapper.UserMapper;
import com.fengrui.shortlink.admin.dto.resp.UserRespDTO;
import com.fengrui.shortlink.admin.service.UserService;
import com.fengrui.shortlink.admin.toolkit.DataConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final UserMapper userMapper;
    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null){
            // TODO 判断空
        }
        return DataConverter.INSTANCE.toUserDTO(userDO);
    }
}

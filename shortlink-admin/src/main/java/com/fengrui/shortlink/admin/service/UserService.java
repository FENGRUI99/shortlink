package com.fengrui.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fengrui.shortlink.admin.dao.entity.UserDO;
import com.fengrui.shortlink.admin.dto.resp.UserActualRespDTO;
import com.fengrui.shortlink.admin.dto.resp.UserRespDTO;

public interface UserService extends IService<UserDO> {
    public UserRespDTO getUserByUsername(String username);

    public UserActualRespDTO getActualUserByUsername(String username);
}

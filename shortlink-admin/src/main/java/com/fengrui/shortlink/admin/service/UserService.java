package com.fengrui.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fengrui.shortlink.admin.dao.entity.UserDO;
import com.fengrui.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.fengrui.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.fengrui.shortlink.admin.dto.resp.UserActualRespDTO;
import com.fengrui.shortlink.admin.dto.resp.UserRespDTO;

import java.util.List;

public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户信息
     * @param username
     * @return 用户实体类
     */
    public UserRespDTO getUserByUsername(String username);

    /**
     * 根据用户名查询用户真实信息
     * @param username
     * @return 用户实体类
     */
    public UserActualRespDTO getActualUserByUsername(String username);

    /**
     * 判断用户名是否存在
     * @param username
     * @return 存在返回True，不存在返回False
     */
    public Boolean hasUsername(String username);

    /**
     * 用户注册
     * @param userRegisterReqDTO
     */
    public void register(UserRegisterReqDTO userRegisterReqDTO);

    /**
     * 获取全部用户名
     */
    public List<UserDO> queryUsernameByPage(long pageNum, long pageSize);

    public void update(UserUpdateReqDTO userUpdateReqDTO);
}

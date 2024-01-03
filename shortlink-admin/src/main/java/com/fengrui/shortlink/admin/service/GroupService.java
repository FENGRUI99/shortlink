package com.fengrui.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fengrui.shortlink.admin.dao.entity.GroupDO;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增分组短链接
     * @param username
     * @param groupName
     */
    void saveGroup(String username, String groupName);

    /**
     * 新增分组短链接
     * @param groupName
     */
    void saveGroup(String groupName);
}

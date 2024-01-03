package com.fengrui.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fengrui.shortlink.admin.dao.entity.GroupDO;
import com.fengrui.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.fengrui.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增短链接分组
     * @param username
     * @param groupName
     */
    void saveGroup(String username, String groupName);

    /**
     * 新增短链接分组
     * @param groupName
     */
    void saveGroup(String groupName);

    /**
     * 查询短链接分组
     * @return List<ShortLinkGroupRespDTO>
     */
    List<ShortLinkGroupRespDTO> listGroup();

    /**
     * 修改短链接分组
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO shortLinkGroupUpdateReqDTO);
}

package com.fengrui.shortlink.admin.toolkit;

import com.fengrui.shortlink.admin.dao.entity.UserDO;
import com.fengrui.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.fengrui.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.fengrui.shortlink.admin.dto.resp.UserActualRespDTO;
import com.fengrui.shortlink.admin.dto.resp.UserRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DataConverter {
    DataConverter INSTANCE = Mappers.getMapper(DataConverter.class);

    UserRespDTO toUserDTO(UserDO userDO);

    UserActualRespDTO toUserActualDTO(UserDO userDO);

    UserDO toUserDO(UserRegisterReqDTO userRegisterReqDTO);

    UserDO toUserDO(UserUpdateReqDTO userUpdateReqDTO);
}

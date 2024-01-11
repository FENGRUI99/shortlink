package com.fengrui.shortlink.project.toolkit;

import com.fengrui.shortlink.project.dao.entity.ShortLinkDO;
import com.fengrui.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DataConverter {
    DataConverter INSTANCE = Mappers.getMapper(DataConverter.class);

    ShortLinkDO toShortLinkDO(ShortLinkCreateReqDTO shortLinkCreateReqDTO);
}

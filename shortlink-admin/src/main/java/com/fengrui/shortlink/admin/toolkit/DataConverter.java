package com.fengrui.shortlink.admin.toolkit;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DataConverter {
    DataConverter INSTANCE = Mappers.getMapper(DataConverter.class);

}

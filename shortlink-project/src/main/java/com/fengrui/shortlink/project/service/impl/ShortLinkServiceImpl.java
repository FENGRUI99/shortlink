package com.fengrui.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengrui.shortlink.project.dao.entity.ShortLinkDO;
import com.fengrui.shortlink.project.dao.mapper.ShortLinkMapper;
import com.fengrui.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.fengrui.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.fengrui.shortlink.project.service.ShortLinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        return null;
    }
}

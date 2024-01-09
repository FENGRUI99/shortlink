package com.fengrui.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengrui.shortlink.project.dao.entity.ShortLinkDO;
import com.fengrui.shortlink.project.dao.mapper.ShortLinkMapper;
import com.fengrui.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.fengrui.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.fengrui.shortlink.project.service.ShortLinkService;
import com.fengrui.shortlink.project.toolkit.DataConverter;
import com.fengrui.shortlink.project.toolkit.HashUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        String shortLinkSuffix = generateSuffix(shortLinkCreateReqDTO);
        String fullUrl = shortLinkCreateReqDTO.getDomain() + "/" + shortLinkSuffix;
        ShortLinkDO shortLinkDO = DataConverter.INSTANCE.toShortLinkDO(shortLinkCreateReqDTO);
        shortLinkDO.setEnableStatus(0);
        shortLinkDO.setShortUri(shortLinkSuffix);
        shortLinkDO.setFullShortUrl(fullUrl);
        baseMapper.insert(shortLinkDO);
        return ShortLinkCreateRespDTO.builder()
                .gid(shortLinkCreateReqDTO.getGid())
                .originUrl(shortLinkCreateReqDTO.getOriginUrl())
                .fullShortUrl(fullUrl)
                .build();
    }

    private String generateSuffix(ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        String originUrl = shortLinkCreateReqDTO.getOriginUrl();
        return HashUtil.hashToBase62(originUrl);
    }
}

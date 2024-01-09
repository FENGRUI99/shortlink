package com.fengrui.shortlink.project.service.impl;

import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengrui.shortlink.common.convention.exception.ServiceException;
import com.fengrui.shortlink.project.dao.entity.ShortLinkDO;
import com.fengrui.shortlink.project.dao.mapper.ShortLinkMapper;
import com.fengrui.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.fengrui.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.fengrui.shortlink.project.service.ShortLinkService;
import com.fengrui.shortlink.project.toolkit.DataConverter;
import com.fengrui.shortlink.project.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import static com.fengrui.shortlink.common.convention.errorcode.BaseErrorCode.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    /**
     * 加布隆过滤器判断完整短链接是否有重复
     * 保险起见，还得查数据库确认没有重复
     */
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        String shortLinkSuffix = generateSuffix(shortLinkCreateReqDTO);
        String fullUrl = StrBuilder.create(shortLinkCreateReqDTO.getDomain())
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        ShortLinkDO shortLinkDO = DataConverter.INSTANCE.toShortLinkDO(shortLinkCreateReqDTO);
        shortLinkDO.setEnableStatus(0);
        shortLinkDO.setShortUri(shortLinkSuffix);
        shortLinkDO.setFullShortUrl(fullUrl);

        try{
            baseMapper.insert(shortLinkDO);
        } catch (DuplicateKeyException e) {
            log.warn("短链接：{} 重复入库", fullUrl);
            throw new ServiceException(LINK_EXISTS);
        } finally {
            shortUriCreateCachePenetrationBloomFilter.add(fullUrl);
        }

        return ShortLinkCreateRespDTO.builder()
                .gid(shortLinkCreateReqDTO.getGid())
                .originUrl(shortLinkCreateReqDTO.getOriginUrl())
                .fullShortUrl(fullUrl)
                .build();
    }

    private String generateSuffix(ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        int count = 0;
        String suffix;
        while (count < 10){
            String originUrl = shortLinkCreateReqDTO.getOriginUrl();
            suffix =  HashUtil.hashToBase62(originUrl + System.currentTimeMillis()); // 增加随机
            if (!shortUriCreateCachePenetrationBloomFilter.contains(shortLinkCreateReqDTO.getDomain() + "/" + suffix)){
                return suffix;
            }
            count++;
        }
        throw new ServiceException(LINK_GENERATE_TOO_FREQUENT);
    }
}

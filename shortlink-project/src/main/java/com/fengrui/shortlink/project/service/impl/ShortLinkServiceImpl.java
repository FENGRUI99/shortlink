package com.fengrui.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengrui.shortlink.common.convention.exception.ServiceException;
import com.fengrui.shortlink.project.common.enums.ValidDateTypeEnum;
import com.fengrui.shortlink.project.dao.entity.ShortLinkDO;
import com.fengrui.shortlink.project.dao.mapper.ShortLinkMapper;
import com.fengrui.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.fengrui.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.fengrui.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.fengrui.shortlink.project.dto.resp.*;
import com.fengrui.shortlink.project.service.ShortLinkService;
import com.fengrui.shortlink.project.toolkit.DataConverter;
import com.fengrui.shortlink.project.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO shortLinkPageReqDTO) {
        LambdaQueryWrapper<ShortLinkDO> lambdaQueryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, shortLinkPageReqDTO.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> result = baseMapper.selectPage(shortLinkPageReqDTO, lambdaQueryWrapper);
        return result.convert(each -> BeanUtil.toBean(each, ShortLinkPageRespDTO.class));
    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> gids) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid", gids)
                .eq("enable_status", 0)
                .eq("del_flag", 0)
                .eq("del_time", 0)
                .groupBy("gid");
        List<Map<String, Object>> result = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(result, ShortLinkGroupCountQueryRespDTO.class);
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO shortLinkUpdateReqDTO) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, shortLinkUpdateReqDTO.getFullShortUrl())
                .eq(ShortLinkDO::getGid, shortLinkUpdateReqDTO.getOriginGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0);
        ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
        if (shortLinkDO == null){
            throw new ServiceException(LINK_NOT_EXISTS);
        }

        // 修改短链接信息
        if (Objects.equals(shortLinkDO.getGid(), shortLinkUpdateReqDTO.getGid())){
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, shortLinkUpdateReqDTO.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, shortLinkUpdateReqDTO.getOriginGid())
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .set(Objects.equals(shortLinkUpdateReqDTO.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
            ShortLinkDO updateDO = ShortLinkDO.builder()
                    .domain(shortLinkDO.getDomain())
                    .shortUri(shortLinkDO.getShortUri())
                    .favicon(shortLinkDO.getFavicon())
                    .createdType(shortLinkDO.getCreatedType())
                    .gid(shortLinkUpdateReqDTO.getGid())
                    .originUrl(shortLinkUpdateReqDTO.getOriginUrl())
                    .describe(shortLinkUpdateReqDTO.getDescribe())
                    .validDateType(shortLinkUpdateReqDTO.getValidDateType())
                    .validDate(shortLinkUpdateReqDTO.getValidDate())
                    .build();
            baseMapper.update(updateDO, updateWrapper);
        }
        // 修改短链接分组
        else {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, shortLinkUpdateReqDTO.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, shortLinkUpdateReqDTO.getOriginGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getDelTime, 0L)
                    .eq(ShortLinkDO::getEnableStatus, 0);

            ShortLinkDO updateDO = ShortLinkDO.builder()
                    .delTime(System.currentTimeMillis())
                    .build();
            updateDO.setDelFlag(1);
            baseMapper.update(updateDO, updateWrapper);

            ShortLinkDO insertDO = ShortLinkDO.builder()
                    .domain(shortLinkDO.getDomain())
                    .originUrl(shortLinkUpdateReqDTO.getOriginUrl())
                    .gid(shortLinkUpdateReqDTO.getGid())
                    .createdType(shortLinkDO.getCreatedType())
                    .validDateType(shortLinkUpdateReqDTO.getValidDateType())
                    .validDate(shortLinkUpdateReqDTO.getValidDate())
                    .describe(shortLinkUpdateReqDTO.getDescribe())
                    .shortUri(shortLinkDO.getShortUri())
                    .enableStatus(shortLinkDO.getEnableStatus())
                    .totalPv(shortLinkDO.getTotalPv())
                    .totalUv(shortLinkDO.getTotalUv())
                    .totalUip(shortLinkDO.getTotalUip())
                    .fullShortUrl(shortLinkDO.getFullShortUrl())
                    .favicon(getFavicon(shortLinkUpdateReqDTO.getOriginUrl()))
                    .delTime(0L)
                    .build();
            baseMapper.insert(insertDO);
        }
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

    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == responseCode) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }
}

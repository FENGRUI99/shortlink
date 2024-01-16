package com.fengrui.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengrui.shortlink.common.convention.exception.ServiceException;
import com.fengrui.shortlink.project.common.enums.ValidDateTypeEnum;
import com.fengrui.shortlink.project.dao.entity.ShortLinkDO;
import com.fengrui.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.fengrui.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.fengrui.shortlink.project.dao.mapper.ShortLinkMapper;
import com.fengrui.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.fengrui.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.fengrui.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.fengrui.shortlink.project.dto.resp.*;
import com.fengrui.shortlink.project.service.ShortLinkService;
import com.fengrui.shortlink.project.toolkit.DataConverter;
import com.fengrui.shortlink.project.toolkit.HashUtil;
import com.fengrui.shortlink.project.toolkit.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.fengrui.shortlink.common.convention.errorcode.BaseErrorCode.*;
import static com.fengrui.shortlink.project.common.constant.RedisKeyConstant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;

    /**
     * 加布隆过滤器判断完整短链接是否有重复
     * 保险起见，还得查数据库确认没有重复
     */
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        String shortLinkSuffix = generateSuffix(shortLinkCreateReqDTO);
        String fullUrl = StrBuilder.create(createShortLinkDefaultDomain)
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        ShortLinkDO shortLinkDO = DataConverter.INSTANCE.toShortLinkDO(shortLinkCreateReqDTO);
        shortLinkDO.setDomain(createShortLinkDefaultDomain);
        shortLinkDO.setEnableStatus(0);
        shortLinkDO.setShortUri(shortLinkSuffix);
        shortLinkDO.setFullShortUrl(fullUrl);

        ShortLinkGotoDO linkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(fullUrl)
                .gid(shortLinkCreateReqDTO.getGid())
                .build();

        try{
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(linkGotoDO);
        } catch (DuplicateKeyException e) {
            log.warn("短链接：{} 重复入库", fullUrl);
            throw new ServiceException(LINK_EXISTS);
        } finally {
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_SHORT_LINK_KEY, fullUrl),
                    shortLinkCreateReqDTO.getOriginUrl(),
                    LinkUtil.getLinkCacheValidTime(shortLinkCreateReqDTO.getValidDate()), TimeUnit.MILLISECONDS
            );
            shortUriCreateCachePenetrationBloomFilter.add(fullUrl);
        }

        return ShortLinkCreateRespDTO.builder()
                .gid(shortLinkCreateReqDTO.getGid())
                .originUrl(shortLinkCreateReqDTO.getOriginUrl())
                .fullShortUrl("http://" + fullUrl)
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
        return result.convert(each -> {
            ShortLinkPageRespDTO r = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            r.setDomain("http://" + r.getDomain());
            return r;
        });
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

    @SneakyThrows
    @Override
    public void redirectUrl(String shortUri, ServletRequest request, ServletResponse response) {
        String serverName = request.getServerName();
        String serverPort = Optional.of(request.getServerPort())
                .filter(each -> !Objects.equals(each, 80))
                .map(String::valueOf)
                .map(each -> ":" + each)
                .orElse("");
        String fullShortUrl = serverName + serverPort + "/" + shortUri;
        String originLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originLink)){
            ((HttpServletResponse) response).sendRedirect(originLink);
            return;
        }

        // 布隆过滤器防止缓存穿透，但仍会误判
//        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
//        if (!contains){
//            ((HttpServletResponse) response).sendRedirect("/page/notfound");
//            return;
//        }

        // 判断key对应的是否为空值
        originLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originLink)){
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }

        /**
         * 缓存击穿：使用缓存系统时，由于某个热点数据失效或首次请求导致缓存未命中，
         * 而该数据的高并发访问数据库
         *
         * Solution：分布式锁，双重判断
         */
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try{
            originLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originLink)){
                ((HttpServletResponse) response).sendRedirect(originLink);
                return;
            }
            // 根据goto表获取uid
            LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);
            if (shortLinkGotoDO == null){
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.SECONDS);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            // 根据uid获取originUrl
            LambdaQueryWrapper<ShortLinkDO> lambdaQueryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(lambdaQueryWrapper);
            if (shortLinkDO == null || (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(new Date()))){
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.SECONDS);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLinkDO.getOriginUrl(),
                    LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()), TimeUnit.MILLISECONDS
            );
            ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
        } finally {
            lock.unlock();
        }
    }

    private String generateSuffix(ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        int count = 0;
        String suffix;
        while (count < 10){
            String originUrl = shortLinkCreateReqDTO.getOriginUrl();
            suffix =  HashUtil.hashToBase62(originUrl + System.currentTimeMillis()); // 增加随机
            if (!shortUriCreateCachePenetrationBloomFilter.contains(createShortLinkDefaultDomain + "/" + suffix)){
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

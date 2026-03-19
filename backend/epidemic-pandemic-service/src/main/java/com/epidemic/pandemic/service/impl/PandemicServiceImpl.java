package com.epidemic.pandemic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import com.epidemic.pandemic.entity.PandemicNews;
import com.epidemic.pandemic.entity.PushRecord;
import com.epidemic.pandemic.feign.UserFeignClient;
import com.epidemic.pandemic.mapper.PandemicNewsMapper;
import com.epidemic.pandemic.mapper.PushRecordMapper;
import com.epidemic.pandemic.service.PandemicCacheService;
import com.epidemic.pandemic.service.PandemicService;
import com.epidemic.pandemic.service.UserNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 疫情信息服务实现类
 */
@Service
@Slf4j
public class PandemicServiceImpl extends ServiceImpl<PandemicNewsMapper, PandemicNews> implements PandemicService {

    @Autowired
    private PushRecordMapper pushRecordMapper;

    @Autowired
    private PandemicCacheService cacheService;

    @Autowired
    private UserNotificationService userNotificationService;

    @Autowired
    private UserFeignClient userFeignClient;

    @Override
    public PageResult<PandemicNews> getNewsList(Integer page, Integer size, String status) {
        Page<PandemicNews> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<PandemicNews> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(status)) {
            wrapper.eq(PandemicNews::getStatus, status);
        }
        wrapper.orderByDesc(PandemicNews::getPublishTime);

        Page<PandemicNews> result = baseMapper.selectPage(pageParam, wrapper);

        // 缓存首页新闻列表（第一页）
        if (page == 1) {
            try {
                List<Map<String, Object>> cacheData = new ArrayList<>();
                for (PandemicNews news : result.getRecords()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", news.getId());
                    map.put("title", news.getTitle());
                    map.put("summary", news.getSummary());
                    map.put("author", news.getAuthor());
                    map.put("status", news.getStatus());
                    map.put("viewCount", news.getViewCount());
                    map.put("publishTime", news.getPublishTime());
                    cacheData.add(map);
                }
                cacheService.setNewsListCache(status, cacheData);
            } catch (Exception e) {
                log.warn("缓存新闻列表失败：{}", e.getMessage());
            }
        }

        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public void publishNews(PandemicNews news) {
        news.setId("N" + System.currentTimeMillis());
        news.setCreateTime(LocalDateTime.now());
        if (news.getPublishTime() == null) {
            news.setPublishTime(LocalDateTime.now());
        }
        if (news.getViewCount() == null) {
            news.setViewCount(0);
        }
        if (!StringUtils.hasText(news.getAuthor())) {
            news.setAuthor("系统管理员");
        }
        baseMapper.insert(news);

        // 清除新闻列表缓存
        cacheService.deleteNewsListCache();
        log.info("发布新闻后清除缓存");
    }

    @Override
    public PageResult<Map<String, Object>> getKnowledgeList(Integer page, Integer size) {
        // Mock implementation for Knowledge as it's not fully defined in DB schema
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> knowledge1 = new HashMap<>();
        knowledge1.put("id", "K001");
        knowledge1.put("title", "如何正确佩戴N95口罩");
        knowledge1.put("summary", "正确佩戴N95口罩是预防呼吸道传染病的重要措施，请按照以下步骤操作。");
        knowledge1.put("category", "personal");
        knowledge1.put("author", "疾控中心");
        knowledge1.put("viewCount", 3250);
        knowledge1.put("sortOrder", 1);
        list.add(knowledge1);

        Map<String, Object> knowledge2 = new HashMap<>();
        knowledge2.put("id", "K002");
        knowledge2.put("title", "家庭日常消毒指南");
        knowledge2.put("summary", "家庭日常消毒是预防疫情的重要环节，请了解以下消毒方法和注意事项。");
        knowledge2.put("category", "community");
        knowledge2.put("author", "疾控中心");
        knowledge2.put("viewCount", 2180);
        knowledge2.put("sortOrder", 2);
        list.add(knowledge2);

        Map<String, Object> knowledge3 = new HashMap<>();
        knowledge3.put("id", "K003");
        knowledge3.put("title", "医疗机构内个人防护要点");
        knowledge3.put("summary", "医疗机构内工作人员应严格做好个人防护，防止职业暴露。");
        knowledge3.put("category", "hospital");
        knowledge3.put("author", "卫健委");
        knowledge3.put("viewCount", 1680);
        knowledge3.put("sortOrder", 3);
        list.add(knowledge3);

        return PageResult.of(list, 10L, page, size);
    }

    @Override
    public Map<String, Object> getPandemicData() {
        Map<String, Object> data = new HashMap<>();
        data.put("confirmed", 156);
        data.put("cured", 120);
        data.put("dead", 3);
        data.put("suspected", 10);
        data.put("updateTime", LocalDateTime.now());
        return data;
    }

    @Override
    public List<Map<String, Object>> getPushStats() {
        // 先尝试从缓存获取
        List<Map<String, Object>> cachedStats = cacheService.getPushStatsCache();
        if (cachedStats != null) {
            log.debug("推送统计命中缓存");
            return cachedStats;
        }

        List<Map<String, Object>> list = new ArrayList<>();

        // 查询推送总数
        Long totalCount = pushRecordMapper.selectCount(null);
        Map<String, Object> stat1 = new HashMap<>();
        stat1.put("label", "推送总数");
        stat1.put("value", totalCount != null ? totalCount.toString() : "0");
        list.add(stat1);

        // 查询今日推送数量
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        Long todayCount = pushRecordMapper.selectCount(
            new LambdaQueryWrapper<PushRecord>().ge(PushRecord::getPushTime, startOfDay)
        );
        Map<String, Object> stat2 = new HashMap<>();
        stat2.put("label", "今日推送");
        stat2.put("value", todayCount != null ? todayCount.toString() : "0");
        list.add(stat2);

        // 查询推送成功数量
        Long successCount = pushRecordMapper.selectCount(
            new LambdaQueryWrapper<PushRecord>().eq(PushRecord::getStatus, "success")
        );
        Map<String, Object> stat3 = new HashMap<>();
        stat3.put("label", "推送成功");
        stat3.put("value", successCount != null ? successCount.toString() : "0");
        list.add(stat3);

        // 查询推送失败数量
        Long failedCount = pushRecordMapper.selectCount(
            new LambdaQueryWrapper<PushRecord>().eq(PushRecord::getStatus, "failed")
        );
        Map<String, Object> stat4 = new HashMap<>();
        stat4.put("label", "推送失败");
        stat4.put("value", failedCount != null ? failedCount.toString() : "0");
        list.add(stat4);

        // 缓存结果
        cacheService.setPushStatsCache(list);

        return list;
    }

    @Override
    public List<Map<String, Object>> getPushList() {
        // 先尝试从缓存获取
        List<Map<String, Object>> cachedList = cacheService.getPushListCache();
        if (cachedList != null) {
            log.debug("推送列表命中缓存");
            return cachedList;
        }

        List<PushRecord> records = pushRecordMapper.selectList(
            new LambdaQueryWrapper<PushRecord>().orderByDesc(PushRecord::getPushTime)
        );

        List<Map<String, Object>> list = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (PushRecord record : records) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("title", record.getTitle());
            map.put("target", convertTargetName(record.getTarget()));
            map.put("channelList", record.getChannels() != null ? record.getChannels().split(",") : new String[]{});
            map.put("time", record.getPushTime() != null ? record.getPushTime().format(formatter) : "");
            map.put("status", "success".equals(record.getStatus()) ? "成功" : "失败");
            list.add(map);
        }

        // 缓存结果
        cacheService.setPushListCache(list);

        return list;
    }

    private String convertTargetName(String target) {
        if (target == null) return "未知";
        return switch (target) {
            case "all" -> "全部用户";
            case "hospital_user" -> "医院用户";
            case "community_staff" -> "社区人员";
            case "material_approver" -> "物资审核员";
            default -> target;
        };
    }

    @Override
    public void sendPush(Map<String, Object> pushData) {
        PushRecord record = new PushRecord();
        record.setTitle((String) pushData.get("title"));
        record.setContent((String) pushData.get("content"));
        record.setTarget((String) pushData.get("target"));

        @SuppressWarnings("unchecked")
        List<String> channels = (List<String>) pushData.get("channel");
        if (channels != null && !channels.isEmpty()) {
            record.setChannels(String.join(",", channels));
        }

        record.setStatus("success");
        record.setPushTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());

        pushRecordMapper.insert(record);

        // 为目标用户创建通知
        String target = (String) pushData.get("target");
        List<Long> userIds = getUserIdsByTarget(target);
        if (!userIds.isEmpty()) {
            userNotificationService.createNotifications(
                userIds,
                record.getTitle(),
                record.getContent(),
                "push",
                record.getId()
            );
        }

        // 清除推送相关缓存
        cacheService.deletePushStatsCache();
        cacheService.deletePushListCache();
        log.info("推送消息后清除相关缓存");
    }

    /**
     * 根据推送目标获取用户ID列表
     * @param target 推送目标
     * @return 用户ID列表
     */
    private List<Long> getUserIdsByTarget(String target) {
        log.info("获取推送目标用户: target={}", target);
        Result<List<Long>> result;
        if (target == null || "all".equals(target)) {
            // 推送给所有用户（排除管理员）
            result = userFeignClient.getUserIdsByRole(null);
        } else {
            // 推送给指定角色的用户
            result = userFeignClient.getUserIdsByRole(target);
        }
        List<Long> userIds = (result != null && result.getData() != null) ? result.getData() : new ArrayList<>();
        log.info("获取到的用户ID列表: {}", userIds);
        return userIds;
    }

    @Override
    public List<Map<String, Object>> getUserRoleStats() {
        // 先尝试从缓存获取
        List<Map<String, Object>> cachedStats = cacheService.getUserRoleStatsCache();
        if (cachedStats != null) {
            log.debug("用户角色统计命中缓存");
            return cachedStats;
        }

        // 调用 user-service 获取数据
        Result<List<Map<String, Object>>> result = userFeignClient.getRoleCounts();
        List<Map<String, Object>> results = (result != null && result.getData() != null)
            ? result.getData()
            : new ArrayList<>();

        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, String> roleNameMap = new HashMap<>();
        roleNameMap.put("admin", "管理员");
        roleNameMap.put("hospital_user", "医院用户");
        roleNameMap.put("community_staff", "社区人员");
        roleNameMap.put("donor", "捐赠方");

        Map<String, String> colorMap = new HashMap<>();
        colorMap.put("admin", "#722ed1");
        colorMap.put("hospital_user", "#1890ff");
        colorMap.put("community_staff", "#52c41a");
        colorMap.put("donor", "#faad14");

        for (Map<String, Object> row : results) {
            Map<String, Object> item = new HashMap<>();
            String role = (String) row.get("role");
            item.put("name", roleNameMap.getOrDefault(role, role));
            item.put("value", row.get("count"));
            item.put("itemStyle", Map.of("color", colorMap.getOrDefault(role, "#1890ff")));
            list.add(item);
        }

        // 缓存结果（用户角色统计变化较少，缓存30分钟）
        cacheService.setUserRoleStatsCache(list);

        return list;
    }

    @Override
    public void deletePushRecord(Long id) {
        pushRecordMapper.deleteById(id);
        // 清除推送相关缓存
        cacheService.deletePushStatsCache();
        cacheService.deletePushListCache();
        log.info("删除推送记录：id={}", id);
    }
}

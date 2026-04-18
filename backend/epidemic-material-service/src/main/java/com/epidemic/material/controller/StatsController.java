package com.epidemic.material.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.epidemic.common.entity.OperateLog;
import com.epidemic.common.feign.LogFeignClient;
import com.epidemic.common.result.Result;
import com.epidemic.common.util.UserContext;
import com.epidemic.material.entity.Application;
import com.epidemic.material.entity.DashboardVO;
import com.epidemic.material.entity.Donation;
import com.epidemic.material.service.ApplicationService;
import com.epidemic.material.service.DonationService;
import com.epidemic.material.service.InventoryLogService;
import com.epidemic.material.service.MaterialService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 数据统计控制器
 * 提供仪表盘、个人中心、趋势图等各类统计数据的聚合接口
 */
@Tag(name = "数据统计", description = "统计相关接口")
@RestController
@RequestMapping("/stats")
@CrossOrigin
@Slf4j
public class StatsController {

    @Autowired
    private MaterialService materialService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private DonationService donationService;

    @Autowired
    private InventoryLogService inventoryLogService;

    @Autowired
    private LogFeignClient logFeignClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // JSON 序列化工具
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 仪表盘缓存 Key
    private static final String DASHBOARD_CACHE_KEY = "api:cache:dashboard:full";
    // 缓存时间：30 秒
    private static final long CACHE_TTL = 30;

    /**
     * 获取管理端仪表盘综合统计数据
     * 包括物资总量、待审核申请、待审核捐赠及库存预警数量
     *
     * @return 仪表盘统计数据Map
     */
    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() throws Exception {
        Map<String, Object> data = new HashMap<>();

        // 使用并行查询优化性能
        CompletableFuture<Long> totalMaterialsFuture = CompletableFuture.supplyAsync(() -> materialService.count());
        CompletableFuture<Map<String, Object>> appStatsFuture = CompletableFuture.supplyAsync(() -> applicationService.getStats());
        CompletableFuture<Map<String, Object>> donationStatsFuture = CompletableFuture.supplyAsync(() -> donationService.getStats());
        CompletableFuture<List<Map<String, Object>>> warningListFuture = CompletableFuture.supplyAsync(() -> materialService.getWarningList());
        CompletableFuture<Map<String, Integer>> logStatsFuture = CompletableFuture.supplyAsync(() -> inventoryLogService.getTodayStats());
        CompletableFuture<Map<String, Object>> statsTrendFuture = CompletableFuture.supplyAsync(() -> inventoryLogService.getStatsTrend());
        CompletableFuture<Long> totalStockFuture = CompletableFuture.supplyAsync(() -> materialService.getTotalStock());

        // 等待所有并行任务完成
        CompletableFuture.allOf(totalMaterialsFuture, appStatsFuture, donationStatsFuture, warningListFuture, logStatsFuture, statsTrendFuture, totalStockFuture).join();

        // 收集结果
        data.put("totalMaterials", totalMaterialsFuture.get());
        Map<String, Object> appStats = appStatsFuture.get();
        data.put("pendingApplications", appStats.get("pendingCount") != null ? appStats.get("pendingCount") : 0);
        Map<String, Object> donationStats = donationStatsFuture.get();
        data.put("pendingDonations", donationStats.get("pendingCount") != null ? donationStats.get("pendingCount") : 0);
        List<Map<String, Object>> warningList = warningListFuture.get();
        data.put("lowStockItems", warningList != null ? warningList.size() : 0);

        Map<String, Integer> logStats = logStatsFuture.get();
        int todayInbound = logStats.get("todayInbound") != null ? logStats.get("todayInbound") : 0;
        int todayOutbound = logStats.get("todayOutbound") != null ? logStats.get("todayOutbound") : 0;
        data.put("todayInbound", todayInbound);
        data.put("todayOutbound", todayOutbound);

        Map<String, Object> statsTrend = statsTrendFuture.get();
        data.put("todayInboundTrend", statsTrend.get("inboundTrend"));
        data.put("todayInboundTrendType", statsTrend.get("inboundTrendType"));
        data.put("todayOutboundTrend", statsTrend.get("outboundTrend"));
        data.put("todayOutboundTrendType", statsTrend.get("outboundTrendType"));

        data.put("totalMaterialsTrend", 0);
        data.put("totalMaterialsTrendType", "up");
        data.put("lowStockItemsTrend", 0);
        data.put("lowStockItemsTrendType", "down");

        return Result.success("获取成功", data);
    }
    
    /**
     * 获取用户端个人中心统计数据
     * 包括我的申领总数、我的待审核申领数、我的捐赠总数
     *
     * @param userIdStr 用户ID (从网关透传Header获取)
     * @return 个人统计数据Map
     */
    @Operation(summary = "获取用户个人统计数据")
    @GetMapping("/user")
    public Result<Map<String, Object>> getUserStats() throws Exception {
        Long userId = UserContext.getUserId();

        // 使用并行查询优化，所有统计查询同时执行
        // 申领统计
        CompletableFuture<Long> myAppCountFuture = CompletableFuture.supplyAsync(() ->
            applicationService.count(new LambdaQueryWrapper<Application>().eq(Application::getApplicantId, userId)));
        CompletableFuture<Long> pendingAppCountFuture = CompletableFuture.supplyAsync(() ->
            applicationService.count(new LambdaQueryWrapper<Application>().eq(Application::getApplicantId, userId).eq(Application::getStatus, "pending")));
        // 已通过状态：approved, delivered, received 都属于已通过
        CompletableFuture<Long> approvedAppCountFuture = CompletableFuture.supplyAsync(() ->
            applicationService.count(new LambdaQueryWrapper<Application>()
                .eq(Application::getApplicantId, userId)
                .in(Application::getStatus, "approved", "delivered", "received")));

        // 捐赠统计
        CompletableFuture<Long> myDonationCountFuture = CompletableFuture.supplyAsync(() ->
            donationService.count(new LambdaQueryWrapper<Donation>().eq(Donation::getDonorId, userId)));
        CompletableFuture<Long> pendingDonationCountFuture = CompletableFuture.supplyAsync(() ->
            donationService.count(new LambdaQueryWrapper<Donation>().eq(Donation::getDonorId, userId).eq(Donation::getStatus, "pending")));
        CompletableFuture<Long> approvedDonationCountFuture = CompletableFuture.supplyAsync(() ->
            donationService.count(new LambdaQueryWrapper<Donation>().eq(Donation::getDonorId, userId).eq(Donation::getStatus, "approved")));

        // 等待所有并行任务完成
        CompletableFuture.allOf(myAppCountFuture, pendingAppCountFuture, approvedAppCountFuture,
            myDonationCountFuture, pendingDonationCountFuture, approvedDonationCountFuture).join();

        Map<String, Object> data = new HashMap<>();
        data.put("myApplicationCount", myAppCountFuture.get());
        data.put("pendingApplicationCount", pendingAppCountFuture.get());
        data.put("approvedApplicationCount", approvedAppCountFuture.get());
        data.put("myDonationCount", myDonationCountFuture.get());
        data.put("pendingDonationCount", pendingDonationCountFuture.get());
        data.put("approvedDonationCount", approvedDonationCountFuture.get());

        return Result.success(data);
    }
    
    /**
     * 获取库存预警物资列表
     *
     * @return 预警物资列表
     */
    @Operation(summary = "获取库存预警列表")
    @GetMapping("/warning")
    public Result<List<Map<String, Object>>> getWarningList() {
        return Result.success(materialService.getWarningList());
    }

    /**
     * 获取物资入库与出库趋势数据
     * 支持按周、月、年维度查询
     * 数据经过日期补全处理，确保图表横坐标连续
     *
     * @param period 统计周期（week/month/year），默认为week
     * @return 趋势数据Map（dates, inbound, outbound）
     */
    @Operation(summary = "获取趋势数据")
    @GetMapping("/trend")
    public Result<Map<String, Object>> getTrendData(@RequestParam(defaultValue = "week") String period) {
        // 计算统计时间范围
        LocalDate end = LocalDate.now();
        LocalDate start;
        if ("month".equals(period)) {
            start = end.minusMonths(1);
        } else if ("year".equals(period)) {
            start = end.minusYears(1);
        } else {
            start = end.minusWeeks(1);
        }

        String startDateStr = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDateStr = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 从 inventory_log 获取出入库数据
        Map<String, List<Map<String, Object>>> trendDataMap = inventoryLogService.getTrendDataByDateRange(startDateStr, endDateStr);
        List<Map<String, Object>> inboundList = trendDataMap.get("inbound");
        List<Map<String, Object>> outboundList = trendDataMap.get("outbound");

        // 数据预处理：转为 Map<日期, 数量> 方便查找
        List<String> dates = new ArrayList<>();
        List<Integer> inbound = new ArrayList<>();
        List<Integer> outbound = new ArrayList<>();

        Map<String, Integer> inboundMap = inboundList != null ? inboundList.stream().collect(Collectors.toMap(
            m -> (String) m.get("date"),
            m -> ((Number) m.get("count")).intValue(),
            Integer::sum
        )) : new HashMap<>();

        Map<String, Integer> outboundMap = outboundList != null ? outboundList.stream().collect(Collectors.toMap(
            m -> (String) m.get("date"),
            m -> ((Number) m.get("count")).intValue(),
            Integer::sum
        )) : new HashMap<>();

        // 补全日期并填充数据
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            dates.add(dateStr);
            inbound.add(inboundMap.getOrDefault(dateStr, 0));
            outbound.add(outboundMap.getOrDefault(dateStr, 0));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("dates", dates);
        data.put("inbound", inbound);
        data.put("outbound", outbound);

        return Result.success("获取成功", data);
    }

    /**
     * 获取物资分类统计数据（用于饼图）
     *
     * @return 物资分类统计结果
     */
    @Operation(summary = "获取物资统计数据")
    @GetMapping("/material")
    public Result<Map<String, Object>> getMaterialStats() {
        return Result.success(materialService.getStats());
    }

    /**
     * 获取申请统计数据
     *
     * @return 申请统计结果
     */
    @Operation(summary = "获取申请统计数据")
    @GetMapping("/application")
    public Result<Map<String, Object>> getApplicationStats() {
        return Result.success(applicationService.getStats());
    }

    /**
     * 获取捐赠统计数据
     *
     * @return 捐赠统计结果
     */
    @Operation(summary = "获取捐赠统计数据")
    @GetMapping("/donation")
    public Result<Map<String, Object>> getDonationStats() {
        return Result.success(donationService.getStats());
    }
    
    /**
     * 获取实时数据大屏综合数据
     * 包含核心指标、物资分类统计、趋势数据、区域分布、预警信息、实时动态等
     * 使用 Redis 缓存，减少数据库查询压力
     *
     * @return 实时数据大屏 VO
     */
    @Operation(summary = "获取实时数据大屏数据")
    @GetMapping("/dashboard/full")
    public Result<DashboardVO> getFullDashboard() {
        // 尝试从 Redis 缓存获取
        try {
            Object cached = redisTemplate.opsForValue().get(DASHBOARD_CACHE_KEY);
            if (cached != null) {
                log.debug("仪表盘数据缓存命中");
                DashboardVO dashboard = objectMapper.convertValue(cached, DashboardVO.class);
                return Result.success(dashboard);
            }
        } catch (Exception e) {
            log.warn("读取仪表盘缓存失败：{}", e.getMessage());
        }

        // 缓存未命中，构建数据
        log.debug("仪表盘数据缓存未命中，开始构建");
        DashboardVO dashboard = buildDashboardData();

        // 存入缓存
        try {
            redisTemplate.opsForValue().set(DASHBOARD_CACHE_KEY, dashboard, CACHE_TTL, TimeUnit.SECONDS);
            log.info("仪表盘数据已缓存，TTL={}s", CACHE_TTL);
        } catch (Exception e) {
            log.warn("缓存仪表盘数据失败：{}", e.getMessage());
        }

        return Result.success(dashboard);
    }

    /**
     * 构建仪表盘数据
     */
    private DashboardVO buildDashboardData() {
        DashboardVO dashboard = new DashboardVO();

        // 1. 核心指标
        DashboardVO.CoreMetrics coreMetrics = new DashboardVO.CoreMetrics();
        coreMetrics.setTotalMaterials(materialService.count());
        coreMetrics.setTotalStock(materialService.getTotalStock());

        // 今日出入库
        Map<String, Integer> logStats = inventoryLogService.getTodayStats();
        coreMetrics.setTodayInbound(logStats.get("todayInbound") != null ? logStats.get("todayInbound") : 0);
        coreMetrics.setTodayOutbound(logStats.get("todayOutbound") != null ? logStats.get("todayOutbound") : 0);

        // 出入库趋势（与昨日对比）
        Map<String, Object> statsTrend = inventoryLogService.getStatsTrend();
        coreMetrics.setTodayInboundTrend(((Number) statsTrend.get("inboundTrend")).intValue());
        coreMetrics.setTodayInboundTrendType((String) statsTrend.get("inboundTrendType"));
        coreMetrics.setTodayOutboundTrend(((Number) statsTrend.get("outboundTrend")).intValue());
        coreMetrics.setTodayOutboundTrendType((String) statsTrend.get("outboundTrendType"));

        // 物资总量趋势（暂设为0，实际可按需计算）
        coreMetrics.setTotalMaterialsTrend(0);
        coreMetrics.setTotalMaterialsTrendType("up");

        // 待审核申请和捐赠
        Map<String, Object> appStats = applicationService.getStats();
        coreMetrics.setPendingApplications(appStats.get("pendingCount") != null ? ((Number) appStats.get("pendingCount")).longValue() : 0L);

        Map<String, Object> donationStats = donationService.getStats();
        coreMetrics.setPendingDonations(donationStats.get("pendingCount") != null ? ((Number) donationStats.get("pendingCount")).longValue() : 0L);

        // 库存预警
        List<Map<String, Object>> warningList = materialService.getWarningList();
        coreMetrics.setLowStockItems(warningList.size());

        // 库存预警趋势（暂设为0）
        coreMetrics.setLowStockItemsTrend(0);
        coreMetrics.setLowStockItemsTrendType("down");

        // 累计捐赠总额
        coreMetrics.setTotalDonationAmount(donationService.getTotalAmount());

        dashboard.setCoreMetrics(coreMetrics);

        // 2. 物资分类统计
        dashboard.setMaterialCategoryStats(buildMaterialCategoryStats());

        // 3. 趋势数据
        dashboard.setTrendData(buildTrendData("week"));

        // 4. 区域统计（需要实现）
        dashboard.setRegionStats(buildRegionStats());

        // 5. 预警列表
        dashboard.setWarningList(buildWarningList(warningList));

        // 6. 实时动态
        dashboard.setRealtimeActivities(buildRealtimeActivities());

        // 7. 近期操作日志
        dashboard.setOperationLogs(buildOperationLogs());

        return dashboard;
    }
    
    /**
     * 构建物资分类统计数据
     */
    private List<DashboardVO.CategoryStats> buildMaterialCategoryStats() {
        Map<String, Object> stats = materialService.getStats();
        List<DashboardVO.CategoryStats> result = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> typeStats = (List<Map<String, Object>>) stats.get("typeStats");
        if (typeStats == null || typeStats.isEmpty()) {
            return result;
        }

        // 计算总库存
        int totalStock = typeStats.stream()
                .mapToInt(m -> ((Number) m.get("stock")).intValue())
                .sum();

        for (Map<String, Object> item : typeStats) {
            DashboardVO.CategoryStats categoryStats = new DashboardVO.CategoryStats();
            categoryStats.setCategory((String) item.get("name"));
            categoryStats.setCount(((Number) item.get("stock")).intValue());
            if (totalStock > 0) {
                categoryStats.setPercentage(((Number) item.get("stock")).doubleValue() / totalStock * 100);
            } else {
                categoryStats.setPercentage(0.0);
            }
            result.add(categoryStats);
        }

        return result;
    }
    
    /**
     * 构建趋势数据
     */
    private DashboardVO.TrendData buildTrendData(String period) {
        LocalDate end = LocalDate.now();
        LocalDate start;
        if ("month".equals(period)) {
            start = end.minusMonths(1);
        } else if ("year".equals(period)) {
            start = end.minusYears(1);
        } else {
            start = end.minusWeeks(1);
        }

        String startDateStr = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDateStr = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Map<String, List<Map<String, Object>>> trendDataMap = inventoryLogService.getTrendDataByDateRange(startDateStr, endDateStr);

        List<String> dates = new ArrayList<>();
        List<Integer> inbound = new ArrayList<>();
        List<Integer> outbound = new ArrayList<>();

        List<Map<String, Object>> inboundList = trendDataMap.get("inbound");
        List<Map<String, Object>> outboundList = trendDataMap.get("outbound");

        Map<String, Integer> inboundMap = inboundList != null ? inboundList.stream()
                .collect(Collectors.toMap(m -> (String) m.get("date"), m -> ((Number) m.get("count")).intValue()))
                : new HashMap<>();
        Map<String, Integer> outboundMap = outboundList != null ? outboundList.stream()
                .collect(Collectors.toMap(m -> (String) m.get("date"), m -> ((Number) m.get("count")).intValue()))
                : new HashMap<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            dates.add(dateStr);
            inbound.add(inboundMap.getOrDefault(dateStr, 0));
            outbound.add(outboundMap.getOrDefault(dateStr, 0));
        }

        DashboardVO.TrendData trendData = new DashboardVO.TrendData();
        trendData.setDates(dates);
        trendData.setInbound(inbound);
        trendData.setOutbound(outbound);

        return trendData;
    }
    
    /**
     * 构建区域统计数据
     */
    private List<DashboardVO.RegionStats> buildRegionStats() {
        // TODO: 需要从申领记录中统计各省市的需求数据
        List<DashboardVO.RegionStats> result = new ArrayList<>();
        
        // 示例数据
        DashboardVO.RegionStats stats1 = new DashboardVO.RegionStats();
        stats1.setProvince("湖北省");
        stats1.setDemandCount(100);
        stats1.setFulfilledCount(80);
        stats1.setFulfillmentRate(80.0);
        result.add(stats1);
        
        return result;
    }
    
    /**
     * 构建预警列表
     */
    private List<DashboardVO.WarningItem> buildWarningList(List<Map<String, Object>> warningList) {
        List<DashboardVO.WarningItem> result = new ArrayList<>();
        
        if (warningList != null) {
            for (Map<String, Object> item : warningList) {
                if (item != null) {
                    DashboardVO.WarningItem warningItem = new DashboardVO.WarningItem();
                    warningItem.setMaterialId(item.get("id") != null ? item.get("id").toString() : "");
                    warningItem.setMaterialName((String) item.get("name"));
                    warningItem.setCurrentStock(item.get("stock") != null ? ((Number) item.get("stock")).intValue() : 0);
                    warningItem.setWarningThreshold(item.get("threshold") != null ? ((Number) item.get("threshold")).intValue() : 0);
            
                    // 计算预警级别
                    int stock = warningItem.getCurrentStock();
                    int minStock = warningItem.getWarningThreshold();
                    if (stock <= minStock * 0.3) {
                        warningItem.setWarningLevel("high");
                    } else if (stock <= minStock * 0.6) {
                        warningItem.setWarningLevel("medium");
                    } else {
                        warningItem.setWarningLevel("low");
                    }
                    
                    result.add(warningItem);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 构建实时活动列表
     */
    private List<DashboardVO.RealtimeActivity> buildRealtimeActivities() {
        List<DashboardVO.RealtimeActivity> result = new ArrayList<>();
        
        // 获取最新的申领记录
        LambdaQueryWrapper<Application> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.orderByDesc(Application::getApplyTime).last("LIMIT 5");
        // 显式选择数据库中存在的字段
        appWrapper.select(
            Application::getId,
            Application::getStatus,
            Application::getApplyTime,
            Application::getPurpose
        );
        List<Application> applications = applicationService.list(appWrapper);
        
        for (Application app : applications) {
            DashboardVO.RealtimeActivity activity = new DashboardVO.RealtimeActivity();
            activity.setId(app.getId());
            activity.setType("application");
            activity.setTitle("物资申领");
            activity.setDescription(app.getPurpose());
            activity.setCreateTime(app.getApplyTime() != null ? app.getApplyTime().toString() : "");
            activity.setStatus(app.getStatus());
            result.add(activity);
        }
        
        // 获取最新的捐赠记录
        LambdaQueryWrapper<Donation> donationWrapper = new LambdaQueryWrapper<>();
        donationWrapper.orderByDesc(Donation::getDonateTime).last("LIMIT 5");
        // 显式选择数据库中存在的字段
        donationWrapper.select(
            Donation::getId,
            Donation::getStatus,
            Donation::getDonateTime,
            Donation::getRemark
        );
        List<Donation> donations = donationService.list(donationWrapper);
        
        for (Donation donation : donations) {
            DashboardVO.RealtimeActivity activity = new DashboardVO.RealtimeActivity();
            activity.setId(donation.getId());
            activity.setType("donation");
            activity.setTitle("物资捐赠");
            activity.setDescription(donation.getRemark());
            activity.setCreateTime(donation.getDonateTime() != null ? donation.getDonateTime().toString() : "");
            activity.setStatus(donation.getStatus());
            result.add(activity);
        }
        
        // 按时间排序并取最新 10 条
        result.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));

        return result.size() > 10 ? result.subList(0, 10) : result;
    }

    /**
     * 构建近期操作日志列表
     */
    private List<DashboardVO.OperateLogVO> buildOperationLogs() {
        List<DashboardVO.OperateLogVO> result = new ArrayList<>();

        try {
            // 通过 LogFeignClient 获取最新的 10 条操作日志
            Result<List<OperateLog>> response = logFeignClient.getRecentLogs(10);
            if (response != null && response.getData() != null) {
                for (OperateLog log : response.getData()) {
                    DashboardVO.OperateLogVO vo = new DashboardVO.OperateLogVO();
                    vo.setTime(log.getOperateTime() != null ? log.getOperateTime().toString() : "");
                    vo.setUser(log.getUsername());
                    vo.setAction(log.getOperation());
                    vo.setDetail(log.getMethod() + (log.getParams() != null ? " - " + log.getParams() : ""));
                    vo.setIp(log.getIp());
                    result.add(vo);
                }
            }
        } catch (Exception e) {
            // 日志服务不可用时返回空列表
            System.err.println("获取操作日志失败: " + e.getMessage());
        }

        return result;
    }
}

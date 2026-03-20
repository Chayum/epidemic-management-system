package com.epidemic.material.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.epidemic.common.entity.OperateLog;
import com.epidemic.common.feign.LogFeignClient;
import com.epidemic.common.result.Result;
import com.epidemic.material.entity.Application;
import com.epidemic.material.entity.DashboardVO;
import com.epidemic.material.entity.Donation;
import com.epidemic.material.service.ApplicationService;
import com.epidemic.material.service.DonationService;
import com.epidemic.material.service.InventoryLogService;
import com.epidemic.material.service.MaterialService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据统计控制器
 * 提供仪表盘、个人中心、趋势图等各类统计数据的聚合接口
 */
@Tag(name = "数据统计", description = "统计相关接口")
@RestController
@RequestMapping("/stats")
@CrossOrigin
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

    /**
     * 获取管理端仪表盘综合统计数据
     * 包括物资总量、待审核申请、待审核捐赠及库存预警数量
     *
     * @return 仪表盘统计数据Map
     */
    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> data = new HashMap<>();

        // 统计物资总数
        data.put("totalMaterials", materialService.count());

        // 统计待审核申请数
        Map<String, Object> appStats = applicationService.getStats();
        data.put("pendingApplications", appStats.get("pendingCount") != null ? appStats.get("pendingCount") : 0);

        // 统计待审核捐赠数
        Map<String, Object> donationStats = donationService.getStats();
        data.put("pendingDonations", donationStats.get("pendingCount") != null ? donationStats.get("pendingCount") : 0);

        // 统计库存预警物资数
        int lowStockItems = materialService.getWarningList() != null ? materialService.getWarningList().size() : 0;
        data.put("lowStockItems", lowStockItems);

        // 统计今日出入库
        Map<String, Integer> logStats = inventoryLogService.getTodayStats();
        int todayInbound = logStats.get("todayInbound") != null ? logStats.get("todayInbound") : 0;
        int todayOutbound = logStats.get("todayOutbound") != null ? logStats.get("todayOutbound") : 0;
        data.put("todayInbound", todayInbound);
        data.put("todayOutbound", todayOutbound);

        // 计算出入库趋势（与昨日对比）
        Map<String, Object> statsTrend = inventoryLogService.getStatsTrend();
        data.put("todayInboundTrend", statsTrend.get("inboundTrend"));
        data.put("todayInboundTrendType", statsTrend.get("inboundTrendType"));
        data.put("todayOutboundTrend", statsTrend.get("outboundTrend"));
        data.put("todayOutboundTrendType", statsTrend.get("outboundTrendType"));

        // 物资总量趋势（暂用库存总量对比）
        Long totalStock = materialService.getTotalStock();
        data.put("totalMaterialsTrend", 0);
        data.put("totalMaterialsTrendType", "up");

        // 库存预警趋势（与昨日预警数量对比）
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
    public Result<Map<String, Object>> getUserStats(@RequestHeader("X-User-Id") String userIdStr) {
        if (userIdStr == null) {
            return Result.error(401, "无效的Token或用户未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        
        Map<String, Object> data = new HashMap<>();
        
        // 统计我的申领总数
        LambdaQueryWrapper<Application> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.eq(Application::getApplicantId, userId);
        long myApplicationCount = applicationService.count(appWrapper);
        data.put("myApplicationCount", myApplicationCount);
        
        // 统计我的待审核申领数
        LambdaQueryWrapper<Application> pendingAppWrapper = new LambdaQueryWrapper<>();
        pendingAppWrapper.eq(Application::getApplicantId, userId)
                         .eq(Application::getStatus, "pending");
        long pendingApplicationCount = applicationService.count(pendingAppWrapper);
        data.put("pendingApplicationCount", pendingApplicationCount);
        
        // 统计我的捐赠总数
        LambdaQueryWrapper<Donation> donationWrapper = new LambdaQueryWrapper<>();
        donationWrapper.eq(Donation::getDonorId, userId);
        long myDonationCount = donationService.count(donationWrapper);
        data.put("myDonationCount", myDonationCount);

        // 统计我的待审核捐赠数
        LambdaQueryWrapper<Donation> pendingDonationWrapper = new LambdaQueryWrapper<>();
        pendingDonationWrapper.eq(Donation::getDonorId, userId)
                             .eq(Donation::getStatus, "pending");
        long myPendingDonationCount = donationService.count(pendingDonationWrapper);
        data.put("myPendingDonationCount", myPendingDonationCount);

        // 统计我的已通过捐赠数
        LambdaQueryWrapper<Donation> approvedDonationWrapper = new LambdaQueryWrapper<>();
        approvedDonationWrapper.eq(Donation::getDonorId, userId)
                              .eq(Donation::getStatus, "approved");
        long myApprovedDonationCount = donationService.count(approvedDonationWrapper);
        data.put("myApprovedDonationCount", myApprovedDonationCount);

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
        
        // 获取原始统计数据
        List<Map<String, Object>> inboundList = donationService.getTrendData(startDateStr);
        List<Map<String, Object>> outboundList = applicationService.getTrendData(startDateStr);
        
        // 数据预处理：转为 Map<日期, 数量> 方便查找
        List<String> dates = new ArrayList<>();
        List<Integer> inbound = new ArrayList<>();
        List<Integer> outbound = new ArrayList<>();
        
        Map<String, Integer> inboundMap = inboundList != null ? inboundList.stream().collect(Collectors.toMap(
            m -> (String) m.get("date"),
            m -> ((Number) m.get("count")).intValue()
        )) : new HashMap<>();
        
        Map<String, Integer> outboundMap = outboundList != null ? outboundList.stream().collect(Collectors.toMap(
            m -> (String) m.get("date"),
            m -> ((Number) m.get("count")).intValue()
        )) : new HashMap<>();
        
        // 补全日期并填充数据
        for (LocalDate date = start.plusDays(1); !date.isAfter(end); date = date.plusDays(1)) {
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
     *
     * @return 实时数据大屏 VO
     */
    @Operation(summary = "获取实时数据大屏数据")
    @GetMapping("/dashboard/full")
    public Result<DashboardVO> getFullDashboard() {
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
        coreMetrics.setTodayInboundTrend(((Double) statsTrend.get("inboundTrend")).intValue());
        coreMetrics.setTodayInboundTrendType((String) statsTrend.get("inboundTrendType"));
        coreMetrics.setTodayOutboundTrend(((Double) statsTrend.get("outboundTrend")).intValue());
        coreMetrics.setTodayOutboundTrendType((String) statsTrend.get("outboundTrendType"));

        // 物资总量趋势（暂设为0，实际可按需计算）
        coreMetrics.setTotalMaterialsTrend(0);
        coreMetrics.setTotalMaterialsTrendType("up");

        // 待审核申请和捐赠
        Map<String, Object> appStats = applicationService.getStats();
        coreMetrics.setPendingApplications(appStats.get("pendingCount") != null ? (Long) appStats.get("pendingCount") : 0L);

        Map<String, Object> donationStats = donationService.getStats();
        coreMetrics.setPendingDonations(donationStats.get("pendingCount") != null ? (Long) donationStats.get("pendingCount") : 0L);

        // 库存预警
        List<Map<String, Object>> warningList = materialService.getWarningList();
        coreMetrics.setLowStockItems(warningList.size());

        // 库存预警趋势（暂设为0）
        coreMetrics.setLowStockItemsTrend(0);
        coreMetrics.setLowStockItemsTrendType("down");

        // 累计捐赠总额和受益人数（需要实现对应方法）
        coreMetrics.setTotalDonationAmount(donationService.getTotalAmount());
        coreMetrics.setTotalBeneficiaries(applicationService.getTotalBeneficiaries());
        
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

        return Result.success(dashboard);
    }
    
    /**
     * 构建物资分类统计数据
     */
    private List<DashboardVO.CategoryStats> buildMaterialCategoryStats() {
        Map<String, Object> stats = materialService.getStats();
        List<DashboardVO.CategoryStats> result = new ArrayList<>();
        
        // 从 stats 中提取分类数据（需要根据实际返回结构调整）
        // 示例代码，实际需要根据 getStats() 的返回格式调整
        return result;
    }
    
    /**
     * 构建趋势数据
     */
    private DashboardVO.TrendData buildTrendData(String period) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusWeeks(1);
        
        List<String> dates = new ArrayList<>();
        List<Integer> inbound = new ArrayList<>();
        List<Integer> outbound = new ArrayList<>();
        
        for (LocalDate date = start.plusDays(1); !date.isAfter(end); date = date.plusDays(1)) {
            dates.add(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            // 这里需要调用实际的服务方法获取数据
            inbound.add(0);
            outbound.add(0);
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
                    warningItem.setCurrentStock(item.get("stock") != null ? (Integer) item.get("stock") : 0);
                    warningItem.setWarningThreshold(item.get("threshold") != null ? (Integer) item.get("threshold") : 0);
            
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
            // 通过 Feign 获取最新的 10 条操作日志
            Result<List<OperateLog>> response = logFeignClient.getRecentLogs(10);
            if (response.getCode() == 200 && response.getData() != null) {
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

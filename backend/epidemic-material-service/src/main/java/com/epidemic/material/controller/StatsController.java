package com.epidemic.material.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.epidemic.common.result.Result;
import com.epidemic.material.entity.Application;
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
        Map<String, Object> matStats = materialService.getStats();
        data.put("totalMaterials", materialService.count());
        
        // 统计待审核申请数
        Map<String, Object> appStats = applicationService.getStats();
        data.put("pendingApplications", appStats.get("pendingCount"));
        
        // 统计待审核捐赠数
        Map<String, Object> donationStats = donationService.getStats();
        data.put("pendingDonations", donationStats.get("pendingCount"));
        
        // 统计库存预警物资数
        data.put("lowStockItems", materialService.getWarningList().size());

        // 统计今日出入库
        Map<String, Integer> logStats = inventoryLogService.getTodayStats();
        data.put("todayInbound", logStats.get("todayInbound"));
        data.put("todayOutbound", logStats.get("todayOutbound"));
        
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
        
        Map<String, Integer> inboundMap = inboundList.stream().collect(Collectors.toMap(
            m -> (String) m.get("date"),
            m -> ((Number) m.get("count")).intValue()
        ));
        
        Map<String, Integer> outboundMap = outboundList.stream().collect(Collectors.toMap(
            m -> (String) m.get("date"),
            m -> ((Number) m.get("count")).intValue()
        ));
        
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
}

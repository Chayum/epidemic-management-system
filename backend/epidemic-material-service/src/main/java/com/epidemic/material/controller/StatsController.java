package com.epidemic.material.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.epidemic.common.result.Result;
import com.epidemic.material.entity.Application;
import com.epidemic.material.entity.Donation;
import com.epidemic.material.service.ApplicationService;
import com.epidemic.material.service.DonationService;
import com.epidemic.material.service.MaterialService;
import com.epidemic.material.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据统计控制器
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
    private JwtUtil jwtUtil;

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> data = new HashMap<>();
        // Aggregate data from services
        // Ideally services should provide specific methods or we use count()
        // For simplicity, we assume services expose count methods or we reuse getStats if available
        
        // Material stats
        Map<String, Object> matStats = materialService.getStats();
        data.put("totalMaterials", materialService.count());
        
        // Pending applications
        Map<String, Object> appStats = applicationService.getStats();
        data.put("pendingApplications", appStats.get("pendingCount"));
        
        // Pending donations
        Map<String, Object> donationStats = donationService.getStats();
        data.put("pendingDonations", donationStats.get("pendingCount"));
        
        // Low stock items
        data.put("lowStockItems", materialService.getWarningList().size());
        
        return Result.success("获取成功", data);
    }
    
    @Operation(summary = "获取用户个人统计数据")
    @GetMapping("/user")
    public Result<Map<String, Object>> getUserStats(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "无效的Token或用户未登录");
        }
        
        Map<String, Object> data = new HashMap<>();
        
        // 我的申领总数
        LambdaQueryWrapper<Application> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.eq(Application::getApplicantId, userId);
        long myApplicationCount = applicationService.count(appWrapper);
        data.put("myApplicationCount", myApplicationCount);
        
        // 我的待审核申领
        LambdaQueryWrapper<Application> pendingAppWrapper = new LambdaQueryWrapper<>();
        pendingAppWrapper.eq(Application::getApplicantId, userId)
                         .eq(Application::getStatus, "pending");
        long pendingApplicationCount = applicationService.count(pendingAppWrapper);
        data.put("pendingApplicationCount", pendingApplicationCount);
        
        // 我的捐赠总数
        LambdaQueryWrapper<Donation> donationWrapper = new LambdaQueryWrapper<>();
        donationWrapper.eq(Donation::getDonorId, userId);
        long myDonationCount = donationService.count(donationWrapper);
        data.put("myDonationCount", myDonationCount);
        
        return Result.success(data);
    }
    
    @Operation(summary = "获取库存预警列表")
    @GetMapping("/warning")
    public Result<List<Map<String, Object>>> getWarningList() {
        return Result.success(materialService.getWarningList());
    }

    @Operation(summary = "获取趋势数据")
    @GetMapping("/trend")
    public Result<Map<String, Object>> getTrendData(@RequestParam(defaultValue = "week") String period) {
        // Mock trend data as real implementation requires complex time-series queries
        Map<String, Object> data = new HashMap<>();
        data.put("dates", new String[]{"周一", "周二", "周三", "周四", "周五", "周六", "周日"});
        data.put("inbound", new int[]{120, 132, 101, 134, 190, 230, 210});
        data.put("outbound", new int[]{80, 92, 91, 124, 180, 150, 120});
        return Result.success("获取成功", data);
    }

    @Operation(summary = "获取物资统计数据")
    @GetMapping("/material")
    public Result<Map<String, Object>> getMaterialStats() {
        return Result.success(materialService.getStats());
    }

    @Operation(summary = "获取申请统计数据")
    @GetMapping("/application")
    public Result<Map<String, Object>> getApplicationStats() {
        return Result.success(applicationService.getStats());
    }

    @Operation(summary = "获取捐赠统计数据")
    @GetMapping("/donation")
    public Result<Map<String, Object>> getDonationStats() {
        return Result.success(donationService.getStats());
    }
}

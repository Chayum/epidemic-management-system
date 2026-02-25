package com.epidemic.material.controller;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import com.epidemic.material.dto.ApplicationApproveDTO;
import com.epidemic.material.dto.ApplicationQueryDTO;
import com.epidemic.material.dto.ApplicationSubmitDTO;
import com.epidemic.material.service.ApplicationService;
import com.epidemic.material.util.JwtUtil;
import com.epidemic.material.vo.ApplicationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 物资申请管理控制器
 */
@Tag(name = "物资申请管理", description = "物资申请相关接口")
@RestController
@RequestMapping("/application")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "获取申请列表")
    @GetMapping("/list")
    public Result<PageResult<ApplicationVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String applicantName) {
        ApplicationQueryDTO queryDTO = new ApplicationQueryDTO();
        queryDTO.setPage(page);
        queryDTO.setSize(size);
        queryDTO.setStatus(status);
        queryDTO.setApplicantName(applicantName);
        return Result.success(applicationService.getApplicationList(queryDTO));
    }

    @Operation(summary = "获取申请详情")
    @GetMapping("/{id}")
    public Result<ApplicationVO> getById(@PathVariable String id) {
        return Result.success(applicationService.getDetail(id));
    }

    @Operation(summary = "提交物资申请")
    @PostMapping
    public Result<String> submit(@Validated @RequestBody ApplicationSubmitDTO submitDTO, @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        if (userId == null) {
            return Result.error(401, "无效的Token或用户未登录");
        }
        // 如果无法获取用户名，使用默认值或ID
        if (username == null) {
            username = "User_" + userId;
        }
        applicationService.submitApplication(submitDTO, userId, username);
        return Result.success("提交成功");
    }

    @Operation(summary = "审核申请")
    @PostMapping("/approve")
    public Result<String> approve(@Validated @RequestBody ApplicationApproveDTO approveDTO, @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "无效的Token或用户未登录");
        }
        applicationService.approveApplication(approveDTO, userId);
        return Result.success("审核完成");
    }

    @Operation(summary = "取消申请")
    @PostMapping("/{id}/cancel")
    public Result<String> cancel(@PathVariable String id, @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "无效的Token或用户未登录");
        }
        applicationService.cancelApplication(id, userId);
        return Result.success("取消成功");
    }

    @Operation(summary = "获取我的申请")
    @GetMapping("/my")
    public Result<PageResult<ApplicationVO>> myApplications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "无效的Token或用户未登录");
        }
        ApplicationQueryDTO queryDTO = new ApplicationQueryDTO();
        queryDTO.setPage(page);
        queryDTO.setSize(size);
        queryDTO.setStatus(status);
        queryDTO.setApplicantId(userId);
        return Result.success(applicationService.getMyApplications(queryDTO));
    }

    @Operation(summary = "获取物流追踪信息")
    @GetMapping("/{id}/track")
    public Result<Map<String, Object>> track(@PathVariable String id) {
        return Result.success(applicationService.getTrackInfo(id));
    }
}

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
 * 提供物资申请的增删改查、审核、取消等功能接口
 */
@Tag(name = "物资申请管理", description = "物资申请相关接口")
@RestController
@RequestMapping("/application")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取申请列表
     * @param page 页码，默认为1
     * @param size 每页大小，默认为10
     * @param status 申请状态（可选）
     * @param applicantName 申请人姓名（可选）
     * @return 分页后的申请列表
     */
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

    /**
     * 获取申请详情
     * @param id 申请ID
     * @return 申请详情VO对象
     */
    @Operation(summary = "获取申请详情")
    @GetMapping("/{id}")
    public Result<ApplicationVO> getById(@PathVariable String id) {
        return Result.success(applicationService.getDetail(id));
    }

    /**
     * 提交物资申请
     * @param submitDTO 申请提交数据传输对象
     * @param token 用户Token，用于获取当前登录用户ID
     * @return 提交结果消息
     */
    @Operation(summary = "提交物资申请")
    @PostMapping
    public Result<String> submit(@Validated @RequestBody ApplicationSubmitDTO submitDTO, @RequestHeader("Authorization") String token) {
        // 从Token中解析用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        if (userId == null) {
            return Result.error(401, "无效的Token或用户未登录");
        }
        // 如果无法获取用户名，使用默认值或ID拼接
        if (username == null) {
            username = "User_" + userId;
        }
        applicationService.submitApplication(submitDTO, userId, username);
        return Result.success("提交成功");
    }

    /**
     * 审核申请
     * @param approveDTO 审核信息数据传输对象
     * @param token 用户Token，用于验证权限（实际业务中应校验管理员角色）
     * @return 审核结果消息
     */
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

    /**
     * 取消申请
     * @param id 申请ID
     * @param token 用户Token，用于验证操作人是否为申请人本人
     * @return 取消结果消息
     */
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

    /**
     * 获取我的申请列表
     * @param page 页码
     * @param size 每页大小
     * @param status 申请状态
     * @param token 用户Token
     * @return 当前用户的申请列表
     */
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

    /**
     * 获取物流追踪信息
     * @param id 申请ID
     * @return 物流信息Map
     */
    @Operation(summary = "获取物流追踪信息")
    @GetMapping("/{id}/track")
    public Result<Map<String, Object>> track(@PathVariable String id) {
        return Result.success(applicationService.getTrackInfo(id));
    }
}

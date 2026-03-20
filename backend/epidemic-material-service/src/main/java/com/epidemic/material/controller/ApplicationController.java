package com.epidemic.material.controller;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import com.epidemic.material.annotation.OperateLog;
import com.epidemic.material.dto.ApplicationApproveDTO;
import com.epidemic.material.dto.ApplicationQueryDTO;
import com.epidemic.material.dto.ApplicationSubmitDTO;
import com.epidemic.material.service.ApplicationService;
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
     * @param userIdStr 用户ID (从网关透传Header获取)
     * @param username 用户名 (从网关透传Header获取)
     * @return 提交结果消息
     */
    @Operation(summary = "提交物资申请")
    @PostMapping
    @OperateLog(module = "物资申请", operation = "提交申请")
    public Result<String> submit(@Validated @RequestBody ApplicationSubmitDTO submitDTO, 
                                 @RequestHeader("X-User-Id") String userIdStr,
                                 @RequestHeader(value = "X-User-Name", required = false) String username) {
        if (userIdStr == null) {
             return Result.error(401, "用户未登录");
        }
        Long userId = Long.valueOf(userIdStr);
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
     * @param userIdStr 用户ID
     * @param username 用户名
     * @return 审核结果消息
     */
    @Operation(summary = "审核申请")
    @PostMapping("/approve")
    @OperateLog(module = "物资申请", operation = "审核申请")
    public Result<String> approve(@Validated @RequestBody ApplicationApproveDTO approveDTO, 
                                  @RequestHeader("X-User-Id") String userIdStr,
                                  @RequestHeader(value = "X-User-Name", required = false) String username) {
        if (userIdStr == null) {
             return Result.error(401, "用户未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        // 如果无法获取用户名，使用默认值或ID拼接
        if (username == null) {
            username = "User_" + userId;
        }
        applicationService.approveApplication(approveDTO, userId, username);
        return Result.success("审核完成");
    }

    /**
     * 取消申请
     * @param id 申请ID
     * @param userIdStr 用户ID
     * @return 取消结果消息
     */
    @Operation(summary = "取消申请")
    @PostMapping("/{id}/cancel")
    @OperateLog(module = "物资申请", operation = "取消申请")
    public Result<String> cancel(@PathVariable String id, @RequestHeader("X-User-Id") String userIdStr) {
        if (userIdStr == null) {
             return Result.error(401, "用户未登录");
        }
        Long userId = Long.valueOf(userIdStr);
        applicationService.cancelApplication(id, userId);
        return Result.success("取消成功");
    }

    /**
     * 获取我的申请列表
     * @param page 页码
     * @param size 每页大小
     * @param status 申请状态
     * @param userIdStr 用户ID
     * @return 当前用户的申请列表
     */
    @Operation(summary = "获取我的申请")
    @GetMapping("/my")
    public Result<PageResult<ApplicationVO>> myApplications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestHeader("X-User-Id") String userIdStr) {
        if (userIdStr == null) {
             return Result.error(401, "用户未登录");
        }
        Long userId = Long.valueOf(userIdStr);
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

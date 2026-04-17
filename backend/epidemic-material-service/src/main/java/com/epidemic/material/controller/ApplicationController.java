package com.epidemic.material.controller;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import com.epidemic.common.util.UserContext;
import com.epidemic.common.annotation.OperateLog;
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
     * @param queryDTO 查询参数
     * @return 分页后的申请列表
     */
    @Operation(summary = "获取申请列表")
    @GetMapping("/list")
    public Result<PageResult<ApplicationVO>> list(@ModelAttribute ApplicationQueryDTO queryDTO) {
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
        // URL解码，防止+号被解析为空格（UUID中可能包含+字符）
        String decodedId = java.net.URLDecoder.decode(id, java.nio.charset.StandardCharsets.UTF_8);
        return Result.success(applicationService.getDetail(decodedId));
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
    public Result<String> submit(@Validated @RequestBody ApplicationSubmitDTO submitDTO) {
        Long userId = UserContext.getUserId();
        String username = UserContext.getUsernameOrDefault("User_");
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
    public Result<String> approve(@Validated @RequestBody ApplicationApproveDTO approveDTO) {
        Long userId = UserContext.getUserId();
        String username = UserContext.getUsernameOrDefault("User_");
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
    public Result<String> cancel(@PathVariable String id) {
        // URL解码，防止+号被解析为空格（UUID中可能包含+字符）
        String decodedId = java.net.URLDecoder.decode(id, java.nio.charset.StandardCharsets.UTF_8);
        Long userId = UserContext.getUserId();
        applicationService.cancelApplication(decodedId, userId);
        return Result.success("取消成功");
    }

    /**
     * 获取我的申请列表
     * @param queryDTO 查询参数
     * @return 当前用户的申请列表
     */
    @Operation(summary = "获取我的申请")
    @GetMapping("/my")
    public Result<PageResult<ApplicationVO>> myApplications(@ModelAttribute ApplicationQueryDTO queryDTO) {
        queryDTO.setApplicantId(UserContext.getUserId());
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
        // URL解码，防止+号被解析为空格（UUID中可能包含+字符）
        String decodedId = java.net.URLDecoder.decode(id, java.nio.charset.StandardCharsets.UTF_8);
        return Result.success(applicationService.getTrackInfo(decodedId));
    }

    /**
     * 确认收货
     * @param id 申请ID
     * @return 确认结果消息
     */
    @Operation(summary = "确认收货")
    @PostMapping("/{id}/confirm")
    @OperateLog(module = "物资申请", operation = "确认收货")
    public Result<String> confirmReceive(@PathVariable String id) {
        // URL解码，防止+号被解析为空格（UUID中可能包含+字符）
        String decodedId = java.net.URLDecoder.decode(id, java.nio.charset.StandardCharsets.UTF_8);
        Long userId = UserContext.getUserId();
        applicationService.confirmReceive(decodedId, userId);
        return Result.success("确认收货成功");
    }
}

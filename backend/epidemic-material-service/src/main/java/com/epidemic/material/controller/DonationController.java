package com.epidemic.material.controller;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import com.epidemic.common.util.UserContext;
import com.epidemic.common.annotation.OperateLog;
import com.epidemic.material.dto.DonationApproveDTO;
import com.epidemic.material.dto.DonationQueryDTO;
import com.epidemic.material.dto.DonationSubmitDTO;
import com.epidemic.material.service.DonationService;
import com.epidemic.material.vo.DonationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 捐赠管理控制器
 * 提供捐赠记录的查询、提交、审核等功能接口
 */
@Tag(name = "捐赠管理", description = "物资捐赠相关接口")
@RestController
@RequestMapping("/donation")
public class DonationController {

    @Autowired
    private DonationService donationService;


    /**
     *  获取捐赠列表
     * @param queryDTO
     * @return
     */
    @Operation(summary = "获取捐赠列表")
    @GetMapping("/list")
    public Result<PageResult<DonationVO>> list(@ModelAttribute DonationQueryDTO queryDTO) {
        return Result.success(donationService.getDonationList(queryDTO));
    }

    /**
     * 获取捐赠详情
     * @param id 捐赠ID
     * @return 捐赠详情VO对象
     */
    @Operation(summary = "获取捐赠详情")
    @GetMapping("/{id}")
    public Result<DonationVO> getById(@PathVariable String id) {
        DonationVO vo = donationService.getDetail(id);
        if (vo == null) {
            return Result.error(404, "捐赠记录不存在");
        }
        return Result.success(vo);
    }

    /**
     * 提交捐赠申请
     * @param submitDTO 捐赠提交数据传输对象
     * @param userIdStr 用户ID (从网关透传Header获取)
     * @param username 用户名 (从网关透传Header获取)
     * @return 提交结果消息
     */
    @Operation(summary = "提交捐赠申请")
    @PostMapping
    @OperateLog(module = "捐赠管理", operation = "提交捐赠")
    public Result<String> submit(@Validated @RequestBody DonationSubmitDTO submitDTO) {
        Long userId = UserContext.getUserIdOrNull();
        String username = UserContext.getUsernameOrDefault("User_");
        donationService.submitDonation(submitDTO, userId, username);
        return Result.success("提交成功");
    }

    /**
     * 获取我的捐赠记录
     * @param page 页码
     * @param size 每页大小
     * @param status 状态（可选）
     * @param id 捐赠ID（可选）
     * @param userIdStr 用户ID (从网关透传Header获取)
     * @return 当前用户的捐赠列表
     */
    @Operation(summary = "获取我的捐赠")
    @GetMapping("/my")
    public Result<PageResult<DonationVO>> myDonations(@ModelAttribute DonationQueryDTO queryDTO) {
        queryDTO.setDonorId(UserContext.getUserId());
        return Result.success(donationService.getDonationList(queryDTO));
    }

    /**
     * 审核捐赠
     * @param approveDTO 审核信息数据传输对象
     * @param userIdStr 用户ID (从网关透传Header获取)
     * @param username 用户名 (从网关透传Header获取)
     * @return 审核结果消息
     */
    @Operation(summary = "审核捐赠")
    @PostMapping("/approve")
    @OperateLog(module = "捐赠管理", operation = "审核捐赠")
    public Result<String> approve(@Validated @RequestBody DonationApproveDTO approveDTO) {
        Long userId = UserContext.getUserIdOrNull();
        String username = UserContext.getUsernameOrDefault("User_");

        donationService.approveDonation(approveDTO, userId, username);
        return Result.success("审核完成");
    }

    /**
     * 获取捐赠统计数据
     * @return 捐赠统计Map
     */
    @Operation(summary = "获取捐赠统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(donationService.getStats());
    }
}

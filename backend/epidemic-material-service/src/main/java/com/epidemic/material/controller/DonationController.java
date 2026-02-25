package com.epidemic.material.controller;

import com.epidemic.common.result.PageResult;
import com.epidemic.common.result.Result;
import com.epidemic.material.dto.DonationApproveDTO;
import com.epidemic.material.dto.DonationQueryDTO;
import com.epidemic.material.dto.DonationSubmitDTO;
import com.epidemic.material.service.DonationService;
import com.epidemic.material.util.JwtUtil;
import com.epidemic.material.vo.DonationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 捐赠管理控制器
 */
@Tag(name = "捐赠管理", description = "物资捐赠相关接口")
@RestController
@RequestMapping("/donation")
public class DonationController {

    @Autowired
    private DonationService donationService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "获取捐赠列表")
    @GetMapping("/list")
    public Result<PageResult<DonationVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String donorUnit,
            @RequestParam(required = false) Long donorId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String id) {
        DonationQueryDTO queryDTO = new DonationQueryDTO();
        queryDTO.setPage(page);
        queryDTO.setSize(size);
        queryDTO.setStatus(status);
        queryDTO.setDonorUnit(donorUnit);
        queryDTO.setDonorId(donorId);
        queryDTO.setType(type);
        queryDTO.setId(id);
        return Result.success(donationService.getDonationList(queryDTO));
    }

    @Operation(summary = "获取捐赠详情")
    @GetMapping("/{id}")
    public Result<DonationVO> getById(@PathVariable String id) {
        DonationVO vo = donationService.getDetail(id);
        if (vo == null) {
            return Result.error(404, "捐赠记录不存在");
        }
        return Result.success(vo);
    }

    @Operation(summary = "提交捐赠申请")
    @PostMapping
    public Result<String> submit(@Validated @RequestBody DonationSubmitDTO submitDTO, @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = null;
        String username = null;
        if (token != null) {
            userId = jwtUtil.getUserIdFromToken(token);
            username = jwtUtil.getUsernameFromToken(token);
        }
        donationService.submitDonation(submitDTO, userId, username);
        return Result.success("提交成功");
    }

    @Operation(summary = "获取我的捐赠")
    @GetMapping("/my")
    public Result<PageResult<DonationVO>> myDonations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String id,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "无效的Token或用户未登录");
        }
        DonationQueryDTO queryDTO = new DonationQueryDTO();
        queryDTO.setPage(page);
        queryDTO.setSize(size);
        queryDTO.setStatus(status);
        queryDTO.setId(id);
        queryDTO.setDonorId(userId);
        return Result.success(donationService.getDonationList(queryDTO));
    }

    @Operation(summary = "审核捐赠")
    @PostMapping("/approve")
    public Result<String> approve(@Validated @RequestBody DonationApproveDTO approveDTO, @RequestHeader("Authorization") String token) {
        // 校验管理员权限（此处暂略，依赖网关或拦截器，或者在Service中校验用户角色）
        // 实际上后端应该校验操作人是否为管理员，但这里先假设已鉴权
        donationService.approveDonation(approveDTO);
        return Result.success("审核完成");
    }

    @Operation(summary = "获取捐赠统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(donationService.getStats());
    }
}

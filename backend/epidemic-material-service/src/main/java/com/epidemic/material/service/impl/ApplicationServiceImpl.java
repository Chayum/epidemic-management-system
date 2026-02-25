package com.epidemic.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epidemic.common.exception.BusinessException;
import com.epidemic.common.result.PageResult;
import com.epidemic.material.convert.ApplicationConverter;
import com.epidemic.material.dto.ApplicationApproveDTO;
import com.epidemic.material.dto.ApplicationQueryDTO;
import com.epidemic.material.dto.ApplicationSubmitDTO;
import com.epidemic.material.entity.Application;
import com.epidemic.material.entity.Material;
import com.epidemic.material.mapper.ApplicationMapper;
import com.epidemic.material.mapper.MaterialMapper;
import com.epidemic.material.service.ApplicationService;
import com.epidemic.material.vo.ApplicationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 申请服务实现类
 */
@Slf4j
@Service
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application> implements ApplicationService {

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private ApplicationConverter applicationConverter;

    @Override
    public PageResult<ApplicationVO> getApplicationList(ApplicationQueryDTO queryDTO) {
        Page<Application> pageParam = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(Application::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getApplicantId() != null) {
            wrapper.eq(Application::getApplicantId, queryDTO.getApplicantId());
        }
        
        wrapper.orderByDesc(Application::getApplyTime);
        
        Page<Application> result = baseMapper.selectPage(pageParam, wrapper);
        List<ApplicationVO> voList = applicationConverter.toVOList(result.getRecords());
        
        return PageResult.of(voList, result.getTotal(), queryDTO.getPage(), queryDTO.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitApplication(ApplicationSubmitDTO submitDTO, Long applicantId, String applicantName) {
        // 1. 校验物资
        Material material = materialMapper.selectById(submitDTO.getMaterialId());
        if (material == null) {
            throw new BusinessException("物资不存在");
        }
        
        // 2. 校验库存（虽然是提交申请，但也可以预先校验一下）
        if (material.getStock() < submitDTO.getQuantity()) {
            throw new BusinessException("库存不足，当前库存: " + material.getStock());
        }

        // 3. 转换实体
        Application application = applicationConverter.toEntity(submitDTO);
        application.setApplicantId(applicantId);
        application.setApplicantName(applicantName);
        application.setMaterialName(material.getName());
        application.setUnit(material.getUnit() != null ? material.getUnit() : "个");
        application.setId("A" + System.currentTimeMillis());
        application.setStatus("pending");
        application.setApplyTime(LocalDateTime.now());
        
        // 4. 保存
        baseMapper.insert(application);
        log.info("用户[{}]提交了物资申请: {}", applicantId, application.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveApplication(ApplicationApproveDTO approveDTO, Long approverId) {
        Application application = baseMapper.selectById(approveDTO.getApplicationId());
        if (application == null) {
            throw new BusinessException("申请单不存在");
        }
        if (!"pending".equals(application.getStatus())) {
            throw new BusinessException("该申请单已处理，无需重复审批");
        }
        
        if ("approved".equals(approveDTO.getStatus())) {
            // 扣减库存
            Material material = materialMapper.selectById(application.getMaterialId());
            if (material == null) {
                throw new BusinessException("物资不存在");
            }
            if (material.getStock() < application.getQuantity()) {
                throw new BusinessException("库存不足，无法审批通过");
            }
            material.setStock(material.getStock() - application.getQuantity());
            materialMapper.updateById(material);
        }
        
        application.setStatus(approveDTO.getStatus());
        application.setApproveRemark(approveDTO.getRemark());
        application.setApproveTime(LocalDateTime.now());
        application.setApproverId(approverId);
        baseMapper.updateById(application);
        log.info("管理员[{}]审批了物资申请: {}, 结果: {}", approverId, application.getId(), approveDTO.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelApplication(String applicationId, Long userId) {
        Application application = baseMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("申请单不存在");
        }
        if (!application.getApplicantId().equals(userId)) {
            throw new BusinessException("无权取消他人的申请");
        }
        if (!"pending".equals(application.getStatus())) {
            throw new BusinessException("只能取消待审核状态的申请");
        }
        application.setStatus("cancelled");
        baseMapper.updateById(application);
        log.info("用户[{}]取消了物资申请: {}", userId, applicationId);
    }

    @Override
    public PageResult<ApplicationVO> getMyApplications(ApplicationQueryDTO queryDTO) {
        // 复用 getApplicationList，只要 queryDTO 中设置了 applicantId
        return getApplicationList(queryDTO);
    }

    @Override
    public ApplicationVO getDetail(String id) {
        Application application = baseMapper.selectById(id);
        if (application == null) {
            throw new BusinessException("申请单不存在");
        }
        return applicationConverter.toVO(application);
    }

    @Override
    public Map<String, Object> getTrackInfo(String applicationId) {
        Map<String, Object> trackInfo = new HashMap<>();
        trackInfo.put("applicationId", applicationId);
        
        Application application = baseMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("申请单不存在");
        }
        
        trackInfo.put("status", application.getStatus());
        
        List<Map<String, Object>> tracks = new ArrayList<>();
        
        // 提交记录
        Map<String, Object> track1 = new HashMap<>();
        track1.put("time", application.getApplyTime());
        track1.put("status", "submitted");
        track1.put("description", "申请已提交，等待审核");
        tracks.add(track1);
        
        // 审批记录
        if (application.getApproveTime() != null) {
            Map<String, Object> track2 = new HashMap<>();
            track2.put("time", application.getApproveTime());
            String status = application.getStatus();
            track2.put("status", status);
            String desc = "approved".equals(status) ? "审核通过，物资准备出库" : "审核驳回: " + application.getApproveRemark();
            track2.put("description", desc);
            tracks.add(track2);
            
            // 如果是通过，模拟发货
            if ("approved".equals(status)) {
                Map<String, Object> track3 = new HashMap<>();
                track3.put("time", LocalDateTime.now());
                track3.put("status", "shipped");
                track3.put("description", "物资已出库，正在配送中");
                tracks.add(track3);
            }
        }
        
        trackInfo.put("tracks", tracks);
        return trackInfo;
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalApplications", count());
        
        LambdaQueryWrapper<Application> approvedWrapper = new LambdaQueryWrapper<>();
        approvedWrapper.eq(Application::getStatus, "approved");
        stats.put("approvedCount", count(approvedWrapper));
        
        LambdaQueryWrapper<Application> rejectedWrapper = new LambdaQueryWrapper<>();
        rejectedWrapper.eq(Application::getStatus, "rejected");
        stats.put("rejectedCount", count(rejectedWrapper));
        
        LambdaQueryWrapper<Application> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Application::getStatus, "pending");
        stats.put("pendingCount", count(pendingWrapper));
        
        return stats;
    }
}

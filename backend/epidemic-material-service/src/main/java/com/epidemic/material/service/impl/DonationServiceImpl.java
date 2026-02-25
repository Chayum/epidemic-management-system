package com.epidemic.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.epidemic.common.exception.BusinessException;
import com.epidemic.common.result.PageResult;
import com.epidemic.material.convert.DonationConverter;
import com.epidemic.material.dto.DonationApproveDTO;
import com.epidemic.material.dto.DonationQueryDTO;
import com.epidemic.material.dto.DonationSubmitDTO;
import com.epidemic.material.entity.Donation;
import com.epidemic.material.entity.Material;
import com.epidemic.material.mapper.DonationMapper;
import com.epidemic.material.mapper.MaterialMapper;
import com.epidemic.material.service.DonationService;
import com.epidemic.material.vo.DonationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 捐赠服务实现类
 */
@Slf4j
@Service
public class DonationServiceImpl extends ServiceImpl<DonationMapper, Donation> implements DonationService {

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private DonationConverter donationConverter;

    @Override
    public PageResult<DonationVO> getDonationList(DonationQueryDTO queryDTO) {
        Page<Donation> pageParam = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        LambdaQueryWrapper<Donation> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(Donation::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getDonorUnit())) {
            wrapper.like(Donation::getDonorUnit, queryDTO.getDonorUnit());
        }
        if (queryDTO.getDonorId() != null) {
            wrapper.eq(Donation::getDonorId, queryDTO.getDonorId());
        }
        if (StringUtils.hasText(queryDTO.getType())) {
            wrapper.eq(Donation::getType, queryDTO.getType());
        }
        if (StringUtils.hasText(queryDTO.getId())) {
            wrapper.eq(Donation::getId, queryDTO.getId());
        }
        
        wrapper.orderByDesc(Donation::getDonateTime);
        
        Page<Donation> result = baseMapper.selectPage(pageParam, wrapper);
        List<DonationVO> voList = donationConverter.toVOList(result.getRecords());
        
        return PageResult.of(voList, result.getTotal(), queryDTO.getPage(), queryDTO.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitDonation(DonationSubmitDTO submitDTO, Long userId, String username) {
        Donation donation = donationConverter.toEntity(submitDTO);
        donation.setId("D" + System.currentTimeMillis());
        donation.setStatus("pending");
        donation.setDonateTime(LocalDateTime.now());
        
        if (userId != null) {
            donation.setDonorId(userId);
            // 如果DTO中没有填单位名称，且用户已登录，可以考虑使用用户所属单位
            // 但这里简单起见，如果DTO填了就用DTO的，否则前端应该必填
        }
        
        // 为 unit 字段设置默认值
        if (!StringUtils.hasText(donation.getUnit())) {
            donation.setUnit("件");
        }
        
        baseMapper.insert(donation);
        log.info("收到新的捐赠申请: {}", donation.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveDonation(DonationApproveDTO approveDTO) {
        Donation donation = baseMapper.selectById(approveDTO.getDonationId());
        if (donation == null) {
            throw new BusinessException("捐赠记录不存在");
        }
        if (!"pending".equals(donation.getStatus())) {
            throw new BusinessException("该捐赠已处理，无需重复审批");
        }
        
        if ("approved".equals(approveDTO.getStatus())) {
            // 如果指定了入库物资ID，则增加库存
            if (StringUtils.hasText(approveDTO.getTargetMaterialId())) {
                Material material = materialMapper.selectById(approveDTO.getTargetMaterialId());
                if (material == null) {
                    throw new BusinessException("目标物资不存在，无法入库");
                }
                // 增加库存
                material.setStock(material.getStock() + donation.getQuantity());
                materialMapper.updateById(material);
                log.info("捐赠[{}]审核通过，物资[{}]库存增加: {}", donation.getId(), material.getName(), donation.getQuantity());
            } else {
                log.info("捐赠[{}]审核通过，但未指定入库物资，仅记录", donation.getId());
            }
        }
        
        donation.setStatus(approveDTO.getStatus());
        donation.setRemark(approveDTO.getRemark());
        donation.setApproveTime(LocalDateTime.now());
        baseMapper.updateById(donation);
    }

    @Override
    public DonationVO getDetail(String id) {
        Donation donation = baseMapper.selectById(id);
        if (donation == null) {
            // 返回null而不是抛出异常，让Controller或前端处理
            // 或者抛出特定的业务异常
            // 这里为了避免前端显示“系统异常”，我们返回null，然后在Controller判空
            return null;
        }
        return donationConverter.toVO(donation);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDonations", count());
        
        // 统计审核通过的总物资数量
        stats.put("totalQuantity", baseMapper.sumApprovedQuantity()); 
        
        LambdaQueryWrapper<Donation> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Donation::getStatus, "pending");
        stats.put("pendingCount", count(pendingWrapper));
        
        LambdaQueryWrapper<Donation> approvedWrapper = new LambdaQueryWrapper<>();
        approvedWrapper.eq(Donation::getStatus, "approved");
        stats.put("approvedCount", count(approvedWrapper));
        
        return stats;
    }
}

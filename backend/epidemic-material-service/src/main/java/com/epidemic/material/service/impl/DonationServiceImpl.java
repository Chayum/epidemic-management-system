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
import java.util.UUID;
import com.epidemic.material.dto.StockOrderDTO;
import com.epidemic.material.service.StockService;
import java.math.BigDecimal;
import java.util.Collections;

/**
 * 捐赠服务实现类
 * 处理捐赠申请的提交、审核、查询及统计，并负责与物资库存进行联动
 */
@Slf4j
@Service
public class DonationServiceImpl extends ServiceImpl<DonationMapper, Donation> implements DonationService {

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private DonationConverter donationConverter;

    @Autowired
    private StockService stockService;

    /**
     * 分页查询捐赠列表
     * 支持多条件组合查询（状态、单位、捐赠人、类型、单号）
     *
     * @param queryDTO 查询参数DTO
     * @return 捐赠记录VO分页对象
     */
    @Override
    public PageResult<DonationVO> getDonationList(DonationQueryDTO queryDTO) {
        Page<Donation> pageParam = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        LambdaQueryWrapper<Donation> wrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
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
        
        // 按捐赠时间倒序排列
        wrapper.orderByDesc(Donation::getDonateTime);
        
        Page<Donation> result = baseMapper.selectPage(pageParam, wrapper);
        List<DonationVO> voList = donationConverter.toVOList(result.getRecords());
        
        return PageResult.of(voList, result.getTotal(), queryDTO.getPage(), queryDTO.getSize());
    }

    /**
     * 提交捐赠申请
     *
     * @param submitDTO 提交参数
     * @param userId 当前登录用户ID（可为空）
     * @param username 当前登录用户名（可为空）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitDonation(DonationSubmitDTO submitDTO, Long userId, String username) {
        Donation donation = donationConverter.toEntity(submitDTO);
        donation.setId("D" + UUID.randomUUID().toString().replace("-", "").substring(0, 16)); // 生成捐赠单号
        donation.setStatus("pending"); // 初始状态为待审核
        donation.setDonateTime(LocalDateTime.now());
        
        if (userId != null) {
            donation.setDonorId(userId);
        }
        
        // 为 unit 字段设置默认值
        if (!StringUtils.hasText(donation.getUnit())) {
            donation.setUnit("件");
        }
        
        baseMapper.insert(donation);
        log.info("收到新的捐赠申请: {}", donation.getId());
    }

    /**
     * 审核捐赠申请
     * 若审核通过且指定了目标物资，会自动创建入库单并增加库存
     *
     * @param approveDTO 审核参数（包含审核状态、目标物资ID、备注）
     * @param userId 操作人ID
     * @param username 操作人姓名
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveDonation(DonationApproveDTO approveDTO, Long userId, String username) {
        Donation donation = baseMapper.selectById(approveDTO.getDonationId());
        if (donation == null) {
            throw new BusinessException("捐赠记录不存在");
        }
        if (!"pending".equals(donation.getStatus())) {
            throw new BusinessException("该捐赠已处理，无需重复审批");
        }
        
        if ("approved".equals(approveDTO.getStatus())) {
            // 如果指定了入库物资ID，则创建入库单
            if (StringUtils.hasText(approveDTO.getTargetMaterialId())) {
                Material material = materialMapper.selectById(approveDTO.getTargetMaterialId());
                if (material == null) {
                    throw new BusinessException("目标物资不存在，无法入库");
                }
                
                // 创建入库单
                StockOrderDTO orderDTO = new StockOrderDTO();
                orderDTO.setType("inbound");
                orderDTO.setSourceType("donation");
                orderDTO.setSourceId(donation.getId());
                orderDTO.setSupplier(donation.getDonorUnit());
                orderDTO.setRemark("捐赠入库: " + donation.getRemark());
                
                StockOrderDTO.StockOrderItemDTO itemDTO = new StockOrderDTO.StockOrderItemDTO();
                itemDTO.setMaterialId(material.getId());
                itemDTO.setQuantity(donation.getQuantity());
                itemDTO.setPrice(BigDecimal.ZERO); // 捐赠通常无价格，或者设为0
                itemDTO.setRemark(donation.getRemark());
                
                orderDTO.setItems(Collections.singletonList(itemDTO));
                
                String orderId = stockService.createOrder(orderDTO, userId, username);
                // 自动审核入库单
                stockService.auditOrder(orderId, userId, username, true, "自动审核捐赠入库");
                
                log.info("捐赠[{}]审核通过，已生成入库单[{}]", donation.getId(), orderId);
            } else {
                log.info("捐赠[{}]审核通过，但未指定入库物资，仅记录", donation.getId());
            }
        }
        
        // 更新捐赠单状态
        donation.setStatus(approveDTO.getStatus());
        donation.setRemark(approveDTO.getRemark());
        donation.setApproveTime(LocalDateTime.now());
        baseMapper.updateById(donation);
    }

    /**
     * 获取捐赠详情
     *
     * @param id 捐赠单ID
     * @return 捐赠详情VO，不存在时返回null
     */
    @Override
    public DonationVO getDetail(String id) {
        Donation donation = baseMapper.selectById(id);
        if (donation == null) {
            return null;
        }
        return donationConverter.toVO(donation);
    }

    /**
     * 获取捐赠统计概览
     * 包括总捐赠数、已通过物资总量、待审核及已通过数量
     *
     * @return 统计Map
     */
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

    /**
     * 获取捐赠（入库）趋势数据
     *
     * @param startDate 统计开始日期
     * @return 每日入库统计列表
     */
    @Override
    public List<Map<String, Object>> getTrendData(String startDate) {
        return baseMapper.countApprovedByDate(startDate);
    }
    
    /**
     * 获取累计捐赠总额
     */
    @Override
    public Double getTotalAmount() {
        // TODO: 如果 Donation 实体类有 amount 字段，可以计算总金额
        // 暂时返回 0 或者根据 quantity 估算
        LambdaQueryWrapper<Donation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Donation::getStatus, "approved");
        List<Donation> donations = list(wrapper);
        
        // 这里简单估算，实际应该有金额字段
        return 0.0;
    }
}

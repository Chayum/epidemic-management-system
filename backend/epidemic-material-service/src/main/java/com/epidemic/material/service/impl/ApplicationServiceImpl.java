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
import com.epidemic.material.entity.ApplicationTrack;
import com.epidemic.material.entity.Material;
import com.epidemic.material.mapper.ApplicationMapper;
import com.epidemic.material.mapper.ApplicationTrackMapper;
import com.epidemic.material.mapper.MaterialMapper;
import com.epidemic.material.service.ApplicationService;
import com.epidemic.material.service.CacheService;
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
import com.epidemic.material.dto.StockOrderDTO;
import com.epidemic.material.service.StockService;
import java.math.BigDecimal;
import java.util.Collections;

/**
 * 申请服务实现类
 * 负责物资申领的业务逻辑处理，包括申请提交、审核、库存扣减、状态流转及轨迹追踪
 */
@Slf4j
@Service
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application> implements ApplicationService {

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private ApplicationConverter applicationConverter;

    @Autowired
    private StockService stockService;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private ApplicationTrackMapper applicationTrackMapper;

    /**
     * 分页查询申请列表
     * 支持根据状态和申请人ID进行筛选
     *
     * @param queryDTO 查询参数DTO
     * @return 申请记录VO分页对象
     */
    @Override
    public PageResult<ApplicationVO> getApplicationList(ApplicationQueryDTO queryDTO) {
        Page<Application> pageParam = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(Application::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getApplicantId() != null) {
            wrapper.eq(Application::getApplicantId, queryDTO.getApplicantId());
        }
        
        // 按申请时间倒序排列
        wrapper.orderByDesc(Application::getApplyTime);
        
        Page<Application> result = baseMapper.selectPage(pageParam, wrapper);
        List<ApplicationVO> voList = applicationConverter.toVOList(result.getRecords());
        
        return PageResult.of(voList, result.getTotal(), queryDTO.getPage(), queryDTO.getSize());
    }

    /**
     * 提交物资申请
     * 校验物资存在性及库存，生成申请单并初始化状态
     *
     * @param submitDTO 提交参数（包含物资ID、数量、用途等）
     * @param applicantId 申请人ID
     * @param applicantName 申请人姓名
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitApplication(ApplicationSubmitDTO submitDTO, Long applicantId, String applicantName) {
        // 1. 校验物资是否存在
        Material material = materialMapper.selectById(submitDTO.getMaterialId());
        if (material == null) {
            throw new BusinessException("物资不存在");
        }
        
        // 2. 校验库存（预先校验，防止库存严重不足时仍提交申请）
        if (material.getStock() < submitDTO.getQuantity()) {
            throw new BusinessException("库存不足，当前库存: " + material.getStock());
        }

        // 3. 转换实体并补充信息
        Application application = applicationConverter.toEntity(submitDTO);
        application.setApplicantId(applicantId);
        application.setApplicantName(applicantName);
        application.setMaterialName(material.getName());
        application.setUnit(material.getUnit() != null ? material.getUnit() : "个");
        application.setId("A" + System.currentTimeMillis()); // 生成申请单号
        application.setStatus("pending"); // 初始状态：待审核
        application.setApplyTime(LocalDateTime.now());
        
        // 4. 保存到数据库
        baseMapper.insert(application);

        // 5. 插入追踪记录
        addTrackRecord(application.getId(), "pending", "提交物资申请，等待审核", applicantId, applicantName);

        log.info("用户[{}]提交了物资申请: {}", applicantId, application.getId());
    }

    /**
     * 审核物资申请
     * 若审核通过，将自动创建出库单并扣减相应物资库存
     *
     * @param approveDTO 审核参数（包含申请ID、审核状态、备注）
     * @param approverId 审核人ID
     * @param approverName 审核人姓名
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveApplication(ApplicationApproveDTO approveDTO, Long approverId, String approverName) {
        Application application = baseMapper.selectById(approveDTO.getApplicationId());
        if (application == null) {
            throw new BusinessException("申请单不存在");
        }
        if (!"pending".equals(application.getStatus())) {
            throw new BusinessException("该申请单已处理，无需重复审批");
        }
        
        if ("approved".equals(approveDTO.getStatus())) {
            // 审核通过逻辑：校验并创建出库单
            Material material = materialMapper.selectById(application.getMaterialId());
            if (material == null) {
                throw new BusinessException("物资不存在");
            }
            if (material.getStock() < application.getQuantity()) {
                throw new BusinessException("库存不足，无法审批通过");
            }
            
            // 创建出库单
            StockOrderDTO orderDTO = new StockOrderDTO();
            orderDTO.setType("outbound");
            orderDTO.setSourceType("application");
            orderDTO.setSourceId(application.getId());
            orderDTO.setDepartment(application.getDepartment()); // 领用部门/单位
            orderDTO.setRemark("申领出库: " + application.getPurpose());
            
            StockOrderDTO.StockOrderItemDTO itemDTO = new StockOrderDTO.StockOrderItemDTO();
            itemDTO.setMaterialId(material.getId());
            itemDTO.setQuantity(application.getQuantity());
            itemDTO.setPrice(BigDecimal.ZERO); // 出库价格通常需计算成本，这里暂设0或由StockService计算
            itemDTO.setRemark(application.getPurpose());
            
            orderDTO.setItems(Collections.singletonList(itemDTO));
            
            String orderId = stockService.createOrder(orderDTO, approverId, approverName);
            // 自动审核出库单
            stockService.auditOrder(orderId, approverId, approverName, true, "自动审核申领出库");
            
            log.info("申请[{}]审核通过，已生成出库单[{}]", application.getId(), orderId);
        }
        
        // 更新申请单状态及审核信息
        application.setStatus(approveDTO.getStatus());
        application.setApproveRemark(approveDTO.getRemark());
        application.setApproveTime(LocalDateTime.now());
        application.setApproverId(approverId);
        application.setApproverName(approverName);
        baseMapper.updateById(application);

        // 添加追踪记录
        String trackDesc = "approved".equals(approveDTO.getStatus())
                ? "审核通过，物资准备出库"
                : "审核驳回: " + approveDTO.getRemark();
        addTrackRecord(application.getId(), approveDTO.getStatus(), trackDesc, approverId, approverName);

        // 清除申请单状态缓存
        cacheService.deleteApplicationStatus(application.getId());
        log.info("管理员 [{}] 审批了物资申请：{}, 结果：{}, 已清除缓存", approverId, application.getId(), approveDTO.getStatus());
    }

    /**
     * 取消物资申请
     * 仅允许申请人取消处于待审核状态的申请
     *
     * @param applicationId 申请单ID
     * @param userId 操作用户ID
     */
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

        // 添加追踪记录
        addTrackRecord(applicationId, "cancelled", "申请人取消申请", userId, application.getApplicantName());

        log.info("用户[{}]取消了物资申请: {}", userId, applicationId);
    }

    /**
     * 获取我的申请列表
     * 复用通用查询逻辑，强制限定applicantId
     *
     * @param queryDTO 查询参数
     * @return 我的申请列表
     */
    @Override
    public PageResult<ApplicationVO> getMyApplications(ApplicationQueryDTO queryDTO) {
        // 复用 getApplicationList，只要 queryDTO 中设置了 applicantId
        return getApplicationList(queryDTO);
    }

    /**
     * 获取申请详情
     *
     * @param id 申请单ID
     * @return 申请详情VO
     */
    @Override
    public ApplicationVO getDetail(String id) {
        Application application = baseMapper.selectById(id);
        if (application == null) {
            throw new BusinessException("申请单不存在");
        }
        return applicationConverter.toVO(application);
    }

    /**
     * 获取物流追踪信息
     * 从 application_track 表读取真实的追踪记录
     *
     * @param applicationId 申请单ID
     * @return 物流追踪信息Map
     */
    @Override
    public Map<String, Object> getTrackInfo(String applicationId) {
        Map<String, Object> trackInfo = new HashMap<>();
        trackInfo.put("applicationId", applicationId);

        Application application = baseMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("申请单不存在");
        }

        trackInfo.put("status", application.getStatus());

        // 从数据库读取真实的追踪记录
        List<ApplicationTrack> trackList = applicationTrackMapper.selectByApplicationId(applicationId);

        List<Map<String, Object>> tracks = new ArrayList<>();
        for (ApplicationTrack track : trackList) {
            Map<String, Object> trackMap = new HashMap<>();
            trackMap.put("time", track.getOperateTime());
            trackMap.put("status", track.getStatus());
            trackMap.put("description", track.getDescription());
            trackMap.put("operator", track.getOperatorName());
            tracks.add(trackMap);
        }

        trackInfo.put("tracks", tracks);
        return trackInfo;
    }

    /**
     * 获取申请统计概览
     * 包括总申请数、各状态（通过、驳回、待审核）的数量统计
     *
     * @return 统计Map
     */
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

    /**
     * 获取申请（出库）趋势数据
     *
     * @param startDate 统计开始日期
     * @return 每日出库统计列表
     */
    @Override
    public List<Map<String, Object>> getTrendData(String startDate) {
        return baseMapper.countApprovedByDate(startDate);
    }
    
    /**
     * 获取累计受益人数
     */
    @Override
    public Integer getTotalBeneficiaries() {
        // 统计所有已批准的申请中的受益人总数
        // 注意：如果数据库表中没有 beneficiaries 字段，这里暂时返回 0
        // 需要在数据库中添加 beneficiaries 字段后才能正确统计
        try {
            LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Application::getStatus, "approved");
            List<Application> applications = list(wrapper);
            
            // 尝试从 remark 或其他字段提取受益人信息（临时方案）
            // TODO: 需要在数据库中添加 beneficiaries 字段
            return 0;
        } catch (Exception e) {
            log.error("获取受益人数失败", e);
            return 0;
        }
    }

    /**
     * 添加追踪记录
     *
     * @param applicationId 申请单号
     * @param status 状态
     * @param description 描述
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    private void addTrackRecord(String applicationId, String status, String description, Long operatorId, String operatorName) {
        ApplicationTrack track = new ApplicationTrack();
        track.setApplicationId(applicationId);
        track.setStatus(status);
        track.setDescription(description);
        track.setOperatorId(operatorId);
        track.setOperatorName(operatorName);
        track.setOperateTime(LocalDateTime.now());
        applicationTrackMapper.insert(track);
    }
}

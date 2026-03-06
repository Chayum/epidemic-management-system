package com.epidemic.material.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epidemic.common.result.PageResult;
import com.epidemic.material.dto.ApplicationApproveDTO;
import com.epidemic.material.dto.ApplicationQueryDTO;
import com.epidemic.material.dto.ApplicationSubmitDTO;
import com.epidemic.material.entity.Application;
import com.epidemic.material.vo.ApplicationVO;

import java.util.List;
import java.util.Map;

/**
 * 申请服务接口
 */
public interface ApplicationService extends IService<Application> {

    /**
     * 获取申请列表
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<ApplicationVO> getApplicationList(ApplicationQueryDTO queryDTO);

    /**
     * 提交申请
     * @param submitDTO 提交参数
     * @param applicantId 申请人ID
     * @param applicantName 申请人姓名
     */
    void submitApplication(ApplicationSubmitDTO submitDTO, Long applicantId, String applicantName);

    /**
     * 审核申请
     * @param approveDTO 审批参数
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     */
    void approveApplication(ApplicationApproveDTO approveDTO, Long approverId, String approverName);

    /**
     * 取消申请
     * @param applicationId 申请单ID
     * @param userId 操作用户ID
     */
    void cancelApplication(String applicationId, Long userId);

    /**
     * 获取我的申请
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<ApplicationVO> getMyApplications(ApplicationQueryDTO queryDTO);

    /**
     * 获取申请详情
     * @param id 申请单ID
     * @return 详情VO
     */
    ApplicationVO getDetail(String id);

    /**
     * 获取物流追踪信息
     * @param applicationId 申请单ID
     * @return 追踪信息
     */
    Map<String, Object> getTrackInfo(String applicationId);

    /**
     * 获取申请统计
     * @return 统计数据
     */
    Map<String, Object> getStats();

    /**
     * 获取趋势数据
     * @param startDate 开始日期
     * @return 趋势数据列表
     */
    List<Map<String, Object>> getTrendData(String startDate);
    
    /**
     * 获取累计受益人数
     * @return 受益人数
     */
    Integer getTotalBeneficiaries();
}

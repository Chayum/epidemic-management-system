package com.epidemic.material.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epidemic.common.result.PageResult;
import com.epidemic.material.dto.DonationApproveDTO;
import com.epidemic.material.dto.DonationQueryDTO;
import com.epidemic.material.dto.DonationSubmitDTO;
import com.epidemic.material.entity.Donation;
import com.epidemic.material.vo.DonationVO;

import java.util.List;
import java.util.Map;

/**
 * 捐赠服务接口
 */
public interface DonationService extends IService<Donation> {

    /**
     * 获取捐赠列表
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<DonationVO> getDonationList(DonationQueryDTO queryDTO);

    /**
     * 提交捐赠申请
     * @param submitDTO 捐赠提交参数
     * @param userId 捐赠人ID(如果已登录)
     * @param username 捐赠人名称(如果已登录)
     */
    void submitDonation(DonationSubmitDTO submitDTO, Long userId, String username);

    /**
     * 审核捐赠
     * @param approveDTO 审批参数
     */
    void approveDonation(DonationApproveDTO approveDTO);

    /**
     * 获取捐赠详情
     * @param id 捐赠ID
     * @return 详情VO
     */
    DonationVO getDetail(String id);

    /**
     * 获取捐赠统计
     * @return 统计数据
     */
    Map<String, Object> getStats();

    /**
     * 获取趋势数据
     * @param startDate 开始日期
     * @return 趋势数据列表
     */
    List<Map<String, Object>> getTrendData(String startDate);
}

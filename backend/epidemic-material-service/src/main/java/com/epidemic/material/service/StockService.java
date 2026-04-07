package com.epidemic.material.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.epidemic.material.dto.StockOrderDTO;
import com.epidemic.material.dto.StockOrderQueryDTO;
import com.epidemic.material.entity.StockOrder;

import com.epidemic.material.vo.InventoryLedgerVO;

/**
 * 库存单据服务接口
 */
public interface StockService extends IService<StockOrder> {

    /**
     * 获取库存台账
     * @param page 页码
     * @param size 每页大小
     * @param keyword 关键字
     * @return 分页结果
     */
    Page<InventoryLedgerVO> getInventoryLedger(Integer page, Integer size, String keyword);

    /**
     * 创建库存单据
     * @param dto 单据信息
     * @param userId 操作人ID
     * @param username 操作人姓名
     * @return 单据ID
     */
    String createOrder(StockOrderDTO dto, Long userId, String username);

    /**
     * 创建库存单据（带自动审核）
     * @param dto 单据信息
     * @param userId 操作人ID
     * @param username 操作人姓名
     * @param autoApprove 是否自动审核通过
     * @return 单据ID
     */
    String createOrder(StockOrderDTO dto, Long userId, String username, boolean autoApprove);

    /**
     * 审核单据
     * @param orderId 单据ID
     * @param userId 审核人ID
     * @param username 审核人姓名
     * @param approved 是否通过
     * @param remark 审核意见
     */
    void auditOrder(String orderId, Long userId, String username, boolean approved, String remark);

    /**
     * 作废单据
     * @param orderId 单据ID
     * @param userId 操作人ID
     */
    void voidOrder(String orderId, Long userId);

    /**
     * 分页查询单据
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    Page<StockOrder> getOrderList(StockOrderQueryDTO queryDTO);
}

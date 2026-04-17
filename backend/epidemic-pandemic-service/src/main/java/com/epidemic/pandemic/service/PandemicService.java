package com.epidemic.pandemic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.epidemic.common.result.PageResult;
import com.epidemic.pandemic.dto.PushRecordQueryDTO;
import com.epidemic.pandemic.entity.PandemicNews;

import java.util.List;
import java.util.Map;

/**
 * 疫情信息服务接口
 */
public interface PandemicService extends IService<PandemicNews> {

    /**
     * 获取新闻列表
     * @param page 页码
     * @param size 每页数量
     * @param status 状态
     * @return 分页结果
     */
    PageResult<PandemicNews> getNewsList(Integer page, Integer size, String status);

    /**
     * 发布新闻
     * @param news 新闻信息
     */
    void publishNews(PandemicNews news);

    /**
     * 获取实时疫情数据
     * @return 疫情数据
     */
    Map<String, Object> getPandemicData();

    /**
     * 获取推送统计
     * @return 统计列表
     */
    List<Map<String, Object>> getPushStats();

    /**
     * 获取推送记录（不分页）
     * @return 记录列表
     */
    List<Map<String, Object>> getPushList();

    /**
     * 获取推送记录（分页）
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<Map<String, Object>> getPushListByPage(PushRecordQueryDTO queryDTO);

    /**
     * 发送推送
     * @param pushData 推送数据
     */
    void sendPush(Map<String, Object> pushData);

    /**
     * 获取用户角色分布统计
     * @return 角色统计列表
     */
    List<Map<String, Object>> getUserRoleStats();

    /**
     * 删除推送记录
     * @param id 推送记录ID
     */
    void deletePushRecord(Long id);
}

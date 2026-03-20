package com.epidemic.material.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 实时数据大屏展示数据实体
 * 用于封装大屏所需的各类统计数据
 */
@Data
public class DashboardVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 核心指标
     */
    private CoreMetrics coreMetrics;
    
    /**
     * 物资分类统计（饼图数据）
     */
    private List<CategoryStats> materialCategoryStats;
    
    /**
     * 物资入库与出库趋势（折线图数据）
     */
    private TrendData trendData;
    
    /**
     * 各省市物资需求分布（地图数据）
     */
    private List<RegionStats> regionStats;
    
    /**
     * 库存预警列表
     */
    private List<WarningItem> warningList;
    
    /**
     * 实时动态（最新申领和捐赠）
     */
    private List<RealtimeActivity> realtimeActivities;

    /**
     * 近期操作日志
     */
    private List<OperateLogVO> operationLogs;
    
    /**
     * 核心指标
     */
    @Data
    public static class CoreMetrics {
        /**
         * 物资总量
         */
        private Long totalMaterials;

        /**
         * 物资总量涨跌趋势百分比
         */
        private Integer totalMaterialsTrend;

        /**
         * 物资总量趋势类型（up/down）
         */
        private String totalMaterialsTrendType;

        /**
         * 可用库存总量
         */
        private Long totalStock;

        /**
         * 今日入库量
         */
        private Integer todayInbound;

        /**
         * 今日入库涨跌趋势百分比
         */
        private Integer todayInboundTrend;

        /**
         * 今日入库趋势类型（up/down）
         */
        private String todayInboundTrendType;

        /**
         * 今日出库量
         */
        private Integer todayOutbound;

        /**
         * 今日出库涨跌趋势百分比
         */
        private Integer todayOutboundTrend;

        /**
         * 今日出库趋势类型（up/down）
         */
        private String todayOutboundTrendType;

        /**
         * 待审核申请数
         */
        private Long pendingApplications;

        /**
         * 待审核捐赠数
         */
        private Long pendingDonations;

        /**
         * 库存预警物资数
         */
        private Integer lowStockItems;

        /**
         * 库存预警涨跌趋势百分比
         */
        private Integer lowStockItemsTrend;

        /**
         * 库存预警趋势类型（up/down）
         */
        private String lowStockItemsTrendType;

        /**
         * 累计捐赠总额
         */
        private Double totalDonationAmount;

        /**
         * 累计受益人数
         */
        private Integer totalBeneficiaries;
    }
    
    /**
     * 物资分类统计
     */
    @Data
    public static class CategoryStats {
        /**
         * 物资类别名称
         */
        private String category;
        
        /**
         * 数量
         */
        private Integer count;
        
        /**
         * 占比
         */
        private Double percentage;
    }
    
    /**
     * 趋势数据
     */
    @Data
    public static class TrendData {
        /**
         * 日期列表
         */
        private List<String> dates;
        
        /**
         * 入库数据
         */
        private List<Integer> inbound;
        
        /**
         * 出库数据
         */
        private List<Integer> outbound;
    }
    
    /**
     * 区域统计
     */
    @Data
    public static class RegionStats {
        /**
         * 省份名称
         */
        private String province;
        
        /**
         * 需求数量
         */
        private Integer demandCount;
        
        /**
         * 已满足数量
         */
        private Integer fulfilledCount;
        
        /**
         * 满足率
         */
        private Double fulfillmentRate;
    }
    
    /**
     * 预警项
     */
    @Data
    public static class WarningItem {
        /**
         * 物资 ID
         */
        private String materialId;
        
        /**
         * 物资名称
         */
        private String materialName;
        
        /**
         * 当前库存
         */
        private Integer currentStock;
        
        /**
         * 预警阈值
         */
        private Integer warningThreshold;
        
        /**
         * 预警级别：low-低，medium-中，high-高
         */
        private String warningLevel;
    }
    
    /**
     * 实时活动
     */
    @Data
    public static class RealtimeActivity {
        /**
         * 活动 ID
         */
        private String id;

        /**
         * 活动类型：application-申领，donation-捐赠
         */
        private String type;

        /**
         * 活动标题
         */
        private String title;

        /**
         * 活动描述
         */
        private String description;

        /**
         * 创建时间
         */
        private String createTime;

        /**
         * 状态
         */
        private String status;
    }

    /**
     * 操作日志VO
     */
    @Data
    public static class OperateLogVO {
        /**
         * 时间
         */
        private String time;

        /**
         * 操作人
         */
        private String user;

        /**
         * 操作类型
         */
        private String action;

        /**
         * 操作详情
         */
        private String detail;

        /**
         * IP地址
         */
        private String ip;
    }
}

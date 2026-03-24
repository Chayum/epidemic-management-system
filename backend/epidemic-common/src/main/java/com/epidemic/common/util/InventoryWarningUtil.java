package com.epidemic.common.util;

/**
 * 库存预警工具类
 * 统一管理库存预警状态的计算逻辑
 */
public class InventoryWarningUtil {

    /**
     * 库存预警状态：正常
     */
    public static final String STATUS_NORMAL = "normal";

    /**
     * 库存预警状态：预警
     */
    public static final String STATUS_WARNING = "warning";

    /**
     * 预警等级：高风险
     */
    public static final String LEVEL_HIGH = "high";

    /**
     * 预警等级：低风险
     */
    public static final String LEVEL_LOW = "low";

    /**
     * 根据库存和阈值计算预警状态
     *
     * @param stock     当前库存
     * @param threshold 预警阈值
     * @return 预警状态（normal/warning）
     */
    public static String calculateStatus(Integer stock, Integer threshold) {
        if (stock == null || threshold == null) {
            return STATUS_NORMAL;
        }
        return stock < threshold ? STATUS_WARNING : STATUS_NORMAL;
    }

    /**
     * 根据库存和阈值计算预警等级
     *
     * @param stock     当前库存
     * @param threshold 预警阈值
     * @return 预警等级（high/low）
     */
    public static String calculateWarningLevel(Integer stock, Integer threshold) {
        if (stock == null || threshold == null) {
            return LEVEL_LOW;
        }
        // 库存 < 阈值的一半为高风险，否则为低风险
        return stock < threshold * 0.5 ? LEVEL_HIGH : LEVEL_LOW;
    }

    /**
     * 判断库存是否低于阈值
     *
     * @param stock     当前库存
     * @param threshold 预警阈值
     * @return 是否低于阈值
     */
    public static boolean isBelowThreshold(Integer stock, Integer threshold) {
        if (stock == null || threshold == null) {
            return false;
        }
        return stock < threshold;
    }

    /**
     * 判断库存是否充足（库存 >= 阈值）
     *
     * @param stock     当前库存
     * @param threshold 预警阈值
     * @return 是否充足
     */
    public static boolean isSufficient(Integer stock, Integer threshold) {
        if (stock == null || threshold == null) {
            return false;
        }
        return stock >= threshold;
    }

    /**
     * 判断库存是否不足（库存 < 阈值 * 0.5）
     *
     * @param stock     当前库存
     * @param threshold 预警阈值
     * @return 是否不足
     */
    public static boolean isInsufficient(Integer stock, Integer threshold) {
        if (stock == null || threshold == null) {
            return false;
        }
        return stock < threshold * 0.5;
    }

    /**
     * 判断库存是否处于预警状态（阈值 * 0.5 <= 库存 < 阈值）
     *
     * @param stock     当前库存
     * @param threshold 预警阈值
     * @return 是否处于预警状态
     */
    public static boolean isWarning(Integer stock, Integer threshold) {
        if (stock == null || threshold == null) {
            return false;
        }
        return stock >= threshold * 0.5 && stock < threshold;
    }
}

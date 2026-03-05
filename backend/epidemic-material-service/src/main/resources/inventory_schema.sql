-- 库存变动管理子系统相关表

-- 库存单据主表（出入库单）
CREATE TABLE IF NOT EXISTS `stock_order` (
    `id` VARCHAR(50) NOT NULL COMMENT '单据编号',
    `type` VARCHAR(20) NOT NULL COMMENT '单据类型：inbound-入库单，outbound-出库单',
    `source_type` VARCHAR(20) DEFAULT NULL COMMENT '来源类型：purchase-采购，donation-捐赠，application-申领，transfer-调拨，check-盘点',
    `source_id` VARCHAR(50) DEFAULT NULL COMMENT '来源单号（如申领单ID）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态：draft-草稿，pending-待审核，approved-已审核，void-已作废',
    `total_amount` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '总金额',
    `supplier` VARCHAR(100) DEFAULT NULL COMMENT '供应商/来源单位',
    `department` VARCHAR(100) DEFAULT NULL COMMENT '领用部门/目标单位',
    `handler_id` BIGINT DEFAULT NULL COMMENT '经办人ID',
    `handler_name` VARCHAR(50) DEFAULT NULL COMMENT '经办人姓名',
    `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `auditor_name` VARCHAR(50) DEFAULT NULL COMMENT '审核人姓名',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存单据主表';

-- 库存单据明细表
CREATE TABLE IF NOT EXISTS `stock_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `order_id` VARCHAR(50) NOT NULL COMMENT '单据编号',
    `material_id` VARCHAR(50) NOT NULL COMMENT '物资ID',
    `material_name` VARCHAR(100) NOT NULL COMMENT '物资名称',
    `specification` VARCHAR(100) DEFAULT NULL COMMENT '规格型号',
    `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位',
    `quantity` INT NOT NULL COMMENT '数量',
    `price` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '单价',
    `amount` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '金额',
    `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    `expiry_date` DATE DEFAULT NULL COMMENT '有效期至',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存单据明细表';

-- 物资成本表（用于计算库存价值）
CREATE TABLE IF NOT EXISTS `material_cost` (
    `material_id` VARCHAR(50) NOT NULL COMMENT '物资ID',
    `avg_price` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '平均单价（成本）',
    `last_price` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '最新入库单价',
    `total_value` DECIMAL(15, 2) DEFAULT 0.00 COMMENT '当前库存总价值',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物资成本表';

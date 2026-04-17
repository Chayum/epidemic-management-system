-- ========================================
-- 用户角色系统迁移脚本
-- 将 hospital_user 和 community_staff 合并为 applicant
-- 新增捐赠证书表
-- 执行时间：2026-04-17
-- ========================================

-- ========================================
-- 第一步：备份数据
-- ========================================

-- 备份用户表
CREATE TABLE IF NOT EXISTS `sys_user_backup_20260417` AS
SELECT * FROM `sys_user`;

-- 验证备份
SELECT '用户表备份完成' AS message, COUNT(*) AS backup_count FROM `sys_user_backup_20260417`;

-- ========================================
-- 第二步：角色迁移
-- ========================================

-- 将 hospital_user 和 community_staff 合并为 applicant
UPDATE `sys_user`
SET `role` = 'applicant'
WHERE `role` IN ('hospital_user', 'community_staff');

-- 验证迁移结果
SELECT role, COUNT(*) AS count
FROM `sys_user`
GROUP BY role;

-- 预期结果：admin(3), applicant(15), donor(7)

-- ========================================
-- 第三步：更新推送消息目标
-- ========================================

-- 更新 epidemic_pandemic 数据库中的 push_record 表
-- 注意：表名是 push_record，不是 push_message
UPDATE `push_record`
SET `target` = 'applicant'
WHERE `target` IN ('hospital_user', 'community_staff');

-- ========================================
-- 第四步：新增捐赠证书表
-- ========================================

-- 创建捐赠证书表
CREATE TABLE IF NOT EXISTS `donation_certificate` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '证书ID',
    `donation_id` VARCHAR(50) NOT NULL COMMENT '捐赠ID',
    `donor_id` BIGINT NOT NULL COMMENT '捐赠人ID',
    `donor_name` VARCHAR(50) NOT NULL COMMENT '捐赠人姓名',
    `certificate_no` VARCHAR(50) NOT NULL COMMENT '证书编号',
    `material_type` VARCHAR(50) NOT NULL COMMENT '捐赠物资类型',
    `quantity` INT NOT NULL COMMENT '捐赠数量',
    `unit` VARCHAR(20) DEFAULT NULL COMMENT '物资单位',
    `issue_time` DATETIME NOT NULL COMMENT '颁发时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_donation_id` (`donation_id`),
    UNIQUE KEY `uk_certificate_no` (`certificate_no`),
    KEY `idx_donor_id` (`donor_id`),
    KEY `idx_issue_time` (`issue_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='捐赠证书表';

-- ========================================
-- 第五步：更新表字段注释
-- ========================================

-- 更新 sys_user 表的 role 字段注释
ALTER TABLE `sys_user`
MODIFY COLUMN `role` VARCHAR(20) NOT NULL COMMENT '角色：admin-管理员，applicant-申请方(医院/社区)，donor-捐赠方';

-- ========================================
-- 迁移完成验证
-- ========================================

-- 显示迁移后的角色分布
SELECT '===== 迁移后角色分布 =====' AS message;
SELECT role, COUNT(*) AS count FROM `sys_user` GROUP BY role ORDER BY role;

-- 显示证书表创建结果
SELECT '===== 捐赠证书表创建结果 =====' AS message;
SHOW CREATE TABLE `donation_certificate`;

-- ========================================
-- 脚本执行完成
-- ========================================
SELECT '角色迁移完成！' AS message;

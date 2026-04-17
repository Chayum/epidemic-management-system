-- ========================================
-- 疫情防控物资调度管理系统 - 用户服务数据库
-- 数据库版本：MySQL 8.0+
-- 字符集：utf8mb4
-- 创建时间：2026-03-19
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `epidemic_user` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `epidemic_user`;

-- ========================================
-- 用户表
-- ========================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `role` VARCHAR(20) NOT NULL COMMENT '角色：admin-管理员，applicant-申请方(医院/社区)，donor-捐赠方',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `unit` VARCHAR(100) DEFAULT NULL COMMENT '所属单位',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active-正常，disabled-禁用',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================
-- 操作日志表
-- ========================================
DROP TABLE IF EXISTS `sys_operate_log`;
CREATE TABLE `sys_operate_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `module` VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
    `operation` VARCHAR(100) DEFAULT NULL COMMENT '操作描述',
    `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
    `params` TEXT DEFAULT NULL COMMENT '请求参数',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    `error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
    `execute_time` BIGINT DEFAULT NULL COMMENT '执行时长（毫秒）',
    `operate_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_module` (`module`),
    KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ========================================
-- 初始化用户数据
-- ========================================

-- 管理员用户
INSERT INTO `sys_user` (`id`, `username`, `password`, `name`, `role`, `phone`, `unit`, `status`) VALUES
(1, 'admin', '123456', '系统管理员', 'admin', '13800138000', '疫情防控指挥部', 'active'),
(2, 'admin2', '123456', '李主任', 'admin', '13800138001', '疫情防控指挥部', 'active'),
(3, 'admin3', '123456', '张副主任', 'admin', '13800138002', '卫生健康委员会', 'active');

-- 申请方（医院/社区）
INSERT INTO `sys_user` (`id`, `username`, `password`, `name`, `role`, `phone`, `unit`, `status`) VALUES
(4, 'hospital1', '123456', '张医生', 'applicant', '13800138004', '市第一医院', 'active'),
(5, 'hospital2', '123456', '李护士', 'applicant', '13800138005', '市第一医院', 'active'),
(6, 'hospital3', '123456', '王主任', 'applicant', '13800138006', '市第二医院', 'active'),
(7, 'hospital4', '123456', '刘医生', 'applicant', '13800138007', '市第二医院', 'active'),
(8, 'hospital5', '123456', '陈护士长', 'applicant', '13800138008', '市中心医院', 'active'),
(9, 'hospital6', '123456', '赵医生', 'applicant', '13800138009', '市中心医院', 'active'),
(10, 'hospital7', '123456', '孙护士', 'applicant', '13800138010', '市中医院', 'active'),
(11, 'community1', '123456', '李主任', 'applicant', '13800138011', '和平社区居委会', 'active'),
(12, 'community2', '123456', '张干事', 'applicant', '13800138012', '和平社区居委会', 'active'),
(13, 'community3', '123456', '王书记', 'applicant', '13800138013', '解放社区居委会', 'active'),
(14, 'community4', '123456', '刘干事', 'applicant', '13800138014', '解放社区居委会', 'active'),
(15, 'community5', '123456', '陈主任', 'applicant', '13800138015', '建设社区居委会', 'active'),
(16, 'community6', '123456', '赵书记', 'applicant', '13800138016', '建设社区居委会', 'active'),
(17, 'community7', '123456', '周干事', 'applicant', '13800138017', '先锋社区居委会', 'active');

-- 捐赠方
INSERT INTO `sys_user` (`id`, `username`, `password`, `name`, `role`, `phone`, `unit`, `status`) VALUES
(18, 'donor1', '123456', '王经理', 'donor', '13800138018', '市慈善总会', 'active'),
(19, 'donor2', '123456', '李总', 'donor', '13800138019', '省红十字会', 'active'),
(20, 'donor3', '123456', '张董事长', 'donor', '13800138020', '华润医药集团', 'active'),
(21, 'donor4', '123456', '刘总裁', 'donor', '13800138021', '九州通医药集团', 'active'),
(22, 'donor5', '123456', '陈会长', 'donor', '13800138022', '市企业联合会', 'active'),
(23, 'donor6', '123456', '黄总经理', 'donor', '13800138023', '国药集团', 'active'),
(24, 'donor7', '123456', '周主管', 'donor', '13800138024', '海王生物集团', 'active');

-- 禁用状态用户
INSERT INTO `sys_user` (`id`, `username`, `password`, `name`, `role`, `phone`, `unit`, `status`) VALUES
(25, 'old_admin', '123456', '前任管理员', 'admin', '13800138025', '疫情防控指挥部', 'disabled'),
(26, 'old_applicant', '123456', '离职医生', 'applicant', '13800138026', '市第一医院', 'disabled');

-- ========================================
-- 脚本执行完成
-- ========================================
SELECT 'epidemic_user 数据库初始化完成！共24条用户数据' AS message;

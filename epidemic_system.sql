-- ========================================
-- 疫情防控物资调度管理系统 - 系统服务数据库
-- 数据库版本：MySQL 8.0+
-- 字符集：utf8mb4
-- 创建时间：2026-03-19
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `epidemic_system` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `epidemic_system`;

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
    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`),
    KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ========================================
-- 初始化操作日志数据
-- ========================================
INSERT INTO `sys_operate_log` (`user_id`, `username`, `module`, `operation`, `method`, `params`, `ip`, `status`, `error_msg`, `execute_time`, `operate_time`) VALUES
-- 用户管理模块日志
(1, 'admin', '用户管理', '用户登录', 'AuthController.login', '{"username":"admin"}', '192.168.1.100', 1, NULL, 156, '2026-03-19 08:30:15'),
(1, 'admin', '用户管理', '用户登出', 'AuthController.logout', '{}', '192.168.1.100', 1, NULL, 23, '2026-03-19 18:15:30'),
(2, 'admin2', '用户管理', '用户登录', 'AuthController.login', '{"username":"admin2"}', '192.168.1.101', 1, NULL, 145, '2026-03-19 08:45:20'),
(4, 'hospital1', '用户管理', '用户登录', 'AuthController.login', '{"username":"hospital1"}', '192.168.1.102', 1, NULL, 138, '2026-03-19 08:50:10'),
(11, 'community1', '用户管理', '用户登录', 'AuthController.login', '{"username":"community1"}', '192.168.1.103', 1, NULL, 152, '2026-03-19 09:00:05'),
(18, 'donor1', '用户管理', '用户登录', 'AuthController.login', '{"username":"donor1"}', '192.168.1.104', 1, NULL, 167, '2026-03-19 09:15:30'),
(1, 'admin', '用户管理', '查询用户列表', 'UserController.list', '{"pageNum":1,"pageSize":10}', '192.168.1.100', 1, NULL, 89, '2026-03-19 09:30:00'),
(1, 'admin', '用户管理', '创建用户', 'UserController.create', '{"username":"test","name":"测试用户"}', '192.168.1.100', 1, NULL, 234, '2026-03-19 10:00:00'),
(1, 'admin', '用户管理', '更新用户', 'UserController.update', '{"id":25,"name":"新名称"}', '192.168.1.100', 1, NULL, 178, '2026-03-19 10:30:00'),
(1, 'admin', '用户管理', '删除用户', 'UserController.delete', '{"id":25}', '192.168.1.100', 1, NULL, 145, '2026-03-19 10:35:00'),
-- 物资管理模块日志
(1, 'admin', '物资管理', '查询物资列表', 'MaterialController.list', '{"pageNum":1,"pageSize":10}', '192.168.1.100', 1, NULL, 78, '2026-03-19 09:45:00'),
(1, 'admin', '物资管理', '查询物资详情', 'MaterialController.getById', '{"id":"M001"}', '192.168.1.100', 1, NULL, 45, '2026-03-19 09:50:00'),
(1, 'admin', '物资管理', '创建物资', 'MaterialController.create', '{"id":"M036","name":"新物资"}', '192.168.1.100', 1, NULL, 256, '2026-03-19 11:00:00'),
(1, 'admin', '物资管理', '更新物资', 'MaterialController.update', '{"id":"M001","stock":8600}', '192.168.1.100', 1, NULL, 189, '2026-03-19 11:15:00'),
(1, 'admin', '物资管理', '删除物资', 'MaterialController.delete', '{"id":"M036"}', '192.168.1.100', 1, NULL, 167, '2026-03-19 11:20:00'),
(1, 'admin', '物资管理', '批量导入物资', 'MaterialController.import', '{"fileName":"materials.xlsx"}', '192.168.1.100', 1, NULL, 1567, '2026-03-19 14:00:00'),
(1, 'admin', '物资管理', '导出物资数据', 'MaterialController.export', '{"type":"protective"}', '192.168.1.100', 1, NULL, 2345, '2026-03-19 14:30:00'),
-- 库存管理模块日志
(1, 'admin', '库存管理', '查询库存列表', 'StockController.list', '{"pageNum":1,"pageSize":10}', '192.168.1.100', 1, NULL, 67, '2026-03-19 10:00:00'),
(1, 'admin', '库存管理', '查询库存详情', 'StockController.getById', '{"id":"M001"}', '192.168.1.100', 1, NULL, 34, '2026-03-19 10:05:00'),
(1, 'admin', '库存管理', '创建入库单', 'StockController.createInbound', '{"type":"purchase"}', '192.168.1.100', 1, NULL, 345, '2026-03-19 13:00:00'),
(1, 'admin', '库存管理', '审核入库单', 'StockController.approveInbound', '{"id":"PO20260301001"}', '192.168.1.100', 1, NULL, 234, '2026-03-19 13:30:00'),
(1, 'admin', '库存管理', '创建出库单', 'StockController.createOutbound', '{"type":"outbound"}', '192.168.1.100', 1, NULL, 289, '2026-03-19 14:00:00'),
(1, 'admin', '库存管理', '审核出库单', 'StockController.approveOutbound', '{"id":"OO20260303001"}', '192.168.1.100', 1, NULL, 198, '2026-03-19 14:30:00'),
(1, 'admin', '库存管理', '查询库存流水', 'StockController.getInventoryLog', '{"materialId":"M001"}', '192.168.1.100', 1, NULL, 123, '2026-03-19 15:00:00'),
(1, 'admin', '库存管理', '盘点调整', 'StockController.adjust', '{"materialId":"M012","adjustQty":-50}', '192.168.1.100', 1, NULL, 234, '2026-03-19 15:30:00'),
-- 物资申请模块日志
(4, 'hospital1', '物资申请', '提交物资申请', 'ApplicationController.create', '{"materialId":"M001","quantity":500}', '192.168.1.102', 1, NULL, 456, '2026-03-03 08:00:00'),
(6, 'hospital2', '物资申请', '提交物资申请', 'ApplicationController.create', '{"materialId":"M001","quantity":1000}', '192.168.1.105', 1, NULL, 378, '2026-03-06 08:00:00'),
(11, 'community1', '物资申请', '提交物资申请', 'ApplicationController.create', '{"materialId":"M002","quantity":10000}', '192.168.1.103', 1, NULL, 412, '2026-03-04 08:00:00'),
(1, 'admin', '物资申请', '审核物资申请', 'ApplicationController.approve', '{"id":"A20260303001","status":"approved"}', '192.168.1.100', 1, NULL, 234, '2026-03-03 10:00:00'),
(1, 'admin', '物资申请', '审核物资申请', 'ApplicationController.approve', '{"id":"A20260304001","status":"approved"}', '192.168.1.100', 1, NULL, 256, '2026-03-04 09:00:00'),
(1, 'admin', '物资申请', '审核物资申请', 'ApplicationController.approve', '{"id":"A20260306001","status":"approved"}', '192.168.1.100', 1, NULL, 245, '2026-03-06 09:30:00'),
(1, 'admin', '物资申请', '驳回物资申请', 'ApplicationController.reject', '{"id":"A20260302001","remark":"库存不足"}', '192.168.1.100', 1, NULL, 189, '2026-03-02 14:00:00'),
(8, 'hospital3', '物资申请', '查询申请列表', 'ApplicationController.list', '{"status":"pending"}', '192.168.1.106', 1, NULL, 78, '2026-03-19 11:00:00'),
(15, 'community3', '物资申请', '查询申请详情', 'ApplicationController.getById', '{"id":"A20260314001"}', '192.168.1.107', 1, NULL, 45, '2026-03-19 11:30:00'),
-- 物资捐赠模块日志
(18, 'donor1', '物资捐赠', '提交捐赠申请', 'DonationController.create', '{"materialName":"防护服","quantity":200}', '192.168.1.104', 1, NULL, 567, '2026-03-08 09:00:00'),
(19, 'donor2', '物资捐赠', '提交捐赠申请', 'DonationController.create', '{"materialName":"N95口罩","quantity":3000}', '192.168.1.108', 1, NULL, 489, '2026-03-12 08:00:00'),
(20, 'donor3', '物资捐赠', '提交捐赠申请', 'DonationController.create', '{"materialName":"消毒液","quantity":200}', '192.168.1.109', 1, NULL, 512, '2026-03-15 08:00:00'),
(1, 'admin', '物资捐赠', '审核捐赠申请', 'DonationController.approve', '{"id":"D20260308001","status":"approved"}', '192.168.1.100', 1, NULL, 234, '2026-03-08 14:00:00'),
(1, 'admin', '物资捐赠', '审核捐赠申请', 'DonationController.approve', '{"id":"D20260312001","status":"approved"}', '192.168.1.100', 1, NULL, 245, '2026-03-12 10:00:00'),
(1, 'admin', '物资捐赠', '驳回捐赠申请', 'DonationController.reject', '{"id":"D20260310001","remark":"库存充足"}', '192.168.1.100', 1, NULL, 198, '2026-03-10 14:00:00'),
(18, 'donor1', '物资捐赠', '查询捐赠记录', 'DonationController.list', '{"donorId":18}', '192.168.1.104', 1, NULL, 89, '2026-03-19 10:00:00'),
-- 疫情数据模块日志
(1, 'admin', '疫情数据', '查询疫情数据', 'PandemicDataController.list', '{"region":"全市"}', '192.168.1.100', 1, NULL, 123, '2026-03-19 09:00:00'),
(1, 'admin', '疫情数据', '查询疫情趋势', 'PandemicDataController.getTrend', '{"region":"全市","days":30}', '192.168.1.100', 1, NULL, 234, '2026-03-19 09:05:00'),
(1, 'admin', '疫情数据', '更新疫情数据', 'PandemicDataController.update', '{"region":"A区","dataDate":"2026-03-19"}', '192.168.1.100', 1, NULL, 345, '2026-03-19 12:00:00'),
(1, 'admin', '疫情数据', '批量导入疫情数据', 'PandemicDataController.import', '{"fileName":"pandemic_data.xlsx"}', '192.168.1.100', 1, NULL, 1234, '2026-03-19 12:30:00'),
-- 新闻管理模块日志
(1, 'admin', '新闻管理', '创建新闻', 'NewsController.create', '{"title":"新新闻标题"}', '192.168.1.100', 1, NULL, 456, '2026-03-19 10:00:00'),
(1, 'admin', '新闻管理', '更新新闻', 'NewsController.update', '{"id":"N001","title":"更新标题"}', '192.168.1.100', 1, NULL, 345, '2026-03-19 10:30:00'),
(1, 'admin', '新闻管理', '发布新闻', 'NewsController.publish', '{"id":"N001"}', '192.168.1.100', 1, NULL, 234, '2026-03-19 11:00:00'),
(1, 'admin', '新闻管理', '删除新闻', 'NewsController.delete', '{"id":"N011"}', '192.168.1.100', 1, NULL, 189, '2026-03-19 11:30:00'),
(1, 'admin', '新闻管理', '查询新闻列表', 'NewsController.list', '{"status":"published"}', '192.168.1.100', 1, NULL, 78, '2026-03-19 14:00:00'),
(4, 'hospital1', '新闻管理', '查看新闻详情', 'NewsController.getById', '{"id":"N001"}', '192.168.1.102', 1, NULL, 56, '2026-03-19 14:30:00'),
-- 知识管理模块日志
(1, 'admin', '知识管理', '创建知识', 'KnowledgeController.create', '{"title":"新知识标题","category":"personal"}', '192.168.1.100', 1, NULL, 478, '2026-03-19 10:00:00'),
(1, 'admin', '知识管理', '更新知识', 'KnowledgeController.update', '{"id":"K001","content":"更新内容"}', '192.168.1.100', 1, NULL, 356, '2026-03-19 10:30:00'),
(1, 'admin', '知识管理', '发布知识', 'KnowledgeController.publish', '{"id":"K001"}', '192.168.1.100', 1, NULL, 234, '2026-03-19 11:00:00'),
(1, 'admin', '知识管理', '删除知识', 'KnowledgeController.delete', '{"id":"K016"}', '192.168.1.100', 1, NULL, 189, '2026-03-19 11:30:00'),
(1, 'admin', '知识管理', '查询知识列表', 'KnowledgeController.list', '{"category":"personal"}', '192.168.1.100', 1, NULL, 67, '2026-03-19 14:00:00'),
(11, 'community1', '知识管理', '查看知识详情', 'KnowledgeController.getById', '{"id":"K006"}', '192.168.1.103', 1, NULL, 89, '2026-03-19 15:00:00'),
-- 消息推送模块日志
(1, 'admin', '消息推送', '创建推送任务', 'PushController.create', '{"title":"物资提醒","target":"all"}', '192.168.1.100', 1, NULL, 234, '2026-03-19 10:30:00'),
(1, 'admin', '消息推送', '发送推送', 'PushController.send', '{"id":1}', '192.168.1.100', 1, NULL, 1234, '2026-03-19 10:35:00'),
(1, 'admin', '消息推送', '查询推送记录', 'PushController.list', '{"status":"success"}', '192.168.1.100', 1, NULL, 78, '2026-03-19 14:00:00'),
(1, 'admin', '消息推送', '查询推送详情', 'PushController.getById', '{"id":1}', '192.168.1.100', 1, NULL, 45, '2026-03-19 14:30:00'),
-- 通知管理模块日志
(1, 'admin', '通知管理', '查询用户通知', 'NotificationController.list', '{"userId":1}', '192.168.1.100', 1, NULL, 56, '2026-03-19 09:00:00'),
(4, 'hospital1', '通知管理', '标记通知已读', 'NotificationController.markRead', '{"id":5}', '192.168.1.102', 1, NULL, 34, '2026-03-19 15:00:00'),
(4, 'hospital1', '通知管理', '查询未读通知', 'NotificationController.getUnread', '{"userId":4}', '192.168.1.102', 1, NULL, 45, '2026-03-19 15:30:00'),
-- 统计分析模块日志
(1, 'admin', '统计分析', '查询仪表盘数据', 'StatsController.getDashboard', '{}', '192.168.1.100', 1, NULL, 567, '2026-03-19 09:00:00'),
(1, 'admin', '统计分析', '查询物资统计', 'StatsController.getMaterialStats', '{"type":"protective"}', '192.168.1.100', 1, NULL, 345, '2026-03-19 10:00:00'),
(1, 'admin', '统计分析', '查询申请统计', 'StatsController.getApplicationStats', '{"period":"week"}', '192.168.1.100', 1, NULL, 456, '2026-03-19 11:00:00'),
(1, 'admin', '统计分析', '导出统计数据', 'StatsController.export', '{"type":"material"}', '192.168.1.100', 1, NULL, 2345, '2026-03-19 14:00:00'),
-- 系统管理模块日志
(1, 'admin', '系统管理', '系统参数配置', 'SystemController.updateConfig', '{"paramKey":"system.name"}', '192.168.1.100', 1, NULL, 123, '2026-03-19 10:00:00'),
(1, 'admin', '系统管理', '查看系统日志', 'SystemController.getLogs', '{"module":"user"}', '192.168.1.100', 1, NULL, 234, '2026-03-19 11:00:00'),
(1, 'admin', '系统管理', '清理过期数据', 'SystemController.cleanData', '{"days":30}', '192.168.1.100', 1, NULL, 1567, '2026-03-19 16:00:00'),
-- 登录失败日志
(NULL, NULL, '用户管理', '用户登录', 'AuthController.login', '{"username":"admin"}', '192.168.1.200', 0, '用户名或密码错误', 45, '2026-03-19 08:00:00'),
(NULL, NULL, '用户管理', '用户登录', 'AuthController.login', '{"username":"test"}', '192.168.1.201', 0, '用户不存在', 34, '2026-03-19 09:00:00'),
(NULL, NULL, '用户管理', '用户登录', 'AuthController.login', '{"username":"admin"}', '192.168.1.202', 0, '密码错误', 56, '2026-03-19 15:00:00'),
-- 操作失败日志
(1, 'admin', '物资管理', '创建物资', 'MaterialController.create', '{"id":"M001"}', '192.168.1.100', 0, '物资ID已存在', 123, '2026-03-19 11:30:00'),
(1, 'admin', '库存管理', '审核入库单', 'StockController.approveInbound', '{"id":"PO20260318001"}', '192.168.1.100', 0, '订单状态不允许审核', 89, '2026-03-19 14:00:00'),
(4, 'hospital1', '物资申请', '提交物资申请', 'ApplicationController.create', '{"materialId":"M026","quantity":100}', '192.168.1.102', 0, '库存不足', 234, '2026-03-19 16:00:00');

-- ========================================
-- 脚本执行完成
-- ========================================
SELECT 'epidemic_system 数据库初始化完成！包含55条操作日志记录' AS message;

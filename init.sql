-- ========================================
-- 疫情防控物资调度管理系统 - 数据库初始化脚本
-- 数据库版本：MySQL 8.0+
-- 字符集：utf8mb4
-- 创建时间：2026-02-24
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `epidemic_material` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `epidemic_material`;

-- ========================================
-- 1. 用户表
-- ========================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `role` VARCHAR(20) NOT NULL COMMENT '角色：admin-管理员，hospital_user-医院用户，community_staff-社区人员，donor-捐赠方',
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
-- 2. 物资表
-- ========================================
DROP TABLE IF EXISTS `material`;
CREATE TABLE `material` (
    `id` VARCHAR(50) NOT NULL COMMENT '物资ID',
    `name` VARCHAR(100) NOT NULL COMMENT '物资名称',
    `type` VARCHAR(20) NOT NULL COMMENT '物资类型：protective-防护物资，disinfection-消杀物资，testing-检测物资，equipment-医疗设备，other-其他',
    `specification` VARCHAR(100) DEFAULT NULL COMMENT '规格型号',
    `unit` VARCHAR(20) NOT NULL COMMENT '单位',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    `threshold` INT NOT NULL DEFAULT 100 COMMENT '预警阈值',
    `warehouse` VARCHAR(50) DEFAULT NULL COMMENT '仓库',
    `location` VARCHAR(100) DEFAULT NULL COMMENT '库位',
    `description` TEXT DEFAULT NULL COMMENT '描述',
    `status` VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '状态：normal-正常，warning-预警，low-低库存',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_warehouse` (`warehouse`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物资表';

-- ========================================
-- 3. 物资申请表
-- ========================================
DROP TABLE IF EXISTS `application`;
CREATE TABLE `application` (
    `id` VARCHAR(50) NOT NULL COMMENT '申请单号',
    `material_id` VARCHAR(50) NOT NULL COMMENT '物资ID',
    `material_name` VARCHAR(100) NOT NULL COMMENT '物资名称',
    `quantity` INT NOT NULL COMMENT '申请数量',
    `unit` VARCHAR(20) NOT NULL COMMENT '单位',
    `applicant_id` BIGINT NOT NULL COMMENT '申请人ID',
    `applicant_name` VARCHAR(50) NOT NULL COMMENT '申请人姓名',
    `applicant_unit` VARCHAR(100) DEFAULT NULL COMMENT '申请人单位',
    `purpose` TEXT DEFAULT NULL COMMENT '用途说明',
    `urgency` VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '紧急程度：normal-普通，urgent-较急，critical-紧急',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '收货地址',
    `receiver` VARCHAR(50) DEFAULT NULL COMMENT '收货人',
    `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT '收货电话',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending-待审核，approved-已通过，rejected-已驳回，cancelled-已取消，delivered-已发货，received-已收货',
    `apply_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `approve_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `approver_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `approver_name` VARCHAR(50) DEFAULT NULL COMMENT '审核人姓名',
    `approve_remark` TEXT DEFAULT NULL COMMENT '审核意见',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_material_id` (`material_id`),
    KEY `idx_applicant_id` (`applicant_id`),
    KEY `idx_status` (`status`),
    KEY `idx_apply_time` (`apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物资申请表';

-- ========================================
-- 4. 申请物流追踪表
-- ========================================
DROP TABLE IF EXISTS `application_track`;
CREATE TABLE `application_track` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '追踪ID',
    `application_id` VARCHAR(50) NOT NULL COMMENT '申请单号',
    `status` VARCHAR(20) NOT NULL COMMENT '状态',
    `description` VARCHAR(255) NOT NULL COMMENT '描述',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
    `operate_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_application_id` (`application_id`),
    KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='申请物流追踪表';

-- ========================================
-- 5. 物资捐赠表
-- ========================================
DROP TABLE IF EXISTS `donation`;
CREATE TABLE `donation` (
    `id` VARCHAR(50) NOT NULL COMMENT '捐赠单号',
    `material_name` VARCHAR(100) NOT NULL COMMENT '物资名称',
    `type` VARCHAR(20) NOT NULL COMMENT '物资类型',
    `quantity` INT NOT NULL COMMENT '捐赠数量',
    `unit` VARCHAR(20) NOT NULL COMMENT '单位',
    `donor_id` BIGINT DEFAULT NULL COMMENT '捐赠人ID',
    `donor_unit` VARCHAR(100) NOT NULL COMMENT '捐赠单位',
    `contact_person` VARCHAR(50) NOT NULL COMMENT '联系人',
    `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `receive_address` VARCHAR(255) DEFAULT NULL COMMENT '收货地址',
    `source` VARCHAR(50) DEFAULT NULL COMMENT '物资来源',
    `production_date` DATE DEFAULT NULL COMMENT '生产日期',
    `expiry_date` DATE DEFAULT NULL COMMENT '有效期至',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending-待审核，approved-已通过，rejected-已驳回',
    `donate_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '捐赠时间',
    `approve_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `approver_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `approver_name` VARCHAR(50) DEFAULT NULL COMMENT '审核人姓名',
    `approve_remark` TEXT DEFAULT NULL COMMENT '审核意见',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_donor_id` (`donor_id`),
    KEY `idx_status` (`status`),
    KEY `idx_donate_time` (`donate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物资捐赠表';

-- ========================================
-- 6. 疫情新闻表
-- ========================================
DROP TABLE IF EXISTS `pandemic_news`;
CREATE TABLE `pandemic_news` (
    `id` VARCHAR(50) NOT NULL COMMENT '新闻ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `summary` VARCHAR(500) DEFAULT NULL COMMENT '摘要',
    `content` TEXT NOT NULL COMMENT '内容',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    `author` VARCHAR(50) DEFAULT NULL COMMENT '作者',
    `author_id` BIGINT DEFAULT NULL COMMENT '作者ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态：published-已发布，draft-草稿',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_publish_time` (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='疫情新闻表';

-- ========================================
-- 7. 防疫知识表
-- ========================================
DROP TABLE IF EXISTS `pandemic_knowledge`;
CREATE TABLE `pandemic_knowledge` (
    `id` VARCHAR(50) NOT NULL COMMENT '知识ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `summary` VARCHAR(500) DEFAULT NULL COMMENT '摘要',
    `content` TEXT NOT NULL COMMENT '内容',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类：personal-个人防护，community-社区防控，hospital-医疗机构指南',
    `author` VARCHAR(50) DEFAULT NULL COMMENT '作者',
    `status` VARCHAR(20) NOT NULL DEFAULT 'published' COMMENT '状态：published-已发布，draft-草稿',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='防疫知识表';

-- ========================================
-- 8. 实时疫情数据表
-- ========================================
DROP TABLE IF EXISTS `pandemic_data`;
CREATE TABLE `pandemic_data` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据ID',
    `region` VARCHAR(50) NOT NULL COMMENT '地区',
    `confirmed` INT NOT NULL DEFAULT 0 COMMENT '确诊人数',
    `cured` INT NOT NULL DEFAULT 0 COMMENT '治愈人数',
    `dead` INT NOT NULL DEFAULT 0 COMMENT '死亡人数',
    `suspected` INT NOT NULL DEFAULT 0 COMMENT '疑似人数',
    `data_date` DATE NOT NULL COMMENT '数据日期',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_region_date` (`region`, `data_date`),
    KEY `idx_data_date` (`data_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实时疫情数据表';

-- ========================================
-- 9. 库存变动记录表
-- ========================================
DROP TABLE IF EXISTS `inventory_log`;
CREATE TABLE `inventory_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `material_id` VARCHAR(50) NOT NULL COMMENT '物资ID',
    `material_name` VARCHAR(100) NOT NULL COMMENT '物资名称',
    `change_type` VARCHAR(20) NOT NULL COMMENT '变动类型：in-入库，out-出库，donation-捐赠，adjust-盘点调整',
    `change_quantity` INT NOT NULL COMMENT '变动数量（正数增加，负数减少）',
    `before_quantity` INT NOT NULL COMMENT '变动前数量',
    `after_quantity` INT NOT NULL COMMENT '变动后数量',
    `related_id` VARCHAR(50) DEFAULT NULL COMMENT '关联单号（申请单号或捐赠单号）',
    `remark` TEXT DEFAULT NULL COMMENT '备注',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
    `operate_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_material_id` (`material_id`),
    KEY `idx_change_type` (`change_type`),
    KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存变动记录表';

-- ========================================
-- 10. 操作日志表
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
-- 初始化数据
-- ========================================

-- 初始化管理员用户（密码：123456，明文存储）
INSERT INTO `sys_user` (`id`, `username`, `password`, `name`, `role`, `phone`, `unit`, `status`) VALUES
(1, 'admin', '123456', '系统管理员', 'admin', '13800138000', '疫情防控指挥部', 'active'),
(2, 'hospital1', '123456', '张医生', 'hospital_user', '13800138001', '市第一医院', 'active'),
(3, 'community1', '123456', '李主任', 'community_staff', '13800138002', '和平社区居委会', 'active'),
(4, 'donor1', '123456', '王经理', 'donor', '13800138003', '市慈善总会', 'active');

-- 初始化物资数据
INSERT INTO `material` (`id`, `name`, `type`, `specification`, `unit`, `stock`, `threshold`, `warehouse`, `location`, `description`, `status`) VALUES
('M001', 'N95医用口罩', 'protective', 'N95', '个', 8500, 5000, '一号仓库', 'A区-01架-01层', '医用防护口罩，符合GB19083-2010标准', 'normal'),
('M002', '医用外科口罩', 'protective', '灭菌级', '个', 15000, 10000, '一号仓库', 'A区-01架-02层', '医用外科口罩，三层结构', 'normal'),
('M003', '防护服', 'protective', '连体式', '套', 1200, 500, '一号仓库', 'A区-02架-01层', '医用一次性防护服', 'normal'),
('M004', '护目镜', 'protective', '防雾型', '副', 800, 300, '一号仓库', 'A区-02架-02层', '医用护目镜', 'normal'),
('M005', '75%酒精', 'disinfection', '500ml/瓶', '瓶', 3200, 5000, '二号仓库', 'B区-01架-01层', '75%医用酒精', 'warning'),
('M006', '84消毒液', 'disinfection', '5L/桶', '桶', 2500, 1000, '二号仓库', 'B区-01架-02层', '84消毒液，含氯消毒剂', 'normal'),
('M007', '新冠病毒检测试剂', 'testing', '20人份/盒', '盒', 4500, 3000, '三号仓库', 'C区-01架-01层', '新冠病毒抗原检测试剂盒', 'normal'),
('M008', '体温枪', 'testing', '红外测温', '把', 600, 200, '三号仓库', 'C区-02架-01层', '非接触式红外体温计', 'normal'),
('M009', '一次性手套', 'protective', 'M码', '双', 20000, 10000, '一号仓库', 'A区-03架-01层', '医用检查手套', 'normal'),
('M010', '免洗洗手液', 'disinfection', '500ml/瓶', '瓶', 2800, 3000, '二号仓库', 'B区-02架-01层', '免洗手消毒凝胶', 'warning');

-- 初始化疫情新闻数据
INSERT INTO `pandemic_news` (`id`, `title`, `summary`, `content`, `author`, `status`, `view_count`, `publish_time`) VALUES
('N001', '我市新增3例确诊病例，均在隔离管控中发现', '今日新增3例确诊病例，均在隔离管控中发现，全市疫情防控形势总体平稳可控。', '今日（2月24日），我市新增3例新型冠状病毒肺炎确诊病例，均在集中隔离管控人员中发现。目前，上述病例已闭环转运至定点医疗机构隔离治疗，相关密切接触者均已按要求落实管控措施。

市疫情防控指挥部提醒广大市民，请继续做好个人防护，坚持科学佩戴口罩、勤洗手、常通风、保持安全社交距离。如出现发热、干咳、乏力、咽痛等症状，请及时到就近医疗机构发热门诊就诊。', '疫情防控指挥部', 'published', 1520, '2026-02-24 09:00:00'),
('N002', '关于加强重点场所疫情防控工作的通知', '为进一步做好疫情防控工作，现就加强重点场所疫情防控有关事项通知如下。', '各区县（市）疫情防控指挥部，市疫情防控指挥部各成员单位：

当前，疫情防控形势依然严峻复杂。为进一步做好重点场所疫情防控工作，现就有关事项通知如下：

一、严格落实重点场所防控责任
二、强化人员密集场所管理
三、做好场所通风消毒工作
四、加强从业人员健康监测

请各单位认真贯彻落实本通知要求，切实做好重点场所疫情防控工作。', '疫情防控指挥部', 'published', 890, '2026-02-23 15:00:00');

-- 初始化防疫知识数据
INSERT INTO `pandemic_knowledge` (`id`, `title`, `summary`, `content`, `category`, `author`, `status`, `view_count`, `sort_order`) VALUES
('K001', '如何正确佩戴N95口罩', '正确佩戴N95口罩是预防呼吸道传染病的重要措施，请按照以下步骤操作。', '一、佩戴前准备
1. 清洁双手，用肥皂或洗手液洗手，或使用手消毒剂消毒双手。
2. 检查口罩包装是否完好，是否在有效期内。

二、正确佩戴步骤
1. 手持口罩，将鼻夹侧朝上，深色面朝外。
2. 将口罩罩住鼻、口及下巴，鼻夹部位向上紧贴面部。
3. 用另一只手将下方系带拉过头顶，放在颈后双耳下。
4. 再将上方系带拉至头顶中部。
5. 将双手指尖放在金属鼻夹上，从中间位置开始，用手指向内按鼻夹，并分别向两侧移动和按压，根据鼻梁的形状塑造鼻夹。

三、佩戴后检查
1. 双手完全盖住口罩，快速呼气，检查是否有漏气。
2. 如漏气，应调整口罩位置或更换口罩。', 'personal', '疾控中心', 'published', 3250, 1),
('K002', '家庭日常消毒指南', '家庭日常消毒是预防疫情的重要环节，请了解以下消毒方法和注意事项。', '一、常用消毒剂选择
1. 75%酒精：适用于手部、小物件表面消毒。
2. 含氯消毒剂（如84消毒液）：适用于物体表面、地面等消毒。
3. 季铵盐类消毒剂：适用于物体表面消毒，刺激性较小。

二、消毒方法
1. 物体表面消毒：使用消毒剂擦拭或喷洒，作用时间不少于30分钟。
2. 手部消毒：使用手消毒剂揉搓双手，作用时间不少于1分钟。
3. 餐具消毒：煮沸消毒15-30分钟，或使用餐具消毒柜。

三、注意事项
1. 消毒剂应按照说明书配制使用，现配现用。
2. 酒精消毒时注意防火，远离火源。
3. 含氯消毒剂不宜与洁厕灵等酸性物质混合使用。', 'community', '疾控中心', 'published', 2180, 2),
('K003', '医疗机构内个人防护要点', '医疗机构内工作人员应严格做好个人防护，防止职业暴露。', '一、标准预防措施
1. 所有工作人员均应正确佩戴医用外科口罩或医用防护口罩。
2. 接触患者前后均应进行手卫生。
3. 可能接触患者血液、体液、分泌物时，应佩戴手套。

二、不同风险区域防护要求
1. 低风险区域（如普通门诊）：佩戴医用外科口罩，必要时戴手套。
2. 中风险区域（如发热门诊）：佩戴医用防护口罩、护目镜或防护面屏、穿隔离衣、戴手套。
3. 高风险区域（如隔离病房）：佩戴医用防护口罩、护目镜或防护面屏、穿防护服、戴双层手套、穿鞋套。

三、防护用品穿脱顺序
1. 穿戴：洗手→戴帽子→戴口罩→穿防护服→戴护目镜→戴手套→穿鞋套。
2. 脱卸：摘护目镜→脱防护服（同时脱外层手套）→洗手→摘口罩→摘帽子→洗手→消毒。', 'hospital', '卫健委', 'published', 1680, 3);

-- 初始化疫情数据
INSERT INTO `pandemic_data` (`region`, `confirmed`, `cured`, `dead`, `suspected`, `data_date`) VALUES
('全市', 156, 120, 3, 10, '2026-02-24'),
('A区', 45, 35, 1, 3, '2026-02-24'),
('B区', 38, 30, 0, 2, '2026-02-24'),
('C区', 42, 32, 1, 3, '2026-02-24'),
('D区', 31, 23, 1, 2, '2026-02-24');

-- ========================================
-- 脚本执行完成
-- ========================================
SELECT '数据库初始化完成！' AS message;

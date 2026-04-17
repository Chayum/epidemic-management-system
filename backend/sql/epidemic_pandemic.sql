-- ========================================
-- 疫情防控物资调度管理系统 - 疫情/消息服务数据库
-- 数据库版本：MySQL 8.0+
-- 字符集：utf8mb4
-- 创建时间：2026-03-19
-- 更新时间：2026-04-17（移除防疫知识库和疫情数据表）
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `epidemic_pandemic` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `epidemic_pandemic`;

-- ========================================
-- 1. 疫情新闻表
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
-- 2. 推送记录表
-- ========================================
DROP TABLE IF EXISTS `push_record`;
CREATE TABLE `push_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(200) NOT NULL COMMENT '推送标题',
    `content` TEXT COMMENT '推送内容',
    `target` VARCHAR(50) NOT NULL COMMENT '推送对象(all/applicant/donor)',
    `channels` VARCHAR(100) DEFAULT NULL COMMENT '推送渠道(APP,SMS,WEB)',
    `status` VARCHAR(20) DEFAULT 'success' COMMENT '状态(success/failed)',
    `push_time` DATETIME DEFAULT NULL COMMENT '推送时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推送记录表';

-- ========================================
-- 3. 用户通知表
-- ========================================
DROP TABLE IF EXISTS `user_notification`;
CREATE TABLE `user_notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content` TEXT COMMENT '通知内容',
    `type` VARCHAR(20) DEFAULT 'push' COMMENT '通知类型(push-推送,system-系统)',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读(0-未读,1-已读)',
    `push_record_id` BIGINT DEFAULT NULL COMMENT '关联推送记录ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户通知表';

-- ========================================
-- 初始化疫情新闻数据
-- ========================================
INSERT INTO `pandemic_news` (`id`, `title`, `summary`, `content`, `cover_image`, `author`, `author_id`, `status`, `view_count`, `publish_time`) VALUES
('N001', '我市新增3例确诊病例，均在隔离管控中发现', '今日新增3例确诊病例，均在集中隔离管控人员中发现，全市疫情防控形势总体平稳可控。', '今日（3月19日），我市新增3例新型冠状病毒肺炎确诊病例，均在集中隔离管控人员中发现。目前，上述病例已闭环转运至定点医疗机构隔离治疗，相关密切接触者均已按要求落实管控措施。\n\n市疫情防控指挥部提醒广大市民，请继续做好个人防护，坚持科学佩戴口罩、勤洗手、常通风、保持安全社交距离。如出现发热、干咳、乏力、咽痛等症状，请及时到就近医疗机构发热门诊就诊。', NULL, '疫情防控指挥部', 1, 'published', 1520, '2026-03-19 09:00:00'),
('N002', '关于加强重点场所疫情防控工作的通知', '为进一步做好疫情防控工作，现就加强重点场所疫情防控有关事项通知如下。', '各区县（市）疫情防控指挥部，市疫情防控指挥部各成员单位：\n\n当前，疫情防控形势依然严峻复杂。为进一步做好重点场所疫情防控工作，现就有关事项通知如下：\n\n一、严格落实重点场所防控责任\n二、强化人员密集场所管理\n三、做好场所通风消毒工作\n四、加强从业人员健康监测\n\n请各单位认真贯彻落实本通知要求，切实做好重点场所疫情防控工作。', NULL, '疫情防控指挥部', 1, 'published', 890, '2026-03-18 15:00:00'),
('N003', '全市核酸检测工作有序推进，已完成200万人次检测', '截至今日，全市已完成核酸检测200万人次，检测结果均为阴性。', '市卫健委通报，我市核酸检测工作有序推进。\n\n一、检测进展\n截至3月19日12时，全市累计完成核酸检测200万人次，检测结果均为阴性。\n\n二、检测能力\n全市现有核酸检测机构50家，日检测能力达到50万管。\n\n三、下一步安排\n将继续对重点人群、重点场所开展定期核酸检测，确保疫情早发现、早报告、早隔离、早治疗。', NULL, '市卫健委', 1, 'published', 2100, '2026-03-19 12:00:00'),
('N004', '防疫物资储备充足，可满足全市30天用量', '市疫情防控指挥部通报，全市防疫物资储备充足，可满足全市30天用量。', '市疫情防控指挥部物资保障组通报，我市防疫物资储备充足。\n\n一、物资储备情况\n1. 医用防护口罩：储备1500万只，可满足30天用量\n2. 防护服：储备50万套，可满足30天用量\n3. 消杀物资：储备充足，可满足60天用量\n4. 检测物资：储备200万人份，可满足30天用量\n\n二、调度机制\n已建立完善的物资调度机制，确保物资及时配送到位。\n\n三、保障措施\n各区县均设有物资储备点可就近调拨。', NULL, '疫情防控指挥部', 1, 'published', 1680, '2026-03-18 10:00:00'),
('N005', '社区防控力度持续加强，排查重点人群5万人', '全市社区持续加强防控力度，已排查重点人群5万人。', '市民政局通报，全市社区持续加强疫情防控力度。\n\n一、排查情况\n已累计排查重点人群5万人，其中包括：\n- 境外返市人员：2000人\n- 中高风险地区返市人员：8000人\n- 密切接触者：500人\n- 次密切接触者：2000人\n\n二、管理措施\n对排查出的重点人群均已落实相应管控措施。\n\n三、社区服务\n组织社区工作者为居家隔离人员提供生活物资配送服务。', NULL, '市民政局', 1, 'published', 1250, '2026-03-17 14:00:00'),
('N006', '爱心企业捐赠物资助力疫情防控', '多家爱心企业向市疫情防控指挥部捐赠防疫物资。', '连日来，多家爱心企业积极履行社会责任，向市疫情防控指挥部捐赠防疫物资。\n\n一、捐赠情况\n1. 华润医药集团捐赠防护服500套、消毒液300瓶\n2. 省红十字会调拨N95口罩5000个、检测试剂1000人份\n3. 九州通医药集团捐赠84消毒液200桶\n4. 市慈善总会捐赠防护物资价值50万元\n\n二、物资去向\n所有捐赠物资已入库统一调配，用于一线医护人员和社区防控工作。\n\n三、致谢\n市疫情防控指挥部对爱心企业的善举表示衷心感谢。', NULL, '疫情防控指挥部', 1, 'published', 1450, '2026-03-15 16:00:00'),
-- 草稿状态的新闻
('N007', '关于开展全员核酸检测的公告（草稿）', '拟开展全市全员核酸检测。', '市疫情防控指挥部拟发布全市全员核酸检测公告，具体时间另行通知。', NULL, '疫情防控指挥部', 1, 'draft', 0, NULL);

-- ========================================
-- 初始化推送记录数据
-- ========================================
INSERT INTO `push_record` (`title`, `content`, `target`, `status`, `push_time`) VALUES
('物资紧缺提醒', '口罩、防护服等物资库存告急，请各部门及时补充。', 'applicant', 'success', '2026-03-19 10:30:00'),
('新政策发布', '关于进一步加强疫情防控工作的通知已发布，请查阅。', 'all', 'success', '2026-03-19 09:00:00'),
('消毒物资到位', '新到一批消毒物资，已分配至各社区，请及时领取。', 'applicant', 'success', '2026-03-18 15:30:00'),
('核酸检测安排', '本周核酸检测安排已更新，请查看具体时间地点。', 'all', 'success', '2026-03-18 08:00:00'),
('社区排查通知', '请各社区加强重点人群排查，发现异常及时上报。', 'applicant', 'success', '2026-03-17 10:00:00'),
('爱心企业捐赠', '华润医药集团捐赠防护物资，已入库。', 'all', 'success', '2026-03-15 16:30:00'),
('疫情数据通报', '全市新增确诊病例3例，请做好防护。', 'all', 'success', '2026-03-14 09:00:00'),
('捐赠感谢通知', '感谢捐赠方的爱心捐赠，物资已发放到位。', 'donor', 'success', '2026-03-05 14:00:00');

-- ========================================
-- 初始化用户通知数据
-- ========================================
INSERT INTO `user_notification` (`user_id`, `title`, `content`, `type`, `is_read`, `push_record_id`, `create_time`) VALUES
-- 用户1的通知（管理员）
(1, '新政策发布', '关于进一步加强疫情防控工作的通知已发布，请查阅。', 'push', 1, 2, '2026-03-19 09:05:00'),
(1, '物资紧缺提醒', '口罩、防护服等物资库存告急，请各部门及时补充。', 'push', 0, 1, '2026-03-19 10:35:00'),
(1, '爱心企业捐赠', '华润医药集团捐赠防护物资，已入库。', 'push', 1, 6, '2026-03-15 16:35:00'),
-- 用户4的通知（医院用户）
(4, '物资申请通过', '您的物资申请A20260303001已审核通过，物资已发货。', 'push', 1, NULL, '2026-03-03 15:05:00'),
(4, '发热就诊指引', '如出现发热症状，请就近到发热门诊就诊。', 'push', 1, NULL, '2026-03-10 12:05:00'),
-- 用户11的通知（社区人员）
(11, '消毒物资到位', '新到一批消毒物资，已分配至各社区，请及时领取。', 'push', 1, 3, '2026-03-18 15:35:00'),
(11, '社区排查通知', '请各社区加强重点人群排查，发现异常及时上报。', 'push', 1, 5, '2026-03-17 10:05:00'),
-- 用户18的通知（捐赠方）
(18, '捐赠审核通过', '您提交的捐赠单D20260308001已审核通过，感谢您的爱心捐赠！', 'system', 1, NULL, '2026-03-08 14:05:00'),
(18, '捐赠物资公示', '社会捐赠物资使用情况公示，请查阅监督。', 'push', 1, NULL, '2026-03-06 16:05:00'),
-- 系统通知
(1, '系统更新', '疫情防控物资调度管理系统将于本周日凌晨2点进行更新维护。', 'system', 0, NULL, '2026-03-19 08:00:00');

-- ========================================
-- 脚本执行完成
-- ========================================
SELECT 'epidemic_pandemic 数据库初始化完成！包含7条新闻、8条推送记录、10条用户通知' AS message;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `role` varchar(20) NOT NULL COMMENT '角色(admin/hospital_user/community_staff/donor)',
  `unit` varchar(100) DEFAULT NULL COMMENT '所属单位',
  `status` int(1) DEFAULT 1 COMMENT '状态(1:正常 0:禁用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 物资表
CREATE TABLE IF NOT EXISTS `material` (
  `id` varchar(32) NOT NULL COMMENT '物资ID',
  `name` varchar(100) NOT NULL COMMENT '物资名称',
  `type` varchar(20) NOT NULL COMMENT '物资类型(protective/disinfection/testing)',
  `specification` varchar(100) DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `stock` int(11) NOT NULL DEFAULT 0 COMMENT '库存数量',
  `threshold` int(11) DEFAULT 0 COMMENT '预警阈值',
  `warehouse` varchar(50) DEFAULT NULL COMMENT '存放仓库',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `status` varchar(20) DEFAULT 'normal' COMMENT '状态(normal/warning)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资表';

-- 申请单表
CREATE TABLE IF NOT EXISTS `application` (
  `id` varchar(32) NOT NULL COMMENT '申请单ID',
  `material_id` varchar(32) NOT NULL COMMENT '物资ID',
  `quantity` int(11) NOT NULL COMMENT '申请数量',
  `applicant_id` bigint(20) NOT NULL COMMENT '申请人ID',
  `purpose` varchar(200) DEFAULT NULL COMMENT '用途说明',
  `urgency` varchar(20) DEFAULT 'normal' COMMENT '紧急程度(normal/urgent/critical)',
  `address` varchar(200) DEFAULT NULL COMMENT '收货地址',
  `receiver` varchar(50) DEFAULT NULL COMMENT '收货人',
  `receiver_phone` varchar(20) DEFAULT NULL COMMENT '收货电话',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态(pending/approved/rejected/cancelled)',
  `apply_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
  `approver_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
  `approve_remark` varchar(200) DEFAULT NULL COMMENT '审核备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资申请表';

-- 捐赠表
CREATE TABLE IF NOT EXISTS `donation` (
  `id` varchar(32) NOT NULL COMMENT '捐赠单ID',
  `material_name` varchar(100) NOT NULL COMMENT '物资名称',
  `type` varchar(20) NOT NULL COMMENT '物资类型',
  `quantity` int(11) NOT NULL COMMENT '捐赠数量',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `donor_id` bigint(20) DEFAULT NULL COMMENT '捐赠人ID',
  `donor_unit` varchar(100) DEFAULT NULL COMMENT '捐赠单位/个人名称',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `receive_address` varchar(200) DEFAULT NULL COMMENT '取货地址',
  `source` varchar(20) DEFAULT 'personal' COMMENT '来源(personal/enterprise)',
  `production_date` date DEFAULT NULL COMMENT '生产日期',
  `expiry_date` date DEFAULT NULL COMMENT '有效期至',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态(pending/approved/rejected)',
  `donate_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '捐赠时间',
  `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资捐赠表';

-- 疫情新闻表
CREATE TABLE IF NOT EXISTS `pandemic_news` (
  `id` varchar(32) NOT NULL COMMENT '新闻ID',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `summary` varchar(500) DEFAULT NULL COMMENT '摘要',
  `content` text COMMENT '内容',
  `author` varchar(50) DEFAULT NULL COMMENT '发布人',
  `status` varchar(20) DEFAULT 'published' COMMENT '状态(published/draft)',
  `view_count` int(11) DEFAULT 0 COMMENT '阅读量',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疫情新闻表';

-- 初始化管理员用户
INSERT INTO `user` (`username`, `password`, `name`, `role`, `status`) VALUES ('admin', '123456', '系统管理员', 'admin', 1);

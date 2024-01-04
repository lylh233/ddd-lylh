-- ----------------------------
-- 初始化数据库.
-- ----------------------------

-- 1、用户
CREATE TABLE `user` (
                        `id` bigint(20) NOT NULL AUTO_INCREMENT,
                        `uuid` varchar(36) DEFAULT NULL,
                        `name` varchar(50) DEFAULT NULL,
                        `created` bigint(20) DEFAULT NULL,
                        `last_modified` bigint(20) DEFAULT NULL,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `idx_user_uuid` (`uuid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;
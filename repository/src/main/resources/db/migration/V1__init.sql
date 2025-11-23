-- ----------------------------
-- 初始化数据库.
-- ----------------------------

-- 1、用户
CREATE TABLE `user` (
                        `id` bigint(20) NOT NULL AUTO_INCREMENT,
                        `uuid` varchar(32) NOT NULL,
                        `username` varchar(50) NOT NULL,
                        `password` varchar(50) NOT NULL,
                        `name` varchar(50) NOT NULL,
                        `user_type` int(2) NOT NULL DEFAULT 0 ,
                        `created` bigint(13) NOT NULL,
                        `last_modified` bigint(13) NOT NULL,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `idx_user_uuid` (`uuid`) USING BTREE,
                        UNIQUE KEY `idx_user_name` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4;
-- ----------------------------
-- 初始化数据库.
-- ----------------------------

-- 1、用户
CREATE TABLE `user` (
                        `id` bigint(20) NOT NULL AUTO_INCREMENT,
                        `uuid` varchar(32) DEFAULT NULL,
                        `username` varchar(50) DEFAULT NULL,
                        `password` varchar(50) DEFAULT NULL,
                        `name` varchar(50) DEFAULT NULL,
                        `user_type` int(2) DEFAULT NULL,
                        `created` bigint(13) DEFAULT NULL,
                        `last_modified` bigint(13) DEFAULT NULL,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `idx_user_uuid` (`uuid`) USING BTREE,
                        UNIQUE KEY `idx_user_name` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4;

-- 2、成员
CREATE TABLE `member` (
                          `id` bigint(20) NOT NULL AUTO_INCREMENT,
                          `uuid` varchar(32) DEFAULT NULL,
                          `name` varchar(50) DEFAULT NULL,
                          `position` varchar(50) DEFAULT NULL,
                          `occupationUuid` varchar(32) DEFAULT NULL,
                          `created` bigint(13) DEFAULT NULL,
                          `last_modified` bigint(13) DEFAULT NULL,
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `idx_member_uuid` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3、职业
CREATE TABLE `occupation` (
                              `id` bigint(20) NOT NULL AUTO_INCREMENT,
                              `uuid` varchar(32) DEFAULT NULL,
                              `name` varchar(50) DEFAULT NULL,
                              `color` varchar(10) DEFAULT NULL,
                              `created` bigint(13) DEFAULT NULL,
                              `last_modified` bigint(13) DEFAULT NULL,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `idx_occupation_uuid` (`uuid`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4;

-- 4、团队
CREATE TABLE `team` (
                          `id` bigint(20) NOT NULL AUTO_INCREMENT,
                          `uuid` varchar(32) DEFAULT NULL,
                          `name` varchar(50) DEFAULT NULL,
                          `position` varchar(50) DEFAULT NULL,
                          `occupationUuid` varchar(32) DEFAULT NULL,
                          `created` bigint(13) DEFAULT NULL,
                          `last_modified` bigint(13) DEFAULT NULL,
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `idx_member_uuid` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5、小队
CREATE TABLE `group` (
                         `id` bigint(20) NOT NULL AUTO_INCREMENT,
                         `uuid` varchar(32) DEFAULT NULL,
                         `name` varchar(50) DEFAULT NULL,
                         `serial_number` int(2) DEFAULT NULL,
                         `team_uuid` varchar(32) DEFAULT NULL,
                         `created` bigint(13) DEFAULT NULL,
                         `last_modified` bigint(13) DEFAULT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `idx_group_uuid` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6、小队身份
CREATE TABLE `group_assignment` (
                                    `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                    `uuid` varchar(32) DEFAULT NULL,
                                    `name` varchar(50) DEFAULT NULL,
                                    `description` text,
                                    `created` bigint(13) DEFAULT NULL,
                                    `last_modified` bigint(13) DEFAULT NULL,
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `idx_group_assignment_uuid` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7、小队成员
CREATE TABLE `group_member_association` (
                                            `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                            `serial_number` int(2) DEFAULT NULL,
                                            `group_uuid` varchar(32) DEFAULT NULL,
                                            `member_uuid` varchar(32) DEFAULT NULL,
                                            `created` bigint(13) DEFAULT NULL,
                                            `last_modified` bigint(13) DEFAULT NULL,
                                            PRIMARY KEY (`id`),
                                            UNIQUE KEY `idx_group_member_association_uuid` (`group_uuid`,`member_uuid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


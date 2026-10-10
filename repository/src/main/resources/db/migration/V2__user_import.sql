-- 用户批量导入。
-- 回滚:
-- DROP TABLE IF EXISTS `user_import_fail`;
-- DROP TABLE IF EXISTS `user_import_data`;
-- DROP TABLE IF EXISTS `user_import_task`;

CREATE TABLE `user_import_task` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `file_name` varchar(255) NOT NULL,
    `file_hash` char(64) NOT NULL,
    `dedup_key` char(64) DEFAULT NULL,
    `status` varchar(20) NOT NULL,
    `total_count` int(11) NOT NULL DEFAULT 0,
    `success_count` int(11) NOT NULL DEFAULT 0,
    `fail_count` int(11) NOT NULL DEFAULT 0,
    `error_message` varchar(500) DEFAULT NULL,
    `created` bigint(13) NOT NULL,
    `last_modified` bigint(13) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_import_dedup` (`dedup_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_import_data` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `task_id` bigint(20) NOT NULL,
    `row_num` int(11) NOT NULL,
    `username` varchar(50) NOT NULL,
    `name` varchar(50) NOT NULL,
    `user_type` int(11) NOT NULL,
    `user_uuid` varchar(32) NOT NULL,
    `created` bigint(13) NOT NULL,
    `last_modified` bigint(13) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_user_import_data_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_import_fail` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `task_id` bigint(20) NOT NULL,
    `row_num` int(11) NOT NULL,
    `source_content` varchar(1000) NOT NULL,
    `reason` varchar(255) NOT NULL,
    `created` bigint(13) NOT NULL,
    `last_modified` bigint(13) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_user_import_fail_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

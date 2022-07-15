DROP TABLE IF EXISTS `clan_arena`;
ALTER TABLE `clan_data` ADD COLUMN `arena_stage` TINYINT NOT NULL DEFAULT '0' AFTER `yesterday_attendance_reward`;

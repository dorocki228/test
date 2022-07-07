CREATE TABLE IF NOT EXISTS `character_variables` (
	`obj_id` INT NOT NULL DEFAULT '0',
	`name` VARCHAR(86) CHARACTER SET UTF8 NOT NULL DEFAULT '0',
	`value` TEXT CHARACTER SET UTF8 NOT NULL,
	`expire_time` bigint(20) NOT NULL DEFAULT '0',
	UNIQUE KEY `prim` (`obj_id`,`name`),
	KEY `obj_id` (`obj_id`),
	KEY `name` (`name`),
	KEY `expire_time` (`expire_time`)
) ENGINE=MyISAM;

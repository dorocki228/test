DROP TABLE IF EXISTS `clan_subpledges`;
CREATE TABLE `clan_subpledges` (
	`clan_id` INT UNSIGNED NOT NULL DEFAULT '0',
	`type` SMALLINT NOT NULL DEFAULT '0',
	`name` VARCHAR(45) CHARACTER SET UTF8 NOT NULL DEFAULT '',
	`leader_id` INT UNSIGNED NOT NULL DEFAULT '0',
	`upgraded` INT NOT NULL DEFAULT '0',
	PRIMARY KEY  (`clan_id`,`type`)
) ENGINE=MyISAM;

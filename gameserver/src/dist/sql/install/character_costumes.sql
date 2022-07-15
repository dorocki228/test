DROP TABLE IF EXISTS `character_costumes`;
CREATE TABLE `character_costumes` (
	`char_id` INT NOT NULL,
	`costume_id` SMALLINT UNSIGNED NOT NULL,
	`count` SMALLINT UNSIGNED NOT NULL DEFAULT '0',
	`flags` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	`shortcut_id` SMALLINT UNSIGNED NOT NULL DEFAULT '0',
	PRIMARY KEY  (`char_id`,`costume_id`)
) ENGINE=MyISAM;
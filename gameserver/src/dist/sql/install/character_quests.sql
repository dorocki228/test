DROP TABLE IF EXISTS `character_quests`;
CREATE TABLE `character_quests` (
	`char_id` INT NOT NULL DEFAULT '0',
	`id` INT NOT NULL,
	`var` VARCHAR(40) CHARACTER SET UTF8 NOT NULL,
	`value` VARCHAR(255) CHARACTER SET UTF8,
	PRIMARY KEY  (`char_id`,`id`,`var`)
) ENGINE=MyISAM;

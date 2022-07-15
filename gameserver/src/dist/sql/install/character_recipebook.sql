DROP TABLE IF EXISTS `character_recipebook`;
CREATE TABLE `character_recipebook` (
	`char_id` INT NOT NULL DEFAULT '0',
	`id` SMALLINT UNSIGNED NOT NULL DEFAULT '0',
	PRIMARY KEY  (`id`,`char_id`)
) ENGINE=MyISAM;
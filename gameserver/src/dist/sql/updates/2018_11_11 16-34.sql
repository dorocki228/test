DROP TABLE IF EXISTS `character_elementals`;
CREATE TABLE `character_elementals` (
	`char_obj_id` INT NOT NULL,
	`class_index` SMALLINT NOT NULL,
	`element_id` TINYINT NOT NULL,
	`evolution_level` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	`exp` BIGINT UNSIGNED NOT NULL DEFAULT '0',
	`attack_points` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	`defence_points` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	`crit_rate_points` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	`crit_attack_points` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	PRIMARY KEY  (`char_obj_id`,`class_index`,`element_id`)
) ENGINE=MyISAM;
UPDATE character_elementals SET evolution_level = 1;
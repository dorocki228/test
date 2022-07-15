DROP TABLE IF EXISTS `character_elementals`;
CREATE TABLE `character_elementals` (
	`char_obj_id` INT NOT NULL,
	`class_index` SMALLINT NOT NULL,
	`element_id` TINYINT NOT NULL,
	`elemental_exp` INT UNSIGNED NOT NULL DEFAULT '0',
	`elemental_attack` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	`elemental_defence` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	`elemental_crit_rate` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	`elemental_crit_attack` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	`used_points` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	PRIMARY KEY  (`char_obj_id`,`class_index`,`element_id`)
) ENGINE=MyISAM;
ALTER TABLE `character_subclasses` ADD COLUMN `active_element_id` TINYINT NOT NULL DEFAULT '0' AFTER `type`;
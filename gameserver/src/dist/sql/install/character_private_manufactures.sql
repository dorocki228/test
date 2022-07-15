DROP TABLE IF EXISTS `character_private_manufactures`;
CREATE TABLE `character_private_manufactures` (
	`char_id` INT NOT NULL,
	`recipe_id` SMALLINT UNSIGNED NOT NULL,
	`cost` BIGINT NOT NULL,
	`index` TINYINT NOT NULL,
	PRIMARY KEY  (`char_id`,`recipe_id`)
) ENGINE=MyISAM;

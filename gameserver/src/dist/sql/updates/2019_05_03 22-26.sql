DROP TABLE IF EXISTS `character_private_buys`;
CREATE TABLE `character_private_buys` (
	`char_id` INT NOT NULL,
	`item_id` INT NOT NULL,
	`item_count` BIGINT NOT NULL,
	`owner_price` BIGINT NOT NULL,
	`enchant_level` INT NOT NULL,
	`index` TINYINT NOT NULL
) ENGINE=MyISAM;

DROP TABLE IF EXISTS `character_private_sells`;
CREATE TABLE `character_private_sells` (
	`char_id` INT NOT NULL,
	`package` TINYINT NOT NULL,
	`item_object_id` INT NOT NULL,
	`item_count` BIGINT NOT NULL,
	`owner_price` BIGINT NOT NULL,
	`index` TINYINT NOT NULL,
	PRIMARY KEY  (`char_id`,`item_object_id`)
) ENGINE=MyISAM;

DROP TABLE IF EXISTS `character_private_manufactures`;
CREATE TABLE `character_private_manufactures` (
	`char_id` INT NOT NULL,
	`recipe_id` SMALLINT UNSIGNED NOT NULL,
	`cost` BIGINT NOT NULL,
	`index` TINYINT NOT NULL,
	PRIMARY KEY  (`char_id`,`recipe_id`)
) ENGINE=MyISAM;

DELETE FROM character_variables WHERE name='buylist';
DELETE FROM character_variables WHERE name='selllist';
DELETE FROM character_variables WHERE name='packageselllist';
DELETE FROM character_variables WHERE name='createlist';
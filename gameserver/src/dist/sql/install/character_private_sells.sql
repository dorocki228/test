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

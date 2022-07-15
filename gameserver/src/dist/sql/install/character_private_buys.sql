DROP TABLE IF EXISTS `character_private_buys`;
CREATE TABLE `character_private_buys` (
	`char_id` INT NOT NULL,
	`item_id` INT NOT NULL,
	`item_count` BIGINT NOT NULL,
	`owner_price` BIGINT NOT NULL,
	`enchant_level` INT NOT NULL,
	`index` TINYINT NOT NULL
) ENGINE=MyISAM;

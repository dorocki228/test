DROP TABLE IF EXISTS `items_to_delete`;
CREATE TABLE `items_to_delete` (
	`item_id` int(7) UNSIGNED NOT NULL,
	`description` VARCHAR(255) DEFAULT "",
	PRIMARY KEY (`item_id`)
) ENGINE=MyISAM;
DROP TABLE IF EXISTS `character_product_history`;
CREATE TABLE IF NOT EXISTS `product_history` (
	`account_name` VARCHAR(45) NOT NULL DEFAULT '',
	`product_id` INT(11) NOT NULL,
	`purchased_count` INT(11) NOT NULL,
	`last_purchase_time` INT(11) NOT NULL,
	PRIMARY KEY  (`account_name`,`product_id`)
);
ALTER TABLE `character_product_history` ADD COLUMN `purchased_count` INT(11) NOT NULL AFTER product_id;
UPDATE `character_product_history` SET purchased_count=0;
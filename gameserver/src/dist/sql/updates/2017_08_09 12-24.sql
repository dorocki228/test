ALTER TABLE items ADD COLUMN `variation_stone_id` int(7) NOT NULL AFTER `life_time`;
ALTER TABLE items ADD COLUMN `variation1_id` int(7) NOT NULL AFTER `variation_stone_id`;
ALTER TABLE items ADD COLUMN `variation2_id` int(7) NOT NULL AFTER `variation1_id`;
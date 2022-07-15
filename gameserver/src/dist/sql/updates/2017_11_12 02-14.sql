ALTER TABLE `items` ADD COLUMN `appearance_stone_id` int(7) NOT NULL AFTER custom_flags;
ALTER TABLE `items` ADD COLUMN `visual_id` int(7) NOT NULL AFTER appearance_stone_id;
UPDATE items SET appearance_stone_id=0;
UPDATE items SET visual_id=0;
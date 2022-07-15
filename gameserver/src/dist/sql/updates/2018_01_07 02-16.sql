ALTER TABLE `items` ADD COLUMN `agathion_energy` int(11) NOT NULL AFTER custom_flags;
UPDATE items SET agathion_energy=0;
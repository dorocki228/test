DROP TABLE IF EXISTS `items`;
CREATE TABLE `items` (
  `object_id` int(11) NOT NULL,
  `owner_id` int(11) NOT NULL,
  `item_id` int(7) NOT NULL,
  `count` bigint(20) NOT NULL,
  `enchant_level` int(11) NOT NULL,
  `loc` varchar(32) NOT NULL,
  `loc_data` int(11) NOT NULL,
  `life_time` int(11) NOT NULL,  
  `variation_stone_id` int(7) NOT NULL,
  `variation1_id` int(7) NOT NULL,
  `variation2_id` int(7) NOT NULL,
  `custom_type1` int(5) NOT NULL,
  `custom_type2` int(5) NOT NULL,
  `custom_flags` int(11) NOT NULL,
  `agathion_energy` int(11) NOT NULL,
  `appearance_stone_id` int(7) NOT NULL,
  `visual_id` int(7) NOT NULL,
  PRIMARY KEY  (`object_id`),
  KEY `owner_id` (`owner_id`),
  KEY `loc` (`loc`),
  KEY `item_id` (`item_id`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `items_ensoul`;
CREATE TABLE `items_ensoul` (
  `object_id` int(11) NOT NULL,
  `type` tinyint(1) NOT NULL,
  `id` tinyint(3) NOT NULL,
  `ensoul_id` int(3) NOT NULL,
  PRIMARY KEY  (`object_id`, `type`, `id`),
  FOREIGN KEY (`object_id`) REFERENCES `items` (`object_id`) ON DELETE CASCADE
) ENGINE=InnoDB;
-- ----------------------------
-- Table structure for clan_arena
-- ----------------------------
DROP TABLE IF EXISTS `clan_arena`;
CREATE TABLE `clan_arena` (
  `clan_id` int(10) NOT NULL,
  `stage` int(5) DEFAULT NULL,
  `attempts_today` int(5) DEFAULT NULL,
  PRIMARY KEY (`clan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `castle` (
  `id` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `name` varchar(25) NOT NULL,
  `treasury` bigint(20) unsigned NOT NULL DEFAULT '0',
  `town_id` int(11) NOT NULL,
  `last_siege_date` INT UNSIGNED NOT NULL,
  `owner_id` INT NOT NULL DEFAULT '0',
  `own_date` INT UNSIGNED NOT NULL,
  `siege_date` INT UNSIGNED NOT NULL,
  `side` tinyint(1) unsigned NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
);

-- ----------------------------
-- Records of castle
-- ----------------------------
INSERT INTO castle VALUES ('4', 'Oren', '0', '4', '0', '0', '0', '0', '0');
INSERT INTO castle VALUES ('10', 'Elven', '0', '10', '0', '0', '0', '0', '0');
INSERT INTO castle VALUES ('8', 'Rune', '0', '8', '0', '0', '0', '0', '0');
INSERT INTO castle VALUES ('9', 'Schuttgart', '0', '9', '0', '0', '0', '0', '0');

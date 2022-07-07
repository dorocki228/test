DROP TABLE IF EXISTS `fortress`;
CREATE TABLE `fortress` (
  `id` smallint(3) unsigned NOT NULL DEFAULT '0',
  `name` varchar(45) NOT NULL,
  `owner_id` int(11) NOT NULL DEFAULT '0',
  `fraction` tinyint(1) NOT NULL DEFAULT '0',
  `last_siege_date` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of fortress
-- ----------------------------
INSERT INTO `fortress` VALUES (400, 'Eastern Fortress', 0, 0, 0);
INSERT INTO `fortress` VALUES (401, 'Northern Fortress', 0, 0, 0);
INSERT INTO `fortress` VALUES (402, 'Western Fortress', 0, 0, 0);
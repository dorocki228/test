SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `artifact`
-- ----------------------------
DROP TABLE IF EXISTS `artifact`;
CREATE TABLE `artifact` (
  `id` int(11) NOT NULL,
  `faction` smallint(6) NOT NULL,
  `end_protect` mediumtext NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

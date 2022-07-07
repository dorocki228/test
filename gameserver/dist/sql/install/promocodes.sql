-- ----------------------------
-- Table structure for `promocodes`
-- ----------------------------
DROP TABLE IF EXISTS `promocodes`;
CREATE TABLE `promocodes` (
  `code` varchar(255) NOT NULL,
  `uses` int(11) NOT NULL,
  PRIMARY KEY (`code`),
  KEY `code` (`code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
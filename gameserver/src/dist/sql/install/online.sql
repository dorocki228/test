DROP TABLE IF EXISTS `online`;
CREATE TABLE `online` (
  `index` int(1) NOT NULL,
  `totalOnline` int(6) NOT NULL,
  `totalOffline` int(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `online` VALUES ('0', '0', '0');

CREATE TABLE IF NOT EXISTS `stead_barn` (
  `owner` int(11) NOT NULL,
  `id` int(11) NOT NULL,
  `count` bigint(21) DEFAULT NULL,
  PRIMARY KEY (`owner`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
CREATE TABLE IF NOT EXISTS `retention_log` (
  `date_time` datetime NOT NULL,
  `value` MEDIUMTEXT NOT NULL,
  KEY `date_time` (`date_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8
CREATE TABLE IF NOT EXISTS `bans` (
  `bind_type` enum('login','ip','hwid') NOT NULL,
  `bind_value` VARCHAR(128) NOT NULL,
  `end_time` INT(11) NOT NULL,
  `reason` VARCHAR(256) CHARACTER SET UTF8 NOT NULL DEFAULT '',
  PRIMARY KEY (`bind_type`,`bind_value`)
) DEFAULT CHARSET=utf8;
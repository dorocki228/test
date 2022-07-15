DROP TABLE IF EXISTS `premium_accounts`;
CREATE TABLE `premium_accounts` (
  `account` varchar(32) NOT NULL,
  `type` double NOT NULL,
  `expire_time` int(11) NOT NULL,
  PRIMARY KEY (`account`)
);
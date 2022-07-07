CREATE TABLE `auth_log` (
 `id` int(11) NOT NULL AUTO_INCREMENT,
 `date` datetime NOT NULL,
 `account` varchar(14) NOT NULL,
 `objId` int(10) NOT NULL,
 `hwid` varchar(64) DEFAULT NULL,
 `ip` varchar(16) NOT NULL,
 PRIMARY KEY (`id`)
 ) ENGINE=MyISAM DEFAULT CHARSET=utf8;
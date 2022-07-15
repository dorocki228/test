DROP TABLE IF EXISTS `event_data`;
CREATE TABLE `event_data` (
  `charId` int(15) NOT NULL,
  `score` int(5) DEFAULT NULL,
  PRIMARY KEY (`charId`) )
  ENGINE=MyISAM;

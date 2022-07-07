-- ----------------------------
-- Table structure for `promocodes_data`
-- ----------------------------
DROP TABLE IF EXISTS `promocodes_data`;
CREATE TABLE `promocodes_data` (
  `code` varchar(255) NOT NULL,
  `objId` int(11) NOT NULL,
  `uses` int(11) NOT NULL,
  `hwid` varchar(255) NOT NULL,
  PRIMARY KEY (`code`,`objId`),
  KEY `PCD_FK_CH` (`objId`),
  KEY `PCD_FK_PC` (`code`) USING BTREE,
  CONSTRAINT `promocodes_data_ibfk_1` FOREIGN KEY (`code`) REFERENCES `promocodes` (`code`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `promocodes_data_ibfk_2` FOREIGN KEY (`objId`) REFERENCES `characters` (`obj_Id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
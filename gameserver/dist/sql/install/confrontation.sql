SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `confrontation`
-- ----------------------------
DROP TABLE IF EXISTS `confrontation`;
CREATE TABLE `confrontation` (
  `objId` int(11) NOT NULL,
  `currentPeriodPoints` int(11) NOT NULL,
  `totalPoints` int(11) NOT NULL,
  `availablePoints` int(11) NOT NULL,
  PRIMARY KEY (`objId`),
  UNIQUE KEY `confrontation_objId_uindex` (`objId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `confrontation_skills`
-- ----------------------------
DROP TABLE IF EXISTS `confrontation_skills`;
CREATE TABLE `confrontation_skills` (
  `objId` int(11) NOT NULL,
  `skillId` int(11) NOT NULL,
  `skillLevel` int(11) NOT NULL,
  PRIMARY KEY (`objId`,`skillId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of confrontation_skills
-- ----------------------------

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `faction_leader_privileges`
-- ----------------------------
DROP TABLE IF EXISTS `faction_leader_privileges`;
CREATE TABLE `faction_leader_privileges` (
  `faction` smallint(6) NOT NULL,
  `obj_id` int(11) NOT NULL,
  `privileges` int(11) NOT NULL,
  PRIMARY KEY (`faction`,`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of faction_leader_privileges
-- ----------------------------

-- ----------------------------
-- Table structure for `faction_leader_request`
-- ----------------------------
DROP TABLE IF EXISTS `faction_leader_request`;
CREATE TABLE `faction_leader_request` (
  `faction` smallint(6) NOT NULL,
  `obj_id` int(11) NOT NULL,
  `hwid` varchar(255) NOT NULL,
  PRIMARY KEY (`faction`,`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of faction_leader_request
-- ----------------------------

-- ----------------------------
-- Table structure for `faction_leader_state`
-- ----------------------------
DROP TABLE IF EXISTS `faction_leader_state`;
CREATE TABLE `faction_leader_state` (
  `type` int(11) NOT NULL,
  `cycle` int(11) NOT NULL,
  `state` smallint(6) NOT NULL,
  `end_cycle` bigint(20) NOT NULL,
  `start_cycle` bigint(20) NOT NULL,
  PRIMARY KEY (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `faction_leader_vote`
-- ----------------------------
DROP TABLE IF EXISTS `faction_leader_vote`;
CREATE TABLE `faction_leader_vote` (
  `faction` smallint(6) NOT NULL,
  `voted_obj_id` int(11) NOT NULL,
  `voted_for_obj_id` int(11) NOT NULL,
  `hwid` varchar(255) NOT NULL,
  PRIMARY KEY (`faction`,`voted_obj_id`,`voted_for_obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `multisell_logs`
-- ----------------------------
DROP TABLE IF EXISTS `multisell_logs`;
CREATE TABLE `multisell_logs` (
  `obj_id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `count` bigint(20) NOT NULL,
  PRIMARY KEY (`obj_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

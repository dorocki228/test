DROP TABLE IF EXISTS `clan_leader_request`;
CREATE TABLE `clan_leader_request` (
  `clan_id` int(11) NOT NULL,
  `new_leader_id` int(11) NOT NULL,
  `time` bigint(20) NOT NULL,
  PRIMARY KEY (`clan_id`)
);


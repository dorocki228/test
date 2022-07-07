DROP TABLE IF EXISTS `fortress_upgrade`;
CREATE TABLE `fortress_upgrade`  (
  `id` int(2) NOT NULL,
  `crystal` tinyint(1) NOT NULL DEFAULT 1,
  `gate` tinyint(1) NOT NULL DEFAULT 1,
  `guard` tinyint(1) NOT NULL DEFAULT 1,
  `guardian` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

INSERT INTO `fortress_upgrade` VALUES (400, 1, 1, 1, 1);
INSERT INTO `fortress_upgrade` VALUES (401, 1, 1, 1, 1);
INSERT INTO `fortress_upgrade` VALUES (402, 1, 1, 1, 1);

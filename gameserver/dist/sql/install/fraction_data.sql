DROP TABLE IF EXISTS `fraction_data`;
CREATE TABLE `fraction_data`  (
  `treasure` bigint(19) NULL DEFAULT NULL,
  `fraction` enum('FIRE','WATER') CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

INSERT INTO `fraction_data` VALUES (0, 'FIRE');
INSERT INTO `fraction_data` VALUES (0, 'WATER');

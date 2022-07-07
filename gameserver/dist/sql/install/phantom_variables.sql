
-- ----------------------------
-- Table structure for `phantom_variables`
-- ----------------------------
DROP TABLE IF EXISTS `phantom_variables`;
CREATE TABLE `phantom_variables` (
  `name` varchar(86) NOT NULL DEFAULT '',
  `value` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of phantom_variables
-- ----------------------------
INSERT INTO `phantom_variables` VALUES ('ChanceSpawnP3', '65');
INSERT INTO `phantom_variables` VALUES ('ChanceSpawnHard', '90');
INSERT INTO `phantom_variables` VALUES ('PhantomBasicSpawn', 'on');
INSERT INTO `phantom_variables` VALUES ('CfgPhantomMaxLvl2', '85');
INSERT INTO `phantom_variables` VALUES ('CfgPhantomMaxLvl', '85');
INSERT INTO `phantom_variables` VALUES ('CfgPhantomManualMaxLvl', 'on');
INSERT INTO `phantom_variables` VALUES ('GreatePhantomMaxLvl', '85');
INSERT INTO `phantom_variables` VALUES ('SetMax15EnchLevel', '7');
INSERT INTO `phantom_variables` VALUES ('SetMax30EnchLevel', '10');
INSERT INTO `phantom_variables` VALUES ('PRndEnchSkill', 'on');
INSERT INTO `phantom_variables` VALUES ('PhantomSpawnP3', 'on');
INSERT INTO `phantom_variables` VALUES ('CfgPhantomLimit', '1500');
INSERT INTO `phantom_variables` VALUES ('ChanceSpawnP2', '20');
INSERT INTO `phantom_variables` VALUES ('ChanceSpawnP1', '10');
INSERT INTO `phantom_variables` VALUES ('ChanceSpawnNG', '5');
INSERT INTO `phantom_variables` VALUES ('AttackPlayerPvpFlag', 'on');
INSERT INTO `phantom_variables` VALUES ('ChanceTargetPlayerPvpFlag', '80');
INSERT INTO `phantom_variables` VALUES ('ChanceTargetPhantomPvpFlag', '1');
INSERT INTO `phantom_variables` VALUES ('CheckingNicknamesCP', 'false');
INSERT INTO `phantom_variables` VALUES ('PhantomSpawnP1', 'on');
INSERT INTO `phantom_variables` VALUES ('MaxEquipGrade', 'S85');
INSERT INTO `phantom_variables` VALUES ('PCount_merchants', '150');
INSERT INTO `phantom_variables` VALUES ('PhantomSpawnP2', 'on');
INSERT INTO `phantom_variables` VALUES ('PhantomSpawnNG', 'on');
INSERT INTO `phantom_variables` VALUES ('PhantomSpawnCraftOrTrade', 'on');
INSERT INTO `phantom_variables` VALUES ('EnableSpawnPhantomCP', 'on');
INSERT INTO `phantom_variables` VALUES ('PhantomSpawnHard', 'on');
INSERT INTO `phantom_variables` VALUES ('SpawnBotHunters', 'on');
INSERT INTO `phantom_variables` VALUES ('PhantomSpawnStartLoc', 'on');
INSERT INTO `phantom_variables` VALUES ('ChanceSpawnStartLoc', '80');
INSERT INTO `phantom_variables` VALUES ('MaxEquipGradeServant', 'S85');
INSERT INTO `phantom_variables` VALUES ('PhantomHardAttWeapon', '300');
INSERT INTO `phantom_variables` VALUES ('PhantomHardMinAttArmor', '60');
INSERT INTO `phantom_variables` VALUES ('PhantomHardMaxAttArmor', '120');
INSERT INTO `phantom_variables` VALUES ('PhantomHardMinEnchW', '3');
INSERT INTO `phantom_variables` VALUES ('PhantomHardMaxEnchW', '4');
INSERT INTO `phantom_variables` VALUES ('PhantomHardMinEnchArmor', '3');
INSERT INTO `phantom_variables` VALUES ('PhantomHardMaxEnchArmor', '6');
INSERT INTO `phantom_variables` VALUES ('PEnchSkill', 'on');
INSERT INTO `phantom_variables` VALUES ('testFortSiege', 'Вакидзаси');
INSERT INTO `phantom_variables` VALUES ('testcastleSiege', 'VipClan');

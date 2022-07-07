/*
Navicat MySQL Data Transfer

Source Server         : фанг
Source Server Version : 50541
Source Host           : 62.210.124.93:3306
Source Database       : fange_x100_multicraft

Target Server Type    : MYSQL
Target Server Version : 50541
File Encoding         : 65001

Date: 2017-08-30 02:25:29
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for char_templates
-- ----------------------------
DROP TABLE IF EXISTS `char_templates`;
CREATE TABLE `char_templates` (
  `ClassId` int(11) NOT NULL DEFAULT '0',
  `ClassName` varchar(20) NOT NULL DEFAULT '',
  `RaceId` int(1) NOT NULL DEFAULT '0',
  `STR` int(2) NOT NULL DEFAULT '0',
  `CON` int(2) NOT NULL DEFAULT '0',
  `DEX` int(2) NOT NULL DEFAULT '0',
  `_INT` int(2) NOT NULL DEFAULT '0',
  `WIT` int(2) NOT NULL DEFAULT '0',
  `MEN` int(2) NOT NULL DEFAULT '0',
  `P_ATK` int(3) NOT NULL DEFAULT '0',
  `P_DEF` int(3) NOT NULL DEFAULT '0',
  `M_ATK` int(3) NOT NULL DEFAULT '0',
  `M_DEF` int(2) NOT NULL DEFAULT '0',
  `P_SPD` int(3) NOT NULL DEFAULT '0',
  `M_SPD` int(3) NOT NULL DEFAULT '0',
  `ACC` int(3) NOT NULL DEFAULT '0',
  `CRITICAL` int(3) NOT NULL DEFAULT '0',
  `EVASION` int(3) NOT NULL DEFAULT '0',
  `MOVE_SPD` int(3) NOT NULL DEFAULT '0',
  `_LOAD` int(11) NOT NULL DEFAULT '0',
  `x` int(9) NOT NULL DEFAULT '0',
  `y` int(9) NOT NULL DEFAULT '0',
  `z` int(9) NOT NULL DEFAULT '0',
  `canCraft` int(1) NOT NULL DEFAULT '0',
  `M_UNK1` decimal(4,2) NOT NULL DEFAULT '0.00',
  `M_UNK2` decimal(8,6) NOT NULL DEFAULT '0.000000',
  `M_COL_R` decimal(3,1) NOT NULL DEFAULT '0.0',
  `M_COL_H` decimal(4,1) NOT NULL DEFAULT '0.0',
  `F_UNK1` decimal(4,2) NOT NULL DEFAULT '0.00',
  `F_UNK2` decimal(8,6) NOT NULL DEFAULT '0.000000',
  `F_COL_R` decimal(3,1) NOT NULL DEFAULT '0.0',
  `F_COL_H` decimal(4,1) NOT NULL DEFAULT '0.0',
  PRIMARY KEY (`ClassId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of char_templates
-- ----------------------------
INSERT INTO `char_templates` VALUES ('0', 'Human Fighter', '0', '40', '43', '30', '21', '11', '25', '4', '80', '6', '41', '300', '333', '33', '44', '33', '115', '81900', '-71338', '258271', '-3104', '0', '1.10', '1.188000', '9.0', '23.0', '1.10', '1.188000', '8.0', '23.5');
INSERT INTO `char_templates` VALUES ('1', 'Warrior', '0', '40', '43', '30', '21', '11', '25', '4', '80', '6', '41', '300', '333', '33', '44', '33', '115', '81900', '-71338', '258271', '-3104', '0', '1.10', '1.188000', '9.0', '23.0', '1.10', '1.188000', '8.0', '23.5');
INSERT INTO `char_templates` VALUES ('2', 'Gladiator', '0', '40', '43', '30', '21', '11', '25', '4', '80', '6', '41', '300', '333', '33', '44', '33', '115', '81900', '-71338', '258271', '-3104', '0', '1.10', '1.188000', '9.0', '23.0', '1.10', '1.188000', '8.0', '23.5');
INSERT INTO `char_templates` VALUES ('3', 'Warlord', '0', '40', '43', '30', '21', '11', '25', '4', '80', '6', '41', '300', '333', '33', '44', '33', '115', '81900', '-71338', '258271', '-3104', '0', '1.10', '1.188000', '9.0', '23.0', '1.10', '1.188000', '8.0', '23.5');
INSERT INTO `char_templates` VALUES ('4', 'Human Knight', '0', '40', '43', '30', '21', '11', '25', '4', '80', '6', '41', '300', '333', '33', '44', '33', '115', '81900', '-71338', '258271', '-3104', '0', '1.10', '1.188000', '9.0', '23.0', '1.10', '1.188000', '8.0', '23.5');
INSERT INTO `char_templates` VALUES ('5', 'Paladin', '0', '40', '43', '30', '21', '11', '25', '4', '80', '6', '41', '300', '333', '33', '44', '33', '115', '81900', '-71338', '258271', '-3104', '0', '1.10', '1.188000', '9.0', '23.0', '1.10', '1.188000', '8.0', '23.5');
INSERT INTO `char_templates` VALUES ('6', 'Dark Avenger', '0', '40', '43', '30', '21', '11', '25', '4', '80', '6', '41', '300', '333', '33', '44', '33', '115', '81900', '-71338', '258271', '-3104', '0', '1.10', '1.188000', '9.0', '23.0', '1.10', '1.188000', '8.0', '23.5');
INSERT INTO `char_templates` VALUES ('7', 'Rogue', '0', '40', '43', '30', '21', '11', '25', '4', '80', '6', '41', '300', '333', '33', '44', '33', '115', '81900', '-71338', '258271', '-3104', '0', '1.10', '1.188000', '9.0', '23.0', '1.10', '1.188000', '8.0', '23.5');
INSERT INTO `char_templates` VALUES ('8', 'Treasure Hunter', '0', '40', '43', '30', '21', '11', '25', '4', '80', '6', '41', '300', '333', '33', '44', '33', '115', '81900', '-71338', '258271', '-3104', '0', '1.10', '1.188000', '9.0', '23.0', '1.10', '1.188000', '8.0', '23.5');
INSERT INTO `char_templates` VALUES ('9', 'Hawkeye', '0', '40', '43', '30', '21', '11', '25', '4', '80', '6', '41', '300', '333', '33', '44', '33', '115', '81900', '-71338', '258271', '-3104', '0', '1.10', '1.188000', '9.0', '23.0', '1.10', '1.188000', '8.0', '23.5');
INSERT INTO `char_templates` VALUES ('10', 'Human Mage', '0', '22', '27', '21', '41', '20', '39', '3', '54', '6', '41', '300', '333', '28', '40', '28', '120', '62500', '-90890', '248027', '-3570', '0', '1.01', '0.872640', '7.5', '22.8', '1.01', '0.872640', '6.5', '22.5');
INSERT INTO `char_templates` VALUES ('11', 'Human Wizard', '0', '22', '27', '21', '41', '20', '39', '3', '54', '6', '41', '300', '333', '28', '40', '28', '120', '62500', '-90890', '248027', '-3570', '0', '1.01', '0.872640', '7.5', '22.8', '1.01', '0.872640', '6.5', '22.5');
INSERT INTO `char_templates` VALUES ('12', 'Sorcerer', '0', '22', '27', '21', '41', '20', '39', '3', '54', '6', '41', '300', '333', '28', '40', '28', '120', '62500', '-90890', '248027', '-3570', '0', '1.01', '0.872640', '7.5', '22.8', '1.01', '0.872640', '6.5', '22.5');
INSERT INTO `char_templates` VALUES ('13', 'Necromancer', '0', '22', '27', '21', '41', '20', '39', '3', '54', '6', '41', '300', '333', '28', '40', '28', '120', '62500', '-90890', '248027', '-3570', '0', '1.01', '0.872640', '7.5', '22.8', '1.01', '0.872640', '6.5', '22.5');
INSERT INTO `char_templates` VALUES ('14', 'Warlock', '0', '22', '27', '21', '41', '20', '39', '3', '54', '6', '41', '300', '333', '28', '40', '28', '120', '62500', '-90890', '248027', '-3570', '0', '1.01', '0.872640', '7.5', '22.8', '1.01', '0.872640', '6.5', '22.5');
INSERT INTO `char_templates` VALUES ('15', 'Cleric', '0', '22', '27', '21', '41', '20', '39', '3', '54', '6', '41', '300', '333', '28', '40', '28', '120', '62500', '-90890', '248027', '-3570', '0', '1.01', '0.872640', '7.5', '22.8', '1.01', '0.872640', '6.5', '22.5');
INSERT INTO `char_templates` VALUES ('16', 'Bishop', '0', '22', '27', '21', '41', '20', '39', '3', '54', '6', '41', '300', '333', '28', '40', '28', '120', '62500', '-90890', '248027', '-3570', '0', '1.01', '0.872640', '7.5', '22.8', '1.01', '0.872640', '6.5', '22.5');
INSERT INTO `char_templates` VALUES ('17', 'Human Prophet', '0', '22', '27', '21', '41', '20', '39', '3', '54', '6', '41', '300', '333', '28', '40', '28', '120', '62500', '-90890', '248027', '-3570', '0', '1.01', '0.872640', '7.5', '22.8', '1.01', '0.872640', '6.5', '22.5');
INSERT INTO `char_templates` VALUES ('18', 'Elf Fighter', '1', '36', '36', '35', '23', '14', '26', '4', '80', '6', '41', '300', '333', '36', '46', '36', '125', '73000', '45978', '41196', '-3440', '0', '1.15', '1.242000', '7.5', '24.0', '1.15', '1.242000', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('19', 'Elf Knight', '1', '36', '36', '35', '23', '14', '26', '4', '80', '6', '41', '300', '333', '36', '46', '36', '125', '73000', '45978', '41196', '-3440', '0', '1.15', '1.242000', '7.5', '24.0', '1.15', '1.242000', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('20', 'Temple Knight', '1', '36', '36', '35', '23', '14', '26', '4', '80', '6', '41', '300', '333', '36', '46', '36', '125', '73000', '45978', '41196', '-3440', '0', '1.15', '1.242000', '7.5', '24.0', '1.15', '1.242000', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('21', 'Swordsinger', '1', '36', '36', '35', '23', '14', '26', '4', '80', '6', '41', '300', '333', '36', '46', '36', '125', '73000', '45978', '41196', '-3440', '0', '1.15', '1.242000', '7.5', '24.0', '1.15', '1.242000', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('22', 'Scout', '1', '36', '36', '35', '23', '14', '26', '4', '80', '6', '41', '300', '333', '36', '46', '36', '125', '73000', '45978', '41196', '-3440', '0', '1.15', '1.242000', '7.5', '24.0', '1.15', '1.242000', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('23', 'Plains Walker', '1', '36', '36', '35', '23', '14', '26', '4', '80', '6', '41', '300', '333', '36', '46', '36', '125', '73000', '45978', '41196', '-3440', '0', '1.15', '1.242000', '7.5', '24.0', '1.15', '1.242000', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('24', 'Silver Ranger', '1', '36', '36', '35', '23', '14', '26', '4', '80', '6', '41', '300', '333', '36', '46', '36', '125', '73000', '45978', '41196', '-3440', '0', '1.15', '1.242000', '7.5', '24.0', '1.15', '1.242000', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('25', 'Elf Mage', '1', '21', '25', '24', '37', '23', '40', '3', '54', '6', '41', '300', '333', '30', '41', '30', '122', '62400', '46182', '41198', '-3440', '0', '1.04', '0.898560', '7.5', '24.0', '1.04', '0.898560', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('26', 'Elf Wizard', '1', '21', '25', '24', '37', '23', '40', '3', '54', '6', '41', '300', '333', '30', '41', '30', '122', '62400', '46182', '41198', '-3440', '0', '1.04', '0.898560', '7.5', '24.0', '1.04', '0.898560', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('27', 'Spellsinger', '1', '21', '25', '24', '37', '23', '40', '3', '54', '6', '41', '300', '333', '30', '41', '30', '122', '62400', '46182', '41198', '-3440', '0', '1.04', '0.898560', '7.5', '24.0', '1.04', '0.898560', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('28', 'Elemental Summoner', '1', '21', '25', '24', '37', '23', '40', '3', '54', '6', '41', '300', '333', '30', '41', '30', '122', '62400', '46182', '41198', '-3440', '0', '1.04', '0.898560', '7.5', '24.0', '1.04', '0.898560', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('29', 'Oracle', '1', '21', '25', '24', '37', '23', '40', '3', '54', '6', '41', '300', '333', '30', '41', '30', '122', '62400', '46182', '41198', '-3440', '0', '1.04', '0.898560', '7.5', '24.0', '1.04', '0.898560', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('30', 'Elder', '1', '21', '25', '24', '37', '23', '40', '3', '54', '6', '41', '300', '333', '30', '41', '30', '122', '62400', '46182', '41198', '-3440', '0', '1.04', '0.898560', '7.5', '24.0', '1.04', '0.898560', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('31', 'DE Fighter', '2', '41', '32', '34', '25', '12', '26', '4', '80', '6', '41', '300', '333', '35', '45', '35', '122', '69000', '28377', '10916', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.14', '1.231200', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('32', 'Palus Knight', '2', '41', '32', '34', '25', '12', '26', '4', '80', '6', '41', '300', '333', '35', '45', '35', '122', '69000', '28377', '10916', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.14', '1.231200', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('33', 'Shillien Knight', '2', '41', '32', '34', '25', '12', '26', '4', '80', '6', '41', '300', '333', '35', '45', '35', '122', '69000', '28377', '10916', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.14', '1.231200', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('34', 'Bladedancer', '2', '41', '32', '34', '25', '12', '26', '4', '80', '6', '41', '300', '333', '35', '45', '35', '122', '69000', '28377', '10916', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.14', '1.231200', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('35', 'Assassin', '2', '41', '32', '34', '25', '12', '26', '4', '80', '6', '41', '300', '333', '35', '45', '35', '122', '69000', '28377', '10916', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.14', '1.231200', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('36', 'Abyss Walker', '2', '41', '32', '34', '25', '12', '26', '4', '80', '6', '41', '300', '333', '35', '45', '35', '122', '69000', '28377', '10916', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.14', '1.231200', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('37', 'Phantom Ranger', '2', '41', '32', '34', '25', '12', '26', '4', '80', '6', '41', '300', '333', '35', '45', '35', '122', '69000', '28377', '10916', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.14', '1.231200', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('38', 'DE Mage', '2', '23', '24', '23', '44', '19', '37', '3', '54', '6', '41', '300', '333', '29', '41', '29', '122', '61000', '28295', '11063', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.03', '0.889920', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('39', 'DE Wizard', '2', '23', '24', '23', '44', '19', '37', '3', '54', '6', '41', '300', '333', '29', '41', '29', '122', '61000', '28295', '11063', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.03', '0.889920', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('40', 'Spell Howler', '2', '23', '24', '23', '44', '19', '37', '3', '54', '6', '41', '300', '333', '29', '41', '29', '122', '61000', '28295', '11063', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.03', '0.889920', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('41', 'Phantom Summoner', '2', '23', '24', '23', '44', '19', '37', '3', '54', '6', '41', '300', '333', '29', '41', '29', '122', '61000', '28295', '11063', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.03', '0.889920', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('42', 'Shillien Oracle', '2', '23', '24', '23', '44', '19', '37', '3', '54', '6', '41', '300', '333', '29', '41', '29', '122', '61000', '28295', '11063', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.03', '0.889920', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('43', 'Shillien Elder', '2', '23', '24', '23', '44', '19', '37', '3', '54', '6', '41', '300', '333', '29', '41', '29', '122', '61000', '28295', '11063', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.03', '0.889920', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('44', 'Orc Fighter', '3', '40', '47', '26', '18', '12', '27', '4', '80', '6', '41', '300', '333', '31', '42', '31', '117', '87000', '-56693', '-113610', '-690', '0', '1.06', '1.144800', '11.0', '28.0', '1.06', '1.144800', '7.0', '27.0');
INSERT INTO `char_templates` VALUES ('45', 'Raider', '3', '40', '47', '26', '18', '12', '27', '4', '80', '6', '41', '300', '333', '31', '42', '31', '117', '87000', '-56693', '-113610', '-690', '0', '1.06', '1.144800', '11.0', '28.0', '1.06', '1.144800', '7.0', '27.0');
INSERT INTO `char_templates` VALUES ('46', 'Destroyer', '3', '40', '47', '26', '18', '12', '27', '4', '80', '6', '41', '300', '333', '31', '42', '31', '117', '87000', '-56693', '-113610', '-690', '0', '1.06', '1.144800', '11.0', '28.0', '1.06', '1.144800', '7.0', '27.0');
INSERT INTO `char_templates` VALUES ('47', 'Monk', '3', '40', '47', '26', '18', '12', '27', '4', '80', '6', '41', '300', '333', '31', '42', '31', '117', '87000', '-56682', '-113610', '-690', '0', '1.06', '1.144800', '11.0', '28.0', '1.06', '1.144800', '7.0', '27.0');
INSERT INTO `char_templates` VALUES ('48', 'Tyrant', '3', '40', '47', '26', '18', '12', '27', '4', '80', '6', '41', '300', '333', '31', '42', '31', '117', '87000', '-56693', '-113610', '-690', '0', '1.06', '1.144800', '11.0', '28.0', '1.06', '1.144800', '7.0', '27.0');
INSERT INTO `char_templates` VALUES ('49', 'Orc Mage', '3', '27', '31', '24', '31', '15', '42', '3', '54', '6', '41', '300', '333', '30', '41', '30', '121', '68000', '-56682', '-113730', '-690', '0', '1.04', '0.898560', '7.0', '27.5', '1.04', '0.898560', '8.0', '25.5');
INSERT INTO `char_templates` VALUES ('50', 'Shaman', '3', '27', '31', '24', '31', '15', '42', '3', '54', '6', '41', '300', '333', '30', '41', '30', '121', '68000', '-56682', '-113730', '-690', '0', '1.04', '0.898560', '7.0', '27.5', '1.04', '0.898560', '8.0', '25.5');
INSERT INTO `char_templates` VALUES ('51', 'Overlord', '3', '27', '31', '24', '31', '15', '42', '3', '54', '6', '41', '300', '333', '30', '41', '30', '121', '68000', '-56682', '-113730', '-690', '0', '1.04', '0.898560', '7.0', '27.5', '1.04', '0.898560', '8.0', '25.5');
INSERT INTO `char_templates` VALUES ('52', 'Warcryer', '3', '27', '31', '24', '31', '15', '42', '3', '54', '6', '41', '300', '333', '30', '41', '30', '121', '68000', '-56682', '-113730', '-690', '0', '1.04', '0.898560', '7.0', '27.5', '1.04', '0.898560', '8.0', '25.5');
INSERT INTO `char_templates` VALUES ('53', 'Dwarf Fighter', '4', '39', '45', '29', '20', '10', '27', '4', '80', '6', '41', '300', '333', '33', '43', '33', '115', '83000', '108512', '-174026', '-400', '1', '1.09', '1.487196', '9.0', '18.0', '1.09', '1.487196', '5.0', '19.0');
INSERT INTO `char_templates` VALUES ('54', 'Scavenger', '4', '39', '45', '29', '20', '10', '27', '4', '80', '6', '41', '300', '333', '33', '43', '33', '115', '83000', '108512', '-174026', '-400', '1', '1.09', '1.487196', '9.0', '18.0', '1.09', '1.487196', '5.0', '19.0');
INSERT INTO `char_templates` VALUES ('55', 'Bounty Hunter', '4', '39', '45', '29', '20', '10', '27', '4', '80', '6', '41', '300', '333', '33', '43', '33', '115', '83000', '108512', '-174026', '-400', '1', '1.09', '1.487196', '9.0', '18.0', '1.09', '1.487196', '5.0', '19.0');
INSERT INTO `char_templates` VALUES ('56', 'Artisan', '4', '39', '45', '29', '20', '10', '27', '4', '80', '6', '41', '300', '333', '33', '43', '33', '115', '83000', '108512', '-174026', '-400', '1', '1.09', '1.487196', '9.0', '18.0', '1.09', '1.487196', '5.0', '19.0');
INSERT INTO `char_templates` VALUES ('57', 'Warsmith', '4', '39', '45', '29', '20', '10', '27', '4', '80', '6', '41', '300', '333', '33', '43', '33', '115', '83000', '108512', '-174026', '-400', '1', '1.09', '1.487196', '9.0', '18.0', '1.09', '1.487196', '5.0', '19.0');
INSERT INTO `char_templates` VALUES ('88', 'Duelist', '0', '40', '43', '30', '21', '11', '25', '4', '80', '6', '41', '300', '333', '33', '44', '33', '115', '81900', '-71338', '258271', '-3104', '0', '1.10', '1.188000', '9.0', '23.0', '1.10', '1.188000', '8.0', '23.5');
INSERT INTO `char_templates` VALUES ('89', 'DreadNought', '0', '40', '43', '30', '21', '11', '25', '4', '80', '6', '41', '300', '333', '33', '44', '33', '115', '81900', '-71338', '258271', '-3104', '0', '1.10', '1.188000', '9.0', '23.0', '1.10', '1.188000', '8.0', '23.5');
INSERT INTO `char_templates` VALUES ('90', 'Phoenix Knight', '0', '40', '43', '30', '21', '11', '25', '4', '80', '6', '41', '300', '333', '33', '44', '33', '115', '81900', '-71338', '258271', '-3104', '0', '1.10', '1.188000', '9.0', '23.0', '1.10', '1.188000', '8.0', '23.5');
INSERT INTO `char_templates` VALUES ('91', 'Hell Knight', '0', '40', '43', '30', '21', '11', '25', '4', '80', '6', '41', '300', '333', '33', '44', '33', '115', '81900', '-71338', '258271', '-3104', '0', '1.10', '1.188000', '9.0', '23.0', '1.10', '1.188000', '8.0', '23.5');
INSERT INTO `char_templates` VALUES ('92', 'Sagittarius', '0', '40', '43', '30', '21', '11', '25', '4', '80', '6', '41', '300', '333', '33', '44', '33', '115', '81900', '-71338', '258271', '-3104', '0', '1.10', '1.188000', '9.0', '23.0', '1.10', '1.188000', '8.0', '23.5');
INSERT INTO `char_templates` VALUES ('93', 'Adventurer', '0', '40', '43', '30', '21', '11', '25', '4', '80', '6', '41', '300', '333', '33', '44', '33', '115', '81900', '-71338', '258271', '-3104', '0', '1.10', '1.188000', '9.0', '23.0', '1.10', '1.188000', '8.0', '23.5');
INSERT INTO `char_templates` VALUES ('94', 'Archmage', '0', '22', '27', '21', '41', '20', '39', '3', '54', '6', '41', '300', '333', '28', '40', '28', '120', '62500', '-90890', '248027', '-3570', '0', '1.01', '0.872640', '7.5', '22.8', '1.01', '0.872640', '6.5', '22.5');
INSERT INTO `char_templates` VALUES ('95', 'Soultaker', '0', '22', '27', '21', '41', '20', '39', '3', '54', '6', '41', '300', '333', '28', '40', '28', '120', '62500', '-90890', '248027', '-3570', '0', '1.01', '0.872640', '7.5', '22.8', '1.01', '0.872640', '6.5', '22.5');
INSERT INTO `char_templates` VALUES ('96', 'Arcana Lord', '0', '22', '27', '21', '41', '20', '39', '3', '54', '6', '41', '300', '333', '28', '40', '28', '120', '62500', '-90890', '248027', '-3570', '0', '1.01', '0.872640', '7.5', '22.8', '1.01', '0.872640', '6.5', '22.5');
INSERT INTO `char_templates` VALUES ('97', 'Cardinal', '0', '22', '27', '21', '41', '20', '39', '3', '54', '6', '41', '300', '333', '28', '40', '28', '120', '62500', '-90890', '248027', '-3570', '0', '1.01', '0.872640', '7.5', '22.8', '1.01', '0.872640', '6.5', '22.5');
INSERT INTO `char_templates` VALUES ('98', 'Hierophant', '0', '22', '27', '21', '41', '20', '39', '3', '54', '6', '41', '300', '333', '28', '40', '28', '120', '62500', '-90890', '248027', '-3570', '0', '1.01', '0.872640', '7.5', '22.8', '1.01', '0.872640', '6.5', '22.5');
INSERT INTO `char_templates` VALUES ('99', 'Eva Templar', '1', '36', '36', '35', '23', '14', '26', '4', '80', '6', '41', '300', '333', '36', '46', '36', '125', '73000', '45978', '41196', '-3440', '0', '1.15', '1.242000', '7.5', '24.0', '1.15', '1.242000', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('100', 'Sword Muse', '1', '36', '36', '35', '23', '14', '26', '4', '80', '6', '41', '300', '333', '36', '46', '36', '125', '73000', '45978', '41196', '-3440', '0', '1.15', '1.242000', '7.5', '24.0', '1.15', '1.242000', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('101', 'Wind Rider', '1', '36', '36', '35', '23', '14', '26', '4', '80', '6', '41', '300', '333', '36', '46', '36', '125', '73000', '45978', '41196', '-3440', '0', '1.15', '1.242000', '7.5', '24.0', '1.15', '1.242000', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('102', 'Moonlight Sentinel', '1', '36', '36', '35', '23', '14', '26', '4', '80', '6', '41', '300', '333', '36', '46', '36', '125', '73000', '45978', '41196', '-3440', '0', '1.15', '1.242000', '7.5', '24.0', '1.15', '1.242000', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('103', 'Mystic Muse', '1', '21', '25', '24', '37', '23', '40', '3', '54', '6', '41', '300', '333', '30', '41', '30', '122', '62400', '46182', '41198', '-3440', '0', '1.04', '0.898560', '7.5', '24.0', '1.04', '0.898560', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('104', 'Elemental Master', '1', '21', '25', '24', '37', '23', '40', '3', '54', '6', '41', '300', '333', '30', '41', '30', '122', '62400', '46182', '41198', '-3440', '0', '1.04', '0.898560', '7.5', '24.0', '1.04', '0.898560', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('105', 'Eva Saint', '1', '21', '25', '24', '37', '23', '40', '3', '54', '6', '41', '300', '333', '30', '41', '30', '122', '62400', '46182', '41198', '-3440', '0', '1.04', '0.898560', '7.5', '24.0', '1.04', '0.898560', '7.5', '23.0');
INSERT INTO `char_templates` VALUES ('106', 'Shillien Templar', '2', '41', '32', '34', '25', '12', '26', '4', '80', '6', '41', '300', '333', '35', '45', '35', '122', '69000', '28377', '10916', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.14', '1.231200', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('107', 'Spectral Dancer', '2', '41', '32', '34', '25', '12', '26', '4', '80', '6', '41', '300', '333', '35', '45', '35', '122', '69000', '28377', '10916', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.14', '1.231200', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('108', 'Ghost Hunter', '2', '41', '32', '34', '25', '12', '26', '4', '80', '6', '41', '300', '333', '35', '45', '35', '122', '69000', '28377', '10916', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.14', '1.231200', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('109', 'Ghost Sentinel', '2', '41', '32', '34', '25', '12', '26', '4', '80', '6', '41', '300', '333', '35', '45', '35', '122', '69000', '28377', '10916', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.14', '1.231200', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('110', 'Storm Screamer', '2', '23', '24', '23', '44', '19', '37', '3', '54', '6', '41', '300', '333', '29', '41', '29', '122', '61000', '28295', '11063', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.03', '0.889920', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('111', 'Spectral Master', '2', '23', '24', '23', '44', '19', '37', '3', '54', '6', '41', '300', '333', '29', '41', '29', '122', '61000', '28295', '11063', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.03', '0.889920', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('112', 'Shillen Saint', '2', '23', '24', '23', '44', '19', '37', '3', '54', '6', '41', '300', '333', '29', '41', '29', '122', '61000', '28295', '11063', '-4224', '0', '1.14', '1.231200', '7.5', '24.0', '1.03', '0.889920', '7.0', '23.5');
INSERT INTO `char_templates` VALUES ('113', 'Titan', '3', '40', '47', '26', '18', '12', '27', '4', '80', '6', '41', '300', '333', '31', '42', '31', '117', '87000', '-56693', '-113610', '-690', '0', '1.06', '1.144800', '11.0', '28.0', '1.06', '1.144800', '7.0', '27.0');
INSERT INTO `char_templates` VALUES ('114', 'Grand Khauatari', '3', '40', '47', '26', '18', '12', '27', '4', '80', '6', '41', '300', '333', '31', '42', '31', '117', '87000', '-56693', '-113610', '-690', '0', '1.06', '1.144800', '11.0', '28.0', '1.06', '1.144800', '7.0', '27.0');
INSERT INTO `char_templates` VALUES ('115', 'Dominator', '3', '27', '31', '24', '31', '15', '42', '3', '54', '6', '41', '300', '333', '30', '41', '30', '121', '68000', '-56682', '-113730', '-690', '0', '1.04', '0.898560', '7.0', '27.5', '1.04', '0.898560', '8.0', '25.5');
INSERT INTO `char_templates` VALUES ('116', 'Doomcryer', '3', '27', '31', '24', '31', '15', '42', '3', '54', '6', '41', '300', '333', '30', '41', '30', '121', '68000', '-56682', '-113730', '-690', '0', '1.04', '0.898560', '7.0', '27.5', '1.04', '0.898560', '8.0', '25.5');
INSERT INTO `char_templates` VALUES ('117', 'Fortune Seeker', '4', '39', '45', '29', '20', '10', '27', '4', '80', '6', '41', '300', '333', '33', '43', '33', '115', '83000', '108512', '-174026', '-400', '1', '1.09', '1.487196', '9.0', '18.0', '1.09', '1.487196', '5.0', '19.0');
INSERT INTO `char_templates` VALUES ('118', 'Maestro', '4', '39', '45', '29', '20', '10', '27', '4', '80', '6', '41', '300', '333', '33', '43', '33', '115', '83000', '108512', '-174026', '-400', '1', '1.09', '1.487196', '9.0', '18.0', '1.09', '1.487196', '5.0', '19.0');

/*
Navicat MySQL Data Transfer

Source Server         : SqlServer
Source Server Version : 50620
Source Host           : localhost:3306
Source Database       : acisboard

Target Server Type    : MYSQL
Target Server Version : 50620
File Encoding         : 65001

Date: 2019-08-09 12:52:23
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for m_schemes
-- ----------------------------
DROP TABLE IF EXISTS `m_schemes`;
CREATE TABLE `m_schemes` (
  `PlayerID` int(10) NOT NULL,
  `Description` varchar(50) DEFAULT NULL,
  `IconID` int(10) DEFAULT NULL,
  `Name` varchar(50) DEFAULT NULL,
  `CreatedDate` decimal(20,0) DEFAULT NULL,
  `Skills` varchar(300) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*
MySQL Data Transfer
Source Host: localhost
Source Database: asdasd
Target Host: localhost
Target Database: asdasd
Date: 05/01/2026 12:10:38
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for clan_points
-- ----------------------------
DROP TABLE IF EXISTS `clan_points`;
CREATE TABLE `clan_points` (
  `clan_id` int(11) DEFAULT NULL,
  `champ_points` int(11) DEFAULT NULL,
  `boss_points` int(11) DEFAULT 0,
  `siege_points` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records 
-- ----------------------------

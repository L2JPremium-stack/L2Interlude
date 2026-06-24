/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 14/12/2025 17:42:31
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_hero
-- ----------------------------
DROP TABLE IF EXISTS `character_hero`;
CREATE TABLE `character_hero` (
  `objectId` int(11) NOT NULL DEFAULT 0,
  `duration` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`objectId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------

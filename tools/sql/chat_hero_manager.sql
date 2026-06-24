/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 14/12/2025 17:41:53
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for chat_hero_manager
-- ----------------------------
DROP TABLE IF EXISTS `chat_hero_manager`;
CREATE TABLE `chat_hero_manager` (
  `objectId` int(11) NOT NULL DEFAULT 0,
  `duration` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`objectId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------

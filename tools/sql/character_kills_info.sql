/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 14/12/2025 17:52:53
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_kills_info
-- ----------------------------
DROP TABLE IF EXISTS `character_kills_info`;
CREATE TABLE `character_kills_info` (
  `cycle` int(11) NOT NULL AUTO_INCREMENT,
  `cycle_start` bigint(20) unsigned NOT NULL,
  `winner_pvpkills` int(10) unsigned NOT NULL DEFAULT 0,
  `winner_pvpkills_count` int(10) unsigned NOT NULL DEFAULT 0,
  `winner_pkkills` int(10) unsigned NOT NULL DEFAULT 0,
  `winner_pkkills_count` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`cycle`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------

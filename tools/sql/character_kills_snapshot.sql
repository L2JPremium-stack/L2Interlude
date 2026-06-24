/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 14/12/2025 17:52:57
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for character_kills_snapshot
-- ----------------------------
DROP TABLE IF EXISTS `character_kills_snapshot`;
CREATE TABLE `character_kills_snapshot` (
  `charId` int(10) unsigned NOT NULL DEFAULT 0,
  `pvpkills` int(10) unsigned NOT NULL DEFAULT 0,
  `pkkills` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------

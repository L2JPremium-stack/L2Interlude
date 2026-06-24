/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 14/12/2025 17:54:01
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for characters_mission
-- ----------------------------
DROP TABLE IF EXISTS `characters_mission`;
CREATE TABLE `characters_mission` (
  `obj_Id` decimal(11,0) NOT NULL DEFAULT 0,
  `char_name` varchar(35) NOT NULL DEFAULT '',
  `tvt_event` decimal(1,0) NOT NULL DEFAULT 0,
  `tvt_completed` decimal(1,0) NOT NULL,
  `tvt_hwid` varchar(64) NOT NULL DEFAULT '',
  `pvp_event` decimal(11,0) NOT NULL,
  `pvp_completed` decimal(1,0) NOT NULL,
  `pvp_hwid` varchar(64) NOT NULL DEFAULT '',
  `raid_event` decimal(1,0) NOT NULL,
  `raid_completed` decimal(1,0) NOT NULL,
  `raid_hwid` varchar(64) NOT NULL DEFAULT '',
  `mobs` decimal(11,0) NOT NULL,
  `mob_completed` decimal(1,0) NOT NULL,
  `mob_hwid` varchar(64) NOT NULL DEFAULT '',
  `party_mobs` decimal(11,0) NOT NULL,
  `party_mob_completed` decimal(1,0) NOT NULL,
  `party_mob_hwid` varchar(64) NOT NULL DEFAULT '',
  `tournament_1x1_event` decimal(11,0) NOT NULL,
  `tournament_1x1_completed` decimal(11,0) NOT NULL,
  `tournament_1x1_hwid` varchar(64) NOT NULL DEFAULT '',
  `tournament_2x2_event` decimal(11,0) NOT NULL,
  `tournament_2x2_completed` decimal(11,0) NOT NULL,
  `tournament_2x2_hwid` varchar(64) NOT NULL DEFAULT '',
  `tournament_5x5_event` decimal(11,0) NOT NULL,
  `tournament_5x5_completed` decimal(11,0) NOT NULL,
  `tournament_5x5_hwid` varchar(64) NOT NULL DEFAULT '',
  `tournament_9x9_event` decimal(11,0) NOT NULL,
  `tournament_9x9_completed` decimal(11,0) NOT NULL,
  `tournament_9x9_hwid` varchar(64) NOT NULL DEFAULT '',
  PRIMARY KEY (`obj_Id`,`raid_hwid`,`mob_hwid`,`party_mob_hwid`,`tournament_1x1_hwid`,`tournament_2x2_hwid`,`tournament_5x5_hwid`,`tournament_9x9_hwid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------

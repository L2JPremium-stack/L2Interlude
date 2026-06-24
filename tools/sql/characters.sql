/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 14/12/2025 18:00:07
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for characters
-- ----------------------------
DROP TABLE IF EXISTS `characters`;
CREATE TABLE `characters` (
  `account_name` varchar(45) DEFAULT NULL,
  `obj_Id` int(10) unsigned NOT NULL DEFAULT 0,
  `char_name` varchar(35) NOT NULL,
  `level` tinyint(3) unsigned DEFAULT NULL,
  `maxHp` mediumint(8) unsigned DEFAULT NULL,
  `curHp` mediumint(8) unsigned DEFAULT NULL,
  `maxCp` mediumint(8) unsigned DEFAULT NULL,
  `curCp` mediumint(8) unsigned DEFAULT NULL,
  `maxMp` mediumint(8) unsigned DEFAULT NULL,
  `curMp` mediumint(8) unsigned DEFAULT NULL,
  `face` tinyint(3) unsigned DEFAULT NULL,
  `hairStyle` tinyint(3) unsigned DEFAULT NULL,
  `hairColor` tinyint(3) unsigned DEFAULT NULL,
  `sex` tinyint(3) unsigned DEFAULT NULL,
  `heading` mediumint(9) DEFAULT NULL,
  `x` mediumint(9) DEFAULT NULL,
  `y` mediumint(9) DEFAULT NULL,
  `z` mediumint(9) DEFAULT NULL,
  `exp` bigint(20) unsigned DEFAULT 0,
  `expBeforeDeath` bigint(20) unsigned DEFAULT 0,
  `sp` int(10) unsigned NOT NULL DEFAULT 0,
  `karma` int(10) unsigned DEFAULT NULL,
  `pvpkills` smallint(5) unsigned DEFAULT NULL,
  `pkkills` smallint(5) unsigned DEFAULT NULL,
  `clanid` int(10) unsigned DEFAULT NULL,
  `race` tinyint(3) unsigned DEFAULT NULL,
  `classid` tinyint(3) unsigned DEFAULT NULL,
  `base_class` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `deletetime` bigint(20) DEFAULT NULL,
  `cancraft` tinyint(3) unsigned DEFAULT NULL,
  `title` varchar(16) DEFAULT NULL,
  `rec_have` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `rec_left` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `accesslevel` mediumint(9) DEFAULT 0,
  `online` tinyint(3) unsigned DEFAULT NULL,
  `onlinetime` int(11) DEFAULT NULL,
  `char_slot` tinyint(3) unsigned DEFAULT NULL,
  `lastAccess` bigint(20) unsigned DEFAULT NULL,
  `clan_privs` mediumint(8) unsigned DEFAULT 0,
  `wantspeace` tinyint(3) unsigned DEFAULT 0,
  `isin7sdungeon` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `punish_level` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `punish_timer` int(10) unsigned NOT NULL DEFAULT 0,
  `power_grade` tinyint(3) unsigned DEFAULT NULL,
  `nobless` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `hero` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `subpledge` smallint(6) NOT NULL DEFAULT 0,
  `last_recom_date` bigint(20) unsigned NOT NULL DEFAULT 0,
  `lvl_joined_academy` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `apprentice` int(10) unsigned NOT NULL DEFAULT 0,
  `sponsor` int(10) unsigned NOT NULL DEFAULT 0,
  `varka_ketra_ally` tinyint(4) NOT NULL DEFAULT 0,
  `clan_join_expiry_time` bigint(20) unsigned NOT NULL DEFAULT 0,
  `clan_create_expiry_time` bigint(20) unsigned NOT NULL DEFAULT 0,
  `death_penalty_level` smallint(5) unsigned NOT NULL DEFAULT 0,
  `pc_point` int(5) DEFAULT NULL,
  `name_color` varchar(8) DEFAULT NULL,
  `title_color` varchar(8) DEFAULT NULL,
  `vip` decimal(1,0) DEFAULT NULL,
  `vip_end` decimal(20,0) DEFAULT NULL,
  `event_pvp` smallint(5) DEFAULT NULL,
  `fakeWeaponObjectId` int(10) DEFAULT NULL,
  `fakeWeaponItemId` mediumint(9) DEFAULT NULL,
  `aio` decimal(1,0) NOT NULL DEFAULT 0,
  `aio_end` decimal(20,0) NOT NULL DEFAULT 0,
  PRIMARY KEY (`obj_Id`),
  KEY `clanid` (`clanid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------

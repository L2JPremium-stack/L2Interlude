/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 14/12/2025 17:53:58
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for ban_hwid_chat
-- ----------------------------
DROP TABLE IF EXISTS `ban_hwid_chat`;
CREATE TABLE `ban_hwid_chat` (
  `objectId` int(11) NOT NULL DEFAULT 0,
  `duration` bigint(20) NOT NULL DEFAULT 0,
  `hwid` varchar(64) NOT NULL DEFAULT '',
  `account_name` varchar(45) DEFAULT NULL,
  `char_name` varchar(35) NOT NULL,
  PRIMARY KEY (`hwid`,`objectId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------

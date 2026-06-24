/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 14/12/2025 17:53:54
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for banned_hwid
-- ----------------------------
DROP TABLE IF EXISTS `banned_hwid`;
CREATE TABLE `banned_hwid` (
  `char_name` varchar(35) NOT NULL,
  `hwid` varchar(64) NOT NULL,
  PRIMARY KEY (`hwid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------

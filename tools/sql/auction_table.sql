/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 14/12/2025 17:42:11
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for auction_table
-- ----------------------------
DROP TABLE IF EXISTS `auction_table`;
CREATE TABLE `auction_table` (
  `auctionid` int(10) unsigned NOT NULL DEFAULT 0,
  `ownerid` int(10) unsigned NOT NULL DEFAULT 0,
  `itemid` int(10) unsigned NOT NULL DEFAULT 0,
  `count` int(10) unsigned NOT NULL DEFAULT 0,
  `enchant` int(10) unsigned NOT NULL DEFAULT 0,
  `costid` int(10) unsigned NOT NULL DEFAULT 0,
  `costcount` int(10) unsigned NOT NULL DEFAULT 0,
  PRIMARY KEY (`auctionid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------

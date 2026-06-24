/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 14/12/2025 17:48:38
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for daily_rewarded_players
-- ----------------------------
DROP TABLE IF EXISTS `daily_rewarded_players`;
CREATE TABLE `daily_rewarded_players` (
  `day` int(11) DEFAULT NULL,
  `obj_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------

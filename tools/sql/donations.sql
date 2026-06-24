/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 14/12/2025 17:52:18
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for donations
-- ----------------------------
DROP TABLE IF EXISTS `donations`;
CREATE TABLE `donations` (
  `purchase_id` int(10) unsigned NOT NULL DEFAULT 0,
  `mp_id` bigint(20) unsigned NOT NULL DEFAULT 0,
  `player_id` int(10) unsigned NOT NULL DEFAULT 0,
  `email` varchar(44) NOT NULL DEFAULT '',
  `product_id` int(10) unsigned NOT NULL DEFAULT 0,
  `quantity` int(10) unsigned NOT NULL DEFAULT 0,
  `price` int(10) unsigned NOT NULL DEFAULT 0,
  `date` bigint(20) unsigned DEFAULT 0,
  `hidden` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `status` varchar(10) NOT NULL DEFAULT '',
  PRIMARY KEY (`purchase_id`,`mp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------

/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 14/12/2025 17:53:16
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for ip_protect_epiczone
-- ----------------------------
DROP TABLE IF EXISTS `ip_protect_epiczone`;
CREATE TABLE `ip_protect_epiczone` (
  `ip` varchar(20) NOT NULL DEFAULT '',
  PRIMARY KEY (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------

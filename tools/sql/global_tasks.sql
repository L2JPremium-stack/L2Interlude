/*
MySQL Data Transfer
Source Host: localhost
Source Database: l2jdb
Target Host: localhost
Target Database: l2jdb
Date: 14/12/2025 17:53:31
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for global_tasks
-- ----------------------------
DROP TABLE IF EXISTS `global_tasks`;
CREATE TABLE `global_tasks` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `task` varchar(50) NOT NULL DEFAULT '',
  `type` varchar(50) NOT NULL DEFAULT '',
  `last_activation` decimal(20,0) NOT NULL DEFAULT 0,
  `param1` varchar(100) NOT NULL DEFAULT '',
  `param2` varchar(100) NOT NULL DEFAULT '',
  `param3` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records 
-- ----------------------------

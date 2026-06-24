/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50505
Source Host           : localhost:3306
Source Database       : acis359

Target Server Type    : MYSQL
Target Server Version : 50505
File Encoding         : 65001

Date: 2025-02-02 18:27:22
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `sellbuff_describe`
-- ----------------------------
DROP TABLE IF EXISTS `sellbuff_describe`;
CREATE TABLE `sellbuff_describe` (
  `buffId` int(11) NOT NULL,
  `description` text NOT NULL,
  PRIMARY KEY (`buffId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sellbuff_describe
-- ----------------------------

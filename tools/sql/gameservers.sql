CREATE TABLE IF NOT EXISTS `gameservers` (
  `server_id` int(11) NOT NULL default '0',
  `hexid` varchar(50) NOT NULL default '',
  `host` varchar(50) NOT NULL default '',
  PRIMARY KEY (`server_id`)
);

INSERT INTO `gameservers` VALUES ('1', '-7818263f64e2229ed77c8ebb52223dd8', '127.0.0.1');

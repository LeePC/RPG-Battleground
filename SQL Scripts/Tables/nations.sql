CREATE TABLE `rpg_nations` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Prefix` varchar(8) NOT NULL DEFAULT '',
  `Name` varchar(32) NOT NULL,
  `DisplayNameId` int(11) DEFAULT NULL,
  `Money` int(11) NOT NULL DEFAULT '0',
  `BlockMaterial` varchar(32) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

CREATE TABLE `rpg_guilds` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Prefix` varchar(4) DEFAULT '',
  `Name` varchar(16) DEFAULT '',
  `Description` varchar(128) DEFAULT '',
  `FounderId` int(11) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `rpg_shops` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NameId` int(11) NOT NULL DEFAULT '-1',
  `WorldId` int(11) NOT NULL,
  `PosX` int(11) NOT NULL,
  `PosY` int(11) NOT NULL,
  `PosZ` int(11) NOT NULL,
  `ReqLevel` int(11) DEFAULT '0',
  `ReqQuestIds` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

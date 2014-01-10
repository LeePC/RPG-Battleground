CREATE TABLE `rpg_regions` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `WorldId` int(11) NOT NULL DEFAULT '-1',
  `MinX` double NOT NULL DEFAULT '0',
  `MinY` double NOT NULL DEFAULT '0',
  `MinZ` double NOT NULL DEFAULT '0',
  `MaxX` double NOT NULL DEFAULT '0',
  `MaxY` double NOT NULL DEFAULT '0',
  `MaxZ` double NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

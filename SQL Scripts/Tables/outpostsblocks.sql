CREATE TABLE `rpg_outpostsblocks` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `OutpostID` int(11) NOT NULL,
  `Type` int(11) NOT NULL,
  `Data` int(11) NOT NULL,
  `PosX` int(11) NOT NULL,
  `PosY` int(11) NOT NULL,
  `PosZ` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

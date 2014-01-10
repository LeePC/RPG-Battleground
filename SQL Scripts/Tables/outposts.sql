CREATE TABLE `rpg_outposts` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NameId` int(11) NOT NULL DEFAULT '-1',
  `RegionId` int(11) NOT NULL DEFAULT '-1',
  `FlagId` int(11) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

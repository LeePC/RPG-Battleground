CREATE TABLE `rpg_mobs` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NameId` int(11) NOT NULL DEFAULT '-1',
  `DescriptionId` int(11) DEFAULT '-1',
  `MobType` int(11) NOT NULL DEFAULT '-1',
  `Level` int(11) NOT NULL DEFAULT '0',
  `Drops` varchar(128) DEFAULT '',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

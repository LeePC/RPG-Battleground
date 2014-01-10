CREATE TABLE `rpg_flags` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NameId` int(11) NOT NULL DEFAULT '-1',
  `NationId` int(11) NOT NULL DEFAULT '-1',
  `WorldId` int(11) NOT NULL,
  `PosX` int(11) NOT NULL DEFAULT '0',
  `PosY` int(11) NOT NULL,
  `PosZ` int(11) NOT NULL,
  `CaptureRadius` int(11) NOT NULL DEFAULT '0',
  `CaptureTime` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `rpg_teleports` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NpcId` int(11) NOT NULL DEFAULT '-1',
  `TargetPosX` int(11) NOT NULL,
  `TargetPosY` int(11) NOT NULL,
  `TargetPosZ` int(11) NOT NULL,
  `Level` int(11) NOT NULL DEFAULT '0',
  `Cost` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

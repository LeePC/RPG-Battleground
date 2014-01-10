CREATE TABLE `rpg_chatlog` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `TimeStamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `PlayerId` int(11) NOT NULL DEFAULT '-1',
  `TargetId` int(11) DEFAULT '-1',
  `Type` int(11) NOT NULL DEFAULT '-1' COMMENT '0 = Server, 1 = World, 2 = Nation, 3 = Guild, 4 = Region, 5 = Private',
  `Text` varchar(256) DEFAULT '',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

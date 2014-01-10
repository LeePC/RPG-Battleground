CREATE TABLE `rpg_questprogress` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `QuestId` int(11) NOT NULL DEFAULT '-1',
  `PlayerId` int(11) NOT NULL DEFAULT '-1',
  `Completed` tinyint(1) NOT NULL DEFAULT '0',
  `Data` varchar(512) DEFAULT '',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

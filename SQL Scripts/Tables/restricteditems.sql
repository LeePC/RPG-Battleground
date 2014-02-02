CREATE TABLE `rpg_restricteditems` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `ItemId` int(11) NOT NULL DEFAULT '-1',
  `ReqLevel` int(11) DEFAULT '0',
  `ActionIds` varchar(8) NOT NULL,
  `QuestIds` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

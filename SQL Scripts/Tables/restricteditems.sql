CREATE TABLE `rpg_restricteditems` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `ItemId` int(11) NOT NULL DEFAULT '-1',
  `ReqLevel` int(11) DEFAULT '0',
  `ActionIds` varchar(8) NOT NULL COMMENT '0 = Place, 1 = Damage, 2 = Destroy, 3 = Interact',
  `QuestIds` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

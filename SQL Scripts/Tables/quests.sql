CREATE TABLE `rpg_quests` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NameId` int(11) NOT NULL DEFAULT '-1',
  `DisplayNameId` int(11) DEFAULT '-1',
  `DescriptionId` int(11) DEFAULT '-1',
  `QuestType` int(11) DEFAULT '-1' COMMENT '0 = None, 1 = Bring, 2 = Kill, 3 = Talk',
  `NPCStartId` int(11) DEFAULT '-1',
  `NPCEndId` int(11) DEFAULT '-1',
  `NPCStartTextId` int(11) DEFAULT '-1',
  `NPCEndTextId` int(11) DEFAULT '-1',
  `ReqQuestIds` varchar(64) DEFAULT NULL,
  `ReqMoney` int(11) DEFAULT '0',
  `ReqLevel` int(11) DEFAULT '0',
  `RewardExp` int(11) DEFAULT '0',
  `RewardMoney` int(11) DEFAULT '0',
  `Recompletable` tinyint(1) DEFAULT '0',
  `Tag` varchar(64) DEFAULT '',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

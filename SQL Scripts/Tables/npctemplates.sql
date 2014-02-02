CREATE TABLE `rpg_npctemplates` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `TemplateName` varchar(32) NOT NULL DEFAULT '',
  `NPCName` varchar(64) NOT NULL DEFAULT '',
  `NationId` int(11) NOT NULL DEFAULT '-1',
  `Level` int(11) NOT NULL DEFAULT '0',
  `Money` int(11) NOT NULL DEFAULT '0',
  `TextId` int(11) NOT NULL DEFAULT '-1',
  `ShopId` int(11) NOT NULL DEFAULT '-1',
  `ItemInHand` int(11) NOT NULL DEFAULT '-1',
  `ArmorHead` int(11) NOT NULL DEFAULT '-1',
  `ArmorChest` int(11) NOT NULL DEFAULT '-1',
  `ArmorLegs` int(11) NOT NULL DEFAULT '-1',
  `ArmorFeet` int(11) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

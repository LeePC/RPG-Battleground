CREATE TABLE `rpg_players` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Username` varchar(32) NOT NULL DEFAULT '',
  `Password` varchar(32) DEFAULT NULL,
  `NationId` int(11) NOT NULL DEFAULT '-1',
  `GuildId` int(11) NOT NULL DEFAULT '-1',
  `Level` int(11) NOT NULL DEFAULT '0',
  `Exp` int(11) NOT NULL DEFAULT '0',
  `Money` int(11) NOT NULL DEFAULT '0',
  `State` int(11) NOT NULL DEFAULT '0',
  `Language` varchar(2) NOT NULL DEFAULT 'EN',
  `IP` varchar(16) NOT NULL DEFAULT '',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

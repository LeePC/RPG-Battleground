CREATE TABLE `rpg_worlds` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `WorldName` varchar(32) NOT NULL DEFAULT ' ',
  `LoadOnStart` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

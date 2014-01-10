CREATE TABLE `rpg_log` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `SourceID` int(11) NOT NULL DEFAULT '-1',
  `TargetID` int(11) NOT NULL DEFAULT '-1',
  `Type` varchar(16) NOT NULL DEFAULT '',
  `Category` varchar(32) NOT NULL DEFAULT '',
  `Event` varchar(1024) DEFAULT '',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

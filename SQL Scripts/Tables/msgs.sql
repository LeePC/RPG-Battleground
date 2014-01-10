CREATE TABLE `rpg_msgs` (
  `Name` varchar(32) NOT NULL DEFAULT '',
  `DE` varchar(128) DEFAULT '',
  `EN` varchar(128) DEFAULT '',
  `FR` varchar(128) DEFAULT '',
  PRIMARY KEY (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


INSERT INTO `rpg_msgs`(`Name`,`DE`,`EN`,`FR`) VALUES ('flag_captured','','','');
INSERT INTO `rpg_msgs`(`Name`,`DE`,`EN`,`FR`) VALUES ('flag_captured_short','','','');
INSERT INTO `rpg_msgs`(`Name`,`DE`,`EN`,`FR`) VALUES ('flag_lost','','','');
INSERT INTO `rpg_msgs`(`Name`,`DE`,`EN`,`FR`) VALUES ('flag_lost_short','','','');
INSERT INTO `rpg_msgs`(`Name`,`DE`,`EN`,`FR`) VALUES ('journal_title','§9Tagebuch','§9Journal','');
INSERT INTO `rpg_msgs`(`Name`,`DE`,`EN`,`FR`) VALUES ('journal_title_completed','§9Abgeschlossene Quests','§9Completed Quests','');
INSERT INTO `rpg_msgs`(`Name`,`DE`,`EN`,`FR`) VALUES ('journal_title_current','§9Aktuelle Quests','§9Current Quests','');
INSERT INTO `rpg_msgs`(`Name`,`DE`,`EN`,`FR`) VALUES ('no_perm','§cDu hast die nötigen Berechtigungen nicht um das zu tun','§cYou do not have the required permissions to do that','');
INSERT INTO `rpg_msgs`(`Name`,`DE`,`EN`,`FR`) VALUES ('npc_talk_end','§6Du has aufgeh§rt mit §9%npc_name §6zu reden','§6You have stopped talking to §9%npc_name','');
INSERT INTO `rpg_msgs`(`Name`,`DE`,`EN`,`FR`) VALUES ('npc_talk_start','§6Du sprichst jetzt mit §9%npc_name','§6You are now talking to §9%npc_name','');
INSERT INTO `rpg_msgs`(`Name`,`DE`,`EN`,`FR`) VALUES ('quest_end','§6Du hast den Quest §b%q_name §6abgeschlossen','§6You have completed the quest §b%q_name','');
INSERT INTO `rpg_msgs`(`Name`,`DE`,`EN`,`FR`) VALUES ('quest_end_short','§6Quest abgeschlossen','§6Quest completed','');
INSERT INTO `rpg_msgs`(`Name`,`DE`,`EN`,`FR`) VALUES ('quest_reward_get','','','');
INSERT INTO `rpg_msgs`(`Name`,`DE`,`EN`,`FR`) VALUES ('quest_start','§6Du hast den Quest §b%q_name §6begonnen','§6You have started the quest §b%q_name','');
INSERT INTO `rpg_msgs`(`Name`,`DE`,`EN`,`FR`) VALUES ('quest_start_short','§6Quest begonnen','§6Quest started','');
INSERT INTO `rpg_msgs`(`Name`,`DE`,`EN`,`FR`) VALUES ('ri_minlvl','§cDu musst mindest level §9%ri_minlvl §csein','§cYou have to be at least level §9%ri_minlvl','');
INSERT INTO `rpg_msgs`(`Name`,`DE`,`EN`,`FR`) VALUES ('unknown_command','§cUnknown command','§cUnbekannter Befehl','');

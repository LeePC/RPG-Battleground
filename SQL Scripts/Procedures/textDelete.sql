CREATE PROCEDURE `rpg_textDelete`(IN TextID Int(11))
BEGIN
	DECLARE tid Int(11) DEFAULT -1;
  
  IF tid = -1 THEN
    SELECT ID INTO tid FROM rpg_flags WHERE NameID = TextID LIMIT 0,1;
  END IF;
  
  IF tid = -1 THEN
    SELECT ID INTO tid FROM rpg_mobs WHERE NameID = TextID LIMIT 0,1;
  END IF;
  
  IF tid = -1 THEN
    SELECT ID INTO tid FROM rpg_nations WHERE NameID = TextID LIMIT 0,1;
  END IF;
  
  IF tid = -1 THEN
    SELECT ID INTO tid FROM rpg_npcs WHERE StandardTextID = TextID LIMIT 0,1;
  END IF;
  
  IF tid = -1 THEN
    SELECT ID INTO tid FROM rpg_outposts WHERE NameID = TextID LIMIT 0,1;
  END IF;
  
  IF tid = -1 THEN
    SELECT ID INTO tid FROM rpg_quests WHERE (NameID = TextID OR DisplayNameID = TextID OR DescriptionID = TextID OR NPCStartTextID = TextID OR NPCEndTextID = TextID) LIMIT 0,1;
  END IF;
  
  IF tid = -1 THEN
    SELECT ID INTO tid FROM rpg_shops WHERE NameID = TextID LIMIT 0,1;
  END IF;
  
  IF tid = -1 THEN
    SELECT ID INTO tid FROM rpg_teleports WHERE NameID = TextID LIMIT 0,1;
  END IF;
  
  IF tid = -1 THEN
    DELETE FROM rpg_texts WHERE ID = TextID;
  END IF;
END;

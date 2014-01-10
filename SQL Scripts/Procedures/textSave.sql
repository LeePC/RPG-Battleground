CREATE PROCEDURE `rpg_textSave`(IN Language varchar(2), IN NewText varchar(1024), OUT TextID int(11))
BEGIN
  SET TextID = -1;
  
	IF Language = 'DE' THEN
  	SELECT ID INTO TextID FROM rpg_texts WHERE DE = NewText;
    IF TextID = -1 THEN
      INSERT INTO rpg_texts (DE, EN, FR) VALUES (NewText, '', '');
    END IF;
  END IF;
  IF Language = 'EN' THEN
    SELECT ID INTO TextID FROM rpg_texts WHERE EN = NewText;
    IF TextID = -1 THEN
      INSERT INTO rpg_texts (DE, EN, FR) VALUES ('', NewText, '');
    END IF;
  END IF;
  IF Language = 'FR' THEN
    SELECT ID INTO TextID FROM rpg_texts WHERE FR = NewText;
    IF TextID = -1 THEN
      INSERT INTO rpg_texts (DE, EN, FR) VALUES ('', '', NewText);
    END IF;
  END IF;
  
  IF TextID = -1 THEN
    SET TextID = last_insert_id();
  END IF;
END;

package rpgQuestManager;

import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericTextField;

import rpgGUI.RPG_CreationScreen;
import rpgGUI.RPG_CreationScreenButtons;
import rpgOther.RPG_CreationData;
import rpgPlayer.RPG_Player;
import rpgQuest.RPG_QuestCreationData;
import rpgQuest.RPG_QuestType;

public class RPG_Screen_CreateQuest3 extends RPG_CreationScreen
{
	private GenericTextField txt_start;
	private GenericTextField txt_end;
	
	public String GetQuestStartText()
	{
		return txt_start.getText();
	}
	public String GetQuestEndText()
	{
		return txt_end.getText();
	}
	
	
	public RPG_Screen_CreateQuest3(Plugin Plugin, RPG_Player Player, RPG_CreationData Data)
	{
		super(Plugin, Player, Data, "Create a Quest (3/4)", RPG_CreationScreenButtons.CancelBackNext);
		
		if (((RPG_QuestCreationData)Data).Type == RPG_QuestType.Talk)
			SetBaseComponents("Create a Quest (3/3)", RPG_CreationScreenButtons.CancelBackCreate);
		
		// 
		// Start text
		// 
		
		GenericLabel lbl = new GenericLabel("Start text:");
		lbl.setHeight(15);
		lbl.setWidth(50);
		lbl.setX(10);
		lbl.setY(40);
		lbl.setTooltip("The text that is displayed to the player when the quest is started");
		
		attachWidget(plg, lbl);
		
		txt_start = new GenericTextField();
		txt_start.setMaximumLines(6);
		txt_start.setMaximumCharacters(1024);
		txt_start.setHeight(75);
		txt_start.setWidth(width - 93);
		txt_start.setX(80);
		txt_start.setY(40);
		txt_start.setTooltip("The text that is displayed to the player when the quest is started");
		txt_start.setTabIndex(0);
		
		attachWidget(plg, txt_start);
		
		// 
		// End text
		// 
		
		lbl = new GenericLabel("End text:");
		lbl.setHeight(15);
		lbl.setWidth(50);
		lbl.setX(10);
		lbl.setY(123);
		lbl.setTooltip("The text that is displayed to the player when the quest is completed");
		
		attachWidget(plg, lbl);
		
		txt_end = new GenericTextField();
		txt_end.setMaximumLines(6);
		txt_end.setMaximumCharacters(1024);
		txt_end.setHeight(75);
		txt_end.setWidth(width - 93);
		txt_end.setX(80);
		txt_end.setY(123);
		txt_end.setTooltip("The text that is displayed to the player when the quest is completed");
		txt_end.setTabIndex(1);
		
		attachWidget(plg, txt_end);
	}
}

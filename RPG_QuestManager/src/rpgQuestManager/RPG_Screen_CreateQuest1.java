package rpgQuestManager;

import java.util.ArrayList;

import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.GenericCheckBox;
import org.getspout.spoutapi.gui.GenericComboBox;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericTextField;

import rpgGUI.RPG_CreationScreen;
import rpgGUI.RPG_CreationScreenButtons;
import rpgGUI.RPG_NumericTextField;
import rpgOther.RPG_CreationData;
import rpgPlayer.RPG_Player;
import rpgQuest.RPG_QuestCreationData;
import rpgQuest.RPG_QuestType;

public class RPG_Screen_CreateQuest1 extends RPG_CreationScreen
{
	private GenericLabel lbl_name;
	private GenericTextField txt_name;
	private GenericTextField txt_displayname;
	private GenericLabel lbl_questtype;
	private GenericComboBox box_questtype;
	private GenericCheckBox chk_recompletable;
	private GenericTextField txt_description;
	private RPG_NumericTextField num_reqlevel;
	private RPG_NumericTextField num_reqmoney;
	private RPG_NumericTextField num_rewardmoney;
	private RPG_NumericTextField num_rewardexp;
	
	public String GetQuestName()
	{
		return txt_name.getText();
	}
	public String GetQuestDisplayName()
	{
		if (txt_displayname.getText().isEmpty())
			return txt_name.getText();
		return txt_displayname.getText();
	}
	public String GetQuestDescription()
	{
		return txt_description.getText();
	}
	public boolean GetQuestRecompletable()
	{
		return chk_recompletable.isChecked();
	}
	public RPG_QuestType GetQuestType()
	{
		if (box_questtype.getSelectedItem() == null)
			return RPG_QuestType.None;
		
		String text = box_questtype.getSelectedItem();
		if (text.equalsIgnoreCase("talk"))
			return RPG_QuestType.Talk;
		else if (text.equalsIgnoreCase("bring"))
			return RPG_QuestType.Bring;
		else if (text.equalsIgnoreCase("kill"))
			return RPG_QuestType.Kill;
		
		return RPG_QuestType.None;
	}
	public int GetQuestReqMoney()
	{
		return num_reqmoney.GetValue();
	}
	public int GetQuestReqLevel()
	{		 
		return num_reqlevel.GetValue();
	}
	public int GetQuestRewardMoney()
	{
		return num_rewardmoney.GetValue();
	}
	public int GetQuestRewardExp()
	{
		return num_rewardexp.GetValue();
	}
	
	
	public RPG_Screen_CreateQuest1(Plugin Plugin, RPG_Player Player, RPG_CreationData Data)
	{
		super(Plugin, Player, Data, "Create a Quest", RPG_CreationScreenButtons.CancelNext);
		
		// 
		// Name
		// 
		
		lbl_name = new GenericLabel("Name:");
		lbl_name.setHeight(15);
		lbl_name.setWidth(50);
		lbl_name.setX(10);
		lbl_name.setY(40);
		lbl_name.setTooltip("This is the name of the NPC");
		
		attachWidget(plg, lbl_name);
		
		txt_name = new GenericTextField();
		txt_name.setHeight(15);
		txt_name.setWidth(100);
		txt_name.setX(50);
		txt_name.setY(36);
		txt_name.setTooltip("This is the name of the NPC");
		txt_name.setTabIndex(0);
		
		attachWidget(plg, txt_name);
		
		// 
		// Display name
		// 
		
		GenericLabel lbl = new GenericLabel("Display name:");
		lbl.setHeight(15);
		lbl.setWidth(50);
		lbl.setX(width / 2);
		lbl.setY(40);
		lbl.setTooltip("This is the text that is displayed for the quest when talking to a NPC");
		
		attachWidget(plg, lbl);
		
		txt_displayname = new GenericTextField();
		txt_displayname.setHeight(15);
		txt_displayname.setWidth(100);
		txt_displayname.setX(width / 2 + 75);
		txt_displayname.setY(36);
		txt_displayname.setTooltip("This is the text that is displayed for the quest when talking to a NPC");
		txt_displayname.setTabIndex(1);
		
		attachWidget(plg, txt_displayname);
		
		// 
		// Quest type
		// 
		
		lbl_questtype = new GenericLabel("Type:");
		lbl_questtype.setHeight(15);
		lbl_questtype.setWidth(50);
		lbl_questtype.setX(10);
		lbl_questtype.setY(70);
		lbl_questtype.setTooltip("The type of the quest");
		
		attachWidget(plg, lbl_questtype);
		
		ArrayList<String> items = new ArrayList<String>();
		items.add("Talk");
		items.add("Bring");
		items.add("Kill");
		
		box_questtype = new GenericComboBox();
		box_questtype.setHeight(20);
		box_questtype.setWidth(100);
		box_questtype.setX(49);
		box_questtype.setY(64);
		box_questtype.setText("Type");
		box_questtype.setTooltip("The type of the quest");
		box_questtype.setItems(items);
		
		attachWidget(plg, box_questtype);
		
		// 
		// Recompletable
		// 
		
		chk_recompletable = new GenericCheckBox();
		chk_recompletable.setHeight(15);
		chk_recompletable.setWidth(200);
		chk_recompletable.setX(width / 2);
		chk_recompletable.setY(64);
		chk_recompletable.setText("Can be completed more than once");
		chk_recompletable.setTooltip("Defines if this quest can be completed more than once");
		
		attachWidget(plg, chk_recompletable);
		
		// 
		// Required level
		// 
		
		lbl = new GenericLabel("Required lvl:");
		lbl.setHeight(15);
		lbl.setWidth(50);
		lbl.setX(10);
		lbl.setY(100);
		lbl.setTooltip("The minimum level required to start the quest");
		
		attachWidget(plg, lbl);
		
		num_reqlevel = new RPG_NumericTextField();
		num_reqlevel.setMaximumCharacters(8);
		num_reqlevel.setHeight(15);
		num_reqlevel.setWidth(60);
		num_reqlevel.setX(80);
		num_reqlevel.setY(96);
		num_reqlevel.setTooltip("The minimum level required to start the quest");
		num_reqlevel.setTabIndex(2);
		
		attachWidget(plg, num_reqlevel);
		
		// 
		// Required money
		// 
		
		lbl = new GenericLabel("Required money:");
		lbl.setHeight(15);
		lbl.setWidth(50);
		lbl.setX(width / 2);
		lbl.setY(100);
		lbl.setTooltip("The amount of money required to start the quest");
		
		attachWidget(plg, lbl);
		
		num_reqmoney = new RPG_NumericTextField();
		num_reqmoney.setMaximumCharacters(8);
		num_reqmoney.setHeight(15);
		num_reqmoney.setWidth(60);
		num_reqmoney.setX(width / 2 + 95);
		num_reqmoney.setY(96);
		num_reqmoney.setTooltip("The amount of money required to start the quest");
		num_reqmoney.setTabIndex(3);
		
		attachWidget(plg, num_reqmoney);
		
		// 
		// Reward exp
		// 
		
		lbl = new GenericLabel("Reward exp:");
		lbl.setHeight(15);
		lbl.setWidth(50);
		lbl.setX(10);
		lbl.setY(125);
		lbl.setTooltip("The experience the player receives for completing this quest");
		
		attachWidget(plg, lbl);
		
		num_rewardexp = new RPG_NumericTextField();
		num_rewardexp.setMaximumCharacters(8);
		num_rewardexp.setHeight(15);
		num_rewardexp.setWidth(60);
		num_rewardexp.setX(80);
		num_rewardexp.setY(121);
		num_rewardexp.setTooltip("The experience the player receives for completing this quest");
		num_rewardexp.setTabIndex(4);
		
		attachWidget(plg, num_rewardexp);
		
		// 
		// Reward money
		// 
		
		lbl = new GenericLabel("Reward money:");
		lbl.setHeight(15);
		lbl.setWidth(50);
		lbl.setX(width / 2);
		lbl.setY(125);
		lbl.setTooltip("The amount of money received for completing the quest");
		
		attachWidget(plg, lbl);
		
		num_rewardmoney = new RPG_NumericTextField();
		num_rewardmoney.setMaximumCharacters(8);
		num_rewardmoney.setHeight(15);
		num_rewardmoney.setWidth(60);
		num_rewardmoney.setX(width / 2 + 95);
		num_rewardmoney.setY(121);
		num_rewardmoney.setTooltip("The amount of money received for completing the quest");
		num_rewardmoney.setTabIndex(5);
		
		attachWidget(plg, num_rewardmoney);
		
		// 
		// Description
		// 
		
		lbl = new GenericLabel("Description:");
		lbl.setHeight(15);
		lbl.setWidth(50);
		lbl.setX(10);
		lbl.setY(150);
		lbl.setTooltip("The description of the quest that tells the player what to do.");
		
		attachWidget(plg, lbl);
		
		txt_description = new GenericTextField();
		txt_description.setMaximumLines(4);
		txt_description.setMaximumCharacters(1024);
		txt_description.setHeight(50);
		txt_description.setWidth(width - 93);
		txt_description.setX(80);
		txt_description.setY(147);
		txt_description.setTooltip("The description of the quest that tells the player what to do.");
		txt_description.setTabIndex(6);
		
		attachWidget(plg, txt_description);
		
		// 
		// Data
		// 
		
		RPG_QuestCreationData data = (RPG_QuestCreationData)Data;
		
		txt_name.setText(data.Name);
		txt_displayname.setText(data.DisplayName);
		if (data.Type == RPG_QuestType.Talk)
			box_questtype.setSelection(0);
		else if (data.Type == RPG_QuestType.Bring)
			box_questtype.setSelection(1);
		else if (data.Type == RPG_QuestType.Kill)
			box_questtype.setSelection(2);
		box_questtype.setDirty(true);
		chk_recompletable.setChecked(data.Recompletable);
		num_reqmoney.setText(data.ReqMoney + "");
		num_reqlevel.setText(data.ReqLevel + "");
		num_rewardexp.setText(data.RewardExp + "");
		num_rewardmoney.setText(data.RewardMoney + "");
		txt_description.setText(data.Description);
	}
	
	public void QuestNameNotSpecified()
	{
		lbl_name.setTextColor(new Color(255, 0, 0));
	}
	public void QuestTypeNotSpecified()
	{
		lbl_questtype.setTextColor(new Color(255, 0, 0));
	}
	
	public void ResetColors()
	{
		lbl_name.setTextColor(new Color(255, 255, 255));
		lbl_questtype.setTextColor(new Color(255, 255, 255));
	}
}

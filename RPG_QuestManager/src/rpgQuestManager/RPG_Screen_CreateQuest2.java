package rpgQuestManager;

import java.util.ArrayList;

import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericListWidget;
import org.getspout.spoutapi.gui.ListWidgetItem;
import org.getspout.spoutapi.gui.TextField;

import rpgCore.RPG_Core;
import rpgGUI.RPG_CreationScreen;
import rpgGUI.RPG_CreationScreenButtons;
import rpgGUI.RPG_NumericTextField;
import rpgOther.RPG_CreationData;
import rpgPlayer.RPG_Player;
import rpgQuest.RPG_Quest;
import rpgQuest.RPG_QuestCreationData;
import rpgQuest.RPG_QuestType;
import rpgTexts.RPG_Language;

public class RPG_Screen_CreateQuest2 extends RPG_CreationScreen
{
	private GenericListWidget list_quests;
	private GenericListWidget list_reqs;
	private GenericButton btn_reqs_add;
	private GenericButton btn_reqs_remove;
	private RPG_NumericTextField num_npc_start;
	private RPG_NumericTextField num_npc_end;
	private GenericButton btn_npc_start;
	private GenericButton btn_npc_end;
	private GenericLabel lbl_npc_start;
	private GenericLabel lbl_npc_end;
	
	public ArrayList<Integer> GetQuestReqQuests()
	{
		ArrayList<Integer> reqs = new ArrayList<Integer>();
		for (ListWidgetItem item : list_reqs.getItems())
			reqs.add(Integer.parseInt(item.getTitle()));
		return reqs;
	}
	public int GetQuestStartNPCID()
	{
		if (num_npc_start.getText().isEmpty())
			return -1;
		return num_npc_start.GetValue();
	}
	public int GetQuestEndNPCID()
	{
		if (num_npc_end.getText().isEmpty())
			return -1;
		return num_npc_end.GetValue();
	}
	
	
	public RPG_Screen_CreateQuest2(Plugin Plugin, RPG_Player Player, RPG_CreationData Data)
	{
		super(Plugin, Player, Data, "Create a Quest (2/4)", RPG_CreationScreenButtons.CancelBackNext);
		
		if (((RPG_QuestCreationData)Data).Type == RPG_QuestType.Talk)
			lbl_title.setText("Create a Quest (2/3)");
		
		// 
		// Prereq quests
		// 
		
		GenericLabel lbl = new GenericLabel("Required Quests:");
		lbl.setHeight(15);
		lbl.setWidth(50);
		lbl.setX(10);
		lbl.setY(40);
		lbl.setTooltip("The quests required to start this quest");
		
		attachWidget(plg, lbl);
		
		lbl = new GenericLabel("Availabe");
		lbl.setHeight(15);
		lbl.setWidth(50);
		lbl.setX(120);
		lbl.setY(40);
		lbl.setTooltip("All the quests available on the server");
		
		attachWidget(plg, lbl);
		
		list_quests = new GenericListWidget();
		list_quests.setHeight(90);
		list_quests.setWidth(120);
		list_quests.setX(119);
		list_quests.setY(54);
		list_quests.setTooltip("All the quests available on the server");
		
		attachWidget(plg, list_quests);
		
		lbl = new GenericLabel("Required");
		lbl.setHeight(15);
		lbl.setWidth(50);
		lbl.setX(290);
		lbl.setY(40);
		lbl.setTooltip("The quests required to start this quest");
		
		attachWidget(plg, lbl);
		
		list_reqs = new GenericListWidget();
		list_reqs.setHeight(90);
		list_reqs.setWidth(120);
		list_reqs.setX(289);
		list_reqs.setY(54);
		list_reqs.setTooltip("The quests required to start this quest");
		
		attachWidget(plg, list_reqs);
		
		btn_reqs_add =  new GenericButton("->");
		btn_reqs_add.setHeight(20);
		btn_reqs_add.setWidth(20);
		btn_reqs_add.setX(255);
		btn_reqs_add.setY(75);
		btn_reqs_add.setTooltip("Click here to add the selected quest to the list of required quests");
		
		attachWidget(plg, btn_reqs_add);
		
		btn_reqs_remove =  new GenericButton("<-");
		btn_reqs_remove.setHeight(20);
		btn_reqs_remove.setWidth(20);
		btn_reqs_remove.setX(255);
		btn_reqs_remove.setY(115);
		btn_reqs_remove.setTooltip("Click here to remove the selected quest from the list of required quests");
		
		attachWidget(plg, btn_reqs_remove);
		
		// 
		// Start NPC
		// 
		
		lbl = new GenericLabel("Start NPC:");
		lbl.setHeight(15);
		lbl.setWidth(50);
		lbl.setX(10);
		lbl.setY(160);
		lbl.setTooltip("The NPC at which the quest is started");
		
		attachWidget(plg, lbl);
		
		num_npc_start = new RPG_NumericTextField();
		num_npc_start.setMaximumCharacters(5);
		num_npc_start.setHeight(15);
		num_npc_start.setWidth(40);
		num_npc_start.setX(70);
		num_npc_start.setY(156);
		num_npc_start.setTooltip("The ID of the NPC at which the quest is started");
		num_npc_start.setTabIndex(2);
		
		attachWidget(plg, num_npc_start);
		
		lbl_npc_start = new GenericLabel("<none>");
		lbl_npc_start.setHeight(15);
		lbl_npc_start.setWidth(80);
		lbl_npc_start.setX(120);
		lbl_npc_start.setY(160);
		lbl_npc_start.setTooltip("The NPC at which the quest is started");
		
		attachWidget(plg, lbl_npc_start);
		
		btn_npc_start =  new GenericButton("Pick");
		btn_npc_start.setHeight(20);
		btn_npc_start.setWidth(80);
		btn_npc_start.setX(200);
		btn_npc_start.setY(153);
		btn_npc_start.setTooltip("Click here to select a NPC if you don't know the ID");
		
		attachWidget(plg, btn_npc_start);
		
		// 
		// End NPC
		// 
		
		lbl = new GenericLabel("End NPC:");
		lbl.setHeight(15);
		lbl.setWidth(50);
		lbl.setX(10);
		lbl.setY(185);
		lbl.setTooltip("The NPC at which the quest is completed");
		
		attachWidget(plg, lbl);
		
		num_npc_end = new RPG_NumericTextField();
		num_npc_end.setMaximumCharacters(5);
		num_npc_end.setHeight(15);
		num_npc_end.setWidth(40);
		num_npc_end.setX(70);
		num_npc_end.setY(181);
		num_npc_end.setTooltip("The ID of the NPC at which the quest is completed");
		num_npc_end.setTabIndex(2);
		
		attachWidget(plg, num_npc_end);
		
		lbl_npc_end = new GenericLabel("<none>");
		lbl_npc_end.setHeight(15);
		lbl_npc_end.setWidth(80);
		lbl_npc_end.setX(120);
		lbl_npc_end.setY(185);
		lbl_npc_end.setTooltip("The NPC at which the quest is completed");
		
		attachWidget(plg, lbl_npc_end);
		
		btn_npc_end =  new GenericButton("Pick");
		btn_npc_end.setHeight(20);
		btn_npc_end.setWidth(80);
		btn_npc_end.setX(200);
		btn_npc_end.setY(178);
		btn_npc_end.setTooltip("Click here to select a NPC if you don't know the ID");
		
		attachWidget(plg, btn_npc_end);
		
		// 
		// Data
		// 
		
		RPG_QuestCreationData data = (RPG_QuestCreationData)Data;
		
		for (RPG_Quest q : ((RPG_QuestManager)Plugin).GetQuests())
		{
			ListWidgetItem item = new ListWidgetItem(q.GetID() + "", q.GetName(RPG_Language.EN));
			if (data.ReqQuests.contains(q.GetID()))
				list_reqs.addItem(item);
			else
				list_quests.addItem(item);
		}
		
		if (data.NPCStartID != -1)
		{
			num_npc_start.setText(data.NPCStartID + "");
			if (RPG_Core.GetNpcManager().GetNPC(data.NPCStartID) != null)
				lbl_npc_start.setText(RPG_Core.GetNpcManager().GetNPC(data.NPCStartID).GetName());
		}
		
		if (data.NPCEndID != -1)
		{
			num_npc_end.setText(data.NPCEndID + "");
			if (RPG_Core.GetNpcManager().GetNPC(data.NPCEndID) != null)
				lbl_npc_end.setText(RPG_Core.GetNpcManager().GetNPC(data.NPCEndID).GetName());
		}
	}
	
	public boolean IsReqsAddButton(Button Btn)
	{
		return Btn.equals(btn_reqs_add);
	}
	public boolean IsReqsRemoveButton(Button Btn)
	{
		return Btn.equals(btn_reqs_remove);
	}
	
	public boolean IsPickNPCStartButton(Button Btn)
	{
		return Btn.equals(btn_npc_start);
	}
	public boolean IsPickNPCEndButton(Button Btn)
	{
		return Btn.equals(btn_npc_end);
	}
	
	public boolean IsNPCStartNumField(TextField Field)
	{
		return num_npc_start.equals(Field);
	}
	public boolean IsNPCEndNumField(TextField Field)
	{
		return num_npc_end.equals(Field);
	}
	
	public void AddItemToReqs()
	{
		if (list_quests.getSelectedItem() != null)
		{
			ListWidgetItem item = list_quests.getSelectedItem();
			list_quests.removeItem(item);
			list_reqs.addItem(item);
			
			list_quests.setDirty(true);
			list_reqs.setDirty(true);
		}
	}
	public void RemoveItemFromReqs()
	{
		if (list_reqs.getSelectedItem() != null)
		{
			ListWidgetItem item = list_reqs.getSelectedItem();
			list_reqs.removeItem(item);
			list_quests.addItem(item);
			
			list_quests.setDirty(true);
			list_reqs.setDirty(true);
		}
	}
	
	public void UpdateStartNPC(int ID)
	{
		if (RPG_Core.GetNpcManager().IsNPCID(ID))
		{
			num_npc_start.setBorderColor(new Color(159, 159, 159));
			lbl_npc_start.setText(RPG_Core.GetNpcManager().GetNPC(ID).GetName());
		}
		else
		{
			num_npc_start.setBorderColor(new Color(255, 0, 0));
			lbl_npc_start.setText("<none>");
		}
		
		num_npc_start.setDirty(true);
	}
	public void UpdateEndNPC(int ID)
	{
		if (RPG_Core.GetNpcManager().IsNPCID(ID))
		{
			num_npc_end.setBorderColor(new Color(159, 159, 159));
			lbl_npc_end.setText(RPG_Core.GetNpcManager().GetNPC(ID).GetName());
		}
		else
		{
			num_npc_end.setBorderColor(new Color(255, 0, 0));
			lbl_npc_end.setText("<none>");
		}
		
		num_npc_end.setDirty(true);
	}
	
	public void StartNPCNotSpecified()
	{
		num_npc_start.setBorderColor(new Color(255, 0, 0));
	}
	public void EndNPCNotSpecified()
	{
		num_npc_end.setBorderColor(new Color(255, 0, 0));
	}

	public void ResetColors()
	{
		num_npc_start.setBorderColor(new Color(159, 159, 159));
	}
}

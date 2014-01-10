package rpgNpcManager;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericComboBox;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericTextField;
import org.getspout.spoutapi.gui.TextField;

import rpgGUI.RPG_CreationScreen;
import rpgGUI.RPG_CreationScreenButtons;
import rpgGUI.RPG_NumericTextField;
import rpgNpc.RPG_NpcArmourClass;
import rpgOther.RPG_CreationData;
import rpgPlayer.RPG_Player;

public class RPG_Screen_CreateNpc extends RPG_CreationScreen
{
	private GenericLabel lbl_name;
	private GenericTextField txt_name;
	private GenericTextField txt_text;
	private RPG_NumericTextField num_level;
	private RPG_NumericTextField num_money;
	private RPG_NumericTextField num_item;
	private GenericTextField txt_item;
	private GenericLabel lbl_item;
	private GenericComboBox box_head;
	private GenericComboBox box_chest;
	private GenericComboBox box_legs;
	private GenericComboBox box_feet;
	private GenericTextField txt_templatename;
	private GenericButton btn_saveastemplate;
	
	public String GetNPCName()
	{
		return txt_name.getText();
	}
	public String GetNPCText()
	{
		return txt_text.getText();
	}
	public int GetNPCLevel()
	{
		return num_level.GetValue();
	}
	public int GetNPCMoney()
	{
		return num_money.GetValue();
	}
	public int GetItemInHand()
	{
		return num_item.GetValue();
	}
	public String GetItemText()
	{
		return txt_item.getText();
	}
	public RPG_NpcArmourClass GetArmourHead()
	{		 
		if (box_head.getSelectedItem() == null)
			return RPG_NpcArmourClass.None;
		String txt = box_head.getSelectedItem();
		
		if (txt.equalsIgnoreCase("leather"))
			return RPG_NpcArmourClass.Leather;
		else if (txt.equalsIgnoreCase("chain"))
			return RPG_NpcArmourClass.Chain;
		else if (txt.equalsIgnoreCase("iron"))
			return RPG_NpcArmourClass.Iron;
		else if (txt.equalsIgnoreCase("gold"))
			return RPG_NpcArmourClass.Gold;
		else if (txt.equalsIgnoreCase("diamond"))
			return RPG_NpcArmourClass.Diamond;
		
		return RPG_NpcArmourClass.None;
	}
	public RPG_NpcArmourClass GetArmourChest()
	{
		if (box_chest.getSelectedItem() == null)
			return RPG_NpcArmourClass.None;
		String txt = box_chest.getSelectedItem();
		
		if (txt.equalsIgnoreCase("leather"))
			return RPG_NpcArmourClass.Leather;
		else if (txt.equalsIgnoreCase("chain"))
			return RPG_NpcArmourClass.Chain;
		else if (txt.equalsIgnoreCase("iron"))
			return RPG_NpcArmourClass.Iron;
		else if (txt.equalsIgnoreCase("gold"))
			return RPG_NpcArmourClass.Gold;
		else if (txt.equalsIgnoreCase("diamond"))
			return RPG_NpcArmourClass.Diamond;
		
		return RPG_NpcArmourClass.None;
	}
	public RPG_NpcArmourClass GetArmourLegs()
	{
		if (box_legs.getSelectedItem() == null)
			return RPG_NpcArmourClass.None;
		String txt = box_legs.getSelectedItem();
		
		if (txt.equalsIgnoreCase("leather"))
			return RPG_NpcArmourClass.Leather;
		else if (txt.equalsIgnoreCase("chain"))
			return RPG_NpcArmourClass.Chain;
		else if (txt.equalsIgnoreCase("iron"))
			return RPG_NpcArmourClass.Iron;
		else if (txt.equalsIgnoreCase("gold"))
			return RPG_NpcArmourClass.Gold;
		else if (txt.equalsIgnoreCase("diamond"))
			return RPG_NpcArmourClass.Diamond;
		
		return RPG_NpcArmourClass.None;
	}
	public RPG_NpcArmourClass GetArmourFeet()
	{
		if (box_feet.getSelectedItem() == null)
			return RPG_NpcArmourClass.None;
		String txt = box_feet.getSelectedItem();
		
		if (txt.equalsIgnoreCase("leather"))
			return RPG_NpcArmourClass.Leather;
		else if (txt.equalsIgnoreCase("chain"))
			return RPG_NpcArmourClass.Chain;
		else if (txt.equalsIgnoreCase("iron"))
			return RPG_NpcArmourClass.Iron;
		else if (txt.equalsIgnoreCase("gold"))
			return RPG_NpcArmourClass.Gold;
		else if (txt.equalsIgnoreCase("diamond"))
			return RPG_NpcArmourClass.Diamond;
		
		return RPG_NpcArmourClass.None;
	}
	public String GetNPCTemplateName()
	{
		return txt_templatename.getText();
	}
	
	
	public RPG_Screen_CreateNpc(Plugin Plugin, RPG_Player Player, RPG_CreationData Data)
	{
		super(Plugin, Player, Data, "Create a NPC", RPG_CreationScreenButtons.CancelCreate);
		
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
		// Text
		// 
		
		GenericLabel lbl = new GenericLabel("Text:");
		lbl.setHeight(15);
		lbl.setWidth(50);
		lbl.setX(10);
		lbl.setY(70);
		lbl.setTooltip("This is the text that the NPC says to every person that talks to him/her");
		
		attachWidget(plg, lbl);
		
		txt_text = new GenericTextField();
		txt_text.setMaximumLines(4);
		txt_text.setMaximumCharacters(1024);
		txt_text.setHeight(50);
		txt_text.setWidth(width - 63);
		txt_text.setX(50);
		txt_text.setY(66);
		txt_text.setTooltip("This is the text that the NPC says to every person that talks to him/her");
		txt_text.setTabIndex(1);
		
		attachWidget(plg, txt_text);
		
		// 
		// Level
		// 
		
		lbl = new GenericLabel("Level:");
		lbl.setHeight(15);
		lbl.setWidth(50);
		lbl.setX(10);
		lbl.setY(135);
		lbl.setTooltip("The level of the NPC.");
		
		attachWidget(plg, lbl);
		
		num_level = new RPG_NumericTextField();
		num_level.setMaximumCharacters(4);
		num_level.setHeight(15);
		num_level.setWidth(35);
		num_level.setX(50);
		num_level.setY(130);
		num_level.setTooltip("The level of the NPC");
		num_level.setTabIndex(2);
		
		attachWidget(plg, num_level);
		
		// 
		// Money
		// 
		
		lbl = new GenericLabel("Money:");
		lbl.setHeight(15);
		lbl.setWidth(50);
		lbl.setX(10);
		lbl.setY(160);
		lbl.setTooltip("The money of the NPC.");
		
		attachWidget(plg, lbl);
		
		num_money = new RPG_NumericTextField();
		num_money.setMaximumCharacters(8);
		num_money.setHeight(15);
		num_money.setWidth(60);
		num_money.setX(50);
		num_money.setY(156);
		num_money.setTooltip("The money of the NPC");
		num_money.setTabIndex(3);
		
		attachWidget(plg, num_money);
		
		// 
		// Item in hand
		// 
		
		lbl = new GenericLabel("Item:");
		lbl.setHeight(15);
		lbl.setWidth(50);
		lbl.setX(10);
		lbl.setY(185);
		lbl.setTooltip("The item that the NPC is holding in hand");
		
		attachWidget(plg, lbl);
		
		num_item = new RPG_NumericTextField();
		num_item.setMaximumCharacters(3);
		num_item.setHeight(15);
		num_item.setWidth(40);
		num_item.setX(50);
		num_item.setY(180);
		num_item.setTooltip("The item that the NPC is holding in hand");
		num_item.setTabIndex(4);
		
		attachWidget(plg, num_item);
		
		lbl_item = new GenericLabel("<none>");
		lbl_item.setHeight(15);
		lbl_item.setWidth(80);
		lbl_item.setX(100);
		lbl_item.setY(185);
		lbl_item.setTooltip("The name of the item that the NPC is holding in hand");
		
		attachWidget(plg, lbl_item);
		
		// 
		// Armour
		// 
		
		lbl = new GenericLabel("Armour:");
		lbl.setHeight(15);
		lbl.setWidth(50);
		lbl.setX(150);
		lbl.setY(135);
		lbl.setTooltip("This is the armour that the NPC is wearing");
		
		attachWidget(plg, lbl);
		
		ArrayList<String> items = new ArrayList<String>();
		items.add("None");
		items.add("Leather");
		items.add("Chain");
		items.add("Iron");
		items.add("Gold");
		items.add("Diamond");
		
		box_head = new GenericComboBox();
		box_head.setHeight(20);
		box_head.setWidth(100);
		box_head.setX(200);
		box_head.setY(132);
		box_head.setText("Head");
		box_head.setTooltip("This is the head armour that the NPC is wearing");
		box_head.setItems(items);
		
		attachWidget(plg, box_head);
		
		box_chest = new GenericComboBox();
		box_chest.setHeight(20);
		box_chest.setWidth(100);
		box_chest.setX(200);
		box_chest.setY(157);
		box_chest.setText("Chest");
		box_chest.setTooltip("This is the chest armour that the NPC is wearing");
		box_chest.setItems(items);
		
		attachWidget(plg, box_chest);
		
		box_legs = new GenericComboBox();
		box_legs.setHeight(20);
		box_legs.setWidth(100);
		box_legs.setX(width - 112);
		box_legs.setY(132);
		box_legs.setText("Legs");
		box_legs.setTooltip("This is the legs armour that the NPC is wearing");
		box_legs.setItems(items);
		
		attachWidget(plg, box_legs);
		
		box_feet = new GenericComboBox();
		box_feet.setHeight(20);
		box_feet.setWidth(100);
		box_feet.setX(width - 112);
		box_feet.setY(157);
		box_feet.setText("Feet");
		box_feet.setTooltip("This is the feet armour that the NPC is wearing");
		box_feet.setItems(items);
		
		attachWidget(plg, box_feet);
		
		// 
		// Template name
		// 
		
		txt_templatename = new GenericTextField();
		txt_templatename.setMaximumCharacters(16);
		txt_templatename.setHeight(15);
		txt_templatename.setWidth(100);
		txt_templatename.setX(width - 210);
		txt_templatename.setY(height - 50);
		txt_templatename.setTooltip("The name of the template");
		txt_templatename.setVisible(false);
		txt_templatename.setTabIndex(5);
		
		attachWidget(plg, txt_templatename);
		
		// 
		// Save as template
		// 
		
		btn_saveastemplate =  new GenericButton("Save as template");
		btn_saveastemplate.setHeight(20);
		btn_saveastemplate.setWidth(120);
		btn_saveastemplate.setX(width - 220);
		btn_saveastemplate.setY(height - 30);
		btn_saveastemplate.setTooltip("Click here to save this NPC as a template");
		
		attachWidget(plg, btn_saveastemplate);
	}
	
	public boolean IsSaveAsTemplateButton(Button Btn)
	{
		return Btn.equals(btn_saveastemplate);
	}
	
	public boolean IsItemTextField(TextField textField)
	{
		return textField.equals(num_item);
	}
	
	public void ShowTemplateTextField()
	{
		txt_templatename.setVisible(true);
	}
	public boolean IsTemplateNameFieldVisible()
	{
		return txt_templatename.isVisible();
	}
	
	public void UpdateItem(int ID)
	{
		Material mat = Material.getMaterial(ID);
		if (mat != null)
		{
			lbl_item.setText(mat.name().replaceAll("_", " "));
			num_item.setBorderColor(new Color(159, 159, 159));
		}
		else
		{
			lbl_item.setText("<none>");
			num_item.setBorderColor(new Color(255, 0, 0));
		}
		
		num_item.setDirty(true);
	}
	
	public void NPCNameNotSpecified()
	{
		lbl_name.setTextColor(new Color(255, 0, 0));
	}
	public void TemplateNameNotSpecified()
	{
		txt_templatename.setBorderColor(new Color(255, 0, 0));
	}
	
	public void ResetColors()
	{
		lbl_name.setTextColor(new Color(255, 255, 255));
		txt_templatename.setBorderColor(new Color(159, 159, 159));
		num_item.setBorderColor(new Color(159, 159, 159));
	}
}

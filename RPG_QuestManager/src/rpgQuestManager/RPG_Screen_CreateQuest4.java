package rpgQuestManager;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericListWidget;
import org.getspout.spoutapi.gui.ListWidgetItem;
import org.getspout.spoutapi.gui.TextField;

import rpgGUI.RPG_CreationScreen;
import rpgGUI.RPG_CreationScreenButtons;
import rpgGUI.RPG_NumericTextField;
import rpgOther.RPG_CreationData;
import rpgPlayer.RPG_Player;
import rpgQuest.RPG_QuestCreationData;
import rpgQuest.RPG_QuestRequest;
import rpgQuest.RPG_QuestType;

public class RPG_Screen_CreateQuest4 extends RPG_CreationScreen
{
	private RPG_NumericTextField num_newitem;
	private RPG_NumericTextField num_newitemamount;
	private GenericLabel lbl_newitem;
	private GenericButton btn_newitem;
	
	private GenericButton btn_removeitem;
	
	private GenericListWidget list_data;
	
	public ArrayList<RPG_QuestRequest> GetQuestData()
	{
		ArrayList<RPG_QuestRequest> stacks = new ArrayList<RPG_QuestRequest>();
		
		for (ListWidgetItem item : list_data.getItems())
		{
			String[] splits = item.getText().split("x");
			stacks.add(new RPG_QuestRequest(Integer.parseInt(splits[0]), Integer.parseInt(splits[1])));
		}
		
		return stacks;
	}
	
	
	public RPG_Screen_CreateQuest4(Plugin Plugin, RPG_Player Player, RPG_CreationData Data)
	{
		super(Plugin, Player, Data, "Create a Quest (4/4)", RPG_CreationScreenButtons.CancelBackCreate);
		
		// 
		// Quest requests
		// 
		
		if (((RPG_QuestCreationData)Data).Type == RPG_QuestType.Bring)
		{
			GenericLabel lbl = new GenericLabel("Items:");
			lbl.setHeight(15);
			lbl.setWidth(50);
			lbl.setX(10);
			lbl.setY(40);
			lbl.setTooltip("");
			
			attachWidget(plg, lbl);
			
			lbl = new GenericLabel("Add:");
			lbl.setHeight(15);
			lbl.setWidth(50);
			lbl.setX(50);
			lbl.setY(40);
			lbl.setTooltip("");
			
			attachWidget(plg, lbl);
			
			lbl = new GenericLabel("ID:");
			lbl.setHeight(15);
			lbl.setWidth(50);
			lbl.setX(70);
			lbl.setY(60);
			lbl.setTooltip("");
			
			attachWidget(plg, lbl);
			
			num_newitem = new RPG_NumericTextField();
			num_newitem.setMaximumCharacters(5);
			num_newitem.setHeight(15);
			num_newitem.setWidth(40);
			num_newitem.setX(120);
			num_newitem.setY(56);
			num_newitem.setTooltip("The ID of the new item");
			num_newitem.setTabIndex(1);
			
			attachWidget(plg, num_newitem);
			
			lbl_newitem = new GenericLabel("<none>");
			lbl_newitem.setHeight(15);
			lbl_newitem.setWidth(80);
			lbl_newitem.setX(170);
			lbl_newitem.setY(60);
			lbl_newitem.setTooltip("The new item needed to complete the quest");
			
			attachWidget(plg, lbl_newitem);
			
			lbl = new GenericLabel("Amount:");
			lbl.setHeight(15);
			lbl.setWidth(50);
			lbl.setX(70);
			lbl.setY(85);
			lbl.setTooltip("");
			
			attachWidget(plg, lbl);
			
			num_newitemamount = new RPG_NumericTextField();
			num_newitemamount.setMaximumCharacters(5);
			num_newitemamount.setHeight(15);
			num_newitemamount.setWidth(40);
			num_newitemamount.setX(120);
			num_newitemamount.setY(81);
			num_newitemamount.setTooltip("The amount of the item needed");
			num_newitemamount.setTabIndex(2);
			
			attachWidget(plg, num_newitemamount);
			
			btn_newitem =  new GenericButton("Add");
			btn_newitem.setHeight(20);
			btn_newitem.setWidth(80);
			btn_newitem.setX(70);
			btn_newitem.setY(106);
			btn_newitem.setTooltip("Adds the new item to the list");
			
			attachWidget(plg, btn_newitem);
			
			lbl = new GenericLabel("Remove:");
			lbl.setHeight(15);
			lbl.setWidth(50);
			lbl.setX(50);
			lbl.setY(140);
			lbl.setTooltip("");
			
			attachWidget(plg, lbl);
			
			btn_removeitem =  new GenericButton("Remove");
			btn_removeitem.setHeight(20);
			btn_removeitem.setWidth(80);
			btn_removeitem.setX(70);
			btn_removeitem.setY(156);
			btn_removeitem.setTooltip("Removes the selected item from the list");
			
			attachWidget(plg, btn_removeitem);
			
			list_data = new GenericListWidget();
			list_data.setHeight(150);
			list_data.setWidth(120);
			list_data.setX(width - 130);
			list_data.setY(34);
			list_data.setTooltip("The items needed to complete the quest");
			
			attachWidget(plg, list_data);
		}
	}
	
	public boolean IsAddItemButton(Button Btn)
	{
		return Btn.equals(btn_newitem);
	}
	public boolean IsRemoveItemButton(Button Btn)
	{
		return Btn.equals(btn_removeitem);
	}
	
	public boolean IsNewItemField(TextField Field)
	{
		return num_newitem.equals(Field);
	}
	
	public void AddItemToList()
	{
		ResetColors();
		
		Material mat = Material.getMaterial(num_newitem.GetValue());
		if (mat != null && !num_newitem.getText().isEmpty())
		{
			if (num_newitemamount.GetValue() > 0)
			{
				boolean contains = false;
				for (ListWidgetItem item : list_data.getItems())
				{
					if (item.getText().split("x")[0].equalsIgnoreCase(num_newitem.getText()))
					{
						contains = true;
						break;
					}
				}
				
				if (!contains)
				{
					ListWidgetItem item = new ListWidgetItem(mat.name().replaceAll("_", " ") + " x " + num_newitemamount.GetValue(), mat.getId() + "x" + num_newitemamount.GetValue());
					list_data.addItem(item);
					list_data.setDirty(true);
					
					num_newitem.setText("");
					num_newitemamount.setText("");
					lbl_newitem.setText("<none>");
				}
				else
					list_data.setBackgroundColor(new Color(255, 0, 0));
			}
			else
				num_newitemamount.setBorderColor(new Color(255, 0, 0));
		}
		else
			num_newitem.setBorderColor(new Color(255, 0, 0));
		
		list_data.setDirty(true);
	}
	public void RemoveItemFromList()
	{
		ResetColors();
		
		if (list_data.getSelectedItem() != null)
			list_data.removeItem(list_data.getSelectedItem());
		else
			list_data.setBackgroundColor(new Color(255, 0, 0));
		
		list_data.setDirty(true);
	}
	
	public void UpdateNewItem(int ID)
	{		
		Material mat = Material.getMaterial(ID);
		if (mat != null)
		{
			lbl_newitem.setText(mat.name().replaceAll("_", " "));
			num_newitem.setBorderColor(new Color(159, 159, 159));
		}
		else
		{
			lbl_newitem.setText("<none>");
			num_newitem.setBorderColor(new Color(255, 0, 0));
		}
		
		num_newitem.setDirty(true);
	}
	
	public void ResetColors()
	{
		num_newitem.setBorderColor(new Color(159, 159, 159));
		num_newitemamount.setBorderColor(new Color(159, 159, 159));
		list_data.setBackgroundColor(new Color(0, 0, 0));
	}
}

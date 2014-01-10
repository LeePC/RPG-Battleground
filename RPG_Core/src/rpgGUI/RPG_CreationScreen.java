package rpgGUI;

import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.Widget;
import org.getspout.spoutapi.gui.WidgetAnchor;

import rpgOther.RPG_CreationData;
import rpgPlayer.RPG_Player;

public class RPG_CreationScreen extends RPG_Screen
{
	protected GenericLabel lbl_title;
	protected GenericButton btn_cancel;
	protected GenericButton btn_back;
	protected GenericButton btn_next;
	protected GenericButton btn_create;
	
	
	public RPG_CreationScreen(Plugin plugin, RPG_Player player, RPG_CreationData Data, String Title, RPG_CreationScreenButtons Buttons)
	{
		super(plugin, player);
		
		SetBaseComponents(Title, Buttons);
	}
	
	protected void SetBaseComponents(String Title, RPG_CreationScreenButtons Buttons)
	{
		for (Widget w : getAttachedWidgets())
			removeWidget(w);
		
		// 
		// Title
		// 
		
		lbl_title = new GenericLabel(Title);
		lbl_title.setHeight(20);
		lbl_title.setWidth(100);
		lbl_title.setAlign(WidgetAnchor.CENTER_CENTER);
		lbl_title.setAnchor(WidgetAnchor.TOP_CENTER);
		lbl_title.setY(20);
		
		attachWidget(plg, lbl_title);
		
		// 
		// Create
		// 
		
		if (Buttons == RPG_CreationScreenButtons.CancelCreate || Buttons == RPG_CreationScreenButtons.CancelBackCreate || Buttons == RPG_CreationScreenButtons.CancelBackNextCreate)
		{
			btn_create = new GenericButton("Create");
			btn_create.setHeight(20);
			btn_create.setWidth(80);
			btn_create.setX(width - 90);
			btn_create.setY(height - 30);
			
			attachWidget(plg, btn_create);
		}
		
		// 
		// Next
		// 
		
		if (Buttons == RPG_CreationScreenButtons.CancelNext || Buttons == RPG_CreationScreenButtons.CancelBackNext)
		{
			btn_next = new GenericButton("Next");
			btn_next.setHeight(20);
			btn_next.setWidth(80);
			btn_next.setX(width - 90);
			btn_next.setY(height - 30);
			
			attachWidget(plg, btn_next);
		}
		else if (Buttons == RPG_CreationScreenButtons.CancelBackNextCreate)
		{
			btn_next = new GenericButton("Next");
			btn_next.setHeight(20);
			btn_next.setWidth(80);
			btn_next.setX(width - 180);
			btn_next.setY(height - 30);
			
			attachWidget(plg, btn_next);
		}
		
		// 
		// Back
		// 
		
		if (Buttons == RPG_CreationScreenButtons.CancelBackNext || Buttons == RPG_CreationScreenButtons.CancelBackCreate)
		{
			btn_back = new GenericButton("Back");
			btn_back.setHeight(20);
			btn_back.setWidth(80);
			btn_back.setX(width - 180);
			btn_back.setY(height - 30);
			btn_back.setTooltip("Click here to go back to the last step");
			
			attachWidget(plg, btn_back);
		}
		else if (Buttons == RPG_CreationScreenButtons.CancelBackNextCreate)
		{
			btn_back = new GenericButton("Back");
			btn_back.setHeight(20);
			btn_back.setWidth(80);
			btn_back.setX(width - 270);
			btn_back.setY(height - 30);
			btn_back.setTooltip("Click here to go back to the last step");
			
			attachWidget(plg, btn_back);
		}
		
		// 
		// Cancel
		// 
		
		btn_cancel = new GenericButton("Cancel");
		btn_cancel.setHeight(20);
		btn_cancel.setWidth(80);
		btn_cancel.setX(10);
		btn_cancel.setY(height - 30);
		btn_cancel.setTooltip("Click here to abort the creation wizard");
		
		attachWidget(plg, btn_cancel);
	}
	
	public boolean IsCancelButton(Button Btn)
	{
		return Btn.equals(btn_cancel);
	}
	public boolean IsBackButton(Button Btn)
	{
		return Btn.equals(btn_back);
	}
	public boolean IsNextButton(Button Btn)
	{
		return Btn.equals(btn_next);
	}
	public boolean IsCreateButton(Button Btn)
	{
		return Btn.equals(btn_create);
	}
}

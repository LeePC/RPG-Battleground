package rpgGUI;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.WidgetAnchor;

import rpgPlayer.RPG_Player;

public class RPG_InfoScreen extends RPG_Screen
{
	private GenericButton btn_close;
	private HashMap<GenericButton, String> navButtons = new HashMap<GenericButton, String>();
	private HashMap<GenericButton, String> linkButtons = new HashMap<GenericButton, String>();
	
	
	public RPG_InfoScreen(Plugin plugin, RPG_Player player, String Name, String Title, ArrayList<RPG_InfoScreenItem> Infos)
	{
		super(plugin, player);
		
		GenericLabel lbl_title = new GenericLabel(ChatColor.BLUE + Title);
		lbl_title.setHeight(20);
		lbl_title.setWidth(100);
		lbl_title.setAlign(WidgetAnchor.CENTER_CENTER);
		lbl_title.setAnchor(WidgetAnchor.TOP_CENTER);
		lbl_title.setY(20);
		
		attachWidget(plg, lbl_title);
		
		int count = 0;
		String[] splits = Name.split("\\.");
		for (int i = 0; i < splits.length - 1; i++)
		{
			GenericButton btn = new GenericButton(splits[i]);
			btn.setHeight(16);
			btn.setWidth(30);
			btn.setX(5 + i * 40);
			btn.setY(5);
			
			attachWidget(plg, btn);
			count = Name.indexOf(".", count + 1);
			navButtons.put(btn, Name.substring(0, count));
			
			GenericLabel lbl = new GenericLabel(">");
			lbl.setHeight(15);
			lbl.setWidth(12);
			lbl.setX(38 + i * 40);
			lbl.setY(10);
			
			attachWidget(plg, lbl);
		}
		
		for (int i = 0; i < Infos.size(); i++)
		{
			if (!player.GetPlayer().hasPermission(Infos.get(i).Perms))
				continue;
			
			GenericLabel lbl_name = new GenericLabel(ChatColor.GOLD + Infos.get(i).Name);
			lbl_name.setHeight(20);
			lbl_name.setWidth(100);
			lbl_name.setX(20);
			lbl_name.setY(35 + i * 15);
			
			attachWidget(plg, lbl_name);
			
			GenericLabel lbl_desc = new GenericLabel(Infos.get(i).Description);
			lbl_desc.setHeight(20);
			lbl_desc.setWidth(100);
			lbl_desc.setX(100);
			lbl_desc.setY(35 + i * 15);
			
			attachWidget(plg, lbl_desc);
			
			if (Infos.get(i).LinkScreen != null)
			{
				GenericButton btn_link = new GenericButton(">");
				btn_link.setHeight(12);
				btn_link.setWidth(12);
				btn_link.setX(5);
				btn_link.setY(33 + i * 15);
				
				attachWidget(plg, btn_link);
				linkButtons.put(btn_link, Infos.get(i).LinkScreen);
			}
		}
		
		btn_close = new GenericButton("Close");
		btn_close.setHeight(20);
		btn_close.setWidth(80);
		btn_close.setX(width - 90);
		btn_close.setY(height - 30);
		
		attachWidget(plg, btn_close);
	}
	
	public boolean IsCloseButton(Button Btn)
	{
		return Btn.equals(btn_close);
	}
	public String GetNavLink(Button Btn)
	{
		for (GenericButton btn : navButtons.keySet())
		{
			if (btn.equals(Btn))
				return navButtons.get(btn);
		}
		return null;
	}
	public String GetLink(Button Btn)
	{
		for (GenericButton btn : linkButtons.keySet())
		{
			if (btn.equals(Btn))
				return linkButtons.get(btn);
		}
		return null;
	}
}

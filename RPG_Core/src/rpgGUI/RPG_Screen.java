package rpgGUI;

import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.gui.GenericPopup;

import rpgPlayer.RPG_Player;

public abstract class RPG_Screen extends GenericPopup
{
	protected RPG_Player p;
	protected Plugin plg;
	
	
	public RPG_Screen(Plugin plugin, RPG_Player player)
	{
		setBgVisible(true);
		
		plg = plugin;
		p = player;
		
		setWidth(SpoutManager.getPlayer(p.GetPlayer()).getMainScreen().getWidth());
		setHeight(SpoutManager.getPlayer(p.GetPlayer()).getMainScreen().getHeight());
	}
	
	public void Show()
	{
		SpoutManager.getPlayer(p.GetPlayer()).getMainScreen().attachPopupScreen(this);
	}
	
	public void Hide()
	{
		this.close();
	}
}

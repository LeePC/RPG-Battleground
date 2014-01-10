package rpgWebManager;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import rpgCore.RPG_Core;
import rpgGUI.RPG_InfoScreenItem;
import rpgOther.RPG_LogType;
import rpgPlayer.RPG_EditMode;
import rpgPlayer.RPG_Player;

public class RPG_WebManager extends JavaPlugin implements Listener
{
	private static String prefix = "[RPG Web Manager] ";
	private static Logger logger = Logger.getLogger("Minecraft");
	
	private HashMap<Integer, String> pws = new HashMap<Integer, String>();
	
	
	// 
	// Main
	// 
	public RPG_WebManager()
	{ }
	
	@Override
	public void onEnable()
	{
		if (!getServer().getPluginManager().isPluginEnabled("RPG Core"))
		{
			logger.severe(prefix + "RPG CORE IS NOT ENABLED!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		logger.info(prefix + "Registering events...");
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		
		logger.info(prefix + "Adding info screens...");
		
		ArrayList<RPG_InfoScreenItem> items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("password",	"Manage your password that is used to login in the web interface",	"rpg.web.password"));
		RPG_Core.AddInfoScreen("rpg.web", "Web Manager", items);
		
		PluginDescriptionFile pdf = this.getDescription();
		logger.info(prefix +  pdf.getName() + " version " + pdf.getVersion() + " is enabled");
		RPG_Core.Log(RPG_LogType.Information, "Start", pdf.getName() + " version " + pdf.getVersion() + " enabled");
	}
	@Override
	public void onDisable()
	{		
		PluginDescriptionFile pdf = this.getDescription();
		logger.info(prefix + pdf.getName() + " is disabled");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		RPG_Core.Log(sender, commandLabel, args);
		
		RPG_Player rpg_p = null;
		if (RPG_Core.IsPlayer(sender))
			rpg_p = RPG_Core.GetPlayer(((Player)sender).getName());
		
		if (commandLabel.equalsIgnoreCase("web"))
		{			
			if (!RPG_Core.HasPermission(sender, "web"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (args.length == 0)
			{
				RPG_Core.ShowInfoScreen("rpg.web", sender);
			}
			else
			{
				if (args[0].equalsIgnoreCase("password") || args[0].equalsIgnoreCase("pw"))
				{
					if (!RPG_Core.HasPermission(sender, "web.password"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (!RPG_Core.IsPlayer(sender))
					{
						sender.sendMessage(ChatColor.RED + "Only players can set a password");
						return false;
					}
					
					rpg_p.SetEditMode(RPG_EditMode.Account_SetPW);
					rpg_p.SetEditStep(0);
					
					sender.sendMessage(RPG_Core.GetTextSeperator());
					sender.sendMessage(ChatColor.BLUE + "Please enter your password:");
					sender.sendMessage(RPG_Core.GetTextSeperator());
				}
				else
					sender.sendMessage(ChatColor.RED + "Unknown command");
			}
		}
		else if (commandLabel.equalsIgnoreCase("password") || commandLabel.equalsIgnoreCase("pw"))
		{
			if (!RPG_Core.HasPermission(sender, "web.password"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (!RPG_Core.IsPlayer(sender))
			{
				sender.sendMessage(ChatColor.RED + "Only players can set a password");
				return false;
			}
			
			rpg_p.SetEditMode(RPG_EditMode.Account_SetPW);
			rpg_p.SetEditStep(0);
			
			sender.sendMessage(RPG_Core.GetTextSeperator());
			sender.sendMessage(ChatColor.BLUE + "Please enter your password:");
			sender.sendMessage(RPG_Core.GetTextSeperator());
		}
		
		return true;
	}
	
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		RPG_Player rpg_p = RPG_Core.GetPlayer(e.getPlayer().getName());
		if (pws.containsKey(rpg_p.GetID()))
			pws.remove(rpg_p.GetID());
	}
	public void onPlayerKick(PlayerKickEvent e)
	{
		RPG_Player rpg_p = RPG_Core.GetPlayer(e.getPlayer().getName());
		if (pws.containsKey(rpg_p.GetID()))
			pws.remove(rpg_p.GetID());
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e)
	{
		Player p = e.getPlayer();
		RPG_Player rpg_p = RPG_Core.GetPlayer(p.getName());
		
		if (rpg_p.GetEditMode() == RPG_EditMode.Account_SetPW)
		{
			e.setCancelled(true);
			
			if (rpg_p.GetEditStep() == 0)
			{				
				String pwhash = "";
				try
				{
					byte[] bytes = MessageDigest.getInstance("MD5").digest(e.getMessage().getBytes("UTF-8"));
					StringBuilder sb = new StringBuilder();
					for(byte b: bytes)
						sb.append(String.format("%02x", b&0xff));
					pwhash = sb.toString();
				}
				catch (Exception ex)
				{
					p.sendMessage(ChatColor.RED + "Could not generate password hash");
					return;
				}
				
				pws.put(rpg_p.GetID(), pwhash);
				rpg_p.SetEditStep(1);
				
				p.sendMessage(RPG_Core.GetTextSeperator());
				p.sendMessage(ChatColor.BLUE + "Please enter the password a second time:");
				p.sendMessage(RPG_Core.GetTextSeperator());
			}
			else if (rpg_p.GetEditStep() == 1)
			{				
				String pwhash = "";
				try
				{
					byte[] bytes = MessageDigest.getInstance("MD5").digest(e.getMessage().getBytes("UTF-8"));
					StringBuilder sb = new StringBuilder();
					for(byte b: bytes)
						sb.append(String.format("%02x", b&0xff));
					pwhash = sb.toString();
				}
				catch (Exception ex)
				{
					p.sendMessage(ChatColor.RED + "Could not generate password hash");
					return;
				}
				
				if (!pws.containsKey(rpg_p.GetID()))
				{
					p.sendMessage(ChatColor.RED + "You need to enter your password 2 times. Please try again!");
					rpg_p.SetEditMode(RPG_EditMode.None);
					return;
				}
				
				if (!pws.get(rpg_p.GetID()).equals(pwhash))
				{
					p.sendMessage(ChatColor.RED + "The passwords do not match Please try again!");
					rpg_p.SetEditMode(RPG_EditMode.None);
					return;
				}
				
				pws.remove(rpg_p.GetID());
				
				RPG_Core.SetPasswordForPlayer(rpg_p.GetID(), pwhash);
				
				p.sendMessage(ChatColor.GREEN + "Your password has been set");
				rpg_p.SetEditMode(RPG_EditMode.None);
			}
		}
	}
	
	public void onPlayerInventoryOpen(InventoryOpenEvent e)
	{
		
	}
	public void onPlayerInventoryClick(InventoryClickEvent e)
	{
		getServer().broadcastMessage("ID: " + e.getCurrentItem().getTypeId());
	}
	public void onPlayerInventoryClose(InventoryCloseEvent e)
	{
		
	}
}

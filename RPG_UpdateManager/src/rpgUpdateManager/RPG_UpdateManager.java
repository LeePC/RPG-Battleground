package rpgUpdateManager;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.FileUtil;

import rpgCore.RPG_Core;
import rpgGUI.RPG_InfoScreenItem;
import rpgOther.RPG_LogType;
import rpgOther.RPG_PluginVersion;

public class RPG_UpdateManager extends JavaPlugin implements Listener
{
	private static String prefix = "[RPG Update Manager] ";
	private static Logger logger = Logger.getLogger("Minecraft");
	
	private boolean asyncRunning = false;
	private BukkitTask task;
	private int updateTickInterval = 0;
	private String linkVersion = "";
	private String linkDownload = "";
	
	
	// 
	// Main
	// 
	public RPG_UpdateManager()
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
	    
		logger.info(prefix + "Adding info screens");
		
		ArrayList<RPG_InfoScreenItem> items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("check", 	"Checks for updates for any of the RPG plugins", 	"rpg.update.check"));
		items.add(new RPG_InfoScreenItem("force", 	"Force all the RPG plugins to update", 				"rpg.update.force"));
		RPG_Core.AddInfoScreen("rpg.update", "Update Manager", items);
		
		logger.info(prefix + "Loading config...");
		
		getConfig().addDefault("updateTickInterval", 72000);
		getConfig().addDefault("linkVersion", "https://raw.github.com/MC-Story/MC-Story/master/{name}/plugin.yml");
		getConfig().addDefault("linkDownload", "https://raw.github.com/MC-Story/MC-Story/master/Exports/{name}.jar");
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		updateTickInterval = getConfig().getInt("updateTickInterval");
		linkVersion = getConfig().getString("linkVersion");
		linkDownload = getConfig().getString("linkDownload");
		
		logger.info(prefix + "Creating update directory...");
		File f = new File(getDataFolder().getAbsolutePath() + File.separator + "updates");
		if (f.exists())
			logger.info(prefix + "Folder already exists.");
		else if (f.mkdirs() == false)
			logger.info(prefix + "Could not create updates folder! Updates will be saved in plugin folder.");
		
		logger.info(prefix + "Starting threads...");
		task = getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() { public void run() { onCheckUpdateTick(); } }, 20, updateTickInterval);
		
		PluginDescriptionFile pdf = this.getDescription();
		logger.info(prefix +  pdf.getName() + " version " + pdf.getVersion() + " is enabled");
		RPG_Core.Log(RPG_LogType.Information, "Start", pdf.getName() + " version " + pdf.getVersion() + " enabled");
	}
	@Override
	public void onDisable()
	{		
		logger.info(prefix + "Shutting down threads...");
		
		if (task != null)
			task.cancel();
		
		while (asyncRunning)
		{
			try
			{
				Thread.sleep(1000);
				logger.info(prefix + "Still waiting...");
			}
			catch (InterruptedException e)
			{ }
		}
		
		PluginDescriptionFile pdf = this.getDescription();
		logger.info(prefix + pdf.getName() + " is disabled");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{		
		if (commandLabel.equalsIgnoreCase("um"))
		{
			RPG_Core.Log(sender, commandLabel, args);
			
			if (!RPG_Core.HasPermission(sender, "rpg.update"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (args.length == 0)
			{
				RPG_Core.ShowInfoScreen("rpg.update", sender);
			}
			else
			{
				if (args[0].equalsIgnoreCase("check"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.update.check"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					sender.sendMessage(ChatColor.GREEN + "Checking for updates...");
					onCheckUpdateTick();
					sender.sendMessage(ChatColor.GREEN + "Done");
				}
				else if (args[0].equalsIgnoreCase("force"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.update.force"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					sender.sendMessage(ChatColor.GREEN + "Updating...");
					for (Plugin p : getServer().getPluginManager().getPlugins())
					{
						try
						{
							if (!p.getName().startsWith("RPG"))
								continue;
							
							UpdatePlugin(p.getName().replace("RPG ", "RPG_").replace(" ", ""));
						}
						catch (Exception ex)
						{
							RPG_Core.Log(RPG_LogType.Error, "PluginUpdateForce", ex);
						}
					}
					sender.sendMessage(ChatColor.GREEN + "Done");
				}
				else
					sender.sendMessage(ChatColor.RED + "Unknown command.");
			}
		}
		
		return true;
	}
	
	public void onCheckUpdateTick()
	{
		asyncRunning = true;
		
		RPG_Core.Log(RPG_LogType.Information, "PluginUpdateCheck", "Started");
		
		boolean reload = false;
		for (Plugin p : getServer().getPluginManager().getPlugins())
		{
			try
			{
				if (!p.getName().startsWith("RPG"))
					continue;
				
				String name = p.getName().replace("RPG ", "RPG_").replace(" ", "");
				
				RPG_PluginVersion ver = new RPG_PluginVersion(p.getDescription().getVersion());
				RPG_PluginVersion ver_new = GetNewestVersion(name);
				
				if (ver_new.IsLarger(ver))
				{
					UpdatePlugin(name);
					reload = true;
				}
			}
			catch (Exception ex)
			{
				RPG_Core.Log(RPG_LogType.Error, "PluginUpdateCheck", ex);
			}
		}
		
		if (reload)
			RPG_Core.SendServerChat("update", "New RPG Plugin versions found, please restart the server!");
		
		RPG_Core.Log(RPG_LogType.Information, "PluginUpdateCheck", "Finished");
		
		asyncRunning = false;
	}
	
	private RPG_PluginVersion GetNewestVersion(String PluginName)
	{
		try
		{
			RPG_Core.Log(RPG_LogType.Information, "PluginVersion", PluginName);
			
			String versiontext = RPG_Core.GetStringFromWebsite(linkVersion.replace("{name}", PluginName));
			
			versiontext = versiontext.substring(versiontext.indexOf("version: ") + 9, versiontext.indexOf("description:")).replaceAll("\n", "").replaceAll("\r", "");
			
			return new RPG_PluginVersion(versiontext);
		}
		catch (Exception ex)
		{
			RPG_Core.Log(RPG_LogType.Error, "PluginVersion", ex);
			
			return null;
		}
	}
	private boolean UpdatePlugin(String PluginName)
	{
		RPG_Core.Log(RPG_LogType.Information, "PluginUpdate", PluginName);
		
		try
		{
			String updatefolder = this.getDataFolder().getAbsolutePath() + File.separator + "updates" + File.separator;
			if (!(new File(updatefolder)).exists())
				updatefolder = this.getDataFolder().getAbsolutePath() + File.separator;
			
			URL website = new URL(linkDownload.replace("{name}", PluginName));
		    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		    FileOutputStream fos = new FileOutputStream(updatefolder + File.separator + PluginName + ".jar");
		    fos.getChannel().transferFrom(rbc, 0, 1073741824L);
		    fos.close();
		    
		    FileUtil.copy(new File(updatefolder + File.separator + PluginName + ".jar"), new File(this.getDataFolder().getParent() + File.separator + PluginName + ".jar"));
		}
		catch (Exception ex)
		{
			RPG_Core.Log(RPG_LogType.Error, "PluginUpdate", ex);
		}
		
		return false;
	}
}

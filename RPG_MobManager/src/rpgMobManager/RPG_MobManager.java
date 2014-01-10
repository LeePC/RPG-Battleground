package rpgMobManager;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import rpgCore.RPG_Core;
import rpgGUI.RPG_InfoScreenItem;
import rpgMobs.RPG_MobDrop;
import rpgMobs.RPG_MobSpawner;
import rpgMobs.RPG_MobType;
import rpgOther.RPG_LogType;
import rpgPlayer.RPG_Player;

public class RPG_MobManager extends JavaPlugin implements Listener
{
	private static String prefix = "[RPG Mob Manager] ";
	private static Logger logger = Logger.getLogger("Minecraft");
	private static String SQLTablePrefix;
	
	private boolean asyncRunning = false;
	private BukkitTask task;
	private int mobTickInterval = 0;
	
	private static HashMap<Integer, RPG_MobType> mobtypes = new HashMap<Integer, RPG_MobType>();
	private static HashMap<Integer, RPG_MobSpawner> mobspawners = new HashMap<Integer, RPG_MobSpawner>();
	
	
	// 
	// Main
	// 
	public RPG_MobManager()
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
		
		SQLTablePrefix = RPG_Core.GetSQLTablePrefix();
		
		logger.info(prefix + "Registering events...");
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		
		logger.info(prefix + "Adding info screens...");
		
		ArrayList<RPG_InfoScreenItem> items = new ArrayList<RPG_InfoScreenItem>();
		RPG_Core.AddInfoScreen("rpg.mobs", "Mob Manager", items);
		
		logger.info(prefix + "Loading config...");
		
		getConfig().addDefault("mobTickInterval", 20);
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		mobTickInterval = getConfig().getInt("mobTickInterval");
		
		LoadMobTypesFromDB();
		
		LoadMobSpawnersFromDB();
		
		logger.info(prefix + "Starting threads...");
		task = getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() { public void run() { onMobUpdateTick(); } }, mobTickInterval, mobTickInterval);
		
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
		RPG_Core.Log(sender, commandLabel, args);
		
		RPG_Player rpg_p = null;
		if (RPG_Core.IsPlayer(sender))
			rpg_p = RPG_Core.GetPlayer(((Player)sender).getName());
		
		if (commandLabel.equalsIgnoreCase("mm"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.mobs"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (args.length < 1)
			{
				RPG_Core.ShowInfoScreen("rpg.mobs", sender);
			}
			else
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("unknown_command", sender));
			}
		}
		
		return true;
	}
	
	private void onMobUpdateTick()
	{
		asyncRunning = true;
		
		
		asyncRunning = false;
	}
	
	// 
	// Mobs
	// 
	public static RPG_MobType GetMobType(int ID)
	{
		if (mobtypes.containsKey(ID))
			return mobtypes.get(ID);
		else
		{
			logger.warning(prefix + "MobType with id " + ID + " does not exist");
			RPG_Core.Log(-1, ID, RPG_LogType.Warning, "MobType", "Does not exist");
			return null;
		}
	}
	
	public static void LoadMobTypesFromDB()
	{
		logger.info(prefix + "Loading Mob types...");
		
		mobtypes.clear();
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			rs = stmt.executeQuery("SELECT ID, NameID, DescriptionID, MobType, Level, Drops FROM " + SQLTablePrefix + "mobs;");
			
			while (rs.next())
			{
				ArrayList<RPG_MobDrop> drops = new ArrayList<RPG_MobDrop>();
				
				if (rs.getString("Drops") != null)
				{
					String[] splits = rs.getString("Drops").split(RPG_Core.GetDBSeperator());
					for (String drop : splits)
					{
						try
						{
							String[] subsplits = drop.split(RPG_Core.GetDBSeperator2());
							
							String[] elements = subsplits[1].split(RPG_Core.GetDBSeperator2());
							byte data = 0;
							if (elements.length > 3)
								data = Byte.parseByte(elements[3]);
							int typeid = Integer.parseInt(elements[0]);
							ItemStack stack = new ItemStack(typeid, Integer.parseInt(elements[1]), Short.parseShort(elements[2]));
							stack.setData(new MaterialData(typeid, data));
							
							drops.add(new RPG_MobDrop(stack, Float.parseFloat(subsplits[0])));
						}
						catch (Exception ex)
						{
							logger.warning(prefix + "  Could not load mobtype " + rs.getInt("ID") + ": '" + rs.getString("Drops") + "' are not valid dropable items");
							continue;
						}
					}
				}
				
				mobtypes.put(rs.getInt("ID"), new RPG_MobType(rs.getInt("ID"), rs.getInt("NameID"), rs.getInt("DescriptionID"), rs.getInt("MobType"), rs.getInt("Level"), drops));
				
				RPG_Core.Log(-1, rs.getInt("ID"), RPG_LogType.Information, "MobTypes", "Loaded");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "Mobs", ex);
		}
		
		logger.info(prefix + "  Loaded " + mobtypes.size() + " Mob(s) from database!");
	}
	
	// 
	// Mob Spawners
	// 
	public static void LoadMobSpawnersFromDB()
	{
		
	}
}

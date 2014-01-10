package rpgCore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;
import net.minecraft.server.v1_7_R1.Packet;

import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.command.ColouredConsoleSender;
import org.bukkit.craftbukkit.v1_7_R1.command.CraftBlockCommandSender;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.getspout.spout.player.SpoutCraftPlayer;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.gui.WidgetType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import rpgBattlefield.RPG_Flag;
import rpgGUI.RPG_InfoScreen;
import rpgGUI.RPG_InfoScreenData;
import rpgGUI.RPG_InfoScreenItem;
import rpgGuild.RPG_Guild;
import rpgInterfaces.RPG_IBattlefieldManager;
import rpgInterfaces.RPG_INpcManager;
import rpgInterfaces.RPG_IQuestManager;
import rpgInterfaces.RPG_IWorldManager;
import rpgNation.RPG_Nation;
import rpgNpc.RPG_Npc;
import rpgOther.RPG_ChatType;
import rpgOther.RPG_EventExecutor;
import rpgOther.RPG_LogType;
import rpgOther.RPG_RestrictedItem;
import rpgOther.RPG_Teleport;
import rpgPlayer.RPG_Player;
import rpgPlayer.RPG_PlayerAction;
import rpgPlayer.RPG_PlayerState;
import rpgQuest.RPG_Quest;
import rpgShop.RPG_Shop;
import rpgTexts.RPG_Language;
import rpgTexts.RPG_Message;
import rpgTexts.RPG_Text;

public class RPG_Core extends JavaPlugin implements Listener
{
	private static String prefix = "[RPG Core] ";
	private static String sqlFilesFolder = "https://api.github.com/repos/MC-Story/MC-Story/contents/SQL%20Scripts/";
	private static String sqlFilesLocation = "https://raw.github.com/MC-Story/MC-Story/master/SQL%20Scripts/";
	
	private static Logger logger = Logger.getLogger("Minecraft");
	private static String txt_seperator = null;
	public static String GetTextSeperator()
	{
		return txt_seperator;
	}
	
	private static Connection conn = null;
	private static String SQLTablePrefix = null;
	private static String db_server = null;
	private static String db_port = null;
	private static String db_name = null;
	private static String db_un = null;
	private static String db_pw = null;
	private static String db_seperator = null;
	private static String db_seperator_2 = null;
	private static String db_seperator_3 = null;
	public static String GetSQLTablePrefix()
	{
		return SQLTablePrefix;
	}
	public static String GetDBSeperator()
	{
		return db_seperator;
	}
	public static String GetDBSeperator2()
	{
		return db_seperator_2;
	}
	public static String GetDBSeperator3()
	{
		return db_seperator_3;
	}
	
	private static boolean spoutEnabled = false;
	private static Permission perm = null;
	private static RPG_EventExecutor rpg_event = new RPG_EventExecutor();
	public static boolean IsSpoutEnabled()
	{
		return spoutEnabled;
	}
	public static RPG_EventExecutor GetEventExecutor()
	{
		return rpg_event;
	}
	
	private static HashMap<Integer, RPG_Player> players = new HashMap<Integer, RPG_Player>();
	private static HashMap<String, Integer> playernames = new HashMap<String, Integer>();
	private static HashMap<Integer, RPG_Nation> nations = new HashMap<Integer, RPG_Nation>();
	private static HashMap<Integer, RPG_Guild> guilds = new HashMap<Integer, RPG_Guild>();
	
	private static HashMap<Integer, RPG_Text> texts = new HashMap<Integer, RPG_Text>();
	private static HashMap<String, RPG_Message> msgs = new HashMap<String, RPG_Message>();
	private static HashMap<Integer, RPG_RestrictedItem> restricteditems = new HashMap<Integer, RPG_RestrictedItem>();
	
	private static HashMap<Integer, RPG_Shop> shops = new HashMap<Integer, RPG_Shop>();
	
	private static HashMap<Integer, RPG_Teleport> teleports = new HashMap<Integer, RPG_Teleport>();
	
	private static HashMap<String, RPG_InfoScreenData> infoScreens = new HashMap<String, RPG_InfoScreenData>();
	
	
	// 
	// Main
	// 
	public RPG_Core()
	{ }
	
	@Override
	public void onEnable()
	{
		logger.info(prefix + "Checking Spout...");
		
		if (getServer().getPluginManager().isPluginEnabled("Spout"))
		{
			logger.info(prefix + "  Spout is ENABLED");
			spoutEnabled = true;
		}
		else
		{
			logger.info(prefix + "  Spout is DISABLED");
			spoutEnabled = false;
		}
		
		logger.info(prefix + "Registering events...");
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(PlayerJoinEvent.class, this, EventPriority.NORMAL, RPG_Core.GetEventExecutor(), this);
		pm.registerEvent(PlayerQuitEvent.class, this, EventPriority.NORMAL, RPG_Core.GetEventExecutor(), this);
		pm.registerEvent(PlayerKickEvent.class, this, EventPriority.NORMAL, RPG_Core.GetEventExecutor(), this);
		if (IsSpoutEnabled())
		{
			pm.registerEvent(ButtonClickEvent.class, this, EventPriority.NORMAL, RPG_Core.GetEventExecutor(), this);
		}
		
		logger.info(prefix + "Adding info screens");
		
		List<File> files = Arrays.asList(this.getDataFolder().getParentFile().listFiles(
			new FilenameFilter() { public boolean accept(File dir, String filename) { return filename.startsWith("RPG") && filename.endsWith(".jar"); }}));
		ArrayList<String> fileNames = new ArrayList<String>(files.size());
		for (File f : files)
			fileNames.add(FilenameUtils.getBaseName(f.getName()));
		
		ArrayList<RPG_InfoScreenItem> items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("lang", 		"Changes the language of the texts", 		"rpg.lang",			true));
		items.add(new RPG_InfoScreenItem("textcode",	"Displays the different text codes", 		"rpg.textcode"));
		items.add(new RPG_InfoScreenItem("version", 	"Displays the versions of the RPG plugins", "rpg.version"));
		items.add(new RPG_InfoScreenItem("", 			"", 										""));
		if (fileNames.contains("RPG_BattlefieldManager"))
			items.add(new RPG_InfoScreenItem("bfm", 	"The Battlefield Manager",	"rpg.battlefield",	true));
		if (fileNames.contains("RPG_MobManager"))
			items.add(new RPG_InfoScreenItem("mm", 		"The Mob Manager", 			"rpg.mobs", 		true));
		if (fileNames.contains("RPG_NPCManager"))
			items.add(new RPG_InfoScreenItem("npc", 	"The NPC Manager", 			"rpg.npc", 			true));
		if (fileNames.contains("RPG_QuestManager"))
			items.add(new RPG_InfoScreenItem("quest", 	"The Quest Manager", 		"rpg.quest", 		true));
		if (fileNames.contains("RPG_UpdateManager"))
			items.add(new RPG_InfoScreenItem("um", 		"The Update Manager", 		"rpg.update", 		true));
		if (fileNames.contains("RPG_WebManager"))
			items.add(new RPG_InfoScreenItem("web", 	"The Web Manager", 			"rpg.web", 			true));
		if (fileNames.contains("RPG_WorldManager"))
			items.add(new RPG_InfoScreenItem("wm", 		"The World Manager", 		"rpg.world", 		true));
		RPG_Core.AddInfoScreen("rpg", "RPG Plugin", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Changes the language of the texts", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /rpg lang [Lang]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [Lang]", "The language to use for the texts", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "    DE", "Set the language to German", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "    EN", "Set the language to English", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "    FR", "Set the language to French", ""));
		RPG_Core.AddInfoScreen("rpg.lang", "RPG Language", items);
		
		logger.info(prefix + "Loading config...");
		
		getConfig().addDefault("sqlFilesFolder", "https://api.github.com/repos/MC-Story/MC-Story/contents/SQL%20Scripts/");
		getConfig().addDefault("sqlFilesLocation", "https://raw.github.com/MC-Story/MC-Story/master/SQL%20Scripts/");
		getConfig().addDefault("db_server", "localhost");
		getConfig().addDefault("db_port", 3306);
		getConfig().addDefault("db_name", "minecraft");
		getConfig().addDefault("db_username", "root");
		getConfig().addDefault("db_password", "");
		getConfig().addDefault("db_tbl_prefix", "rpg_");
		getConfig().addDefault("db_seperator", "|");
		getConfig().addDefault("db_seperator_2", ";");
		getConfig().addDefault("db_seperator_3", ",");
		getConfig().addDefault("db_autoCreateMissing", false);
		getConfig().addDefault("txt_seperator", "--------------------");
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		sqlFilesFolder = getConfig().getString("sqlFilesFolder");
		sqlFilesLocation = getConfig().getString("sqlFilesLocation");
		db_server = getConfig().getString("db_server");
		db_port = getConfig().getString("db_port");
		db_name = getConfig().getString("db_name");
		db_un = getConfig().getString("db_username");
		db_pw = getConfig().getString("db_password");
		SQLTablePrefix = getConfig().getString("db_tbl_prefix");
		db_seperator = getConfig().getString("db_seperator");
		db_seperator_2 = getConfig().getString("db_seperator_2");
		db_seperator_3 = getConfig().getString("db_seperator_3");
		txt_seperator = getConfig().getString("txt_seperator");
		
		logger.info(prefix + "  SQL Table Prefix: " + SQLTablePrefix);
		logger.info(prefix + "  DB Seperator 1: " + db_seperator);
		logger.info(prefix + "  DB Seperator 2: " + db_seperator_2);
		logger.info(prefix + "  DB Seperator 3: " + db_seperator_3);
		
		if (OpenDatabaseConnection())
		{
			CheckDatabase(getConfig().getBoolean("db_autoCreateMissing"));
			
			Log(RPG_LogType.Information, "Database", "Connected to database");
			
			LoadMessagesFromDB();
			
			LoadTextsFromDB();
			
			LoadNationsFromDB();
			
			LoadGuildsFromDB();
			
			LoadShopsFromDB();
			
			LoadRestrictedItemsFromDB();
			
			logger.info(prefix + "Retrieving vault instance...");
			
			RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
	        if (permissionProvider != null)
	            perm = permissionProvider.getProvider();
	        
			PluginDescriptionFile pdf = this.getDescription();
			logger.info(prefix +  pdf.getName() + " version " + pdf.getVersion() + " is enabled");
			Log(RPG_LogType.Information, "Start", pdf.getName() + " version " + pdf.getVersion() + " enabled");
		}
		else
			getServer().getPluginManager().disablePlugin(this);
	}
	@Override
	public void onDisable()
	{
		PluginDescriptionFile pdf = this.getDescription();
		
		if (conn != null)
		{			
			Log(RPG_LogType.Information, "Stop", pdf.getName() + " version " + pdf.getVersion() + " disabled");
			
			try
			{
				conn.close();
			}
			catch (SQLException e)
			{ e.printStackTrace(); }
		}
		
		logger.info(prefix + pdf.getName() + " is disabled");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		RPG_Player rpg_p = null;
		if (IsPlayer(sender))
			rpg_p = GetPlayer(((Player)sender).getName());
		
		if (commandLabel.equalsIgnoreCase("rpg"))
		{
			Log(sender, commandLabel, args);
			
			if (!RPG_Core.HasPermission(sender, "rpg"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (args.length == 0)
			{
				ShowInfoScreen("rpg", sender);
			}
			else
			{
				if (args[0].equalsIgnoreCase("lang"))
				{
					if (!perm.has(sender, "rpg.lang"))
					{
						sender.sendMessage(GetFormattedMessage("no_perm", sender));
						Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (args.length < 2)
					{
						ShowInfoScreen("rpg.lang", sender);
					}
					else
					{
						if (!IsPlayer(sender))
						{
							sender.sendMessage(ChatColor.RED + "Only players can change their language");
							return false;
						}
						
						if (args[1].equalsIgnoreCase("DE"))
						{
							rpg_p.SetLanguage(RPG_Language.DE);
							sender.sendMessage(ChatColor.GREEN + "You have set your language to " + ChatColor.BLUE + "German");
						}
						else if (args[1].equalsIgnoreCase("EN"))
						{
							rpg_p.SetLanguage(RPG_Language.EN);
							sender.sendMessage(ChatColor.GREEN + "You have set your language to " + ChatColor.BLUE + "English");
						}
						else if (args[1].equalsIgnoreCase("FR"))
						{
							rpg_p.SetLanguage(RPG_Language.FR);
							sender.sendMessage(ChatColor.GREEN + "You have set your language to " + ChatColor.BLUE + "French");
						}
					}
				}
				else if (args[0].equalsIgnoreCase("textcode") || args[0].equalsIgnoreCase("textcodes"))
				{
					if (!perm.has(sender, "rpg.textcode"))
					{
						sender.sendMessage(GetFormattedMessage("no_perm", sender));
						Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					sender.sendMessage(ChatColor.GOLD + GetTextSeperator());
					sender.sendMessage(ChatColor.BLUE + "RPG Textcodes");
					sender.sendMessage(ChatColor.GOLD + GetTextSeperator());
					
					sender.sendMessage(ChatColor.BLUE + "%npc_name     " + ChatColor.WHITE + "The NPC's name");
					
					sender.sendMessage(ChatColor.BLUE + "%p_name     " + ChatColor.WHITE + "The player's name");
					sender.sendMessage(ChatColor.BLUE + "%p_lvl     " + ChatColor.WHITE + "The player's level");
					sender.sendMessage(ChatColor.BLUE + "%p_exp     " + ChatColor.WHITE + "The player's current experience");
					sender.sendMessage(ChatColor.BLUE + "%p_money     " + ChatColor.WHITE + "The player's money");
					
					sender.sendMessage(ChatColor.BLUE + "%q_name     " + ChatColor.WHITE + "The name of the quest");
					sender.sendMessage(ChatColor.BLUE + "%q_dispname     " + ChatColor.WHITE + "The display name of the quest");
					sender.sendMessage(ChatColor.BLUE + "%q_descr     " + ChatColor.WHITE + "The description of the quest");
					sender.sendMessage(ChatColor.BLUE + "%q_rewardexp     " + ChatColor.WHITE + "The amount of exp rewarded for completing the quest");
					sender.sendMessage(ChatColor.BLUE + "%q_rewardmoney     " + ChatColor.WHITE + "The amount of money rewarded for completing the quest");
					sender.sendMessage(ChatColor.BLUE + "%q_npc_start     " + ChatColor.WHITE + "The name of the start NPC");
					sender.sendMessage(ChatColor.BLUE + "%q_npc_end     " + ChatColor.WHITE + "The name of the end NPC");
					
					sender.sendMessage(ChatColor.BLUE + "%f_name     " + ChatColor.WHITE + "The name of the flag");
					sender.sendMessage(ChatColor.BLUE + "%f_nation     " + ChatColor.WHITE + "The name of the nation the flag currently belongs to");
					sender.sendMessage(ChatColor.BLUE + "%f_captime     " + ChatColor.WHITE + "The amount of time needed to capture the flag");
					sender.sendMessage(ChatColor.BLUE + "%f_capradius     " + ChatColor.WHITE + "The radius within which the flag can be captured");
					sender.sendMessage(ChatColor.BLUE + "%f_capprogress     " + ChatColor.WHITE + "The current capturing progress of the flag");
					
					sender.sendMessage(ChatColor.BLUE + "%ri_minlvl     " + ChatColor.WHITE + "The minimum level required for the restricted item");
					
					sender.sendMessage(ChatColor.GOLD + GetTextSeperator());
				}
				else if (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("versions"))
				{
					if (!perm.has(sender, "rpg.version"))
					{
						sender.sendMessage(GetFormattedMessage("no_perm", sender));
						Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					sender.sendMessage(ChatColor.GOLD + GetTextSeperator());
					sender.sendMessage(ChatColor.BLUE + "RPG Plugins versions");
					sender.sendMessage(ChatColor.GOLD + GetTextSeperator());
					
					for (Plugin p : getServer().getPluginManager().getPlugins())
					{
						if (!p.getName().startsWith("RPG"))
							continue;
						
						sender.sendMessage(ChatColor.BLUE + "  " + p.getDescription().getName() + " " + ChatColor.WHITE + p.getDescription().getVersion());
					}
					
					sender.sendMessage(ChatColor.GOLD + GetTextSeperator());
				}
				else if (args[0].equalsIgnoreCase("nation"))
				{
					Statement stmt = null;
					
					if (args.length == 1)
					{
						rpg_p.SendMessage(ChatColor.BLUE + "Nations:");
						
						for (RPG_Nation n : nations.values())
							rpg_p.SendMessage(ChatColor.GOLD + "  " + n.GetID() + " " + ChatColor.WHITE + n.GetDisplayName(rpg_p.GetLanguage()) + " (" + n.GetName() + ")");
					}
					else if (args[1].equalsIgnoreCase("clear"))
					{
					    try
					    {
							stmt = RPG_Core.GetDatabaseStatement();
							stmt.executeUpdate("UPDATE " + RPG_Core.SQLTablePrefix + "players SET NationID = '-1' WHERE Username = '" + rpg_p.GetUsername() + "';");
							
							RPG_Core.ReloadPlayer(rpg_p.GetID());
							
					    	rpg_p.SendMessage(ChatColor.GREEN + "Your nation info has been cleared!");
							sender.sendMessage(ChatColor.RED + "DEBUG: NOT IMPLEMENTED YET!");
					    }
					    catch (Exception ex)
					    {
							ex.printStackTrace();
					    }
					}
					else
					{
						if (rpg_p.GetNationId() > -1)
						{
					    	rpg_p.SendMessage(ChatColor.YELLOW + "You have already joined a nation!");
							return false;
						}
						
						boolean joined = false;
						
						for (RPG_Nation n : nations.values())
						{
							int id = -1;
							try
							{
								id = Integer.parseInt(args[1]);
							}
							catch (NumberFormatException ex)
							{ }
							
							if (args[0].equalsIgnoreCase(n.GetName()) || id == n.GetID())
							{
								stmt = null;
							    ResultSet rs = null;
							    
							    try
							    {
									stmt = RPG_Core.GetDatabaseStatement();
								    rs = stmt.executeQuery("SELECT * FROM " + RPG_Core.SQLTablePrefix + "nations WHERE ID = '" + n.GetID() + "';");
								    
								    if (rs.last())
								    {
								    	getServer().broadcastMessage(ChatColor.BLUE + rpg_p.GetUsername() + ChatColor.GREEN + " has joined " + ChatColor.BLUE + n.GetDisplayName(rpg_p.GetLanguage()));
								    	rpg_p.SendMessage(ChatColor.GREEN + "You have joined " + ChatColor.BLUE + n.GetDisplayName(rpg_p.GetLanguage()) + ChatColor.GREEN + " !");
								    	
										stmt = RPG_Core.GetDatabaseStatement();
										stmt.executeUpdate("UPDATE " + RPG_Core.SQLTablePrefix + "players SET NationID = '" + n.GetID() + "' WHERE Username = '" + rpg_p.GetUsername() + "';");
										
										RPG_Core.ReloadPlayer(rpg_p.GetID());
								    }
								    else
								    {
								    	rpg_p.SendMessage(ChatColor.RED + "That nation does not exist!");
								    }
									sender.sendMessage(ChatColor.RED + "DEBUG: NOT IMPLEMENTED YET!");
							    }
							    catch (Exception ex)
							    {
									ex.printStackTrace();
							    }
							    
							    joined = true;
							    
								break;
							}
						}
						
						if (!joined)
						{
					    	rpg_p.SendMessage(ChatColor.RED + "That nation does not exist!");
						}
					}
				}
				else if (args[0].equalsIgnoreCase("bfm"))
				{
					String[] newArgs = new String[args.length - 1];
					for (int i = 0; i < newArgs.length; i++)
						newArgs[i] = args[i + 1];
					
					getServer().getPluginManager().getPlugin("RPG Battlefield Manager").onCommand(sender, cmd, args[0], newArgs);
				}
				else if (args[0].equalsIgnoreCase("mm"))
				{
					String[] newArgs = new String[args.length - 1];
					for (int i = 0; i < newArgs.length; i++)
						newArgs[i] = args[i + 1];
					
					getServer().getPluginManager().getPlugin("RPG Mob Manager").onCommand(sender, cmd, args[0], newArgs);
				}
				else if (args[0].equalsIgnoreCase("npc"))
				{
					String[] newArgs = new String[args.length - 1];
					for (int i = 0; i < newArgs.length; i++)
						newArgs[i] = args[i + 1];
					
					getServer().getPluginManager().getPlugin("RPG NPC Manager").onCommand(sender, cmd, args[0], newArgs);
				}
				else if (args[0].equalsIgnoreCase("quest"))
				{
					String[] newArgs = new String[args.length - 1];
					for (int i = 0; i < newArgs.length; i++)
						newArgs[i] = args[i + 1];
					
					getServer().getPluginManager().getPlugin("RPG Quest Manager").onCommand(sender, cmd, args[0], newArgs);
				}
				else if (args[0].equalsIgnoreCase("um"))
				{
					String[] newArgs = new String[args.length - 1];
					for (int i = 0; i < newArgs.length; i++)
						newArgs[i] = args[i + 1];
					
					getServer().getPluginManager().getPlugin("RPG Update Manager").onCommand(sender, cmd, args[0], newArgs);
				}
				else if (args[0].equalsIgnoreCase("web"))
				{
					String[] newArgs = new String[args.length - 1];
					for (int i = 0; i < newArgs.length; i++)
						newArgs[i] = args[i + 1];
					
					getServer().getPluginManager().getPlugin("RPG Web Manager").onCommand(sender, cmd, args[0], newArgs);
				}
				else if (args[0].equalsIgnoreCase("world"))
				{
					String[] newArgs = new String[args.length - 1];
					for (int i = 0; i < newArgs.length; i++)
						newArgs[i] = args[i + 1];
					
					getServer().getPluginManager().getPlugin("RPG World Manager").onCommand(sender, cmd, args[0], newArgs);
				}
				else
					sender.sendMessage(ChatColor.RED + "Unknown command");
			}
		}
		
		return true;
	}
	
	private boolean OpenDatabaseConnection()
	{
		logger.info(prefix + "Connecting to database...");
		
		try
		{
			conn = DriverManager.getConnection("jdbc:mysql://" + db_server + ":" + db_port + "/" + db_name + "?user=" + db_un + "&password=" + db_pw);
			
			logger.info(prefix + "  Connected to database!");
			
			return true;
		}
		catch (Exception ex)
		{
			logger.severe(prefix + "  Could not connect to database!");
			conn = null;
			
			return false;
		}
	}
	private void CheckDatabase(boolean autoCreateMissing)
	{
		logger.info(prefix + "Checking database structure...");
		if (!autoCreateMissing)
			logger.info(prefix + "  Automaitc creation for missing objects is OFF");
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = conn.createStatement();
			JSONParser p = new JSONParser();
			
			logger.info(prefix + "  Checking procedures...");
			int oks = 0;
			JSONArray list = (JSONArray)p.parse(GetStringFromWebsite(sqlFilesFolder + "Procedures/"));
			for (int i = 0; i < list.size(); i++)
			{
				JSONObject obj = (JSONObject)list.get(i);
				String name = SQLTablePrefix + FilenameUtils.removeExtension(obj.get("name").toString());
				
				try
				{
					rs = stmt.executeQuery("SHOW PROCEDURE STATUS LIKE '" + name + "'");
					if (!rs.first())
					{
						if (autoCreateMissing)
						{
							logger.info(prefix + "    Creating '" + name + "'...");
							
							String query = GetStringFromWebsite(sqlFilesLocation + "Procedures/" + obj.get("name"));
							stmt.executeUpdate(query);
							oks++;
						}
					}
					else
						oks++;
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					logger.warning(prefix + "    Could not check '" + name + "'");
				}
			}
			logger.info(prefix + "  " + oks + "/" + list.size() + " procedures are OK!");
			
			stmt.clearBatch();
			
			logger.info(prefix + "  Checking tables...");
			oks = 0;
			list = (JSONArray)p.parse(GetStringFromWebsite(sqlFilesFolder + "Tables/"));
			for (int i = 0; i < list.size(); i++)
			{
				JSONObject obj = (JSONObject)list.get(i);
				String name = SQLTablePrefix + FilenameUtils.removeExtension(obj.get("name").toString());
				
				try
				{
					rs = stmt.executeQuery("SHOW TABLES LIKE '" + name + "'");
					if (!rs.first())
					{
						if (autoCreateMissing)
						{
							logger.info(prefix + "    Creating '" + name + "'...");
							
							String[] queries = GetStringFromWebsite(sqlFilesLocation + "Tables/" + obj.get("name")).split("(\\s*;\\s*)+");
							
							if (queries.length == 1)
								stmt.executeUpdate(queries[0]);
							else
							{
								for (String query : queries)
								{
									if (!query.trim().equalsIgnoreCase(""))
										stmt.addBatch(query);
								}
								stmt.executeBatch();
							}
							oks++;
						}
					}
					else
						oks++;
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					logger.warning(prefix + "    Could not check '" + name + "'");
				}
			}
			logger.info(prefix + "  " + oks + "/" + list.size() + " tables are OK!");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			logger.warning(prefix + "  COULD NOT CHECK DATABASE STRUCTURE!");
			return;
		}
	}
	public static Statement GetDatabaseStatement()
	{
		try
		{			
			return conn.createStatement();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			
			return null;
		}
	}
	
	public static String GetStringFromWebsite(String Website)
	{
		try
		{
			URL url = new URL(Website);
			InputStream is = url.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			
			StringBuilder builder = new StringBuilder();
			String line = "";
			while ((line = br.readLine()) != null)
				builder.append(line + System.getProperty("line.separator"));
			
			return builder.toString();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Log(RPG_LogType.Error, "Website", ex);
			return "";
		}
	}
	
	// 
	// Plugins
	// 
	public static RPG_IBattlefieldManager GetBattlefieldManager()
	{
		return (RPG_IBattlefieldManager)Bukkit.getPluginManager().getPlugin("RPG Battlefield Manager");
	}
	public static RPG_INpcManager GetNpcManager()
	{
		return (RPG_INpcManager)Bukkit.getPluginManager().getPlugin("RPG NPC Manager");
	}
	public static RPG_IQuestManager GetQuestManager()
	{
		return (RPG_IQuestManager)Bukkit.getPluginManager().getPlugin("RPG Quest Manager");
	}
	public static RPG_IWorldManager GetWorldManager()
	{
		return (RPG_IWorldManager)Bukkit.getPluginManager().getPlugin("RPG World Manager");
	}
	
	// 
	// Events
	// 
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		Statement stmt = null;
		ResultSet rs = null;
		String name = e.getPlayer().getName();
		int id = -1;
		Player p = e.getPlayer();
		
		try
		{			
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT " + SQLTablePrefix + "players.ID, Username, NationId, GuildId, Level, Exp, Money, Language FROM " + SQLTablePrefix + "players " +
					"WHERE Username = '" + name + "';");
			
			if (rs.last())
			{
				id = rs.getInt(SQLTablePrefix + "players.ID");
				RPG_Language lang = RPG_Language.EN;
				
				if (rs.getString("Language").equalsIgnoreCase("EN"))
					lang = RPG_Language.EN;
				else if (rs.getString("Language").equalsIgnoreCase("DE"))
					lang = RPG_Language.DE;
				else if (rs.getString("Language").equalsIgnoreCase("FR"))
					lang = RPG_Language.FR;
				
				playernames.put(name, id);
				players.put(id, new RPG_Player(id, rs.getString("Username"), p, rs.getInt("NationId"), rs.getInt("GuildId"), rs.getInt("Level"), 
						rs.getInt("Exp"), rs.getInt("Money"), lang,	RPG_PlayerState.Online));
			}
			else
			{
				Log(RPG_LogType.Information, "PlayerJoin", e.getPlayer().getName() + " is new on the server");
				AddPlayerToDB(e.getPlayer());
			}
			
			/*for (RPG_NPC rpg_npc : npcs.values())
			{
				SpoutManager.getPlayerFromId(rpg_npc.GetUUID()).setTitleFor(SpoutManager.getPlayer(p), "Test");
			}*/
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Log(RPG_LogType.Error, "PlayerJoin", ex);
		}
		
	    try
	    {
			stmt = conn.createStatement();
		    stmt.executeUpdate("UPDATE " + SQLTablePrefix + "players SET State = '1', IP = '" + p.getAddress().toString() + "' WHERE Username = '" + e.getPlayer().getName() + "';");
	    }
	    catch (Exception ex)
	    {
	    	ex.printStackTrace();
			Log(RPG_LogType.Error, "PlayerJoin", ex);
	    }
	    
		p.sendMessage(ChatColor.GOLD + GetTextSeperator());
	    p.sendMessage(ChatColor.GREEN + "Welcome to the " + ChatColor.BLUE + "Terra Infinitas " + ChatColor.GREEN + "Server");
	    p.sendMessage(ChatColor.WHITE + "Don't forget to visit: www.mc-infinitas.com");
		p.sendMessage(ChatColor.GOLD + GetTextSeperator());
		
	    Log(id, -1, RPG_LogType.Information, "PlayerJoin", e.getPlayer().getName());
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		Statement stmt = null;
		String name = e.getPlayer().getName();
		RPG_Player rpg_p = RPG_Core.GetPlayer(name);
		
		if (playernames.containsKey(name))
		{
			if (players.containsKey(playernames.get(name)))
				players.remove(playernames.get(name));
			playernames.remove(name);
		}
		
	    try
	    {
	    	if (conn == null)
	    		logger.info("Conn is null in onPlayerQuit");
	    	
			stmt = conn.createStatement();
		    stmt.executeUpdate("UPDATE " + SQLTablePrefix + "players SET State = 0, Exp = " + rpg_p.GetExp() + ", Money = " + rpg_p.GetMoney() + 
		    		", Language = '" + rpg_p.GetLanguage() + "' WHERE Username = '" + e.getPlayer().getName() + "';");
	    }
	    catch (Exception ex)
	    {
	    	ex.printStackTrace();
			Log(rpg_p.GetID(), -1, RPG_LogType.Error, "PlayerQuit", ex);
	    }
	    
	    Log(rpg_p.GetID(), -1, RPG_LogType.Information, "PlayerQuit", rpg_p.GetUsername());
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerKick(PlayerKickEvent e)
	{
		Statement stmt = null;
		String name = e.getPlayer().getName();
		RPG_Player rpg_p = RPG_Core.GetPlayer(name);
		
		if (playernames.containsKey(name))
		{
			if (players.containsKey(playernames.get(name)))
				players.remove(playernames.get(name));
			playernames.remove(name);
		}
		
	    try
	    {
			stmt = conn.createStatement();
		    stmt.executeUpdate("UPDATE " + SQLTablePrefix + "players SET State = 0, Exp = " + rpg_p.GetExp() + ", Money = " + rpg_p.GetMoney() + 
		    		", Language = '" + rpg_p.GetLanguage() + "' WHERE Username = '" + e.getPlayer().getName() + "';");
	    }
	    catch (Exception ex)
	    {
	    	ex.printStackTrace();
			Log(rpg_p.GetID(), -1, RPG_LogType.Error, "PlayerKick", ex);
	    }
	    
	    Log(rpg_p.GetID(), -1, RPG_LogType.Information, "PlayerKick", rpg_p.GetUsername());
	}
	
	@EventHandler
	protected void onButtonClick(ButtonClickEvent e)
	{		
		if (e.getScreen().getClass() == RPG_InfoScreen.class)
		{
			RPG_InfoScreen scr_info = (RPG_InfoScreen)e.getScreen();
			
			if (e.getButton().getType() == WidgetType.Button)
			{
				String link = scr_info.GetLink(e.getButton());
				String navLink = scr_info.GetNavLink(e.getButton());
				
				if (scr_info.IsCloseButton(e.getButton()))
				{
					scr_info.Hide();
				}
				else if (navLink != null)
				{
					scr_info.Hide();
					ShowInfoScreen(navLink, e.getPlayer());
				}
				else if (link != null)
				{
					scr_info.Hide();
					ShowInfoScreen(link, e.getPlayer());
				}
			}
		}
	}
	
	// 
	// Logging
	// 
	public static void Log(RPG_LogType Type, String Category, String Event)
	{
		Log(-1, -1, Type, Category, Event);
	}
	public static void Log(RPG_LogType Type, String Category, Exception Ex)
	{
		if (Ex.getStackTrace().length > 0)
		{
			StackTraceElement e = Ex.getStackTrace()[0];
			Log(-1, -1, Type, Category, "Error in file " + e.getFileName() + " in method " + e.getMethodName() + " at line " + e.getLineNumber() + ": " + Ex.getMessage());
		}
		else
			Log(-1, -1, Type, Category, "Error (has not stack trace): " + Ex.toString());
	}
	public static void Log(int SourceID, int TargetID, RPG_LogType Type, String Category, Exception Ex)
	{
		StackTraceElement e = Ex.getStackTrace()[0];
		Log(SourceID, TargetID, Type, Category, "Error in file " + e.getFileName() + " in method " + e.getMethodName() + " at line " + e.getLineNumber());
	}
	public static void Log(int SourceID, int TargetID, RPG_LogType Type, String Category, String Event)
	{
		Statement stmt;
		
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate("INSERT INTO " + SQLTablePrefix + "log (SourceID, TargetID, Type, Category, Event) VALUES (" + SourceID + ", " + TargetID + ", '" + Type + "', '" + 
					Category + "', '" + Event + "');");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	public static void Log(CommandSender Sender, String CommandLabel, String[] Args)
	{
		String line = "";
		for (String a : Args)
			line += a + " ";
		
		if (Sender.getClass() == CraftBlockCommandSender.class)
			Log(-3, -1, RPG_LogType.Information, "Command", CommandLabel + " " + line);
		else if (Sender.getClass() == ColouredConsoleSender.class)
			Log(-2, -1, RPG_LogType.Information, "Command", CommandLabel + " " + line);
		else
		{
			RPG_Player rpg_p = GetPlayer(((Player)Sender).getName());
			Log(rpg_p.GetID(), -1, RPG_LogType.Information, "Command", CommandLabel + " " + line);
		}
	}
	public static void Log(CommandSender Sender, RPG_LogType Type, String Category, String Event)
	{		
		if (Sender.getClass() != ColouredConsoleSender.class)
		{
			RPG_Player rpg_p = GetPlayer(((Player)Sender).getName());
			Log(rpg_p.GetID(), -1, Type, Category, Event);
		}
		else
			Log(-2, -1, Type, Category, Event);
	}
	
	// 
	// Permissions
	// 
	public static boolean HasPermission(Player P, String Permission)
	{
		return perm.has(P, Permission);
	}
	public static boolean HasPermission(RPG_Player p, String Permission)
	{
		return perm.has(p.GetPlayer(), Permission);
	}
	public static boolean HasPermission(CommandSender Sender, String Permission)
	{
		return perm.has(Sender, Permission);
	}
	
	// 
	// Info Screens
	// 
	public static void ShowInfoScreen(String Name, CommandSender Sender)
	{
		if (!infoScreens.containsKey(Name))
		{
			logger.warning(prefix + "Screen with name '" + Name + "' does not exist");
			Log(Sender, RPG_LogType.Warning, "InfoScreen", "Screen not found: " + Name);
			return;
		}
			
		RPG_InfoScreenData data = infoScreens.get(Name);
		
		if (IsPlayer(Sender) && IsSpoutPlayer((Player)Sender))
		{
			RPG_InfoScreen scr = new RPG_InfoScreen(Bukkit.getPluginManager().getPlugin("RPG Core"), RPG_Core.GetPlayer(Sender.getName()), Name, data.Title, data.Items);
			scr.Show();
		}
		else
		{
			Sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
			Sender.sendMessage(ChatColor.BLUE + data.Title);
			Sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
			for (RPG_InfoScreenItem item : data.Items)
			{
				if (HasPermission(Sender, item.Perms))
					Sender.sendMessage(ChatColor.GOLD + " " + item.Name + "   " + ChatColor.WHITE + item.Description);
			}
			Sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
		}
	}
	public static void AddInfoScreen(String Name, String Title, ArrayList<RPG_InfoScreenItem> Items)
	{
		if (!infoScreens.containsKey(Name))
			infoScreens.put(Name, new RPG_InfoScreenData(Title, Items));
	}
	
	// 
	// Players
	// 
	public static boolean IsPlayer(CommandSender Sender)
	{
		if (Sender.getClass() == CraftPlayer.class || (spoutEnabled && Sender.getClass() == SpoutCraftPlayer.class))
			return true;
		
		return false;
	}
	public static boolean IsSpoutPlayer(Player P)
	{
		if (!spoutEnabled)
			return false;
		
		return SpoutManager.getPlayer(P).isSpoutCraftEnabled();
	}
	
	public static RPG_Player GetPlayer(int ID)
	{
		if (players.containsKey(ID))
			return players.get(ID);
		else
		{
			ReloadPlayer(ID);
			
			if (players.containsKey(ID))
				return players.get(ID);
			else
			{
				logger.warning(prefix + "Player with id " + ID + " does not exist");
				Log(ID, -1, RPG_LogType.Warning, "Player", "Does not exist");
				return null;
			}
		}
	}
	public static RPG_Player GetPlayer(String Name)
	{
		if (playernames.containsKey(Name))
			return GetPlayer(playernames.get(Name));
		else
		{			
			ReloadPlayer(Name);
			
			if (playernames.containsKey(Name))
				return GetPlayer(playernames.get(Name));
			else
			{
				logger.warning(prefix + "Player with name " + Name + " does not exist");
				Log(RPG_LogType.Warning, "Player", "Player with name " + Name + " does not exist");
				return null;
			}
		}
	}
	public static void ReloadPlayer(int ID)
	{
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT " + SQLTablePrefix + "players.ID, Username, NationId, GuildId, Level, Exp, Money, Language, State FROM " + SQLTablePrefix + "players " +
					"WHERE " + SQLTablePrefix + "players.ID = " + ID + ";");
			
			if (rs.last())
			{
				int id = rs.getInt(SQLTablePrefix + "players.ID");
				RPG_PlayerState state = GetPlayerState(rs.getInt("State"));
				
				RPG_Language lang = RPG_Language.EN;
				
				if (rs.getString("Language") == "EN")
					lang = RPG_Language.EN;
				else if (rs.getString("Language") == "DE")
					lang = RPG_Language.DE;
				else if (rs.getString("Language") == "FR")
					lang = RPG_Language.FR;
				
				playernames.remove(rs.getString(2));
				players.remove(id);
				
				playernames.put(rs.getString(2), id);
				players.put(id, new RPG_Player(id, rs.getString("Username"), Bukkit.getServer().getPlayer(rs.getString("Username")), rs.getInt("NationId"), rs.getInt("GuildId"), rs.getInt("Level"), 
						rs.getInt("Exp"), rs.getInt("Money"), lang,	state));
				
				Log(id, -1, RPG_LogType.Information, "Player", "Reloaded");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Log(RPG_LogType.Error, "Player", ex);
		}
	}
	public static void ReloadPlayer(String Name)
	{
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT " + SQLTablePrefix + "players.ID, Username, NationId, GuildId, Level, Exp, Money, Language, State FROM " + SQLTablePrefix + "players " +
					"WHERE Username = '" + Name + "';");
			
			if (rs.last())
			{
				int id = rs.getInt(SQLTablePrefix + "players.ID");
				RPG_PlayerState state = GetPlayerState(rs.getInt("State"));
				
				RPG_Language lang = RPG_Language.EN;
				
				if (rs.getString("Language") == "EN")
					lang = RPG_Language.EN;
				else if (rs.getString("Language") == "DE")
					lang = RPG_Language.DE;
				else if (rs.getString("Language") == "FR")
					lang = RPG_Language.FR;
				
				playernames.remove(rs.getString("Username"));
				players.remove(id);
				
				playernames.put(rs.getString("Username"), id);
				players.put(id, new RPG_Player(id, rs.getString("Username"), Bukkit.getServer().getPlayer(rs.getString("Username")), rs.getInt("NationId"), rs.getInt("GuildId"), rs.getInt("Level"), 
						rs.getInt("Exp"), rs.getInt("Money"), lang,	state));
				
				Log(id, -1, RPG_LogType.Information, "Player", "Reloaded");
			}
			else
				AddPlayerToDB(Bukkit.getServer().getPlayer(Name));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Log(RPG_LogType.Error, "Player", ex);
		}
	}
	public static void ReloadPlayers()
	{
		for (RPG_Player p : players.values())
			ReloadPlayer(p.GetID());
	}
	public static void AddPlayerToDB(Player P)
	{
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = conn.createStatement();
		    stmt.executeUpdate("INSERT INTO " + SQLTablePrefix + "players (Username, NationId, GuildId, Level, Exp, Money, Language, State) VALUES ('" + P.getName() + "',-1,-1,0,0,0,'EN',1);");
		    
			rs = stmt.executeQuery("SELECT " + SQLTablePrefix + "players.ID, Username, NationId, GuildId, Level, Exp, Money, Language FROM " + SQLTablePrefix + "players " +
					"WHERE Username = '" + P.getName() + "';");
		    
			int id = -1;
			if (rs.last())
			{
				id = rs.getInt(SQLTablePrefix + "players.ID");
				
				playernames.put(P.getName(), id);
				players.put(id, new RPG_Player(id, rs.getString("Username"), P, rs.getInt("NationId"), rs.getInt("GuildId"), rs.getInt("Level"), 
						rs.getInt("Exp"), rs.getInt("Money"), RPG_Language.EN, RPG_PlayerState.Online));
				
				GetQuestManager().LoadQuestProgressesForPlayer(id);
			}
			else
			{
				Log(id, -1, RPG_LogType.Error, "PlayerAdd", "Could not load the newly created database entry");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Log(RPG_LogType.Error, "PlayerAdd", ex);
		}
	}
	
	public static ArrayList<RPG_Player> GetPlayersForPartialName(String PartName)
	{
		String name = PartName.toLowerCase();
		ArrayList<RPG_Player> pls = new ArrayList<RPG_Player>();
		
		for (RPG_Player p : players.values())
		{
			if (p.GetUsername().equalsIgnoreCase(name))
			{
				pls.clear();
				pls.add(p);
				break;
			}
			else if (p.GetUsername().toLowerCase().startsWith(name))
				pls.add(p);
		}
		
		return pls;
	}
	@SuppressWarnings("unchecked")
	public static Collection<RPG_Player> GetOnlinePlayers()
	{
		return ((HashMap<Integer, RPG_Player>)players.clone()).values();
	}
	
	public static RPG_PlayerState GetPlayerState(int State)
	{		
		if (State == 1)
			return RPG_PlayerState.Online;
		else if (State == 2)
			return RPG_PlayerState.Banned;
		
		return RPG_PlayerState.None;
	}
	
	public static void PlayerStartsQuest(int PlayerID, int QuestID)
	{
		Statement stmt = null;
		
	    try
	    {
			stmt = conn.createStatement();
			stmt.executeUpdate("INSERT INTO " + SQLTablePrefix + "questprogress (QuestID, PlayerID, Completed) VALUES (" + QuestID + ", " + PlayerID + ", 0);");
			
			Log(PlayerID, QuestID, RPG_LogType.Information, "PlayerQuest", "Started");
	    }
	    catch (Exception ex)
	    {
	    	ex.printStackTrace();
			Log(RPG_LogType.Error, "PlayerQuest", ex);
	    }
	    
	    GetQuestManager().LoadQuestProgressesForPlayer(PlayerID);
	}
	public static void PlayerEndsQuest(int PlayerID, int QuestID)
	{
		Statement stmt = null;
		
	    try
	    {
			stmt = conn.createStatement();
			stmt.executeUpdate("UPDATE " + SQLTablePrefix + "questprogress SET Completed = true WHERE (QuestID = " + QuestID + " AND PlayerID = " + PlayerID + ");");
			
			Log(PlayerID, QuestID, RPG_LogType.Information, "PlayerQuest", "Completed");
	    }
	    catch (Exception ex)
	    {
	    	ex.printStackTrace();
			Log(RPG_LogType.Error, "PlayerQuest", ex);
	    }
	    
	    GetQuestManager().LoadQuestProgressesForPlayer(PlayerID);
	}
	
	public static void SendPacketToPlayers(Packet P)
	{
		SendPacketToPlayers(P, null);
	}
	public static void SendPacketToPlayers(Packet P, Player Exception)
	{
		for (Player p : Bukkit.getServer().getOnlinePlayers())
		{
			if (p == null || p == Exception)
				continue;
			
			((CraftPlayer)p).getHandle().playerConnection.sendPacket(P);
		}
	}
	
	public static void SetPasswordForPlayer(int PlayerID, String PasswordHash)
	{
		if (!players.containsKey(PlayerID))
		{
			Log(PlayerID, -1, RPG_LogType.Warning, "Player", "Does not exist");
			return;
		}
		
		Statement stmt = null;
		
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate("UPDATE " + SQLTablePrefix + "players SET Password = '" + PasswordHash + "' WHERE ID = " + PlayerID + ";");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Log(RPG_LogType.Error, "Inventory", ex);
		}
	}
	
	// 
	// Bank Accounts
	// 
	
	
	// 
	// Languages
	// 
	public static RPG_Language StringToLanguage(String Lang)
	{
		if (Lang.equalsIgnoreCase("DE"))
			return RPG_Language.DE;
		else if (Lang.equalsIgnoreCase("EN"))
			return RPG_Language.EN;
		else if (Lang.equalsIgnoreCase("FR"))
			return RPG_Language.FR;
		
		return null;
	}
	
	// 
	// Texts
	// 
	public static RPG_Text GetText(int ID)
	{
		if (texts.containsKey(ID))
			return texts.get(ID);
		else
		{			
			logger.warning(prefix + "Text with id " + ID + " does not exist");
			Log(-1, ID, RPG_LogType.Warning, "Text", "Does not exist");
			return null;
		}
	}
	
	public static String GetTextInLanguage(int ID, RPG_Language Language)
	{
		RPG_Text text = GetText(ID);
		
		if (text == null)
			return null;
		
		if (Language == RPG_Language.DE)
			return text.GetDE();
		else if (Language == RPG_Language.EN)
			return text.GetEN();
		else if (Language == RPG_Language.FR)
			return text.GetFR();
		
		Log(-1, ID, RPG_LogType.Warning, "Text", "Invalid Language");
		return null;
	}
	public static String GetTextInLanguage(int ID, CommandSender Sender)
	{
		if (Sender.getClass() != ColouredConsoleSender.class)
		{
			RPG_Player rpg_p = GetPlayer(((Player)Sender).getName());
			return GetTextInLanguage(ID, rpg_p.GetLanguage());
		}
		else
			return GetTextInLanguage(ID, RPG_Language.EN);
	}
	
	public static String GetFormattedText(int ID, RPG_Language Language, RPG_Npc NPC, RPG_Player Player, RPG_Quest Quest, RPG_Flag Flag, RPG_RestrictedItem RestrictedItem)
	{
		return GetFormattedString(GetTextInLanguage(ID, Language), Language, NPC, Player, Quest, Flag, RestrictedItem);
	}
	
	public static void LoadTextsFromDB()
	{
		logger.info(prefix + "Loading texts...");
		
		texts.clear();
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT ID, DE, EN, FR FROM " + SQLTablePrefix + "texts;");
			
			while (rs.next())
			{
				texts.put(rs.getInt("ID"), new RPG_Text(rs.getInt("ID"), rs.getString("DE"), rs.getString("EN"), rs.getString("FR")));
				
				Log(-1, rs.getInt("ID"), RPG_LogType.Information, "Text", "Loaded");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Log(RPG_LogType.Error, "Text", ex);
		}
		
		logger.info(prefix + "  Loaded " + texts.size() + " text(s) from database!");
	}
	
	public static void ReloadText(int ID)
	{
		if (texts.containsKey(ID))
			texts.remove(ID);
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT ID, DE, EN, FR FROM " + SQLTablePrefix + "texts WHERE ID = " + ID + ";");
			
			if (rs.first())
			{
				texts.put(rs.getInt("ID"), new RPG_Text(rs.getInt("ID"), rs.getString("DE"), rs.getString("EN"), rs.getString("FR")));
				
				Log(-1, ID, RPG_LogType.Information, "Text", "Reloaded");
			}
			else
			{
				Log(-1, ID, RPG_LogType.Warning, "Text", "Could not be reloaded");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Log(RPG_LogType.Error, "Database", ex);
		}
	}
	
	// 
	// Messages
	// 
	public static RPG_Message GetMessage(String Name)
	{
		if (msgs.containsKey(Name))
			return msgs.get(Name);
		else
		{
			logger.warning(prefix + "Message with name " + Name + " does not exist");
			Log(RPG_LogType.Warning, "Message", "Message with name " + Name + " does not exist");
			return null;
		}
	}
	
	public static String GetMessageInLanguage(String Name, RPG_Language Language)
	{
		RPG_Message msg = GetMessage(Name);
		if (msg == null)
			return "";
		
		if (Language == RPG_Language.DE)
			return msg.GetDE();
		else if (Language == RPG_Language.EN)
			return msg.GetEN();
		else if (Language == RPG_Language.FR)
			return msg.GetFR();
		
		Log(RPG_LogType.Warning, "Message", "Invalid Language");
		return "";
	}
	
	public static String GetFormattedMessage(String Name, RPG_Language Language, RPG_Npc NPC, RPG_Player Player, RPG_Quest Quest, RPG_Flag Flag, RPG_RestrictedItem RestrictedItem)
	{
		return GetFormattedString(GetMessageInLanguage(Name, Language), Language, NPC, Player, Quest, Flag, RestrictedItem);
	}
	public static String GetFormattedMessage(String Name, RPG_Language Language)
	{
		return GetFormattedMessage(Name, Language, null, null, null, null, null);
	}
	public static String GetFormattedMessage(String Name, CommandSender Sender)
	{
		if (Sender.getClass() != ColouredConsoleSender.class)
		{
			RPG_Player rpg_p = GetPlayer(((Player)Sender).getName());
			return GetFormattedMessage(Name, rpg_p.GetLanguage(), null, null, null, null, null);
		}
		else
			return GetFormattedMessage(Name, RPG_Language.EN, null, null, null, null, null);
	}
	public static String GetFormattedMessage(String Name, RPG_Language Language, RPG_Npc NPC)
	{
		return GetFormattedMessage(Name, Language, NPC, null, null, null, null);
	}
	public static String GetFormattedMessage(String Name, RPG_Language Language, RPG_Player Player)
	{
		return GetFormattedMessage(Name, Language, null, Player, null, null, null);
	}
	public static String GetFormattedMessage(String Name, RPG_Language Language, RPG_Quest Quest)
	{
		return GetFormattedMessage(Name, Language, null, null, Quest, null, null);
	}
	public static String GetFormattedMessage(String Name, RPG_Language Language, RPG_Flag Flag)
	{
		return GetFormattedMessage(Name, Language, null, null, null, Flag, null);
	}
	public static String GetFormattedMessage(String Name, RPG_Language Language, RPG_RestrictedItem RestrictedItem)
	{
		return GetFormattedMessage(Name, Language, null, null, null, null, RestrictedItem);
	}
	
	public static void LoadMessagesFromDB()
	{
		logger.info(prefix + "Loading messages...");
		
		msgs.clear();
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT Name, DE, EN, FR FROM " + SQLTablePrefix + "msgs;");
			
			while (rs.next())
			{
				msgs.put(rs.getString("Name"), new RPG_Message(rs.getString("Name"), rs.getString("DE"), rs.getString("EN"), rs.getString("FR")));
				
				Log(-1, -1, RPG_LogType.Information, "Message", "Loaded " + rs.getString("Name"));
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Log(RPG_LogType.Error, "Message", ex);
		}
		
		logger.info(prefix + "  Loaded " + msgs.size() + " message(s) from database!");
	}
	
	// 
	// Formatting
	// 
	public static String GetFormattedString(String Text, RPG_Language Language, RPG_Npc NPC, RPG_Player Player, RPG_Quest Quest, RPG_Flag Flag, RPG_RestrictedItem RestrictedItem)
	{		
		if (NPC != null)
		{
			Text = Text.replaceAll("%npc_name", NPC.GetName());
		}
		
		if (Player != null)
		{
			Text = Text.replaceAll("%p_name", Player.GetUsername());
			Text = Text.replaceAll("%p_lvl", Player.GetLevel() + "");
			Text = Text.replaceAll("%p_exp", Player.GetExp() + "");
			Text = Text.replaceAll("%p_money", Player.GetMoney() + "");
		}
		
		if (Quest != null)
		{
			Text = Text.replaceAll("%q_name", Quest.GetName(Language));
			Text = Text.replaceAll("%q_dispname", Quest.GetDisplayName(Language));
			Text = Text.replaceAll("%q_descr", Quest.GetDescription(Language));
			Text = Text.replaceAll("%q_rewardexp", Quest.GetRewardExp() + "");
			Text = Text.replaceAll("%q_rewardmoney", Quest.GetRewardMoney() + "");
			Text = Text.replaceAll("%q_npc_start", GetNpcManager().GetNPC(Quest.GetNPCStartID()).GetName());
			Text = Text.replaceAll("%q_npc_end", GetNpcManager().GetNPC(Quest.GetNPCEndID()).GetName());
		}
		
		if (Flag != null)
		{
			Text = Text.replaceAll("%f_name", Flag.GetName(Language));
			Text = Text.replaceAll("%f_nation", Flag.GetNation().GetDisplayName(Language));
			Text = Text.replaceAll("%f_captime", Flag.GetCaptureTime() + "");
			Text = Text.replaceAll("%f_capradius", Flag.GetCaptureRadius() + "");
			Text = Text.replaceAll("%f_capprogress", Flag.GetCaptureValue() + "");
		}
		
		if (RestrictedItem != null)
		{
			Text = Text.replaceAll("%ri_minlvl", RestrictedItem.GetRequiredLevel() + "");
		}
		
		return Text;
	}
	
	public static String FormatTextForAchievementMessage(String Text)
	{
		if (Text.length() > 26)
			Text = Text.substring(0, 25) + "...";
		return Text;
	}
	
	// 
	// Chat
	// 
	public static void LogChatMessage(int PlayerID, RPG_ChatType Type, String Message)
	{
		LogChatMessage(PlayerID, -1, Type, Message);
	}
	public static void LogChatMessage(int PlayerID, int TargetID, RPG_ChatType Type, String Message)
	{
		Statement stmt;
		
		int type = -1;
		if (Type == RPG_ChatType.Server)
			type = 0;
		else if (Type == RPG_ChatType.World)
			type = 1;
		else if (Type == RPG_ChatType.Nation)
			type = 2;
		else if (Type == RPG_ChatType.Guild)
			type = 3;
		else if (Type == RPG_ChatType.Region)
			type = 4;
		else if (Type == RPG_ChatType.Private)
			type = 5;
		
		try
		{
			stmt = conn.createStatement();
			stmt.executeUpdate("INSERT INTO " + SQLTablePrefix + "chatlog (PlayerID, TargetID, Type, Text) VALUES (" + PlayerID + ", " + TargetID + ", " + type + ", '" + Message + "');");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	public static void LogChatMessage(CommandSender Sender, RPG_ChatType Type, String Message)
	{
		if (IsPlayer(Sender))
		{
			RPG_Player rpg_p = GetPlayer(((Player)Sender).getName());
			LogChatMessage(rpg_p.GetID(), Type, Message);
		}
		else
			LogChatMessage(-2, Type, Message);
	}
	public static void LogChatMessage(CommandSender Sender, int TargetID, RPG_ChatType Type, String Message)
	{
		if (IsPlayer(Sender))
		{
			RPG_Player rpg_p = GetPlayer(((Player)Sender).getName());
			LogChatMessage(rpg_p.GetID(), TargetID, Type, Message);
		}
		else
			LogChatMessage(-2, TargetID, Type, Message);
	}
	
	public static void SendServerChat(String Permission, String Message)
	{
		logger.info(ChatColor.LIGHT_PURPLE + "[Server] " + Message);
		
		for (Player p : Bukkit.getServer().getOnlinePlayers())
		{
			if (!HasPermission(p, "rpg.chat.receive.*") && !HasPermission(p, "rpg.chat.receive." + Permission))
				continue;
			
			p.sendMessage(ChatColor.LIGHT_PURPLE + "[Server] " + Message);
		}
	}
	
	// 
	// Teleports
	// 
	public static RPG_Teleport GetTeleport(int ID)
	{
		if (teleports.containsKey(ID))
			return teleports.get(ID);
		else
		{
			logger.warning(prefix + "Teleport with id " + ID + " does not exist");
			Log(-1, ID, RPG_LogType.Warning, "Teleport", "Does not exist");
			return null;
		}
	}
	public static ArrayList<RPG_Teleport> GetTeleportsByNPC(int NPCID)
	{
		ArrayList<RPG_Teleport> tps = new ArrayList<RPG_Teleport>();
		
		for (RPG_Teleport t : teleports.values())
		{
			if (t.GetNPCID() == NPCID)
				tps.add(t);
		}
		
		return tps;
	}
	public static int GetTeleportCountByNPC(int NPCID)
	{
		int count = 0;
		
		for (RPG_Teleport t : teleports.values())
		{
			if (t.GetNPCID() == NPCID)
				count++;
		}
		
		return count;
	}
	
	public static void LoadTeleportsFromDB()
	{
		logger.info(prefix + "Loading teleports...");
		
		teleports.clear();
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT ID, NPCID, TargetPosID, TargetTextID, Level, Cost FROM " + SQLTablePrefix + "teleports " +
					"INNER JOIN " + SQLTablePrefix + "positions ON " + SQLTablePrefix + "teleports PosID = " + SQLTablePrefix + "positions.ID " +
					"INNER JOIN " + SQLTablePrefix + "worlds ON " + SQLTablePrefix + "positions.WorldID = " + SQLTablePrefix + "worlds.ID;");
			
			while (rs.next())
			{
				Location loc = new Location(Bukkit.getServer().getWorld(rs.getString("WorldName")), rs.getDouble("PosX"), rs.getDouble("PosY"), rs.getDouble("PosZ"));
				
				teleports.put(rs.getInt("ID"), new RPG_Teleport(rs.getInt("ID"), rs.getInt("NPCID"), loc, rs.getInt("TargetTextID"), rs.getInt("Level"), rs.getInt("Cost")));
				Log(-1, rs.getInt("ID"), RPG_LogType.Information, "Teleport", "Loaded");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Log(RPG_LogType.Error, "Nation", ex);
		}
		
		logger.info(prefix + "  Loaded " + teleports.size() + " teleport(s) from database!");
	}
	
	// 
	// Nations
	// 
	public static RPG_Nation GetNation(int ID)
	{
		if (nations.containsKey(ID))
			return nations.get(ID);
		else
		{
			logger.warning(prefix + "Nation with id " + ID + " does not exist");
			Log(-1, ID, RPG_LogType.Warning, "Nation", "Does not exist");
			return null;
		}
	}
	public static ArrayList<RPG_Nation> GetNations()
	{
		return new ArrayList<RPG_Nation>(nations.values());
	}
	
	public static void LoadNationsFromDB()
	{
		logger.info(prefix + "Loading nations...");
		
		nations.clear();
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT ID, Prefix, Name, DisplayNameID, Money, BlockMaterial FROM " + SQLTablePrefix + "nations;");
			
			while (rs.next())
			{
				try
				{
					nations.put(rs.getInt("ID"), new RPG_Nation(rs.getInt("ID"), rs.getString("Prefix"), rs.getString("Name"), rs.getInt("DisplayNameID"), 
							rs.getInt("Money"), Material.getMaterial(rs.getString("BlockMaterial"))));
					Log(-1, rs.getInt("ID"), RPG_LogType.Information, "Nation", "Loaded");
				}
				catch (Exception ex)
				{
					Log(-1, rs.getInt("ID"), RPG_LogType.Error, "Nation", ex);
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Log(RPG_LogType.Error, "Nation", ex);
		}
		
		logger.info(prefix + "  Loaded " + nations.size() + " nation(s) from database!");
	}
	
	// 
	// Guilds
	// 
	public static RPG_Guild GetGuild(int ID)
	{
		if (guilds.containsKey(ID))
			return guilds.get(ID);
		else
		{
			logger.warning(prefix + "Guild with id " + ID + " does not exist");
			Log(-1, ID, RPG_LogType.Warning, "Guild", "Does not exist");
			return null;
		}
	}
	public static ArrayList<RPG_Guild> GetGuilds()
	{
		return new ArrayList<RPG_Guild>(guilds.values());
	}
	
	public static void LoadGuildsFromDB()
	{
		logger.info(prefix + "Loading guilds...");
		
		guilds.clear();
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT ID, Prefix, Name, Description, FounderID FROM " + SQLTablePrefix + "guilds;");
			
			while (rs.next())
			{
				guilds.put(rs.getInt("ID"), new RPG_Guild(rs.getInt("ID"), rs.getString("Prefix"), rs.getString("Name"), rs.getString("Description"), rs.getInt("FounderID")));
				Log(-1, rs.getInt("ID"), RPG_LogType.Information, "Guild", "Loaded");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Log(RPG_LogType.Error, "Guild", ex);
		}
		
		logger.info(prefix + "  Loaded " + guilds.size() + " guild(s) from database!");
	}
	
	// 
	// Shops
	// 
	public static RPG_Shop GetShop(int ID)
	{
		if (shops.containsKey(ID))
			return shops.get(ID);
		else
		{
			logger.warning(prefix + "Shop with id " + ID + " does not exist");
			Log(-1, ID, RPG_LogType.Warning, "Shop", "Does not exist");
			return null;
		}
	}
	
	public static void LoadShopsFromDB()
	{
		logger.info(prefix + "Loading Shops...");
		
		shops.clear();
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT " + SQLTablePrefix + "shops.ID, NameID, WorldName, PosX, PosY, PosZ, ReqLevel, ReqQuestIDs FROM " + SQLTablePrefix + "shops " +
					"INNER JOIN " + SQLTablePrefix + "worlds ON " + SQLTablePrefix + "shops.WorldId = " + SQLTablePrefix + "worlds.ID;");
			
			while (rs.next())
			{
				String prereqquestids = rs.getString("ReqQuestIDs");
				ArrayList<Integer> list = new ArrayList<Integer>();
				
				if (prereqquestids != null)
				{
					String[] splits = prereqquestids.split(db_seperator);
					if (splits.length > 1)
					{
						try
						{
							for (String line : splits)
									list.add(Integer.parseInt(line));
						}
						catch (Exception ex)
						{
							logger.warning(prefix + "  Could not load shop " + rs.getInt("ID") + ": '" + prereqquestids + "' is not a valid quest list");
							Log(-1, rs.getInt("ID"), RPG_LogType.Warning, "Shop", prereqquestids + " is not a valid quest id list");
							continue;
						}
					}
				}
				
				Location pos = new Location(Bukkit.getServer().getWorld(rs.getString("WorldName")), rs.getInt("PosX"), rs.getInt("PosY"), rs.getInt("PosZ"));
				
				shops.put(rs.getInt(1), new RPG_Shop(rs.getInt(1), rs.getInt("NameID"), pos, rs.getInt("ReqLevel"), list));
				
				Log(-1, rs.getInt(1), RPG_LogType.Information, "Shop", "Loaded");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Log(RPG_LogType.Error, "Shop", ex);
		}
		
		logger.info(prefix + "  Loaded " + shops.size() + " shop(s) from database!");
	}
	
	// 
	// Restricted Items
	// 
	public static RPG_RestrictedItem GetRestrictedItem(int ID)
	{
		if (restricteditems.containsKey(ID))
			return restricteditems.get(ID);
		else
		{
			logger.warning(prefix + "Restricted item with id " + ID + " does not exist");
			Log(-1, ID, RPG_LogType.Warning, "RestrictedItem", "Does not exist");
			return null;
		}
	}
	public static ArrayList<RPG_RestrictedItem> GetRestrictedItems()
	{
		return new ArrayList<RPG_RestrictedItem>(restricteditems.values());
	}
	public static RPG_PlayerAction GetPlayerAction(int Action)
	{
		if (Action == 0)
			return RPG_PlayerAction.Place;
		else if (Action == 1)
			return RPG_PlayerAction.Damage;
		else if (Action == 2)
			return RPG_PlayerAction.Destroy;
		else if (Action == 3)
			return RPG_PlayerAction.Interact;
		
		return RPG_PlayerAction.None;
	}
	
	public static void LoadRestrictedItemsFromDB()
	{
		logger.info(prefix + "Loading restricted items...");
		
		restricteditems.clear();
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT ID, ItemID, ReqLevel, ActionIDs, QuestIDs FROM " + SQLTablePrefix + "restricteditems;");
			
			while(rs.next())
			{
				String[] quests = rs.getString(5).split(db_seperator);
				String[] actions = rs.getString(4).split(db_seperator);
				ArrayList<Integer> qids = new ArrayList<Integer>();
				
				try
				{
					for (String line : quests)
					{
						if (!line.equalsIgnoreCase(""))
							qids.add(Integer.parseInt(line));
					}
				}
				catch (Exception ex)
				{
					logger.warning(prefix + "  Could not load restricted item " + rs.getInt(1) + ": '" + rs.getString(5) + "' is not a valid quest id list");
					Log(-1, rs.getInt("ID"), RPG_LogType.Warning, "RestrictedItem", rs.getString("QuestIDs") + " is not a valid quest id list");
					continue;
				}
				
				for (String line : actions)
				{
					try
					{
						RPG_PlayerAction action = GetPlayerAction(Integer.parseInt(line));
						
						restricteditems.put(rs.getInt(1), new RPG_RestrictedItem(rs.getInt(1), rs.getInt(2), action, rs.getInt(3), qids));
						
						Log(-1, rs.getInt("ID"), RPG_LogType.Information, "RestrictedItem", "Loaded");
					}
					catch (Exception ex)
					{
						logger.warning(prefix + "  Could not load restricted item " + rs.getInt(1) + ": '" + line + "' is not a valid action id");
						Log(-1, rs.getInt("ID"), RPG_LogType.Warning, "RestrictedItem", line + " is not a valid action id");
					}
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Log(RPG_LogType.Error, "RestricatedItem", ex);
		}
		
		logger.info(prefix + "  Loaded " + restricteditems.size() + " restricted item(s) from database!");
	}
}

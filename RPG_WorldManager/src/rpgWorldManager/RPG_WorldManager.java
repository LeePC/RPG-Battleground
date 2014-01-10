package rpgWorldManager;

import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import rpgCore.RPG_Core;
import rpgGUI.RPG_InfoScreenItem;
import rpgInterfaces.RPG_IWorldManager;
import rpgOther.RPG_LogType;
import rpgPlayer.RPG_EditMode;
import rpgPlayer.RPG_Player;
import rpgWorld.RPG_World;
import rpgWorld.RPG_WorldCreationData;

public class RPG_WorldManager extends JavaPlugin implements Listener, RPG_IWorldManager
{
	private String prefix = "[RPG World Manager] ";
	private Logger logger = Logger.getLogger("Minecraft");
	private String SQLTablePrefix;
	
	private int worldCreationCounter = 0;
	private HashMap<Integer, RPG_WorldCreationData> worldsInCreation = new HashMap<Integer, RPG_WorldCreationData>();
	
	private HashMap<Integer, RPG_World> worlds = new HashMap<Integer, RPG_World>();
	private HashMap<String, Integer> worldnames = new HashMap<String, Integer>();
	
	
	// 
	// Main
	// 
	public RPG_WorldManager()
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
		items.add(new RPG_InfoScreenItem("add", 		"Adds an already existing world", 		"rpg.world.add", 		true));
		items.add(new RPG_InfoScreenItem("new", 		"Creates a new world", 					"rpg.world.new"));
		items.add(new RPG_InfoScreenItem("load", 		"Loads a world",						"rpg.world.load",		true));
		items.add(new RPG_InfoScreenItem("unload", 		"Unloads a world", 						"rpg.world.unload", 	true));
		items.add(new RPG_InfoScreenItem("evacuate", 	"Evacuate a world", 					"rpg.world.evacuate",	true));
		items.add(new RPG_InfoScreenItem("setspawn", 	"Set the spawn location of a world",	"rpg.world.setspawn",	true));
		items.add(new RPG_InfoScreenItem("tp", 			"Teleport to a world",					"rpg.world.tp",			true));
		items.add(new RPG_InfoScreenItem("list", 		"Lists all the worlds",					"rpg.world.list"));
		RPG_Core.AddInfoScreen("rpg.world", "World Manager", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Adds an existing world to the server without loading it", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /wm add [name]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [name]", ChatColor.WHITE + "The name of the world", ""));
		RPG_Core.AddInfoScreen("rpg.world.add", "Add an exsiting world", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Loads a world to the server", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /wm load [name]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [name]", ChatColor.WHITE + "The name of the world", ""));
		RPG_Core.AddInfoScreen("rpg.world.load", "Load a world", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Unloads a world to the server", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /wm unload [name]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [name]", ChatColor.WHITE + "The name of the world", ""));
		RPG_Core.AddInfoScreen("rpg.world.unload", "Unload a world", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Moves all players from a world to another world", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /wm evacuate [from] [to]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [from]", ChatColor.WHITE + "The name of the world the players are moved from", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [to]", ChatColor.WHITE + "The name of the world the players are moved to", ""));
		RPG_Core.AddInfoScreen("rpg.world.evacuate", "Evacuate a world", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Sets the spawn location for a world", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /wm setspawn [name]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [name]", ChatColor.WHITE + "The name of the world", ""));
		RPG_Core.AddInfoScreen("rpg.world.setspawn", "Set spawn location", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Moves all players from a world to another world", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /wm tp [name] [player]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [name]", ChatColor.WHITE + "The name of the world", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [player]", ChatColor.GREEN + "(optional)" + ChatColor.WHITE + " The name of the player to teleport", ""));
		RPG_Core.AddInfoScreen("rpg.world.evacuate", "Teleport to a world", items);
		
		LoadWorldsFromDB();
		
		LoadWorlds();
		
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
		RPG_Player rpg_p = null;
		if (RPG_Core.IsPlayer(sender))
			rpg_p = RPG_Core.GetPlayer(((Player)sender).getName());
		
		if (commandLabel.equalsIgnoreCase("wm") || commandLabel.equalsIgnoreCase("world") || commandLabel.equalsIgnoreCase("worlds"))
		{
			RPG_Core.Log(sender, commandLabel, args);
			
			if (!RPG_Core.HasPermission(sender, "rpg.world"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (args.length < 1)
			{
				RPG_Core.ShowInfoScreen("rpg.world", sender);
			}
			else
			{
				if (args[0].equalsIgnoreCase("add"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.world.add"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (args.length < 2)
					{
						RPG_Core.ShowInfoScreen("rpg.world.add", sender);
					}
					else
					{
						if (worldnames.containsKey(args[1].toLowerCase()))
						{
							sender.sendMessage(ChatColor.YELLOW + "This world has already been added");
							return false;
						}
						
						File f = new File(this.getDataFolder().getAbsoluteFile().getParentFile().getParentFile().getAbsolutePath());
						
						if (f.exists())
						{
							f = new File(this.getDataFolder().getAbsoluteFile().getParentFile().getParentFile().getAbsolutePath() + File.separator + args[1] + File.separator + "level.dat");
							
							if (f.exists())
							{
								RPG_WorldCreationData d = new RPG_WorldCreationData();
								d.Name = args[1];
								d.LoadOnStart = false;
								
								AddWorldToDB(d);
								
								RPG_World rpg_w = GetWorld(args[1]);
								if (rpg_w == null)
									sender.sendMessage(ChatColor.RED + "Could not add world");
								else
									sender.sendMessage(ChatColor.GREEN + "Added world " + ChatColor.BLUE + rpg_w.GetName());
							}
							else
								sender.sendMessage(ChatColor.RED + "Could not find level.dat file");
						}
						else
							sender.sendMessage(ChatColor.RED + "World not found");
					}
				}
				else if (args[0].equalsIgnoreCase("new"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.world.new"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (!RPG_Core.IsPlayer(sender))
					{
						sender.sendMessage(ChatColor.RED + "Only players can create new worlds");
						return false;
					}
					
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					sender.sendMessage(ChatColor.BLUE + "World Creation Wizard");
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					sender.sendMessage(ChatColor.WHITE + "Welcome to the World creation wizard. This wizard will help you create a World with a few simple steps!");
					sender.sendMessage(ChatColor.WHITE + "Please simply enter the requested data into the chat. You may exit this wizard at any time using: " + ChatColor.GOLD + "/wm cancel");
					
					worldCreationCounter++;
					worldsInCreation.put(worldCreationCounter, new RPG_WorldCreationData());
					
					rpg_p.SetEditMode(RPG_EditMode.World_Create);
					rpg_p.SetEditId(worldCreationCounter);
					rpg_p.SetEditStep(-1);
					
					onPlayerChat(new AsyncPlayerChatEvent(true, rpg_p.GetPlayer(), "", null));
				}
				else if (args[0].equalsIgnoreCase("create"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.world.create"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (!RPG_Core.IsPlayer(sender))
					{
						sender.sendMessage(ChatColor.RED + "Only players can create new worlds");
						return false;
					}
					
					if (rpg_p.GetEditId() == -1)
					{
						sender.sendMessage(ChatColor.YELLOW + "You are not creating a world at the moment. Start creating one by using: " + ChatColor.GOLD + "/wm new");
						return false;
					}
					
					if (rpg_p.GetEditStep() < 100)
					{
						sender.sendMessage(ChatColor.YELLOW + "Please first finish all the steps of the world creation wizard before creating the World!");
						return false;
					}
					
					int id = AddWorldToDB(worldsInCreation.get(rpg_p.GetEditId()));
					
					if (id >= 0)
					{						
						RPG_World rpg_w = worlds.get(id);
						WorldCreator c = new WorldCreator(rpg_w.GetName());
						if (worldsInCreation.get(rpg_p.GetEditId()).IsSuperFlat)
							c.type(WorldType.FLAT);
						Bukkit.createWorld(c);
						
						sender.sendMessage(ChatColor.GREEN + "World with ID " + ChatColor.BLUE + id + ChatColor.GREEN + " created");
						
						worldsInCreation.remove(rpg_p.GetEditId());
						rpg_p.SetEditMode(RPG_EditMode.None);
					}
					else
					{
						sender.sendMessage(ChatColor.RED + "World creation failed!");
						return false;
					}
				}
				else if (args[0].equalsIgnoreCase("cancel"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.world.cancel"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (!RPG_Core.IsPlayer(sender))
					{
						sender.sendMessage(ChatColor.RED + "Only players can edit worlds");
						return false;
					}
					
					if (rpg_p.GetEditMode() == RPG_EditMode.World_Create)
					{						
						sender.sendMessage(ChatColor.GREEN + "World creation canceled");
						
						rpg_p.SetEditMode(RPG_EditMode.None);
					}
					else
					{
						sender.sendMessage(ChatColor.YELLOW + "You are not creating or editing a World at the moment. Start creating one by using: " + ChatColor.GOLD + "/wm new");
						return false;
					}
				}
				else if (args[0].equalsIgnoreCase("load"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.world.load"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (args.length < 2)
					{
						RPG_Core.ShowInfoScreen("rpg.world.load", sender);
					}
					else
					{
						RPG_World rpg_w = GetWorld(args[1]);
						
						if (rpg_w == null)
							sender.sendMessage(ChatColor.RED + "Invalid world name or ID");
						else
						{
							getServer().createWorld(WorldCreator.name(args[1]));
							sender.sendMessage(ChatColor.GREEN + "Loaded world " + ChatColor.BLUE + rpg_w.GetName());
						}
					}
				}
				else if (args[0].equalsIgnoreCase("unload"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.world.unload"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (args.length < 2)
					{
						RPG_Core.ShowInfoScreen("rpg.world.unload", sender);
					}
					else
					{
						RPG_World rpg_w = GetWorld(args[1]);
						
						if (rpg_w == null)
							sender.sendMessage(ChatColor.RED + "Invalid world name or ID");
						else
						{
							if (getServer().unloadWorld(args[1], true))
								sender.sendMessage(ChatColor.GREEN + "Unloaded world " + ChatColor.BLUE + rpg_w.GetName());
							else
								sender.sendMessage(ChatColor.RED + "Could not unload world " + ChatColor.BLUE + rpg_w.GetName());
						}
					}
				}
				else if (args[0].equalsIgnoreCase("evacuate"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.world.evacuate"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (args.length < 3)
					{
						RPG_Core.ShowInfoScreen("rpg.world.evacuate", sender);
					}
					else
					{
						RPG_World rpg_wfrom = GetWorld(args[1]);
						RPG_World rpg_wto = GetWorld(args[2]);
						
						if (rpg_wfrom == null)
							sender.sendMessage(ChatColor.RED + "Invalid world name or ID for 'From'");
						else if (rpg_wto == null)
							sender.sendMessage(ChatColor.RED + "Invalid world name or ID for 'To'");
						else if (!rpg_wfrom.IsLoaded())
							sender.sendMessage(ChatColor.RED + "'From' world is not loaded");
						else if(!rpg_wto.IsLoaded())
							sender.sendMessage(ChatColor.RED + "'To' world is not loaded");
						else
						{
							Location loc = getServer().getWorld(rpg_wto.GetName()).getSpawnLocation();
							for (Player p : getServer().getWorld(rpg_wfrom.GetName()).getPlayers())
								p.teleport(loc);
							sender.sendMessage(ChatColor.GREEN + "Evacuated players from " + ChatColor.BLUE + rpg_wfrom.GetName() + ChatColor.GREEN + " to " + ChatColor.BLUE + rpg_wto.GetName());
						}
					}
				}
				else if (args[0].equalsIgnoreCase("setspawn"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.world.setspawn"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (args.length < 2)
					{
						RPG_Core.ShowInfoScreen("rpg.world.setspawn", sender);
					}
					else
					{
						RPG_World rpg_w = GetWorld(args[1]);
						
						if (rpg_w == null)
							sender.sendMessage(ChatColor.RED + "Invalid world name or ID");
						else
						{
							getServer().getWorld(rpg_w.GetName()).setSpawnLocation(rpg_p.GetPlayer().getLocation().getBlockX(), rpg_p.GetPlayer().getLocation().getBlockY(), rpg_p.GetPlayer().getLocation().getBlockZ());
							sender.sendMessage(ChatColor.GREEN + "Set spawn of " + ChatColor.BLUE + rpg_w.GetName() + ChatColor.GREEN + " to current location");
						}
					}
				}
				else if (args[0].equalsIgnoreCase("tp"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.world.tp"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (args.length <= 1)
					{
						RPG_Core.ShowInfoScreen("rpg.world.tp", sender);
					}
					else
					{
						RPG_World rpg_w = GetWorld(args[1]);
						Player p;
						
						if (args.length > 2)
						{
							p = getServer().getPlayer(args[2]);
							if (p == null)
							{
								sender.sendMessage(ChatColor.RED + "Invalid player name");
								return false;
							}
						}
						else
							p = rpg_p.GetPlayer();
						
						if (p == null)
						{
							sender.sendMessage(ChatColor.RED + "No player specified");
							return false;
						}
						
						if (rpg_w == null)
							sender.sendMessage(ChatColor.RED + "Invalid world name or ID");
						else if (!rpg_w.IsLoaded())
							sender.sendMessage(ChatColor.RED + "World not loaded");
						else
						{
							p.teleport(getServer().getWorld(rpg_w.GetName()).getSpawnLocation());
							sender.sendMessage(ChatColor.GREEN + "Teleported to spawn of " + ChatColor.BLUE + rpg_w.GetName());
						}
					}
				}
				else if (args[0].equalsIgnoreCase("list"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.world.list"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					sender.sendMessage(ChatColor.BLUE + "Worlds");
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					for (RPG_World rpg_w : GetWorlds())
					{
						if (rpg_w.IsLoaded())
							sender.sendMessage("  " + ChatColor.GOLD + rpg_w.GetID() + "  " + ChatColor.GREEN + rpg_w.GetName() + " (" + getServer().getWorld(rpg_w.GetName()).getPlayers().size() + ")");
						else
							sender.sendMessage("  " + ChatColor.GOLD + rpg_w.GetID() + "  " + ChatColor.RED + rpg_w.GetName());
					}
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				}
				else
					sender.sendMessage(ChatColor.RED + "Unknown command");
			}
		}
		
		return false;
	}
	
	// 
	// Events
	// 
	@EventHandler
	public void onWorldLoad(WorldLoadEvent e)
	{		
		RPG_World rpg_w = GetWorld(e.getWorld().getName());
		if (rpg_w != null)
			rpg_w.SetLoaded(true);
		else
		{
			RPG_WorldCreationData d = new RPG_WorldCreationData();
			d.LoadOnStart = true;
			d.Name = e.getWorld().getName();
			AddWorldToDB(d);
			
			rpg_w = GetWorld(e.getWorld().getName());
			if (rpg_w != null)
				rpg_w.SetLoaded(true);
		}
	}
	@EventHandler
	public void onWorldUnload(WorldUnloadEvent e)
	{
		RPG_World rpg_w = GetWorld(e.getWorld().getName());
		if (rpg_w != null)
			rpg_w.SetLoaded(false);
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e)
	{
		RPG_Player rpg_p = RPG_Core.GetPlayer(e.getPlayer().getName());
		
		if (rpg_p.GetEditMode() == RPG_EditMode.World_Create)
		{
			e.setCancelled(true);
			
			if (!RPG_Core.HasPermission(rpg_p, "rpg.world.create"))
			{
				rpg_p.SendMessage(RPG_Core.GetFormattedMessage("no_perm", rpg_p.GetLanguage()));
				RPG_Core.Log(rpg_p.GetID(), -1, RPG_LogType.Warning, "Command", "NoPerm");
				return;
			}
			
			if (rpg_p.GetEditStep() == -1)
			{
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Please enter a " + ChatColor.BLUE + "name " + ChatColor.WHITE + "for your World:");
				
				rpg_p.SetEditStep(0);
			}
			else if (rpg_p.GetEditStep() == 0)
			{
				if (e.getMessage() != "")
				{
					worldsInCreation.get(rpg_p.GetEditId()).Name = e.getMessage();
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Name: " + ChatColor.WHITE + e.getMessage());
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Now decide whether the world should be " + ChatColor.BLUE + "loaded " + ChatColor.WHITE + "when the " + ChatColor.BLUE + "server starts.");
				rpg_p.SendMessage(ChatColor.WHITE + "Use " + ChatColor.BLUE + "true " + ChatColor.WHITE + "to load the world at server start, otherwise " + ChatColor.BLUE + "false");
				
				rpg_p.SetEditStep(1);
			}
			else if (rpg_p.GetEditStep() == 1)
			{
				if (e.getMessage() != "")
				{
					if (e.getMessage().equalsIgnoreCase("true"))
						worldsInCreation.get(rpg_p.GetEditId()).LoadOnStart = true;
					else if (e.getMessage().equalsIgnoreCase("false"))
						worldsInCreation.get(rpg_p.GetEditId()).LoadOnStart = false;
					else
					{
						rpg_p.SendMessage(ChatColor.RED + "You have entered a wrong value. Please use either " + ChatColor.BLUE + "true " + ChatColor.RED + "or " + ChatColor.BLUE + "false" + ChatColor.RED + 
								", or use " + ChatColor.GOLD + "/wm cancel " + ChatColor.RED + "to stop creating a world!");
						return;
					}
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Load on start: " + ChatColor.WHITE + e.getMessage());
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Now decide whether the world should be " + ChatColor.BLUE + "flat " + ChatColor.WHITE + "or " + ChatColor.BLUE + "normal" + ChatColor.WHITE + ":");
				rpg_p.SendMessage(ChatColor.WHITE + "Use " + ChatColor.BLUE + "true " + ChatColor.WHITE + "for a flat world, otherwise " + ChatColor.BLUE + "false");
				
				rpg_p.SetEditStep(2);
			}
			else if (rpg_p.GetEditStep() == 2)
			{
				if (e.getMessage() != "")
				{
					if (e.getMessage().equalsIgnoreCase("true"))
						worldsInCreation.get(rpg_p.GetEditId()).IsSuperFlat = true;
					else if (e.getMessage().equalsIgnoreCase("false"))
						worldsInCreation.get(rpg_p.GetEditId()).IsSuperFlat = false;
					else
					{
						rpg_p.SendMessage(ChatColor.RED + "You have entered a wrong value. Please use either " + ChatColor.BLUE + "true " + ChatColor.RED + "or " + ChatColor.BLUE + "false" + ChatColor.RED + 
								", or use " + ChatColor.GOLD + "/wm cancel " + ChatColor.RED + "to stop creating a world!");
						return;
					}
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Flat: " + ChatColor.WHITE + e.getMessage());
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "You are almost done, please review your data, and enter " + ChatColor.GOLD + "/wm create " + ChatColor.WHITE + "to create the world. Or enter " + 
						ChatColor.GOLD + "/wm cancel " + ChatColor.WHITE + "to exit the wizard.");
				
				RPG_WorldCreationData d = worldsInCreation.get(rpg_p.GetEditId());
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.BLUE + "Name: " + ChatColor.WHITE + d.Name);
				rpg_p.SendMessage(ChatColor.BLUE + "Flat: " + ChatColor.WHITE + d.IsSuperFlat);
				rpg_p.SendMessage(ChatColor.BLUE + "Load on start: " + ChatColor.WHITE + d.LoadOnStart);
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				
				rpg_p.SetEditStep(100);
			}
		}
	}
	
	// 
	// Worlds
	// 
	public RPG_World GetWorld(int ID)
	{
		if (worlds.containsKey(ID))
			return worlds.get(ID);
		else
		{
			logger.warning(prefix + "World with id '" + ID + "' does not exist");
			RPG_Core.Log(-1, ID, RPG_LogType.Warning, "World", "Does not exist");
			return null;
		}
	}
	public RPG_World GetWorld(String Name)
	{		
		if (worldnames.containsKey(Name.toLowerCase()))
			return GetWorld(worldnames.get(Name.toLowerCase()));
		else
		{
			int id = -1;
			try
			{
				id = Integer.parseInt(Name);
			}
			catch (Exception ex)
			{ }
			
			if (worlds.containsKey(id))
				return worlds.get(id);
			else
			{
				logger.warning(prefix + "World with name " + Name + " does not exist");
				RPG_Core.Log(RPG_LogType.Warning, "World", "World with name " + Name + " does not exist");
				return null;
			}
		}
	}
	public Collection<RPG_World> GetWorlds()
	{
		return worlds.values();
	}
	
	public int AddWorldToDB(RPG_WorldCreationData Data)
	{
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			stmt.executeUpdate("INSERT INTO " + SQLTablePrefix + "worlds (WorldName, LoadOnStart) VALUES ('" + Data.Name + "', " + Data.LoadOnStart + ");");
			
			rs = stmt.executeQuery("select last_insert_id();");
			rs.last();
			int id = rs.getInt(1);
			
			RPG_Core.Log(-1, id, RPG_LogType.Information, "WorldCreate", "Created");
			
			try
			{
				stmt = RPG_Core.GetDatabaseStatement();
				rs = stmt.executeQuery("SELECT ID, WorldName, LoadOnStart FROM " + SQLTablePrefix + "worlds WHERE ID = " + id + ";");
				
				if (rs.first())
				{
					worlds.put(rs.getInt("ID"), new RPG_World(rs.getInt("ID"), rs.getString("WorldName"), false, rs.getBoolean("LoadOnStart")));
					worldnames.put(rs.getString("WorldName").toLowerCase(), rs.getInt("ID"));
					RPG_Core.Log(-1, rs.getInt("ID"), RPG_LogType.Information, "World", "Loaded");
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				RPG_Core.Log(RPG_LogType.Error, "World", ex);
			}
			
			return id;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "WorldCreate", ex);
			
			return -1;
		}
	}
	
	public void LoadWorldsFromDB()
	{
		logger.info(prefix + "Loading worlds...");
		
		worlds.clear();
		worldnames.clear();
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			rs = stmt.executeQuery("SELECT ID, WorldName, LoadOnStart FROM " + SQLTablePrefix + "worlds;");
			
			while(rs.next())
			{
				worlds.put(rs.getInt("ID"), new RPG_World(rs.getInt("ID"), rs.getString("WorldName"), false, rs.getBoolean("LoadOnStart")));
				worldnames.put(rs.getString("WorldName").toLowerCase(), rs.getInt("ID"));
				RPG_Core.Log(-1, rs.getInt("ID"), RPG_LogType.Information, "World", "Loaded");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "World", ex);
		}
		
		logger.info(prefix + "  Loaded " + worlds.size() + " world(s) from database!");
	}
	
	public void LoadWorlds()
	{
		for (RPG_World rpg_w : worlds.values())
		{
			if (rpg_w.GetLoadOnStart())
			{
				Bukkit.createWorld(WorldCreator.name(rpg_w.GetName()));
				rpg_w.SetLoaded(true);
			}
		}
	}
}

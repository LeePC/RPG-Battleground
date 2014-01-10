package rpgBattlefieldManager;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.getspout.spoutapi.SpoutManager;

import rpgBattlefield.RPG_Flag;
import rpgBattlefield.RPG_FlagCreationData;
import rpgBattlefield.RPG_Outpost;
import rpgBattlefield.RPG_OutpostBlock;
import rpgBattlefield.RPG_OutpostCreationData;
import rpgCore.RPG_Core;
import rpgGUI.RPG_InfoScreenItem;
import rpgInterfaces.RPG_IBattlefieldManager;
import rpgOther.RPG_LogType;
import rpgOther.RPG_Region;
import rpgPlayer.RPG_EditMode;
import rpgPlayer.RPG_Player;
import rpgTexts.RPG_Language;

public class RPG_BattlefieldManager extends JavaPlugin implements Listener, RPG_IBattlefieldManager
{
	private String prefix = "[RPG Battlefield Manager] ";
	private Logger logger = Logger.getLogger("Minecraft");
	private String SQLTablePrefix;
	
	private boolean asyncRunning = false;
	private BukkitTask task;
	private float defaultCaptureRadius = 0;
	private float defaultCaptureTime = 0;
	private Material defaultFlagBlockMaterial = Material.STONE;
	private long flagTickInterval = 0;
	private int flagTowerHeight = 0;
	
	private int outpostCreationCounter = 0;
	private HashMap<Integer, RPG_OutpostCreationData> outpostsInCreation = new HashMap<Integer, RPG_OutpostCreationData>();
	
	private int flagCreationCounter = 0;
	private HashMap<Integer, RPG_FlagCreationData> flagsInCreation = new HashMap<Integer, RPG_FlagCreationData>();
	
	private HashMap<Integer, RPG_Flag> flags = new HashMap<Integer, RPG_Flag>();
	private HashMap<Integer, RPG_Outpost> outposts = new HashMap<Integer, RPG_Outpost>();
	
	
	// 
	// Main
	// 
	public RPG_BattlefieldManager()
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
		items.add(new RPG_InfoScreenItem("flag", 		"Manage the flags", 	"rpg.bf.flag",		true));
		items.add(new RPG_InfoScreenItem("outpost", 	"Manage the outposts",	"rpg.bf.outpost", 	true));
		RPG_Core.AddInfoScreen("rpg.bf", "Battlefield Manager", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("new", 	"Creates a new Flag", 	"rpg.bf.flag.new",		true));
		items.add(new RPG_InfoScreenItem("edit", 	"Edit a Flag",			"rpg.bf.flag.edit",	true));
		items.add(new RPG_InfoScreenItem("del", 	"Delete a Flag",		"rpg.bf.flag.del",		true));
		items.add(new RPG_InfoScreenItem("list", 	"Lists all the flags",	"rpg.bf.flag.list",	true));
		items.add(new RPG_InfoScreenItem("tp", 		"Teleports to a Flag",	"rpg.bf.flag.tp",		true));
		RPG_Core.AddInfoScreen("rpg.bf.flag", "Flags", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("new", 	"Creates a new outpost", 	"rpg.bf.outpost.new",	true));
		RPG_Core.AddInfoScreen("rpg.bf.outpost", "Outposts", items);
		
		logger.info(prefix + "Loading config...");
		
		getConfig().addDefault("defaultCaptureRadius", 0);
		getConfig().addDefault("defaultCaptureTime", 0);
		getConfig().addDefault("defaultFlagBlock", 0);
		getConfig().addDefault("flagTickInterval", 20);
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		defaultCaptureRadius = (float)getConfig().getDouble("defaultCaptureRadius");
		defaultCaptureTime = (float)getConfig().getDouble("defaultCaptureTime");
		defaultFlagBlockMaterial = Material.getMaterial(getConfig().getString("defaultFlagBlockMaterial"));
		flagTickInterval = getConfig().getLong("flagTickInterval");
		flagTowerHeight = getConfig().getInt("flagTowerHeight");
		
		logger.info(prefix + "  Default flag block: " + defaultFlagBlockMaterial);
		
		LoadFlagsFromDB();
		
		LoadOutpostsFromDB();
		
		logger.info(prefix + "Placing flags...");
		for (RPG_Flag flag : GetFlags())
		{
			float cieling = (float)flagTowerHeight / flag.GetCaptureTime() * flag.GetCaptureValue();
			for (int i = 0; i < flagTowerHeight; i++)
			{
				if (i < cieling)
					flag.GetLocation().getWorld().getBlockAt(flag.GetLocation().getBlockX(), flag.GetLocation().getBlockY() + i, flag.GetLocation().getBlockZ()).setType(flag.GetNation().GetBlockMaterial());
				else
					flag.GetLocation().getWorld().getBlockAt(flag.GetLocation().getBlockX(), flag.GetLocation().getBlockY() + i, flag.GetLocation().getBlockZ()).setType(defaultFlagBlockMaterial);
			}
		}
		
		logger.info(prefix + "Starting threads...");
		task = getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() { public void run() { onThreadFlagTick(); } }, flagTickInterval, flagTickInterval);
		
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
		RPG_Player rpg_p = null;
		if (RPG_Core.IsPlayer(sender))
			rpg_p = RPG_Core.GetPlayer(((Player)sender).getName());
		
		if (commandLabel.equalsIgnoreCase("bfm") || commandLabel.equalsIgnoreCase("bf"))
		{
			RPG_Core.Log(sender, commandLabel, args);
			
			if (!RPG_Core.HasPermission(sender, "rpg.bf"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (args.length < 1)
			{
				RPG_Core.ShowInfoScreen("rpg.bf", sender);
			}
			else
			{
				if (args[0].equalsIgnoreCase("rebuild"))
				{
					// DEBUG: This command only exists for debuging purposes
					
					if (!RPG_Core.HasPermission(sender, "rpg.bf.rebuild"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					for (RPG_Outpost outpost : GetOutposts())
						outpost.Rebuild();
					
					sender.sendMessage(ChatColor.GREEN + "Rebuild complete!");
				}
				else if (args[0].equalsIgnoreCase("flag") || args[0].equalsIgnoreCase("f") || args[0].equalsIgnoreCase("flags"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.bf.flag"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (args.length < 2)
					{
						RPG_Core.ShowInfoScreen("rpg.bf.flag", sender);
					}
					else
					{
						if (args[1].equalsIgnoreCase("new"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.bf.flag.new"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (!RPG_Core.IsPlayer(sender))
							{
								sender.sendMessage(ChatColor.RED + "Only players can create new flags");
								return false;
							}
							
							sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
							sender.sendMessage(ChatColor.BLUE + "Flag Creation Wizard");
							sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
							sender.sendMessage(ChatColor.WHITE + "Welcome to the Flag creation wizard. This wizard will help you create a Flag with a few simple steps!");
							sender.sendMessage(ChatColor.WHITE + "Please simply enter the requested data into the chat. You may exit this wizard at any time using: " + ChatColor.GOLD + "/bfm cancel");
							
							flagCreationCounter++;
							flagsInCreation.put(flagCreationCounter, new RPG_FlagCreationData());
							
							rpg_p.SetEditMode(RPG_EditMode.Flag_Create);
							rpg_p.SetEditId(flagCreationCounter);
							rpg_p.SetEditStep(-1);
							
							onPlayerChat(new AsyncPlayerChatEvent(true, rpg_p.GetPlayer(), "", null));
						}
						else if (args[1].equalsIgnoreCase("create"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.bf.flag.create"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (!RPG_Core.IsPlayer(sender))
							{
								sender.sendMessage(ChatColor.RED + "Only players can create new flags");
								return false;
							}
							
							if (rpg_p.GetEditId() == -1)
							{
								sender.sendMessage(ChatColor.YELLOW + "You are not creating a Flag at the moment. Start creating one by using: " + ChatColor.GOLD + "/bfm flag new");
								return false;
							}
							
							if (rpg_p.GetEditStep() < 100)
							{
								sender.sendMessage(ChatColor.YELLOW + "Please first finish all the steps of the Flag creation wizard before creating the Flag!");
								return false;
							}
							
							int id = AddFlagToDB(flagsInCreation.get(rpg_p.GetEditId()), rpg_p.GetPlayer().getLocation(), rpg_p.GetLanguage());
							
							if (id >= 0)
							{
								sender.sendMessage(ChatColor.GREEN + "Flag with id " + ChatColor.BLUE + id + ChatColor.GREEN + " created");
								
								flagsInCreation.remove(rpg_p.GetEditId());
								rpg_p.SetEditMode(RPG_EditMode.None);
							}
							else
							{
								sender.sendMessage(ChatColor.RED + "Flag creation failed!");
								return false;
							}
						}
						else if (args[1].equalsIgnoreCase("edit"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.bf.flag.edit"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							// tbd
						}
						else if (args[1].equalsIgnoreCase("del") || args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("rem") || args[1].equalsIgnoreCase("remove"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.bf.flag.del"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (args.length <= 2)
							{
								sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
								sender.sendMessage(ChatColor.BLUE + "RPG Commands - Delete a Flag");
								sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
								sender.sendMessage(ChatColor.BLUE + " Description:");
								sender.sendMessage(ChatColor.WHITE + "  Deletes the Flag with the specified ID from the server");
								sender.sendMessage(ChatColor.BLUE + " Syntax:");
								sender.sendMessage(ChatColor.GOLD + "  /bfm flag del [ID]");
								sender.sendMessage(ChatColor.BLUE + " Arguments:");
								sender.sendMessage(ChatColor.GOLD + "  [ID]   " + ChatColor.WHITE + "The ID of the Flag");
								sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
							}
							else
							{
								try
								{
									int id = Integer.parseInt(args[2]);
									if (RemoveFlagFromDB(id))
										sender.sendMessage(ChatColor.GREEN + "Flag with ID " + ChatColor.BLUE + id + ChatColor.GREEN + "removed");
									else
										sender.sendMessage(ChatColor.YELLOW + "Could not remove flag with ID " + ChatColor.BLUE + id);
								}
								catch (Exception ex)
								{
									sender.sendMessage(ChatColor.RED + "Invalid Flag ID");
								}
							}
						}
						else if (args[1].equalsIgnoreCase("list"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.bf.flag.list"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
							sender.sendMessage(ChatColor.BLUE + "Flag List");
							sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
							for (RPG_Flag flag : GetFlags())
							{
								sender.sendMessage(ChatColor.GOLD + "  " + flag.GetID() + " " + ChatColor.WHITE + RPG_Core.GetTextInLanguage(flag.GetNameID(), sender) + " (" + 
										(flag.GetNationID() != -1 ? flag.GetNation().GetName() : "None") + ") [" + flag.GetLocation().getWorld().getName() + ", " + flag.GetLocation().getBlockX() + ", " + 
										flag.GetLocation().getBlockY() + ", " + flag.GetLocation().getBlockZ() + "]");
							}
							sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
						}
						else if (args[1].equalsIgnoreCase("reload"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.bf.flag.reload"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							LoadFlagsFromDB();
							
							sender.sendMessage(ChatColor.GREEN + "Flags reloaded");
						}
						else if (args[1].equalsIgnoreCase("tp"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.npc.tp"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (!RPG_Core.IsPlayer(sender))
							{
								sender.sendMessage(ChatColor.RED + "Only players can teleport to flags");
								return false;
							}
							
							if (args.length <= 2)
							{
								sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
								sender.sendMessage(ChatColor.BLUE + "RPG Commands - Teleport to a Flag");
								sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
								sender.sendMessage(ChatColor.BLUE + " Description:");
								sender.sendMessage(ChatColor.WHITE + "  Teleports you to the location of a Flag");
								sender.sendMessage(ChatColor.BLUE + " Syntax:");
								sender.sendMessage(ChatColor.GOLD + "  /bfm flag tp [ID]");
								sender.sendMessage(ChatColor.BLUE + " Arguments:");
								sender.sendMessage(ChatColor.GOLD + "  [ID]   " + ChatColor.WHITE + "The ID of the Flag");
								sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
							}
							else
							{
								try
								{
									int id = Integer.parseInt(args[2]);
									RPG_Flag rpg_flag = GetFlag(id);
									
									if (rpg_flag == null)
									{
										sender.sendMessage(ChatColor.RED + "The Flag with the ID " + id + " does not exist!");
										return false;
									}
									
									rpg_p.GetPlayer().teleport(rpg_flag.GetLocation());
								}
								catch (Exception ex)
								{
									sender.sendMessage(ChatColor.RED + "Invalid Flag ID");
								}
							}
						}
						else
							sender.sendMessage(ChatColor.RED + "Unknown command");
					}
				}
				else if (args[0].equalsIgnoreCase("outpost") || args[0].equalsIgnoreCase("op") || args[0].equalsIgnoreCase("outposts"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.bf.outpost"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (args.length <= 1)
					{
						RPG_Core.ShowInfoScreen("rpg.bf.outpost", sender);
					}
					else
					{
						if (args[1].equalsIgnoreCase("new"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.bf.outpost.new"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (!RPG_Core.IsPlayer(sender))
							{
								sender.sendMessage(ChatColor.RED + "Only players can create new outposts");
								return false;
							}
							
							sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
							sender.sendMessage(ChatColor.BLUE + "Outpost Creation Wizard");
							sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
							sender.sendMessage(ChatColor.WHITE + "Welcome to the Outpost creation wizard. This wizard will help you create an Outpost with a few simple steps!");
							sender.sendMessage(ChatColor.WHITE + "Please simply enter the requested data into the chat. You may exit this wizard at any time using: " + ChatColor.GOLD + "/bfm cancel");
							
							outpostCreationCounter++;
							outpostsInCreation.put(outpostCreationCounter, new RPG_OutpostCreationData());
							
							rpg_p.SetEditMode(RPG_EditMode.Outpost_Create);
							rpg_p.SetEditId(outpostCreationCounter);
							rpg_p.SetEditStep(0);
							
							onPlayerChat(new AsyncPlayerChatEvent(true, rpg_p.GetPlayer(), "", null));
						}
						else if (args[1].equalsIgnoreCase("create"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.bf.outpost.create"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (!RPG_Core.IsPlayer(sender))
							{
								sender.sendMessage(ChatColor.RED + "Only players can create new outposts");
								return false;
							}
							
							// tbd
						}
						else if (args[1].equalsIgnoreCase("edit"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.bf.outpost.edit"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							// tbd
						}
						else if (args[1].equalsIgnoreCase("del"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.bf.outpost.del"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							// tbd
						}
						else if (args[1].equalsIgnoreCase("list"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.bf.outpost.list"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							// tbd
						}
						else if (args[1].equalsIgnoreCase("reload"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.bf.oustpost.reload"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							// tbd
						}
						else
							sender.sendMessage(ChatColor.RED + "Unknown command");
					}
				}
				else if (args[0].equalsIgnoreCase("next"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.bf.next"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (!RPG_Core.IsPlayer(sender))
					{
						sender.sendMessage(ChatColor.RED + "Only players can create new flags/outposts");
						return false;
					}
					
					if (rpg_p.GetEditMode() == RPG_EditMode.Flag_Create)
					{						
						if (rpg_p.GetEditStep() < 100)
							onPlayerChat(new AsyncPlayerChatEvent(true, rpg_p.GetPlayer(), "", null));
						else
						{
							sender.sendMessage(ChatColor.RED + "You are already done with the wizard. Please use " + ChatColor.GOLD + "/bfm back " + ChatColor.RED + "to go back a step, or use " + 
								ChatColor.GOLD + "/bfm flag create " + ChatColor.RED + "to create your Flag. You can use " + ChatColor.GOLD + "/bfm cancel " + ChatColor.RED + "to exit the wizard.");
						}
					}
					else if (rpg_p.GetEditMode() == RPG_EditMode.Outpost_Create)
					{						
						if (rpg_p.GetEditStep() < 100)
							onPlayerChat(new AsyncPlayerChatEvent(true, rpg_p.GetPlayer(), "", null));
						else
						{
							sender.sendMessage(ChatColor.RED + "You are already done with the wizard. Please use " + ChatColor.GOLD + "/bfm back " + ChatColor.RED + "to go back a step, or use " + 
								ChatColor.GOLD + "/bfm outpost create " + ChatColor.RED + "to create your Outpost. You can use " + ChatColor.GOLD + "/bfm cancel " + ChatColor.RED + "to exit the wizard.");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.YELLOW + "You are not creating anything at the moment. Use " + ChatColor.GOLD + "/bfm outpost new " + ChatColor.WHITE + "or " + ChatColor.GOLD + 
								"/bfm flag new " + ChatColor.WHITE + "to start creating an object");
						return false;
					}
				}
				else if (args[0].equalsIgnoreCase("back"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.bf.back"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (!RPG_Core.IsPlayer(sender))
					{
						sender.sendMessage(ChatColor.RED + "Only players can create new flags/outposts");
						return false;
					}
					
					if (rpg_p.GetEditMode() == RPG_EditMode.Flag_Create)
					{						
						if (rpg_p.GetEditStep() > 0)
						{
							rpg_p.SetEditStep(rpg_p.GetEditStep() - 2);
							onPlayerChat(new AsyncPlayerChatEvent(true, rpg_p.GetPlayer(), "", null));
						}
						else
						{
							sender.sendMessage(ChatColor.RED + "You cannot go backwards at the first step! Use " + ChatColor.GOLD + "/bfm next " + ChatColor.RED + "to go to the next step, or use " + 
									ChatColor.GOLD + "/bfm cancel " + ChatColor.RED + "to exit the wizard!");
						}
					}
					else if (rpg_p.GetEditMode() == RPG_EditMode.Outpost_Create)
					{						
						if (rpg_p.GetEditStep() > 0)
						{
							rpg_p.SetEditStep(rpg_p.GetEditStep() - 2);
							onPlayerChat(new AsyncPlayerChatEvent(true, rpg_p.GetPlayer(), "", null));
						}
						else
						{
							sender.sendMessage(ChatColor.RED + "You cannot go backwards at the first step! Use " + ChatColor.GOLD + "/bfm next " + ChatColor.RED + "to go to the next step, or use " + 
									ChatColor.GOLD + "/bfm cancel " + ChatColor.RED + "to exit the wizard!");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.YELLOW + "You are not creating anything at the moment. Use " + ChatColor.GOLD + "/bfm outpost new " + ChatColor.WHITE + "or " + ChatColor.GOLD + 
								"/bfm flag new " + ChatColor.WHITE + "to start creating an object");
						return false;
					}
				}
				else if (args[0].equalsIgnoreCase("cancel"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.bf.cancel"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (!RPG_Core.IsPlayer(sender))
					{
						sender.sendMessage(ChatColor.RED + "Only players can create new flags/outposts");
						return false;
					}
					
					if (rpg_p.GetEditMode() == RPG_EditMode.Flag_Create)
					{						
						sender.sendMessage(ChatColor.GREEN + "Flag creation canceled");
						
						rpg_p.SetEditMode(RPG_EditMode.None);
					}
					else if (rpg_p.GetEditMode() == RPG_EditMode.Flag_Edit)
					{
						sender.sendMessage(ChatColor.GREEN + "You have stopped editing Flags");
						
						rpg_p.SetEditMode(RPG_EditMode.None);
					}
					else if (rpg_p.GetEditMode() == RPG_EditMode.Outpost_Create)
					{						
						sender.sendMessage(ChatColor.GREEN + "Outpost creation canceled");
						
						rpg_p.SetEditMode(RPG_EditMode.None);
					}
					else if (rpg_p.GetEditMode() == RPG_EditMode.Outpost_Edit)
					{
						sender.sendMessage(ChatColor.GREEN + "You have stopped editing Outposts");
						
						rpg_p.SetEditMode(RPG_EditMode.None);
					}
					else
					{
						sender.sendMessage(ChatColor.YELLOW + "You are not creating anything at the moment. Use " + ChatColor.GOLD + "/bfm outpost new " + ChatColor.WHITE + "or " + ChatColor.GOLD + 
								"/bfm flag new " + ChatColor.WHITE + "to start creating an object");
						return false;
					}
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
	public void onPlayerChat(AsyncPlayerChatEvent e)
	{
		RPG_Player rpg_p = RPG_Core.GetPlayer(e.getPlayer().getName());
		
		if (rpg_p.GetEditMode() == RPG_EditMode.Flag_Create)
		{
			e.setCancelled(true);
			
			if (rpg_p.GetEditStep() == -1)
			{
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "At first, please enter a " + ChatColor.BLUE + "name " + ChatColor.WHITE + "for the Flag:");
				
				rpg_p.SetEditStep(0);
			}
			else if (rpg_p.GetEditStep() == 0)
			{
				if (e.getMessage() != "")
				{
					flagsInCreation.get(rpg_p.GetEditId()).Name = e.getMessage();
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Name: " + ChatColor.WHITE + e.getMessage());
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Now please enter the " + ChatColor.BLUE + "radius in blocks " + ChatColor.WHITE + "within which this flag can be " + ChatColor.BLUE + "captured" + ChatColor.WHITE + ".");
				rpg_p.SendMessage(ChatColor.WHITE + "Or you can use " + ChatColor.BLUE + "<default> " + ChatColor.WHITE + "to use the " + ChatColor.BLUE + "default " + ChatColor.WHITE + "capture radius.");
				
				rpg_p.SetEditStep(1);
			}
			else if (rpg_p.GetEditStep() == 1)
			{
				if (e.getMessage() != "")
				{
					float val = -1;
					
					if (e.getMessage().equalsIgnoreCase("<default>") || e.getMessage().equalsIgnoreCase("default") || e.getMessage().equalsIgnoreCase("<def>") || e.getMessage().equalsIgnoreCase("def"))
					{
						val = defaultCaptureRadius;
						flagsInCreation.get(rpg_p.GetEditId()).CaptureRadius = val;
					}
					else
					{
						try
						{
							val = Float.parseFloat(e.getMessage());
							if (val <= 0)
							{
								rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect capture radius, please enter a numeric value greater than zero, or type: " + ChatColor.GOLD + "/bfm cancel " + 
										ChatColor.RED + "to exit the wizard.");
								return;
							}
							
							flagsInCreation.get(rpg_p.GetEditId()).CaptureRadius = val;
						}
						catch (Exception ex)
						{
							rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect capture radius, please enter a numeric value greater than zero, or type: " + ChatColor.GOLD + "/bfm cancel " + 
									ChatColor.RED + "to exit the wizard.");
							return;
						}
					}
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Capture radius: " + ChatColor.WHITE + val);
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Now please enter the " + ChatColor.BLUE + "number of ticks " + ChatColor.WHITE + "needed by " + ChatColor.BLUE + "one person " + ChatColor.WHITE + "to " + 
						ChatColor.BLUE + "capture " + ChatColor.WHITE + "the flag.");
				rpg_p.SendMessage(ChatColor.WHITE + "A " + ChatColor.BLUE + "tick " + ChatColor.WHITE + "occurs every " + ChatColor.BLUE + (flagTickInterval / 20f) + ChatColor.WHITE + " seconds");
				
				rpg_p.SetEditStep(2);
			}
			else if (rpg_p.GetEditStep() == 2)
			{
				if (e.getMessage() != "")
				{
					float val = -1;
					
					if (e.getMessage().equalsIgnoreCase("<default>") || e.getMessage().equalsIgnoreCase("default") || e.getMessage().equalsIgnoreCase("<def>") || e.getMessage().equalsIgnoreCase("def"))
					{
						val = defaultCaptureTime;
						flagsInCreation.get(rpg_p.GetEditId()).CaptureRadius = val;
					}
					else
					{
						try
						{
							val = Float.parseFloat(e.getMessage());
							if (val <= 0)
							{
								rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect capture time, please enter a numeric value greater than zero, or type: " + ChatColor.GOLD + "/bfm cancel " + ChatColor.RED + "to exit the wizard.");
								return;
							}
							
							flagsInCreation.get(rpg_p.GetEditId()).CaptureTime = val;
						}
						catch (Exception ex)
						{
							rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect capture time, please enter a numeric value greater than zero, or type: " + ChatColor.GOLD + "/bfm cancel " + ChatColor.RED + "to exit the wizard.");
							return;
						}
					}
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Capture time: " + ChatColor.WHITE + val);
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "You are almost done, please review your data, and enter " + ChatColor.GOLD + "/bfm flag create " + ChatColor.WHITE + "to create the flag at your players " + 
						ChatColor.BLUE + "current position" + ChatColor.WHITE + ". Or enter " + ChatColor.GOLD + "/bfm cancel " + ChatColor.WHITE + "to exit the wizard.");
				RPG_FlagCreationData d = flagsInCreation.get(rpg_p.GetEditId());
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.BLUE + "Name: " + ChatColor.WHITE + d.Name);
				rpg_p.SendMessage(ChatColor.BLUE + "Capture radius: " + ChatColor.WHITE + d.CaptureRadius);
				rpg_p.SendMessage(ChatColor.BLUE + "Capture time: " + ChatColor.WHITE + d.CaptureTime);
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				
				rpg_p.SetEditStep(100);
			}
		}
		else if (rpg_p.GetEditMode() == RPG_EditMode.Outpost_Create)
		{
			if (rpg_p.GetEditStep() == -1)
			{
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "At first, please enter a " + ChatColor.BLUE + "name " + ChatColor.WHITE + "for the Outpost:");
				
				rpg_p.SetEditStep(0);
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e)
	{
		for (RPG_Flag flag : GetFlags())
		{
			if (flag.GetLocation().getBlockX() == e.getBlock().getX() && flag.GetLocation().getBlockZ() == e.getBlock().getZ() && 
					flag.GetLocation().getBlockY() <= e.getBlock().getY() && flag.GetLocation().getBlockY() + 10 > e.getBlock().getY())
			{
				e.setCancelled(true);
				break;
			}
		}
	}
	
	public void onThreadFlagTick()
	{
		asyncRunning = true;
		
		try
		{
			for (RPG_Flag flag : GetFlags())
			{
				flag.ResetPlayersFromNations();
				
				for (RPG_Player rpg_p : RPG_Core.GetOnlinePlayers())
				{
					if (rpg_p.GetNationId() == -1)
						continue;
					
					if (!RPG_Core.HasPermission(rpg_p, "rpg.bf.flag.capture"))
					{
						rpg_p.SendMessage(RPG_Core.GetFormattedMessage("no_perm", rpg_p.GetLanguage()));
						RPG_Core.Log(rpg_p.GetID(), -1, RPG_LogType.Warning, "Action", "NoPerm: rpg.bf.flag.capture");
						continue;
					}
					
					if (rpg_p.GetPlayer().getLocation().getWorld().getUID().equals(flag.GetLocation().getWorld().getUID()))
					{
						if (rpg_p.GetPlayer().getLocation().distance(flag.GetLocation()) < flag.GetCaptureRadius())
							flag.AddPlayerFromNation(rpg_p.GetNationId());
					}
				}
				
				int oldnation = flag.GetNationID();
				int oldcap = flag.GetCaptureValue();
				
				flag.UpdateCaptureValue();
				
				if (oldnation != flag.GetNationID() || oldcap != flag.GetCaptureValue())
				{
					if (flag.GetCaptureValue() == flag.GetCaptureTime())
					{
						RPG_Core.Log(oldnation, flag.GetNationID(), RPG_LogType.Information, "FlagCapture", "Captured");
						FlagCaptured(flag.GetID(), flag.GetNationID());
						for (RPG_Player rpg_p : RPG_Core.GetOnlinePlayers())
						{
							String title = "";
							if (rpg_p.GetNationId() == flag.GetNationID())
							{
								rpg_p.SendMessage(RPG_Core.GetFormattedMessage("flag_captured", rpg_p.GetLanguage(), flag));
								title = RPG_Core.FormatTextForAchievementMessage(RPG_Core.GetMessageInLanguage("flag_captured_short", rpg_p.GetLanguage()));
							}
							else if (rpg_p.GetNationId() != -1)
							{
								rpg_p.SendMessage(RPG_Core.GetFormattedMessage("flag_lost", rpg_p.GetLanguage(), flag));
								title = RPG_Core.FormatTextForAchievementMessage(RPG_Core.GetMessageInLanguage("flag_lost_short", rpg_p.GetLanguage()));
							}
							
							if (RPG_Core.IsSpoutEnabled() && rpg_p.IsSpoutPlayer())
							{
								String text = RPG_Core.FormatTextForAchievementMessage(flag.GetName(rpg_p.GetLanguage()));
								SpoutManager.getPlayer(rpg_p.GetPlayer()).sendNotification(title, text, Material.BOOK_AND_QUILL);
							}
						}
					}
					
					float cieling = (float)flagTowerHeight / flag.GetCaptureTime() * flag.GetCaptureValue();
					for (int i = 0; i < flagTowerHeight; i++)
					{
						if (i < cieling)
							flag.GetLocation().getWorld().getBlockAt(flag.GetLocation().getBlockX(), flag.GetLocation().getBlockY() + i, flag.GetLocation().getBlockZ()).setType(flag.GetNation().GetBlockMaterial());
						else
							flag.GetLocation().getWorld().getBlockAt(flag.GetLocation().getBlockX(), flag.GetLocation().getBlockY() + i, flag.GetLocation().getBlockZ()).setType(defaultFlagBlockMaterial);
					}
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		asyncRunning = false;
	}

	// 
	// Flags
	// 
	public RPG_Flag GetFlag(int ID)
	{
		if (flags.containsKey(ID))
			return flags.get(ID);
		else
		{
			logger.warning(prefix + "Flag with id " + ID + " does not exist");
			RPG_Core.Log(-1, ID, RPG_LogType.Warning, "Flag", "Does not exist");
			return null;
		}
	}
	public Collection<RPG_Flag> GetFlags()
	{
		return flags.values();
	}
	
	public void FlagCaptured(int FlagID, int NationID)
	{
		Statement stmt = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			
			stmt.executeUpdate("UPDATE " + SQLTablePrefix + "flags SET NationID = " + NationID + " WHERE ID = " + FlagID + ";");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "FlagUpdate", ex);
		}
	}
	
	public int AddFlagToDB(RPG_FlagCreationData data, Location pos, RPG_Language lang)
	{
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			
			int nameid = -1;
			stmt.executeUpdate("CALL " + SQLTablePrefix + "textSave('" + lang + "','" + data.Name + "',@textid)");
			
			rs = stmt.executeQuery("SELECT @textid AS TextID;");
			rs.first();
			nameid = rs.getInt(1);
			
			int worldId = -1;
			rs = stmt.executeQuery("SELECT ID FROM " + SQLTablePrefix + "worlds WHERE WorldName='" + pos.getWorld().getName() + "';");
			rs.first();
			worldId = rs.getInt(1);
			
			stmt.executeUpdate("INSERT INTO " + SQLTablePrefix + "flags (NameId, NationId, WorldId, PosX, PosY, PosZ, CaptureRadius, CaptureTime) " +
					"VALUES (" + nameid + ",-1," + worldId + "," + pos.getBlockX() + "," + pos.getBlockY() + "," + pos.getBlockZ() + "," + 
					data.CaptureRadius + "," + data.CaptureTime + ");");
			
			rs = stmt.executeQuery("select last_insert_id();");
			rs.last();
			int id = rs.getInt(1);
			
			RPG_Core.Log(-1, id, RPG_LogType.Information, "FlagCreate", "Created");
			
			RPG_Core.ReloadText(nameid);
			
			return id;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "FlagCreate", ex);
			
			return -1;
		}
	}
	public boolean RemoveFlagFromDB(int ID)
	{
		Statement stmt = null;
		
		try
		{
			RPG_Flag rpg_f = GetFlag(ID);
			if (rpg_f == null)
				return false;
			
			stmt = RPG_Core.GetDatabaseStatement();
			stmt.executeUpdate("DELETE FROM " + SQLTablePrefix + "flags WHERE ID = " + ID + ";");
			
			RPG_Core.Log(-1, ID, RPG_LogType.Information, "FlagDelete", "Deleted");
			
			return true;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "FlagDelete", ex);
			return false;
		}
	}
	
	public void LoadFlagsFromDB()
	{
		logger.info(prefix + "Loading flags...");
		
		flags.clear();
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			rs = stmt.executeQuery("SELECT " + SQLTablePrefix + "flags.ID, NameId, NationId, WorldName, PosX, PosY, PosZ, CaptureRadius, CaptureTime FROM " + SQLTablePrefix + "flags " +
					"INNER JOIN " + SQLTablePrefix + "worlds ON " + SQLTablePrefix + "flags.WorldId = " + SQLTablePrefix + "worlds.ID;");
			
			while(rs.next())
			{
				Location loc = new Location(Bukkit.getServer().getWorld(rs.getString("WorldName")), rs.getDouble("PosX"), rs.getDouble("PosY"), rs.getDouble("PosZ"));
				
				flags.put(rs.getInt(1), new RPG_Flag(rs.getInt(1), rs.getInt("NameId"), rs.getInt("NationId"), loc, rs.getInt("CaptureRadius"), rs.getInt("CaptureTime")));
				RPG_Core.Log(-1, rs.getInt(1), RPG_LogType.Information, "Flag", "Loaded");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "Flag", ex);
		}
		
		logger.info(prefix + "  Loaded " + flags.size() + " flag(s) from database!");
	}
	
	// 
	// Outposts
	// 
	public RPG_Outpost GetOutpost(int ID)
	{
		if (outposts.containsKey(ID))
			return outposts.get(ID);
		else
		{
			logger.warning(prefix + "Outpost with id " + ID + " does not exist");
			RPG_Core.Log(-1, ID, RPG_LogType.Warning, "Outpost", "Does not exist");
			return null;
		}
	}
	public Collection<RPG_Outpost> GetOutposts()
	{
		return outposts.values();
	}
	
	public void LoadOutpostsFromDB()
	{
		logger.info(prefix + "Loading outposts...");
		
		outposts.clear();
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			rs = stmt.executeQuery("SELECT " + SQLTablePrefix + "outposts.ID, NameID, WorldName, MinX, MinY, MinZ, MaxX, MaxY, MaxZ, FlagID FROM " + SQLTablePrefix + "outposts " +
					"INNER JOIN " + SQLTablePrefix + "regions ON " + SQLTablePrefix + "outposts.RegionID = " + SQLTablePrefix + "regions.ID " +
					"INNER JOIN " + SQLTablePrefix + "worlds ON " + SQLTablePrefix + "regions.WorldID = " + SQLTablePrefix + "worlds.ID;");
			
			while(rs.next())
			{
				RPG_Region reg = new RPG_Region(new Location(Bukkit.getServer().getWorld(rs.getString("WorldName")), rs.getDouble("MinX"), rs.getDouble("MinY"), rs.getDouble("MinZ")), 
						new Location(Bukkit.getServer().getWorld(rs.getString("WorldName")), rs.getDouble("MaxX"), rs.getDouble("MaxY"), rs.getDouble("MaxZ")));
				
				ArrayList<RPG_OutpostBlock> blocks = new ArrayList<RPG_OutpostBlock>();
				Statement stmt_blocks = RPG_Core.GetDatabaseStatement();
				ResultSet rs_blocks = stmt_blocks.executeQuery("SELECT " + SQLTablePrefix + "outpostsblocks.ID, OutpostID, Type, Data, WorldName, PosX, PosY, PosZ FROM " + SQLTablePrefix + "outpostsblocks " +
						"INNER JOIN " + SQLTablePrefix + "positions ON " + SQLTablePrefix + "outpostsblocks.PosID = " + SQLTablePrefix + "positions.ID " +
						"INNER JOIN " + SQLTablePrefix + "worlds ON " + SQLTablePrefix + "positions.WorldID = " + SQLTablePrefix + "worlds.ID " + 
						"WHERE OutpostID = " + rs.getInt(1) + ";");
				
				while(rs_blocks.next())
				{
					Location loc = new Location(Bukkit.getServer().getWorld(rs_blocks.getString("WorldName")), rs_blocks.getDouble("PosX"), rs_blocks.getDouble("PosY"), rs_blocks.getDouble("PosZ"));
					blocks.add(new RPG_OutpostBlock(rs_blocks.getInt(1), rs_blocks.getInt("OutpostID"), rs_blocks.getInt("Type"), rs_blocks.getByte("Data"), loc));
				}
				
				outposts.put(rs.getInt(1), new RPG_Outpost(rs.getInt(1), rs.getInt("NameID"), reg, rs.getInt("FlagID"), blocks));
				RPG_Core.Log(-1, rs.getInt(1), RPG_LogType.Information, "Outpost", blocks.size() + " blocks");
				RPG_Core.Log(-1, rs.getInt(1), RPG_LogType.Information, "Outpost", "Loaded");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "Outpost", ex);
		}
		
		logger.info(prefix + "  Loaded " + outposts.size() + " outpost(s) from database!");
	}
}

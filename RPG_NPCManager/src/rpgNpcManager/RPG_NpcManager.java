package rpgNpcManager;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.event.screen.TextFieldChangeEvent;
import org.getspout.spoutapi.gui.WidgetType;

import rpgCore.RPG_Core;
import rpgGUI.RPG_InfoScreenItem;
import rpgInterfaces.RPG_INpcManager;
import rpgNpc.RPG_Npc;
import rpgNpc.RPG_NpcArmourClass;
import rpgNpc.RPG_NpcCreationData;
import rpgNpc.RPG_NpcData;
import rpgNpc.RPG_NpcTemplate;
import rpgOther.RPG_LogType;
import rpgOther.RPG_Teleport;
import rpgPlayer.RPG_EditMode;
import rpgPlayer.RPG_Player;
import rpgQuest.RPG_Quest;
import rpgTexts.RPG_Language;

public class RPG_NpcManager extends JavaPlugin implements Listener, RPG_INpcManager
{
	private String prefix = "[RPG NPC Manager] ";
	private Logger logger = Logger.getLogger("Minecraft");
	private String SQLTablePrefix;
	
	private boolean asyncRunning = false;
	private BukkitTask task;
	private int npcTickInterval = 0;
	private double talkdist = 0;
	
	private int npcCreationCounter = 0;
	private HashMap<Integer, RPG_NpcCreationData> npcsInCreation = new HashMap<Integer, RPG_NpcCreationData>();
	
	private HashMap<Integer, RPG_Npc> npcs = new HashMap<Integer, RPG_Npc>();
	private HashMap<Integer, RPG_NpcData> npcdata = new HashMap<Integer, RPG_NpcData>();
	private HashMap<UUID, Integer> npcuuids = new HashMap<UUID, Integer>();
	private HashMap<String, RPG_NpcTemplate> npctemplates = new HashMap<String, RPG_NpcTemplate>();
	
	
	// 
	// Main
	// 
	public RPG_NpcManager()
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
		pm.registerEvent(AsyncPlayerChatEvent.class, this, EventPriority.NORMAL, RPG_Core.GetEventExecutor(), this);
		pm.registerEvent(EntityDamageByEntityEvent.class, this, EventPriority.NORMAL, RPG_Core.GetEventExecutor(), this);
		if (RPG_Core.IsSpoutEnabled())
		{
			pm.registerEvent(ButtonClickEvent.class, this, EventPriority.NORMAL, RPG_Core.GetEventExecutor(), this);
			pm.registerEvent(TextFieldChangeEvent.class, this, EventPriority.NORMAL, RPG_Core.GetEventExecutor(), this);
		}
		else
			logger.info(prefix + "  Skipped Spout events!");
		
		logger.info(prefix + "Adding info screens...");
		
		ArrayList<RPG_InfoScreenItem> items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("new", 		"Start creating a new NPC", 	"rpg.npc.new"));
		items.add(new RPG_InfoScreenItem("edit", 		"Edit an existing NPC", 		"rpg.npc.edit", 		true));
		items.add(new RPG_InfoScreenItem("del", 		"Delete a NPC",					"rpg.npc.del",			true));
		items.add(new RPG_InfoScreenItem("list", 		"List all the NPCs", 			"rpg.npc.list"));
		items.add(new RPG_InfoScreenItem("reload", 		"Reload all the NPCs", 			"rpg.npc.reload"));
		items.add(new RPG_InfoScreenItem("templates",	"Manages the NPC templates",	"rpg.npc.templates",	true));
		items.add(new RPG_InfoScreenItem("tp", 			"Teleports to an NPC", 			"rpg.npc.tp",			true));
		RPG_Core.AddInfoScreen("rpg.npc", "NPC Manager", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("name", 	"Change the name of the NPC", 					"rpg.npc.edit.name", 	true));
		items.add(new RPG_InfoScreenItem("nation", 	"Edit the Nation ID that the NPC belongs to", 	"rpg.npc.edit.nation",	true));
		items.add(new RPG_InfoScreenItem("level", 	"Change the level of the NPC", 					"rpg.npc.edit.level", 	true));
		items.add(new RPG_InfoScreenItem("money", 	"Change the amount of money the NPC has", 		"rpg.npc.edit.money", 	true));
		items.add(new RPG_InfoScreenItem("shop", 	"Edit the Shop ID of the NPC",					"rpg.npc.edit.shop", 	true));
		items.add(new RPG_InfoScreenItem("move", 	"Move the NPC to the current player location",	"rpg.npc.edit.move"));
		items.add(new RPG_InfoScreenItem("text", 	"Change the standard text of the NPC", 			"rpg.npc.edit.text", 	true));
		items.add(new RPG_InfoScreenItem("item", 	"Edit the item that the NCP is holding", 		"rpg.npc.edit.item", 	true));
		items.add(new RPG_InfoScreenItem("head", 	"Edit the head armour of the NPC", 				"rpg.npc.edit.head", 	true));
		items.add(new RPG_InfoScreenItem("chest", 	"Edit the chest armour of the NPC", 			"rpg.npc.edit.chest", 	true));
		items.add(new RPG_InfoScreenItem("legs", 	"Edit the leg armour of the NPC", 				"rpg.npc.edit.legs", 	true));
		items.add(new RPG_InfoScreenItem("feet", 	"Edit the feet armour of the NPC", 				"rpg.npc.edit.feet", 	true));
		RPG_Core.AddInfoScreen("rpg.npc.edit", "Edit a NPC", items);
		
		items = new ArrayList<RPG_InfoScreenItem>(items);
		items.add(0, new RPG_InfoScreenItem(ChatColor.GREEN + "You are now editing npcs. Select an NPC by clicking it, ", "", ""));
		items.add(1, new RPG_InfoScreenItem(ChatColor.GREEN + "or specify it's ID before one of the following arguments:", "", ""));
		RPG_Core.AddInfoScreen("rpg.npc.editing", "Edit a NPC", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Changes the name of the NPC", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /npc edit [id] name [name]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [id]", ChatColor.GREEN + "(optional) " + ChatColor.WHITE + "The ID of the NPC", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [name]", "The new name of the NPC", ""));
		RPG_Core.AddInfoScreen("rpg.npc.edit.name", "Edit a NPC name", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Changes the nation that the NPC belongs to", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /npc edit [id] nation [nationid]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [id]", ChatColor.GREEN + "(optional) " + ChatColor.WHITE + "The ID of the NPC", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [nationid]", "The ID of the new nation that the NPC belongs to", ""));
		RPG_Core.AddInfoScreen("rpg.npc.edit.nation", "Edit the nation of a NPC", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Changes the level of a NPC", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /npc edit [id] level [lvl]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [id]", ChatColor.GREEN + "(optional) " + ChatColor.WHITE + "The ID of the NPC", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [lvl]", "The new level of the NPC", ""));
		RPG_Core.AddInfoScreen("rpg.npc.edit.level", "Edit the level of a NPC", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Changes the amount of money a NPC has", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /npc edit [id] money [amount]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [id]", ChatColor.GREEN + "(optional) " + ChatColor.WHITE + "The ID of the NPC", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [amount]", "The amount of money the NPC has", ""));
		RPG_Core.AddInfoScreen("rpg.npc.edit.money", "Edit the money of a NPC", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Changes the shop that is connected to a NPC", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /npc edit [id] shop [shopid]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [id]", ChatColor.GREEN + "(optional) " + ChatColor.WHITE + "The ID of the NPC", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [shopid]", "The new ID of the shop the NPC is connected to", ""));
		RPG_Core.AddInfoScreen("rpg.npc.edit.shop", "Edit the shop of a NPC", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Changes the standard text that a NPC says to a player", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /npc edit [id] text [lang] [text]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [id]", ChatColor.GREEN + "(optional) " + ChatColor.WHITE + "The ID of the NPC", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [lang]", "The language which is edited", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "    DE", "Change the German text", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "    EN", "Change the English text", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "    FR", "Change the French text", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [text]", "The new text in the specified language", ""));
		RPG_Core.AddInfoScreen("rpg.npc.edit.text", "Edit the text of a NPC", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Changes the item that a NPC is holding in hand", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /npc edit [id] item [itemid]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [id]", ChatColor.GREEN + "(optional) " + ChatColor.WHITE + "The ID of the NPC", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [itemid]", "The ID of the item that the NPC is holding", ""));
		RPG_Core.AddInfoScreen("rpg.npc.edit.item", "Change the item of a NPC", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Changes the item that the NPC is using as head armour", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /npc edit [id] head [itemid]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [id]", ChatColor.GREEN + "(optional) " + ChatColor.WHITE + "The ID of the NPC", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [itemid]", "The ID of the item that the NPC is using as head armor", ""));
		RPG_Core.AddInfoScreen("rpg.npc.edit.head", "Edit the head armour of a NPC", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Changes the item that the NPC is using as chest armour", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /npc edit [id] chest [itemid]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [id]", ChatColor.GREEN + "(optional) " + ChatColor.WHITE + "The ID of the NPC", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [itemid]", "The ID of the item that the NPC is using as chest armor", ""));
		RPG_Core.AddInfoScreen("rpg.npc.edit.chest", "Edit the chest armour of a NPC", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Changes the item that the NPC is using as legs armour", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /npc edit [id] legs [itemid]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [id]", ChatColor.GREEN + "(optional) " + ChatColor.WHITE + "The ID of the NPC", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [itemid]", "The ID of the item that the NPC is using as legs armor", ""));
		RPG_Core.AddInfoScreen("rpg.npc.edit.legs", "Edit the legs armour of a NPC", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Changes the item that the NPC is using as feet armour", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /npc edit [id] feet [itemid]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [id]", ChatColor.GREEN + "(optional) " + ChatColor.WHITE + "The ID of the NPC", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [itemid]", "The ID of the item that the NPC is using as feet armor", ""));
		RPG_Core.AddInfoScreen("rpg.npc.edit.feet", "Edit the feet armour of a NPC", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Deletes the NPC with the specified ID from the server", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /npc del [id]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [id]", "The ID of the NPC", ""));
		RPG_Core.AddInfoScreen("rpg.npc.del", "Delete a NPC", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Saves a NPC Template with the specified name", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /npc save [Name]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [Name]", "The unique name of the template", ""));
		RPG_Core.AddInfoScreen("rpg.npc.save", "Save a NPC Template", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("del", 	"Delete an exisiting template", 			"rpg.npc.template.delete"));
		items.add(new RPG_InfoScreenItem("list", 	"List all the available templates", 		"rpg.npc.template.list"));
		items.add(new RPG_InfoScreenItem("reload",	"Reload the templates from the database",	"rpg.npc.template.reload"));
		RPG_Core.AddInfoScreen("rpg.npc.templates", "NPC Templates", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Teleports you to the location of a NPC", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /npc tp [id]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [id]", "The ID of the NPC", ""));
		RPG_Core.AddInfoScreen("rpg.npc.tp", "Teleport to a NPC", items);
		
		logger.info(prefix + "Loading config...");
		
		getConfig().addDefault("talking_distance", 4.0);
		getConfig().addDefault("npcTickInterval", 20);
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		talkdist = getConfig().getDouble("talking_distance");
		npcTickInterval = getConfig().getInt("npcTickInterval");
		
		LoadNPCDataFromDB();
		
		LoadNPCsFromData();
		
		LoadNPCTemplatesFromDB();
		
		SpawnNPCs();
		
		logger.info(prefix + "Starting threads...");
		task = getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() { public void run() { onNPCUpdateTick(); } }, npcTickInterval, npcTickInterval);
		
		PluginDescriptionFile pdf = this.getDescription();
		logger.info(prefix +  pdf.getName() + " version " + pdf.getVersion() + " is enabled");
		RPG_Core.Log(RPG_LogType.Information, "Start", pdf.getName() + " version " + pdf.getVersion() + " enabled");
	}
	@Override
	public void onDisable()
	{		
		DeSpawnNPCs();
		
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
		
		if (commandLabel.equalsIgnoreCase("npc"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.npc"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (args.length < 1)
			{
				RPG_Core.ShowInfoScreen("rpg.npc", sender);
			}
			else
			{
				if (args[0].equalsIgnoreCase("new"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.npc.new"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (!RPG_Core.IsPlayer(sender))
					{
						sender.sendMessage(ChatColor.RED + "Only players can create new NPCs");
						return false;
					}
					
					npcCreationCounter++;
					npcsInCreation.put(npcCreationCounter, new RPG_NpcCreationData());
					
					rpg_p.SetEditMode(RPG_EditMode.NPC_Create);
					rpg_p.SetEditId(npcCreationCounter);
					
					if (rpg_p.IsSpoutPlayer())
					{
						RPG_Screen_CreateNpc scr = new RPG_Screen_CreateNpc(this, rpg_p, npcsInCreation.get(rpg_p.GetEditId()));
						scr.Show();
					}
					else
					{
						sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
						sender.sendMessage(ChatColor.BLUE + "NPC Creation Wizard");
						sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
						sender.sendMessage(ChatColor.WHITE + "Welcome to the NPC creation wizard. This wizard will help you create a NPC with a few simple steps!");
						sender.sendMessage(ChatColor.WHITE + "Please simply enter the requested data into the chat. You may exit this wizard at any time using: " + ChatColor.GOLD + "/npc cancel");
						
						rpg_p.SetEditStep(-1);
						
						onPlayerChat(new AsyncPlayerChatEvent(true, rpg_p.GetPlayer(), "", null));
					}
				}
				else if (args[0].equalsIgnoreCase("create"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.npc.create"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (!RPG_Core.IsPlayer(sender))
					{
						sender.sendMessage(ChatColor.RED + "Only players can create new NPCs");
						return false;
					}
					
					if (args.length > 1)
					{
						if (!RPG_Core.HasPermission(sender, "rpg.npc.create.template"))
						{
							sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
							RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
							return false;
						}
						
						RPG_NpcTemplate temp = GetNPCTemplate(args[1]);
						if (temp == null)
						{
							sender.sendMessage(ChatColor.RED + "The name you have entered is not a valid template name! Use " + ChatColor.GOLD + "/npc template list " + ChatColor.RED + "for a list of all the templates.");
							return false;
						}
						else
						{
							if (!RPG_Core.HasPermission(sender, "rpg.npc.create.template.*") && !RPG_Core.HasPermission(sender, "rpg.npc.create.template." + temp.GetID()))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							int id = AddNPCToDB(temp, rpg_p.GetPlayer().getLocation());
							
							if (id >= 0)
								sender.sendMessage(ChatColor.GREEN + "NPC with id " + ChatColor.BLUE + id + ChatColor.GREEN + " created");
							else
							{
								sender.sendMessage(ChatColor.RED + "NPC creation failed!");
								return false;
							}
						}
					}
					else
					{
						if (!RPG_Core.HasPermission(sender, "rpg.npc.create.wizard"))
						{
							sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
							RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
							return false;
						}
						
						if (rpg_p.GetEditId() == -1)
						{
							sender.sendMessage(ChatColor.YELLOW + "You are not creating a NPC at the moment. Start creating one by using: " + ChatColor.GOLD + "/npc new");
							sender.sendMessage(ChatColor.YELLOW + "Or use " + ChatColor.GOLD + "/npc create [name] " + ChatColor.YELLOW + "to create a NPC from a template");
							return false;
						}
						
						if (rpg_p.GetEditStep() < 100)
						{
							sender.sendMessage(ChatColor.YELLOW + "Please first finish all the steps of the NPC creation wizard before creating the NPC!");
							return false;
						}
						
						int id = AddNPCToDB(npcsInCreation.get(rpg_p.GetEditId()), rpg_p.GetPlayer().getLocation(), rpg_p.GetLanguage());
						
						if (id >= 0)
						{
							sender.sendMessage(ChatColor.GREEN + "NPC with id " + ChatColor.BLUE + id + ChatColor.GREEN + " created");
							
							npcsInCreation.remove(rpg_p.GetEditId());
							rpg_p.SetEditMode(RPG_EditMode.None);
						}
						else
						{
							sender.sendMessage(ChatColor.RED + "NPC creation failed!");
							return false;
						}
					}
				}
				else if (args[0].equalsIgnoreCase("next"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.npc.next"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (!RPG_Core.IsPlayer(sender))
					{
						sender.sendMessage(ChatColor.RED + "Only players can create new NPCs");
						return false;
					}
					
					if (rpg_p.GetEditMode() == RPG_EditMode.NPC_Create)
					{						
						if (rpg_p.GetEditStep() < 100)
							onPlayerChat(new AsyncPlayerChatEvent(true, rpg_p.GetPlayer(), "", null));
						else
						{
							sender.sendMessage(ChatColor.RED + "You are already done with the wizard. Please use " + ChatColor.GOLD + "/npc back " + ChatColor.RED + "to go back a step, or use " + 
								ChatColor.GOLD + "/npc create " + ChatColor.RED + "to create your NPC. You can use " + ChatColor.GOLD + "/npc cancel " + ChatColor.RED + "to exit the wizard.");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.YELLOW + "You are not creating a NPC at the moment. Start creating one by using: " + ChatColor.GOLD + "/npc new");
						return false;
					}
				}
				else if (args[0].equalsIgnoreCase("back"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.npc.back"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (!RPG_Core.IsPlayer(sender))
					{
						sender.sendMessage(ChatColor.RED + "Only players can create new NPCs");
						return false;
					}
					
					if (rpg_p.GetEditMode() == RPG_EditMode.NPC_Create)
					{						
						if (rpg_p.GetEditStep() > 0)
						{
							rpg_p.SetEditStep(rpg_p.GetEditStep() - 2);
							onPlayerChat(new AsyncPlayerChatEvent(true, rpg_p.GetPlayer(), "", null));
						}
						else
						{
							sender.sendMessage(ChatColor.RED + "You cannot go backwards at the first step! Use " + ChatColor.GOLD + "/npc next " + ChatColor.RED + "to go to the next step, or use " + 
									ChatColor.GOLD + "/npc cancel " + ChatColor.RED + "to exit the wizard!");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.YELLOW + "You are not creating a NPC at the moment. Start creating one by using: " + ChatColor.GOLD + "/npc new");
						return false;
					}
				}
				else if (args[0].equalsIgnoreCase("cancel"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.npc.cancel"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (rpg_p.GetEditMode() == RPG_EditMode.NPC_Create)
					{						
						sender.sendMessage(ChatColor.GREEN + "NPC creation canceled");
						
						npcsInCreation.remove(rpg_p.GetEditId());
						rpg_p.SetEditMode(RPG_EditMode.None);
					}
					else if (rpg_p.GetEditMode() == RPG_EditMode.NPC_Edit)
					{
						sender.sendMessage(ChatColor.GREEN + "You have stopped editing NPCs");
						
						rpg_p.SetEditMode(RPG_EditMode.None);
					}
					else
					{
						sender.sendMessage(ChatColor.YELLOW + "You are not creating or editing a NPC at the moment. Start creating one by using: " + ChatColor.GOLD + "/npc new " + ChatColor.WHITE + "or use " + 
								ChatColor.GOLD + "/npc edit " + ChatColor.WHITE + "to edit an existing one");
						return false;
					}
				}
				else if (args[0].equalsIgnoreCase("edit"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.npc.edit"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (args.length < 2)
					{
						rpg_p.SetEditMode(RPG_EditMode.NPC_Edit);
						
						RPG_Core.ShowInfoScreen("rpg.npc.editing", sender);
					}
					else
					{
						if (rpg_p.GetEditMode() != RPG_EditMode.NPC_Edit)
						{
							sender.sendMessage(ChatColor.YELLOW + "You are not editing a NPC at the moment. Start editing one by using " + ChatColor.GOLD + "/npc edit");
							return false;
						}
						
						RPG_Npc rpg_npc = GetNPC(rpg_p.GetEditId());
						
						int argBase = 1;
						if (rpg_npc == null)
						{
							int id = -1;
							try
							{
								id = Integer.parseInt(args[1]);
								argBase++;
								rpg_p.SetEditId(id);
							}
							catch (Exception ex)
							{ }
						}
						
						if (rpg_p.GetEditId() == -1)
						{
							sender.sendMessage(ChatColor.YELLOW + "You have not selected a NPC to edit. Click any NPC to edit it");
							return false;
						}
						
						if (args[argBase].equalsIgnoreCase("name"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.npc.edit.name"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (args.length < argBase + 2)
							{
								RPG_Core.ShowInfoScreen("rpg.npc.edit.name", sender);
							}
							else
							{
								String oldname = rpg_npc.GetName();
								rpg_npc.SetName(args[argBase + 1]);
								sender.sendMessage(ChatColor.GREEN + "The NPC's name has been changed from " + ChatColor.BLUE + oldname + ChatColor.GREEN + " to " + ChatColor.BLUE + args[argBase + 1]);
							}
						}
						else if (args[argBase].equalsIgnoreCase("nation"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.npc.edit.nation"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (args.length < argBase + 2)
							{
								RPG_Core.ShowInfoScreen("rpg.npc.edit.nation", sender);
							}
							else
							{
								int oldnation = rpg_npc.GetNationID();
								
								try
								{
									int newnation = Integer.parseInt(args[argBase + 1]);
									rpg_npc.SetNationID(newnation);
									sender.sendMessage(ChatColor.GREEN + "The NPC's nation ID has been changed from " + ChatColor.BLUE + oldnation + ChatColor.GREEN + " to " + ChatColor.BLUE + newnation);
								}
								catch (Exception ex)
								{
									sender.sendMessage(ChatColor.RED + "You have entered an invald nation ID");
								}
							}
						}
						else if (args[argBase].equalsIgnoreCase("level"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.npc.edit.level"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (args.length < argBase + 2)
							{
								RPG_Core.ShowInfoScreen("rpg.npc.edit.level", sender);
							}
							else
							{
								int oldlvl = rpg_npc.GetLevel();
								
								try
								{
									int newlvl = Integer.parseInt(args[argBase]);
									rpg_npc.SetLevel(newlvl);
									sender.sendMessage(ChatColor.GREEN + "The NPC's level has been changed from " + ChatColor.BLUE + oldlvl + ChatColor.GREEN + " to " + ChatColor.BLUE + newlvl);
								}
								catch (Exception ex)
								{
									sender.sendMessage(ChatColor.RED + "You have entered an invald level value");
								}
							}
						}
						else if (args[argBase].equalsIgnoreCase("money"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.npc.edit.money"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (args.length < argBase + 2)
							{
								RPG_Core.ShowInfoScreen("rpg.npc.edit.money", sender);
							}
							else
							{
								int oldmoney = rpg_npc.GetMoney();
								
								try
								{
									int newmoney = Integer.parseInt(args[argBase + 1]);
									rpg_npc.SetMoney(newmoney);
									sender.sendMessage(ChatColor.GREEN + "The NPC's money has been changed from " + ChatColor.BLUE + oldmoney + ChatColor.GREEN + " to " + ChatColor.BLUE + newmoney);
								}
								catch (Exception ex)
								{
									sender.sendMessage(ChatColor.RED + "You have entered an invald money amount");
								}
							}
						}
						else if (args[argBase].equalsIgnoreCase("shop"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.npc.edit.shop"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (args.length < argBase + 2)
							{
								RPG_Core.ShowInfoScreen("rpg.npc.edit.shop", sender);
							}
							else
							{
								int oldshop = rpg_npc.GetShopID();
								
								try
								{
									int newshop = Integer.parseInt(args[argBase + 1]);
									rpg_npc.SetShopID(newshop);
									sender.sendMessage(ChatColor.GREEN + "The NPC's shop ID has been changed from " + ChatColor.BLUE + oldshop + ChatColor.GREEN + " to " + ChatColor.BLUE + newshop);
								}
								catch (Exception ex)
								{
									sender.sendMessage(ChatColor.RED + "You have entered an invald shop ID");
								}
							}
						}
						else if (args[argBase].equalsIgnoreCase("move"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.npc.edit.move"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (!RPG_Core.IsPlayer(sender))
							{
								sender.sendMessage(ChatColor.RED + "Only players can move NPCs");
								return false;
							}
							
							if (MoveNPC(rpg_npc.GetID(), rpg_p.GetPlayer().getLocation()) == true)
								sender.sendMessage(ChatColor.GREEN + "The NPC has been moved to your current location");
							else
								sender.sendMessage(ChatColor.RED + "Could not move NPC");
						}
						else if (args[argBase].equalsIgnoreCase("text"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.npc.edit.text"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (args.length < argBase + 3)
							{
								RPG_Core.ShowInfoScreen("rpg.npc.edit.text", sender);
							}
							else
							{
								String newtext = "";
								for (int i = argBase + 2; i < args.length; i++)
									newtext += args[i] + " ";
								
								RPG_Language lang = RPG_Core.StringToLanguage(args[argBase + 1]);
								
								if (lang == null)
									sender.sendMessage(ChatColor.RED + "You have entered an invald language");
								else
								{
									EditNPCText(rpg_npc.GetID(), rpg_npc.GetStandardText().GetID(), rpg_p.GetLanguage(), newtext);
									sender.sendMessage(ChatColor.GREEN + "The NPC's text for " + ChatColor.BLUE + lang + ChatColor.GREEN + " has been changed to " + ChatColor.BLUE + newtext);
								}
							}
						}
						else if (args[argBase].equalsIgnoreCase("item"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.npc.edit.item"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (args.length < argBase + 2)
							{
								RPG_Core.ShowInfoScreen("rpg.npc.edit.item", sender);
							}
							else
							{
								String olditem = rpg_npc.GetItemInHand();
								
								try
								{
									String newitem = args[argBase + 1];
									rpg_npc.SetItemInHand(newitem);
									sender.sendMessage(ChatColor.GREEN + "The NPC's item in hand has been changed from " + ChatColor.BLUE + olditem + ChatColor.GREEN + " to " + ChatColor.BLUE + newitem);
								}
								catch (Exception ex)
								{
									sender.sendMessage(ChatColor.RED + "You have entered an invald item ID");
								}
							}
						}
						else if (args[argBase].equalsIgnoreCase("head"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.npc.edit.head"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (args.length < argBase + 2)
							{
								RPG_Core.ShowInfoScreen("rpg.npc.edit.head", sender);
							}
							else
							{
								String oldhead = rpg_npc.GetArmorHead();
								
								try
								{
									String newhead = args[argBase + 1];
									rpg_npc.SetHeadArmor(newhead);
									sender.sendMessage(ChatColor.GREEN + "The NPC's head armor has been changed from " + ChatColor.BLUE + oldhead + ChatColor.GREEN + " to " + ChatColor.BLUE + newhead);
								}
								catch (Exception ex)
								{
									sender.sendMessage(ChatColor.RED + "You have entered an invald item ID");
								}
							}
						}
						else if (args[argBase].equalsIgnoreCase("chest"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.npc.edit.chest"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (args.length < argBase + 2)
							{
								RPG_Core.ShowInfoScreen("rpg.npc.edit.chest", sender);
							}
							else
							{
								String oldchest = rpg_npc.GetArmorChest();
								
								try
								{
									String newchest = args[argBase + 1];
									rpg_npc.SetChestArmor(newchest);
									sender.sendMessage(ChatColor.GREEN + "The NPC's chest armor has been changed from " + ChatColor.BLUE + oldchest + ChatColor.GREEN + " to " + ChatColor.BLUE + newchest);
								}
								catch (Exception ex)
								{
									sender.sendMessage(ChatColor.RED + "You have entered an invald item ID");
								}
							}
						}
						else if (args[argBase].equalsIgnoreCase("legs"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.npc.edit.legs"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (args.length < argBase + 2)
							{
								RPG_Core.ShowInfoScreen("rpg.npc.edit.legs", sender);
							}
							else
							{
								String oldlegs = rpg_npc.GetArmorLegs();
								
								try
								{
									String newlegs = args[argBase + 1];
									rpg_npc.SetLegsArmor(newlegs);
									sender.sendMessage(ChatColor.GREEN + "The NPC's legs armor has been changed from " + ChatColor.BLUE + oldlegs + ChatColor.GREEN + " to " + ChatColor.BLUE + newlegs);
								}
								catch (Exception ex)
								{
									sender.sendMessage(ChatColor.RED + "You have entered an invald item ID");
								}
							}
						}
						else if (args[argBase].equalsIgnoreCase("feet"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.npc.edit.feet"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (args.length < argBase + 2)
							{
								RPG_Core.ShowInfoScreen("rpg.npc.edit.feet", sender);
							}
							else
							{
								String oldfeet = rpg_npc.GetArmorFeet();
								
								try
								{
									String newfeet = args[argBase + 1];
									rpg_npc.SetFeetArmor(newfeet);
									sender.sendMessage(ChatColor.GREEN + "The NPC's feet armor has been changed from " + ChatColor.BLUE + oldfeet + ChatColor.GREEN + " to " + ChatColor.BLUE + newfeet);
								}
								catch (Exception ex)
								{
									sender.sendMessage(ChatColor.RED + "You have entered an invald item ID");
								}
							}
						}
						else
							sender.sendMessage(RPG_Core.GetFormattedMessage("unknown_command", sender));
					}
				}
				else if (args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("rem") || args[0].equalsIgnoreCase("remove"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.npc.del"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (args.length < 2)
					{
						RPG_Core.ShowInfoScreen("rpg.npc.del", sender);
					}
					else
					{
						try
						{
							int id = Integer.parseInt(args[1]);							
							if (RemoveNPCFromDB(id))
								sender.sendMessage(ChatColor.GREEN + "NPC with id " + ChatColor.BLUE + id + ChatColor.GREEN + " removed");
							else
								sender.sendMessage(ChatColor.YELLOW + "Could not remove NPC with id " + ChatColor.BLUE + id);
						}
						catch (Exception ex)
						{
							sender.sendMessage(ChatColor.RED + "Invalid NPC ID");
						}
					}
				}
				else if (args[0].equalsIgnoreCase("save"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.npc.save"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (args.length < 2)
					{
						RPG_Core.ShowInfoScreen("rpg.npc.save", sender);
					}
					else
					{
						int id = AddNPCTemplateToDB(args[1], npcsInCreation.get(rpg_p.GetEditId()), rpg_p.GetLanguage());
						
						if (id >= 0)
							sender.sendMessage(ChatColor.GREEN + "NPC Template with id " + ChatColor.BLUE + id + ChatColor.GREEN + " created");
						else
							sender.sendMessage(ChatColor.RED + "NPC Template creation failed!");
						
						npcsInCreation.remove(rpg_p.GetEditId());
						rpg_p.SetEditMode(RPG_EditMode.None);
					}
				}
				else if (args[0].equalsIgnoreCase("template") || args[0].equalsIgnoreCase("templates"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.npc.template"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (args.length < 2)
					{
						RPG_Core.ShowInfoScreen("rpg.npc.templates", sender);
					}
					else
					{
						if (args[1].equalsIgnoreCase("del"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.npc.template.del"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							// DEBUG: NOT YET IMPLEMENTED
							sender.sendMessage(ChatColor.DARK_RED + "NOT YET IMPLEMENTED");
						}
						else if (args[1].equalsIgnoreCase("list"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.npc.template.list"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
							sender.sendMessage(ChatColor.BLUE + "NPC Template List");
							sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
							for (RPG_NpcTemplate temp : GetTemplates())
							{
								String nation = "<none>";
								if (temp.GetNationID() != -1)
									nation = RPG_Core.GetNation(temp.GetNationID()).GetName();
								sender.sendMessage(ChatColor.GOLD + "  " + temp.GetID() + " " + ChatColor.WHITE + temp.GetTemplateName() + 
										" (" + ChatColor.BLUE + temp.GetName() + ChatColor.WHITE + ") [" + ChatColor.BLUE + nation + ChatColor.WHITE + "]");
							}
							sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
						}
						else if (args[1].equalsIgnoreCase("reload"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.npc.template.reload"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							// DEBUG: NOT YET IMPLEMENTED
							sender.sendMessage(ChatColor.DARK_RED + "NOT YET IMPLEMENTED");
						}
						else
							sender.sendMessage(RPG_Core.GetFormattedMessage("unknown_command", sender));
					}
				}
				else if (args[0].equalsIgnoreCase("list"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.npc.list"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					sender.sendMessage(ChatColor.BLUE + "NPC List");
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					for (RPG_Npc npc : GetNPCs())
					{
						sender.sendMessage(ChatColor.GOLD + "  " + npc.GetID() + " " + ChatColor.WHITE + npc.GetName() + " (" + npc.GetLocation().getWorld().getName() + " " + 
								npc.GetLocation().getBlockX() + " " + npc.GetLocation().getBlockY() + " " + npc.GetLocation().getBlockZ() + ")");
					}
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				}
				else if (args[0].equalsIgnoreCase("reload"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.npc.reload"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					ReloadNPCs();
					
					sender.sendMessage(ChatColor.GREEN + "NPCs reloaded");
				}
				else if (args[0].equalsIgnoreCase("tp"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.npc.tp"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (!RPG_Core.IsPlayer(sender))
					{
						sender.sendMessage(ChatColor.RED + "Only players can teleport to NPCs");
						return false;
					}
					
					if (args.length <= 1)
					{
						RPG_Core.ShowInfoScreen("rpg.npc.tp", sender);
					}
					else	
					{
						try
						{
							int id = Integer.parseInt(args[1]);
							RPG_Npc rpg_npc = GetNPC(id);
							
							if (rpg_npc == null)
							{
								sender.sendMessage(ChatColor.RED + "The NPC with the ID " + id + " does not exist!");
								return false;
							}
							
							rpg_p.GetPlayer().teleport(rpg_npc.GetLocation());
						}
						catch (Exception ex)
						{
							sender.sendMessage(ChatColor.RED + "Invalid NPC ID");
						}
					}
				}
				else
					sender.sendMessage(RPG_Core.GetFormattedMessage("unknown_command", sender));
			}
		}
		
		return true;
	}
	
	// 
	// Events
	// 
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e)
	{		
		if (e.getCause() == DamageCause.ENTITY_ATTACK && e.getEntityType() == EntityType.PLAYER)
		{
			if (GetIDFromUUID(e.getEntity().getUniqueId()) != -1)
			{
				e.setCancelled(true);
				
				RPG_Npc rpg_npc = GetNPC(e.getEntity().getUniqueId());
				RPG_Player rpg_p = RPG_Core.GetPlayer(((Player)e.getDamager()).getName());
				
				if (rpg_p.GetEditMode() == RPG_EditMode.None)
				{
					if (!RPG_Core.HasPermission(rpg_p, "rpg.npc.talk"))
					{
						rpg_p.SendMessage(RPG_Core.GetFormattedMessage("no_perm", rpg_p.GetLanguage()));
						RPG_Core.Log(rpg_p.GetID(), -1, RPG_LogType.Warning, "Action", "NoPerm: rpg.npc.talk");
						return;
					}
					
					if (rpg_p.GetTalking() == rpg_npc.GetID())
						return;
					else if (rpg_p.GetTalking() != -1)
						rpg_p.SendMessage(RPG_Core.GetFormattedMessage("npc_talk_end", rpg_p.GetLanguage(), rpg_p.GetTalkingToNpc()));
					
					rpg_p.SetTalking(rpg_npc.GetID());
					rpg_p.SendMessage(RPG_Core.GetFormattedMessage("npc_talk_start", rpg_p.GetLanguage(), rpg_npc));
					
					ArrayList<RPG_Quest> qs = rpg_npc.GetQuestStartsForPlayer(rpg_p);
					
					rpg_p.SendMessage(ChatColor.BLUE + rpg_npc.GetName() + ": " + ChatColor.WHITE + rpg_npc.GetFormattedStandardTextInLanguage(rpg_p.GetLanguage(), rpg_p, rpg_npc));
					int i = 1;
					for (RPG_Quest q : qs)
					{
						rpg_p.SendMessage(ChatColor.GOLD + "  " + i + ": " + ChatColor.AQUA + q.GetDisplayName(rpg_p.GetLanguage()));
						i++;
					}
					
					if (rpg_npc.GetTeleportCount() > 0)
					{
						rpg_p.SendMessage(ChatColor.BLUE + "Teleports:");
						for (RPG_Teleport t : rpg_npc.GetTeleports())
							rpg_p.SendMessage(ChatColor.GOLD + t.GetTargetNameInLanguage(rpg_p.GetLanguage()));
					}
					
					rpg_p.SetStartableQuests(qs);
					
					e.setCancelled(true);
				}
				else if (rpg_p.GetEditMode() == RPG_EditMode.NPC_Edit)
				{
					if (!RPG_Core.HasPermission(rpg_p, "rpg.npc.edit"))
					{
						rpg_p.SendMessage(RPG_Core.GetFormattedMessage("no_perm", rpg_p.GetLanguage()));
						RPG_Core.Log(rpg_p.GetID(), -1, RPG_LogType.Warning, "Action", "NoPerm: rpg.npc.edit");
						return;
					}
					
					if (rpg_p.GetEditId() != rpg_npc.GetID())
					{
						rpg_p.SetEditId(rpg_npc.GetID());
						rpg_p.SendMessage(ChatColor.GREEN + "You are now editing the NPC with ID " + ChatColor.GOLD + rpg_npc.GetID());
						rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
						rpg_p.SendMessage(ChatColor.WHITE + "ID: " + ChatColor.BLUE + rpg_npc.GetID());
						rpg_p.SendMessage(ChatColor.WHITE + "Name: " + ChatColor.BLUE + rpg_npc.GetName());
						rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
						rpg_p.SendMessage(ChatColor.WHITE + "Nation: " + ChatColor.BLUE + rpg_npc.GetNationID());
						rpg_p.SendMessage(ChatColor.WHITE + "Level: " + ChatColor.BLUE + rpg_npc.GetLevel());
						rpg_p.SendMessage(ChatColor.WHITE + "Money: " + ChatColor.BLUE + rpg_npc.GetMoney());
						rpg_p.SendMessage(ChatColor.WHITE + "Item: " + ChatColor.BLUE + rpg_npc.GetItemInHand());
						rpg_p.SendMessage(ChatColor.WHITE + "Head: " + ChatColor.BLUE + rpg_npc.GetArmorHead());
						rpg_p.SendMessage(ChatColor.WHITE + "Chest: " + ChatColor.BLUE + rpg_npc.GetArmorChest());
						rpg_p.SendMessage(ChatColor.WHITE + "Legs: " + ChatColor.BLUE + rpg_npc.GetArmorLegs());
						rpg_p.SendMessage(ChatColor.WHITE + "Feet: " + ChatColor.BLUE + rpg_npc.GetArmorFeet());
						rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e)
	{
		Player p = e.getPlayer();
		RPG_Player rpg_p = RPG_Core.GetPlayer(p.getName());
		
		if (rpg_p.IsTalking())
		{
			String msg = e.getMessage();
			
			if (msg.equalsIgnoreCase("exit") || msg.equalsIgnoreCase("quit") || msg.equalsIgnoreCase("bye") || msg.equalsIgnoreCase("good bye") || msg.equalsIgnoreCase("cu") || msg.equalsIgnoreCase("bb"))
			{
				rpg_p.SendMessage(ChatColor.BLUE + rpg_p.GetUsername() + ": " + ChatColor.WHITE + msg);
				rpg_p.SendMessage(RPG_Core.GetFormattedMessage("npc_talk_end", rpg_p.GetLanguage(), rpg_p.GetTalkingToNpc()));
				
				rpg_p.SetTalking(-1);
			}
			else
			{				
				try
				{
					int num = Integer.parseInt(msg) - 1;
					
					if (num > -1 && num < rpg_p.GetStartableQuests().size())
					{
						if (!RPG_Core.HasPermission(p, "rpg.quest.start"))
						{
							rpg_p.SendMessage(RPG_Core.GetFormattedMessage("no_perm", rpg_p.GetLanguage()));
							RPG_Core.Log(rpg_p.GetID(), -1, RPG_LogType.Warning, "Action", "NoPerm: rpg.quest.start");
							return;
						}
						
						RPG_Quest rpg_q = rpg_p.GetStartableQuests().get(num);
						rpg_p.StartQuest(rpg_q.GetID());
						
						rpg_p.SendMessage(RPG_Core.GetFormattedMessage("quest_start", rpg_p.GetLanguage(), rpg_q));
						rpg_p.SendMessage(ChatColor.BLUE + rpg_p.GetTalkingToNpc().GetName() + ": " + ChatColor.WHITE + rpg_q.GetNPCStartText(rpg_p));
						rpg_p.SendMessage(RPG_Core.GetFormattedMessage("npc_talk_end", rpg_p.GetLanguage(), rpg_p.GetTalkingToNpc()));
						
						String title = RPG_Core.FormatTextForAchievementMessage(RPG_Core.GetFormattedMessage("quest_start_short", rpg_p.GetLanguage(), rpg_q));
						String name = RPG_Core.FormatTextForAchievementMessage(rpg_q.GetName(rpg_p.GetLanguage()));
						
						if (rpg_p.IsSpoutPlayer())
							SpoutManager.getPlayer(p).sendNotification(title, name, Material.BOOK_AND_QUILL);
						
						rpg_p.SetTalking(-1);
					}
					else
						rpg_p.SendMessage(RPG_Core.GetFormattedMessage("invalid_response", rpg_p.GetLanguage()));
				}
				catch (Exception ex)
				{
					rpg_p.SendMessage(RPG_Core.GetFormattedMessage("invalid_response", rpg_p.GetLanguage()));
				}
			}
			
			e.setCancelled(true);
		}
		else if (rpg_p.GetEditMode() == RPG_EditMode.NPC_Create)
		{
			e.setCancelled(true);
			
			if (!RPG_Core.HasPermission(p, "rpg.npc.create"))
			{
				rpg_p.SendMessage(RPG_Core.GetFormattedMessage("no_perm", rpg_p.GetLanguage()));
				RPG_Core.Log(rpg_p.GetID(), -1, RPG_LogType.Warning, "Action", "NoPerm: rpg.npc.create");
				return;
			}
			
			if (rpg_p.GetEditStep() == -1)
			{
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "At first, please enter a " + ChatColor.BLUE + "name " + ChatColor.WHITE + "for your NPC:");
				
				rpg_p.SetEditStep(0);
			}
			else if (rpg_p.GetEditStep() == 0)
			{
				if (e.getMessage() != "")
				{
					npcsInCreation.get(rpg_p.GetEditId()).Name = e.getMessage();
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Name: " + ChatColor.WHITE + e.getMessage());
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Now please enter the " + ChatColor.BLUE + "default text " + ChatColor.WHITE + "that the NPC says to a player when he talks with the NPC:");
				rpg_p.SendMessage(ChatColor.WHITE + "You have currently set your language to " + ChatColor.BLUE + rpg_p.GetLanguage() + ChatColor.WHITE + 
						", which means that you are setting the text that the NPC says in " + ChatColor.BLUE + rpg_p.GetLanguage() + ChatColor.WHITE + ". You can set the other texts later.");
				rpg_p.SendMessage(ChatColor.WHITE + "If you use " + ChatColor.BLUE + "%p_name " + ChatColor.WHITE + "in the text it will be replaced with the players name who is talking to the NPC");
				rpg_p.SendMessage(ChatColor.WHITE + "If you want to use a text that already exists enter it's ID as follows: " + ChatColor.BLUE + "id:[id] " + ChatColor.WHITE + "and replace [id] with the ID of the text");
				
				rpg_p.SetEditStep(1);
			}
			else if (rpg_p.GetEditStep() == 1)
			{
				if (e.getMessage() != "")
				{
					npcsInCreation.get(rpg_p.GetEditId()).Text = e.getMessage();
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Text: " + ChatColor.WHITE + e.getMessage());
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Next please enter the " + ChatColor.BLUE + "level " + ChatColor.WHITE + "of the NPC:");
				
				rpg_p.SetEditStep(2);
			}
			else if (rpg_p.GetEditStep() == 2)
			{
				if (e.getMessage() != "")
				{
					try
					{
						npcsInCreation.get(rpg_p.GetEditId()).Level = Integer.parseInt(e.getMessage());
						
						rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
						rpg_p.SendMessage(ChatColor.BLUE + "Level: " + ChatColor.WHITE + e.getMessage());
					}
					catch (Exception ex)
					{
						rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect level, please enter a numeric value, or type: " + ChatColor.GOLD + "/npc cancel " + ChatColor.RED + "to exit the wizard.");
						return;
					}
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Please enter the " + ChatColor.BLUE + "money " + ChatColor.WHITE + "of the NPC:");
				
				rpg_p.SetEditStep(3);
			}
			else if (rpg_p.GetEditStep() == 3)
			{
				if (e.getMessage() != "")
				{
					try
					{
						npcsInCreation.get(rpg_p.GetEditId()).Money = Integer.parseInt(e.getMessage());
						
						rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
						rpg_p.SendMessage(ChatColor.BLUE + "Money: " + ChatColor.WHITE + e.getMessage());
					}
					catch (Exception ex)
					{
						rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect money amount, please enter a numeric value, or type: " + ChatColor.GOLD + "/npc cancel " + ChatColor.RED + "to exit the wizard.");
						return;
					}
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Now please enter the " + ChatColor.BLUE + "item in hand " + ChatColor.BLUE + "of the NPC:");
				rpg_p.SendMessage(ChatColor.WHITE + "You can use any of the following values:");
				rpg_p.SendMessage(ChatColor.GOLD + "  diamond" + ChatColor.WHITE + " for " + ChatColor.BLUE + "diamond " + ChatColor.WHITE + "material");
				rpg_p.SendMessage(ChatColor.GOLD + "  gold" + ChatColor.WHITE + " for " + ChatColor.BLUE + "gold " + ChatColor.WHITE + "material");
				rpg_p.SendMessage(ChatColor.GOLD + "  iron" + ChatColor.WHITE + " for " + ChatColor.BLUE + "iron " + ChatColor.WHITE + "material");
				rpg_p.SendMessage(ChatColor.GOLD + "  stone" + ChatColor.WHITE + " for " + ChatColor.BLUE + "stone " + ChatColor.WHITE + "material");
				rpg_p.SendMessage(ChatColor.GOLD + "  wood" + ChatColor.WHITE + " for " + ChatColor.BLUE + "wood " + ChatColor.WHITE + "material");
				rpg_p.SendMessage(ChatColor.WHITE + "Combined with one of these values:");
				rpg_p.SendMessage(ChatColor.GOLD + "  sword" + ChatColor.WHITE + " for a " + ChatColor.BLUE + "sword");
				rpg_p.SendMessage(ChatColor.GOLD + "  shovel" + ChatColor.WHITE + " for a " + ChatColor.BLUE + "shovel");
				rpg_p.SendMessage(ChatColor.GOLD + "  pickaxe" + ChatColor.WHITE + " for a " + ChatColor.BLUE + "pickaxe");
				rpg_p.SendMessage(ChatColor.GOLD + "  axe" + ChatColor.WHITE + " for an " + ChatColor.BLUE + "axe");
				rpg_p.SendMessage(ChatColor.GOLD + "  hoe" + ChatColor.WHITE + " for a " + ChatColor.BLUE + "hoe");
				rpg_p.SendMessage(ChatColor.WHITE + "Or use " + ChatColor.BLUE + "none " + ChatColor.WHITE + "to have no item");
				rpg_p.SendMessage(ChatColor.WHITE + "Or you can use the minecraft " + ChatColor.BLUE + "item ID " + ChatColor.WHITE + "of the item you want the NPC to hold in hand.");
				
				rpg_p.SetEditStep(4);
			}
			else if (rpg_p.GetEditStep() == 4)
			{
				if (e.getMessage() != "")
				{
					String item = e.getMessage().replaceAll(" ", "_").toUpperCase();
					
					if (item.contains("NONE"))
						npcsInCreation.get(rpg_p.GetEditId()).Item = "";
					else
					{
						try
						{
							npcsInCreation.get(rpg_p.GetEditId()).Item = Material.getMaterial(item).name();
						}
						catch (Exception ex)
						{
							rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect item, please enter a valid item, or type: " + ChatColor.GOLD + "/npc cancel " + ChatColor.RED + "to exit the wizard.");
							return;
						}
					}
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Item in hand: " + ChatColor.WHITE + e.getMessage());
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Now please enter the " + ChatColor.BLUE + "head armour " + ChatColor.BLUE + "of the NPC:");
				rpg_p.SendMessage(ChatColor.WHITE + "You can use any of the following values:");
				rpg_p.SendMessage(ChatColor.GOLD + "  diamond" + ChatColor.WHITE + " for " + ChatColor.BLUE + "diamond " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.GOLD + "  gold" + ChatColor.WHITE + " for " + ChatColor.BLUE + "gold " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.GOLD + "  iron" + ChatColor.WHITE + " for " + ChatColor.BLUE + "iron " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.GOLD + "  chain" + ChatColor.WHITE + " for " + ChatColor.BLUE + "chain " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.GOLD + "  leather" + ChatColor.WHITE + " for " + ChatColor.BLUE + "leather " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.WHITE + "Or use " + ChatColor.BLUE + "none " + ChatColor.WHITE + "to have no armour");
				
				rpg_p.SetEditStep(5);
			}
			else if (rpg_p.GetEditStep() == 5)
			{
				if (e.getMessage() != "")
				{
					if (e.getMessage().equalsIgnoreCase("diamond"))
						npcsInCreation.get(rpg_p.GetEditId()).Head = RPG_NpcArmourClass.Diamond;
					else if (e.getMessage().equalsIgnoreCase("gold"))
						npcsInCreation.get(rpg_p.GetEditId()).Head = RPG_NpcArmourClass.Gold;
					else if (e.getMessage().equalsIgnoreCase("chain"))
						npcsInCreation.get(rpg_p.GetEditId()).Head = RPG_NpcArmourClass.Chain;
					else if (e.getMessage().equalsIgnoreCase("iron"))
						npcsInCreation.get(rpg_p.GetEditId()).Head = RPG_NpcArmourClass.Iron;
					else if (e.getMessage().equalsIgnoreCase("leather"))
						npcsInCreation.get(rpg_p.GetEditId()).Head = RPG_NpcArmourClass.Leather;
					else if (e.getMessage().equalsIgnoreCase("none"))
						npcsInCreation.get(rpg_p.GetEditId()).Head = RPG_NpcArmourClass.None;
					else
					{
						rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect armour type. Please try again or use: " + ChatColor.GOLD + "/npc cancel " + ChatColor.RED + "to exit the wizard.");
						return;
					}
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Head armour: " + ChatColor.WHITE + e.getMessage());
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Next please enter the " + ChatColor.BLUE + "chest armour " + ChatColor.BLUE + "of the NPC:");
				rpg_p.SendMessage(ChatColor.WHITE + "You can use any of the following values:");
				rpg_p.SendMessage(ChatColor.GOLD + "  diamond" + ChatColor.WHITE + " for " + ChatColor.BLUE + "diamond " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.GOLD + "  gold" + ChatColor.WHITE + " for " + ChatColor.BLUE + "gold " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.GOLD + "  iron" + ChatColor.WHITE + " for " + ChatColor.BLUE + "iron " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.GOLD + "  chain" + ChatColor.WHITE + " for " + ChatColor.BLUE + "chain " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.GOLD + "  leather" + ChatColor.WHITE + " for " + ChatColor.BLUE + "leather " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.WHITE + "Or use " + ChatColor.BLUE + "none " + ChatColor.WHITE + "to have no armour");
				
				rpg_p.SetEditStep(6);
			}
			else if (rpg_p.GetEditStep() == 6)
			{
				if (e.getMessage() != "")
				{
					if (e.getMessage().equalsIgnoreCase("diamond"))
						npcsInCreation.get(rpg_p.GetEditId()).Chest = RPG_NpcArmourClass.Diamond;
					else if (e.getMessage().equalsIgnoreCase("gold"))
						npcsInCreation.get(rpg_p.GetEditId()).Chest = RPG_NpcArmourClass.Gold;
					else if (e.getMessage().equalsIgnoreCase("chain"))
						npcsInCreation.get(rpg_p.GetEditId()).Chest = RPG_NpcArmourClass.Chain;
					else if (e.getMessage().equalsIgnoreCase("iron"))
						npcsInCreation.get(rpg_p.GetEditId()).Chest = RPG_NpcArmourClass.Iron;
					else if (e.getMessage().equalsIgnoreCase("leather"))
						npcsInCreation.get(rpg_p.GetEditId()).Chest = RPG_NpcArmourClass.Leather;
					else if (e.getMessage().equalsIgnoreCase("none"))
						npcsInCreation.get(rpg_p.GetEditId()).Chest = RPG_NpcArmourClass.None;
					else
					{
						rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect armour type. Please try again or use: " + ChatColor.GOLD + "/npc cancel " + ChatColor.RED + "to exit the wizard.");
						return;
					}
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Chest armour: " + ChatColor.WHITE + e.getMessage());
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Next please enter the " + ChatColor.BLUE + "legs armour " + ChatColor.BLUE + "of the NPC:");
				rpg_p.SendMessage(ChatColor.WHITE + "You can use any of the following values:");
				rpg_p.SendMessage(ChatColor.GOLD + "  diamond" + ChatColor.WHITE + " for " + ChatColor.BLUE + "diamond " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.GOLD + "  gold" + ChatColor.WHITE + " for " + ChatColor.BLUE + "gold " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.GOLD + "  iron" + ChatColor.WHITE + " for " + ChatColor.BLUE + "iron " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.GOLD + "  chain" + ChatColor.WHITE + " for " + ChatColor.BLUE + "chain " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.GOLD + "  leather" + ChatColor.WHITE + " for " + ChatColor.BLUE + "leather " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.WHITE + "Or use " + ChatColor.BLUE + "none " + ChatColor.WHITE + "to have no armour");
				
				rpg_p.SetEditStep(7);
			}
			else if (rpg_p.GetEditStep() == 7)
			{
				if (e.getMessage() != "")
				{
					if (e.getMessage().equalsIgnoreCase("diamond"))
						npcsInCreation.get(rpg_p.GetEditId()).Legs = RPG_NpcArmourClass.Diamond;
					else if (e.getMessage().equalsIgnoreCase("gold"))
						npcsInCreation.get(rpg_p.GetEditId()).Legs = RPG_NpcArmourClass.Gold;
					else if (e.getMessage().equalsIgnoreCase("chain"))
						npcsInCreation.get(rpg_p.GetEditId()).Legs = RPG_NpcArmourClass.Chain;
					else if (e.getMessage().equalsIgnoreCase("iron"))
						npcsInCreation.get(rpg_p.GetEditId()).Legs = RPG_NpcArmourClass.Iron;
					else if (e.getMessage().equalsIgnoreCase("leather"))
						npcsInCreation.get(rpg_p.GetEditId()).Legs = RPG_NpcArmourClass.Leather;
					else if (e.getMessage().equalsIgnoreCase("none"))
						npcsInCreation.get(rpg_p.GetEditId()).Legs = RPG_NpcArmourClass.None;
					else
					{
						rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect armour type. Please try again or use: " + ChatColor.GOLD + "/npc cancel " + ChatColor.RED + "to exit the wizard.");
						return;
					}
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Legs armour: " + ChatColor.WHITE + e.getMessage());
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Next please enter the " + ChatColor.BLUE + "feet armour " + ChatColor.BLUE + "of the NPC:");
				rpg_p.SendMessage(ChatColor.WHITE + "You can use any of the following values:");
				rpg_p.SendMessage(ChatColor.GOLD + "  diamond" + ChatColor.WHITE + " for " + ChatColor.BLUE + "diamond " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.GOLD + "  gold" + ChatColor.WHITE + " for " + ChatColor.BLUE + "gold " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.GOLD + "  iron" + ChatColor.WHITE + " for " + ChatColor.BLUE + "iron " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.GOLD + "  chain" + ChatColor.WHITE + " for " + ChatColor.BLUE + "chain " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.GOLD + "  leather" + ChatColor.WHITE + " for " + ChatColor.BLUE + "leather " + ChatColor.WHITE + "armour");
				rpg_p.SendMessage(ChatColor.WHITE + "Or use " + ChatColor.BLUE + "none " + ChatColor.WHITE + "to have no armour");
				
				rpg_p.SetEditStep(8);
			}
			else if (rpg_p.GetEditStep() == 8)
			{
				if (e.getMessage() != "")
				{
					if (e.getMessage().equalsIgnoreCase("diamond"))
						npcsInCreation.get(rpg_p.GetEditId()).Feet = RPG_NpcArmourClass.Diamond;
					else if (e.getMessage().equalsIgnoreCase("gold"))
						npcsInCreation.get(rpg_p.GetEditId()).Feet = RPG_NpcArmourClass.Gold;
					else if (e.getMessage().equalsIgnoreCase("chain"))
						npcsInCreation.get(rpg_p.GetEditId()).Feet = RPG_NpcArmourClass.Chain;
					else if (e.getMessage().equalsIgnoreCase("iron"))
						npcsInCreation.get(rpg_p.GetEditId()).Feet = RPG_NpcArmourClass.Iron;
					else if (e.getMessage().equalsIgnoreCase("leather"))
						npcsInCreation.get(rpg_p.GetEditId()).Feet = RPG_NpcArmourClass.Leather;
					else if (e.getMessage().equalsIgnoreCase("none"))
						npcsInCreation.get(rpg_p.GetEditId()).Feet = RPG_NpcArmourClass.None;
					else
					{
						rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect armour type. Please try again or use: " + ChatColor.GOLD + "/npc cancel " + ChatColor.RED + "to exit the wizard.");
						return;
					}
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Feet armour: " + ChatColor.WHITE + e.getMessage());
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "You are almost done, please review your data, and enter " + ChatColor.GOLD + "/npc create " + ChatColor.WHITE + "to create the NPC at your players " + 
						ChatColor.BLUE + "current position" + ChatColor.WHITE + ". Or enter " + ChatColor.GOLD + "/npc cancel " + ChatColor.WHITE + "to exit the wizard.");
				RPG_NpcCreationData d = npcsInCreation.get(rpg_p.GetEditId());
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.BLUE + "Name: " + ChatColor.WHITE + d.Name);
				rpg_p.SendMessage(ChatColor.BLUE + "Text: " + ChatColor.WHITE + d.Text);
				rpg_p.SendMessage(ChatColor.BLUE + "Level: " + ChatColor.WHITE + d.Level);
				rpg_p.SendMessage(ChatColor.BLUE + "Money: " + ChatColor.WHITE + d.Money);
				rpg_p.SendMessage(ChatColor.BLUE + "Item: " + ChatColor.WHITE + d.Item);
				rpg_p.SendMessage(ChatColor.BLUE + "Head: " + ChatColor.WHITE + d.Head);
				rpg_p.SendMessage(ChatColor.BLUE + "Chest: " + ChatColor.WHITE + d.Chest);
				rpg_p.SendMessage(ChatColor.BLUE + "Legs: " + ChatColor.WHITE + d.Legs);
				rpg_p.SendMessage(ChatColor.BLUE + "Feet: " + ChatColor.WHITE + d.Feet);
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				
				rpg_p.SetEditStep(100);
			}
		}
		else if (rpg_p.GetEditMode() == RPG_EditMode.NPC_Edit)
		{
			e.setCancelled(true);
			
			if (!RPG_Core.HasPermission(p, "rpg.npc.edit"))
			{
				rpg_p.SendMessage(RPG_Core.GetFormattedMessage("no_perm", rpg_p.GetLanguage()));
				RPG_Core.Log(rpg_p.GetID(), -1, RPG_LogType.Warning, "Action", "NoPerm: rpg.npc.edit");
				return;
			}
			
			p.sendMessage(ChatColor.YELLOW + "You are in " + ChatColor.BLUE + "NPC edit mode" + ChatColor.YELLOW + ". In this mode chat is interpreted as commands. To switch to normal mode please use " + ChatColor.GOLD + "/npc cancel");
		}
	}
	
	public void onNPCUpdateTick()
	{
		asyncRunning = true;
		
		for (RPG_Player rpg_p : RPG_Core.GetOnlinePlayers())
		{			
			if (rpg_p.IsTalking())
			{
				RPG_Npc rpg_npc = rpg_p.GetTalkingToNpc();
				if (rpg_p.GetPlayer().getLocation().getWorld().equals(rpg_npc.GetLocation()))
				{
					rpg_p.SetTalking(-1);
					return;
				}
				
				double dist = rpg_p.GetPlayer().getLocation().distance(rpg_npc.GetLocation());
				if (dist > talkdist)
				{
					rpg_p.SendMessage(RPG_Core.GetFormattedMessage("npc_talk_end", rpg_p.GetLanguage(), rpg_p.GetTalkingToNpc()));
					
					rpg_p.SetTalking(-1);
				}
			}
		}
		
		asyncRunning = false;
	}
	
	@EventHandler
	protected void onButtonClick(ButtonClickEvent e)
	{
		RPG_Player rpg_p = RPG_Core.GetPlayer(e.getPlayer().getName());
		RPG_NpcCreationData data = npcsInCreation.get(rpg_p.GetEditId());
		
		if (e.getScreen().getClass() == RPG_Screen_CreateNpc.class)
		{
			RPG_Screen_CreateNpc scr_npc = (RPG_Screen_CreateNpc)e.getScreen();
			
			if (e.getButton().getType() == WidgetType.Button)
			{
				data.Name = scr_npc.GetNPCName();
				data.Text = scr_npc.GetNPCText();
				data.Level = scr_npc.GetNPCLevel();
				data.Money = scr_npc.GetNPCMoney();
				data.Head = scr_npc.GetArmourHead();
				data.Chest = scr_npc.GetArmourChest();
				data.Legs = scr_npc.GetArmourLegs();
				data.Feet = scr_npc.GetArmourFeet();
				// data.Item = scr_npc.GetItemInHand();  DEBUG
				
				if (scr_npc.IsCancelButton(e.getButton()))
				{
					scr_npc.Hide();
					
					rpg_p.SendMessage(ChatColor.GREEN + "NPC creation canceled");
					
					npcsInCreation.remove(rpg_p.GetEditId());
					rpg_p.SetEditMode(RPG_EditMode.None);
				}
				else if (scr_npc.IsCreateButton(e.getButton()))
				{
					scr_npc.ResetColors();
					
					if (scr_npc.GetNPCName().equalsIgnoreCase(""))
					{
						scr_npc.NPCNameNotSpecified();
						return;
					}
					
					scr_npc.Hide();
					
					int id = AddNPCToDB(data, rpg_p.GetPlayer().getLocation(), rpg_p.GetLanguage());
					
					if (id >= 0)
						rpg_p.SendMessage(ChatColor.GREEN + "NPC with id " + ChatColor.BLUE + id + ChatColor.GREEN + " created");
					else
						rpg_p.SendMessage(ChatColor.RED + "NPC creation failed!");
					
					npcsInCreation.remove(rpg_p.GetEditId());
					rpg_p.SetEditMode(RPG_EditMode.None);
				}
				else if (scr_npc.IsSaveAsTemplateButton(e.getButton()))
				{
					scr_npc.ResetColors();
					
					if (scr_npc.GetNPCName().equalsIgnoreCase(""))
					{
						scr_npc.NPCNameNotSpecified();
						return;
					}
					
					if (!scr_npc.IsTemplateNameFieldVisible())
						scr_npc.ShowTemplateTextField();
					else
					{
						if (scr_npc.GetNPCTemplateName().equalsIgnoreCase(""))
						{
							scr_npc.TemplateNameNotSpecified();
							return;
						}
						else
						{
							scr_npc.Hide();
							
							int id = AddNPCTemplateToDB(scr_npc.GetNPCTemplateName(), data, rpg_p.GetLanguage());
							
							if (id >= 0)
								rpg_p.SendMessage(ChatColor.GREEN + "NPC Template with id " + ChatColor.BLUE + id + ChatColor.GREEN + " created");
							else
								rpg_p.SendMessage(ChatColor.RED + "NPC Template creation failed!");
							
							npcsInCreation.remove(rpg_p.GetEditId());
							rpg_p.SetEditMode(RPG_EditMode.None);
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	protected void onTextFieldChange(TextFieldChangeEvent e)
	{
		if (e.isCancelled())
			return;
		
		if (e.getScreen().getClass() == RPG_Screen_CreateNpc.class)
		{
			RPG_Screen_CreateNpc scr_q = (RPG_Screen_CreateNpc)e.getScreen();
			
			if (scr_q.IsItemTextField(e.getTextField()))
			{
				int id = -1;
				if (!e.getNewText().equalsIgnoreCase(""))
					id = Integer.parseInt(e.getNewText());
				
				scr_q.UpdateItem(id);
			}
		}
	}
	
	// 
	// NPC
	//
	public boolean IsNPCID(int ID)
	{
		return npcs.containsKey(ID);
	}
	public RPG_Npc GetNPC(int ID)
	{
		if (npcs.containsKey(ID))
			return npcs.get(ID);
		else
		{
			logger.warning(prefix + "NPC with id " + ID + " does not exist");
			RPG_Core.Log(-1, ID, RPG_LogType.Warning, "NPC", "Does not exist");
			return null;
		}
	}
	public RPG_Npc GetNPC(UUID uuid)
	{
		if (npcuuids.containsKey(uuid))
		{
			if (npcs.containsKey(npcuuids.get(uuid)))
				return npcs.get(npcuuids.get(uuid));
		}
		
		logger.warning(prefix + "NPC with uuid " + uuid + " does not exist");
		RPG_Core.Log(RPG_LogType.Warning, "NPC", "NPC with UUID " + uuid + " does not exist");
		return null;
	}
	public RPG_NpcData GetNPCData(int ID)
	{
		if (npcdata.containsKey(ID))
			return npcdata.get(ID);
		else
		{
			logger.warning(prefix + "NPCData with id " + ID + " does not exist");
			RPG_Core.Log(-1, ID, RPG_LogType.Warning, "NPCData", "Does not exist");
			return null;
		}
	}
	public Collection<RPG_Npc> GetNPCs()
	{
		return npcs.values();
	}
	public int GetIDFromUUID(UUID uuid)
	{
		if (!npcuuids.containsKey(uuid))
		{
			RPG_Core.Log(RPG_LogType.Warning, "NPC", "UUID " + uuid + " could not be matched to an ID");
			return -1;
		}
		
		return npcuuids.get(uuid);
	}
	
	public int AddNPCToDB(RPG_NpcCreationData Data, Location Position, RPG_Language Language)
	{
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			int textid = -1;
			
			if (Data.Text.startsWith("id:"))
			{
				try
				{
					textid = Integer.parseInt(Data.Text.split(":")[1]);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					
					RPG_Core.Log(RPG_LogType.Error, "NPCCreate", "Invalid Text ID");
				}
			}
			else
			{
				stmt.executeUpdate("CALL " + SQLTablePrefix + "textSave('" + Language + "', '" + Data.Text + "', @textid)");
				
				rs = stmt.executeQuery("SELECT @textid AS TextID;");
				rs.first();
				textid = rs.getInt(1);
			}
			
			rs = stmt.executeQuery("SELECT ID FROM " + SQLTablePrefix + "worlds WHERE WorldName='" + Position.getWorld().getName() + "';");
			rs.first();
			int worldId = rs.getInt(1);
			
			String head = "";
			if (Data.Head == RPG_NpcArmourClass.Diamond)
				head = "DIAMOND_HELMET";
			else if (Data.Head == RPG_NpcArmourClass.Gold)
				head = "GOLD_HELMET";
			else if (Data.Head == RPG_NpcArmourClass.Iron)
				head = "IRON_HELMET";
			else if (Data.Head == RPG_NpcArmourClass.Chain)
				head = "CHAINMAIL_HELMET";
			else if (Data.Head == RPG_NpcArmourClass.Leather)
				head = "LEATHER_HELMET";
			
			String chest = "";
			if (Data.Chest == RPG_NpcArmourClass.Diamond)
				chest = "DIAMOND_CHESTPLATE";
			else if (Data.Chest == RPG_NpcArmourClass.Gold)
				chest = "GOLD_CHESTPLATE";
			else if (Data.Chest == RPG_NpcArmourClass.Iron)
				chest = "IRON_CHESTPLATE";
			else if (Data.Chest == RPG_NpcArmourClass.Chain)
				chest = "CHAINMAIL_CHESTPLATE";
			else if (Data.Chest == RPG_NpcArmourClass.Leather)
				chest = "LEATHER_CHESTPLATE";
			
			String legs = "";
			if (Data.Legs == RPG_NpcArmourClass.Diamond)
				legs = "DIAMOND_LEGGINGS";
			else if (Data.Legs == RPG_NpcArmourClass.Gold)
				legs = "GOLD_LEGGINGS";
			else if (Data.Legs == RPG_NpcArmourClass.Iron)
				legs = "IRON_LEGGINGS";
			else if (Data.Legs == RPG_NpcArmourClass.Chain)
				legs = "CHAINMAIL_LEGGINGS";
			else if (Data.Legs == RPG_NpcArmourClass.Leather)
				legs = "LEATHER_LEGGINGS";
			
			String feet = "";
			if (Data.Feet == RPG_NpcArmourClass.Diamond)
				feet = "DIAMOND_BOOTS";
			else if (Data.Feet == RPG_NpcArmourClass.Gold)
				feet = "GOLD_BOOTS";
			else if (Data.Feet == RPG_NpcArmourClass.Iron)
				feet = "IRON_BOOTS";
			else if (Data.Feet == RPG_NpcArmourClass.Chain)
				feet = "CHAINMAIL_BOOTS";
			else if (Data.Feet == RPG_NpcArmourClass.Leather)
				feet = "LEATHER_BOOTS";
			
			stmt.executeUpdate("INSERT INTO " + SQLTablePrefix + "npcs (Name, WorldId, PosX, PosY, PosZ, Yaw, Pitch, Level, Money, StandardTextId, ShopId, ItemInHand, ArmorHead, ArmorChest, ArmorLegs, ArmorFeet) " +
					"VALUES ('" + Data.Name + "'," + worldId + "," + Position.getBlockX() + "," + Position.getBlockY() + "," + Position.getBlockZ() + "," + Position.getYaw() + "," + Position.getPitch() + "," +
					Data.Level + "," + Data.Money + "," + textid + ",-1,'" + Data.Item + "','" + head + "','" + chest + "','" + legs + "','" + feet + "');");
			
			rs = stmt.executeQuery("select last_insert_id();");
			rs.last();
			int id = rs.getInt(1);
			
			RPG_Core.Log(-1, id, RPG_LogType.Information, "NPCCreate", "Created");
			
			RPG_Core.ReloadText(textid);
			ReloadNPC(id);
			
			return id;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "NPCCreate", ex);
			
			return -1;
		}
	}
	public int AddNPCToDB(RPG_NpcTemplate Template, Location Position)
	{
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			
			rs = stmt.executeQuery("SELECT ID FROM " + SQLTablePrefix + "worlds WHERE WorldName='" + Position.getWorld().getName() + "';");
			rs.first();
			int worldId = rs.getInt(1);
			
			stmt.executeUpdate("INSERT INTO " + SQLTablePrefix + "npcs (Name, WorldId, PosX, PosY, PosZ, Yaw, Pitch, Level, Money, StandardTextId, ShopId, ItemInHand, ArmorHead, ArmorChest, ArmorLegs, ArmorFeet) " +
					"VALUES ('" + Template.GetName() + "'," + worldId + "," + Position.getBlockX() + "," + Position.getBlockY() + "," + Position.getBlockZ() + "," + Position.getYaw() + "," + Position.getPitch() + "," +
					Template.GetLevel() + "," + Template.GetMoney() + "," + Template.GetTextID() + "," + Template.GetShopID() + ",'" + Template.GetItemInHand() + "','" +
					Template.GetArmorHead() + "','" + Template.GetArmorChest() + "','" + Template.GetArmorLegs() + "','" + Template.GetArmorFeet() + "');");
			
			rs = stmt.executeQuery("select last_insert_id();");
			rs.last();
			int id = rs.getInt(1);
			
			RPG_Core.Log(-1, id, RPG_LogType.Information, "NPCCreate", "Created");
			
			ReloadNPC(id);
			
			return id;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "NPCCreate", ex);
			
			return -1;
		}
	}
	public boolean RemoveNPCFromDB(int ID)
	{
		Statement stmt = null;
		
		try
		{
			RPG_Npc rpg_npc = GetNPC(ID);
			if (rpg_npc == null)
				return false;
			
			rpg_npc.DeSpawn();
			
			stmt = RPG_Core.GetDatabaseStatement();
			stmt.executeUpdate("DELETE FROM " + SQLTablePrefix + "npcs WHERE ID = " + ID + ";");
			
			stmt.executeUpdate("CALL " + SQLTablePrefix + "textDelete(" + rpg_npc.GetStandardText().GetID() + ");");
			
			npcs.remove(ID);
			
			RPG_Core.Log(-1, ID, RPG_LogType.Information, "NPCDelete", "Deleted");
			
			return true;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "NPCDelete", ex);
			return false;
		}
	}
	
	public boolean EditNPCProperty(int ID, String Name, String Value)
	{
		if (!npcs.containsKey(ID))
		{
			RPG_Core.Log(-1, ID, RPG_LogType.Warning, "NPCEdit", "Does not exist");
			return false;
		}
		
		Statement stmt = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			int num = stmt.executeUpdate("UPDATE " + SQLTablePrefix + "npcs SET " + Name + " = '" + Value + "' WHERE ID = " + ID + ";");
			
			if (num == 0)
				return false;
			else
			{
				ReloadNPC(ID);
				return true;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "NPCEdit", ex);
		}
		
		return false;
	}
	public boolean MoveNPC(int ID, Location NewPosition)
	{
		if (!npcs.containsKey(ID))
		{
			RPG_Core.Log(-1, ID, RPG_LogType.Warning, "NPCEdit", "Does not exist");
			return false;
		}
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			rs = stmt.executeQuery("SELECT ID FROM " + SQLTablePrefix + "worlds WHERE WorldName='" + NewPosition.getWorld().getName() + "';");
			rs.first();
			int worldId = rs.getInt(1);
			
			stmt.executeUpdate("UPDATE " + SQLTablePrefix + "npcs SET WorldId=" + worldId + ",PosX=" + NewPosition.getBlockX() + ",PosY=" + NewPosition.getBlockY() + ",PosZ=" + NewPosition.getBlockZ() + 
					",Yaw=" + NewPosition.getYaw() + ",Pitch=" + NewPosition.getPitch() + " WHERE ID=" + ID + ";");
			
			ReloadNPC(ID);
			return true;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "NPCEdit", ex.getMessage());
		}
		
		return false;
	}
	public boolean EditNPCText(int ID, int TextID, RPG_Language Language, String Text)
	{
		if (!npcs.containsKey(ID))
		{
			RPG_Core.Log(1, ID, RPG_LogType.Warning, "NPCEdit", "Does not exist");
			return false;
		}
		
		Statement stmt = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			int num = 0;
			
			if (Language == RPG_Language.DE)
				num = stmt.executeUpdate("UPDATE " + SQLTablePrefix + "texts SET DE = '" + Text + "' WHERE ID = " + TextID + ";");
			else if (Language == RPG_Language.EN)
				num = stmt.executeUpdate("UPDATE " + SQLTablePrefix + "texts SET EN = '" + Text + "' WHERE ID = " + TextID + ";");
			else if (Language == RPG_Language.FR)
				num = stmt.executeUpdate("UPDATE " + SQLTablePrefix + "texts SET FR = '" + Text + "' WHERE ID = " + TextID + ";");
			
			if (num == 0)
				return false;
			
			ReloadNPC(ID);
			return true;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "NPCEdit", ex);
		}
		
		return false;
	}
	
	public void AddNPC(RPG_Npc NPC)
	{
		if (NPC == null)
		{
			RPG_Core.Log(RPG_LogType.Warning, "NPC", "Cannot add an NPC that is NULL");
			return;
		}
		if (npcs.containsKey(NPC.GetID()))
		{
			RPG_Core.Log(RPG_LogType.Warning, "NPC", "An NPC with this ID already exists");
			return;
		}
		if (npcuuids.containsKey(NPC.GetUUID()))
		{
			RPG_Core.Log(RPG_LogType.Warning, "NPC", "An NPC with this UUID already exists");
			return;
		}
		
		npcs.put(NPC.GetID(), NPC);
		npcuuids.put(NPC.GetUUID(), NPC.GetID());
		
		RPG_Core.Log(-1, NPC.GetID(), RPG_LogType.Information, "NPC", "Added");
	}
	public void RemoveNPC(int ID)
	{
		if (npcs.containsKey(ID))
		{
			npcs.get(ID).DeSpawn();
			npcuuids.remove(npcs.get(ID).GetUUID());
			npcs.remove(ID);
			
			RPG_Core.Log(-1, ID, RPG_LogType.Information, "NPC", "Removed");
		}
		else
		{
			RPG_Core.Log(-1, ID, RPG_LogType.Warning, "NPC", "Does not exist");
			return;
		}
	}
	
	public void SpawnNPCs()
	{
		logger.info(prefix + "Spawning NPCs...");
		
		for (RPG_Npc npc : npcs.values())
			npc.Spawn();
		
		logger.info(prefix + "  Spawned " + npcs.size() + " NPC(s)!");
	}
	public void DeSpawnNPCs()
	{
		logger.info(prefix + "Despawning NPCs...");
		
		for (RPG_Npc npc : npcs.values())
			npc.DeSpawn();
		
		logger.info(prefix + "  Despawned " + npcs.size() + " NPC(s)!");
	}
	
	public void LoadNPCDataFromDB()
	{
		logger.info(prefix + "Loading NPC data...");
		
		npcdata.clear();
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			rs = stmt.executeQuery("SELECT " + SQLTablePrefix + "npcs.ID, Name, NationId, WorldName, PosX, PosY, PosZ, Yaw, Pitch, Level, Money, StandardTextId, ShopId, " +
					"ItemInHand, ArmorHead, ArmorChest, ArmorLegs, ArmorFeet FROM " + SQLTablePrefix + "npcs " +
					"INNER JOIN " + SQLTablePrefix + "worlds ON " + SQLTablePrefix + "npcs.WorldId = " + SQLTablePrefix + "worlds.ID " + 
					"ORDER BY ID ASC;");
			
			while (rs.next())
			{
				Location loc = new Location(Bukkit.getServer().getWorld(rs.getString("WorldName")), rs.getDouble("PosX"), rs.getDouble("PosY"), rs.getDouble("PosZ"));
				loc.setYaw(rs.getFloat("Yaw"));
				loc.setPitch(rs.getFloat("Pitch"));
				
				npcdata.put(rs.getInt(SQLTablePrefix + "npcs.ID"), new RPG_NpcData(rs.getInt(SQLTablePrefix + "npcs.ID"), rs.getString("Name"), rs.getInt("NationId"), loc, rs.getInt("Level"), rs.getInt("Money"), 
						rs.getInt("StandardTextID"), rs.getInt("ShopID"), rs.getString("ItemInHand"), rs.getString("ArmorHead"), rs.getString("ArmorChest"),  rs.getString("ArmorLegs"), rs.getString("ArmorFeet")));
				
				RPG_Core.Log(-1, rs.getInt(1), RPG_LogType.Information, "NPCData", "Loaded");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "NPCData", ex);
		}
		
		logger.info(prefix + "  Loaded " + npcdata.size() + " NPC(s) from database!");
	}
	public void LoadNPCsFromData()
	{
		logger.info(prefix + "Loading NPCs...");
		
		npcs.clear();
		
		for (RPG_NpcData data : npcdata.values())
			AddNPC(new RPG_Npc(data));
		
		logger.info(prefix + "  Loaded " + npcs.size() + " NPC(s) from data!");
	}
	
	public void ReloadNPCs()
	{
		DeSpawnNPCs();
		LoadNPCDataFromDB();
		LoadNPCsFromData();
		SpawnNPCs();
	}
	public void ReloadNPC(int ID)
	{
		if (npcs.containsKey(ID))
			npcs.get(ID).DeSpawn();
		
		if (npcs.containsKey(ID))
		{
			npcuuids.remove(npcs.get(ID).GetUUID());
			npcs.remove(ID);
		}
		
		if (npcdata.containsKey(ID))
			npcdata.remove(ID);
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			rs = stmt.executeQuery("SELECT " + SQLTablePrefix + "npcs.ID, Name, NationId, WorldName, PosX, PosY, PosZ, Yaw, Pitch, Level, Money, StandardTextId, ShopId, " +
					"ItemInHand, ArmorHead, ArmorChest, ArmorLegs, ArmorFeet FROM " + SQLTablePrefix + "npcs " +
					"INNER JOIN " + SQLTablePrefix + "worlds ON " + SQLTablePrefix + "npcs.WorldId = " + SQLTablePrefix + "worlds.ID " +
					"WHERE " + SQLTablePrefix + "npcs.ID = " + ID + ";");
			
			if (rs.first())
			{
				Location loc = new Location(Bukkit.getServer().getWorld(rs.getString("WorldName")), rs.getDouble("PosX"), rs.getDouble("PosY"), rs.getDouble("PosZ"));
				loc.setYaw(rs.getFloat("Yaw"));
				loc.setPitch(rs.getFloat("Pitch"));
				
				npcdata.put(rs.getInt(SQLTablePrefix + "npcs.ID"), new RPG_NpcData(rs.getInt(SQLTablePrefix + "npcs.ID"), rs.getString("Name"), rs.getInt("NationId"), loc, rs.getInt("Level"), rs.getInt("Money"), 
						rs.getInt("StandardTextID"), rs.getInt("ShopID"), rs.getString("ItemInHand"), rs.getString("ArmorHead"), rs.getString("ArmorChest"), rs.getString("ArmorLegs"), rs.getString("ArmorFeet")));
				
				AddNPC(new RPG_Npc(npcdata.get(ID)));
				
				npcs.get(ID).Spawn();
				
				RPG_Core.Log(-1, ID, RPG_LogType.Information, "NPC", "Reloaded");
			}
			else
				RPG_Core.Log(-1, ID, RPG_LogType.Warning, "NPC", "Could not be reloaded");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "NPC", ex);
		}
	}
	
	// 
	// NPC Template
	// 
	public RPG_NpcTemplate GetNPCTemplate(int ID)
	{
		for (RPG_NpcTemplate template : npctemplates.values())
		{
			if (template.GetID() == ID)
				return template;
		}
		
		return null;
	}
	public RPG_NpcTemplate GetNPCTemplate(String TemplateName)
	{
		TemplateName = TemplateName.toLowerCase();
		
		if (npctemplates.containsKey(TemplateName))
			return npctemplates.get(TemplateName);
		
		for (RPG_NpcTemplate template : npctemplates.values())
		{
			if (template.GetName().toLowerCase().contains(TemplateName))
				return template;
			else if (String.valueOf(template.GetID()).equalsIgnoreCase(TemplateName))
				return template;
		}
		
		return null;
	}
	public Collection<RPG_NpcTemplate> GetTemplates()
	{
		return npctemplates.values();
	}
	
	public int AddNPCTemplateToDB(String TemplateName, RPG_NpcCreationData Data, RPG_Language Language)
	{
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			int textid = -1;
			
			if (Data.Text.startsWith("id:"))
			{
				try
				{
					textid = Integer.parseInt(Data.Text.split(":")[1]);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					
					RPG_Core.Log(RPG_LogType.Error, "NPCTemplateCreate", "Invalid Text ID");
				}
			}
			else
			{
				stmt.executeUpdate("CALL " + SQLTablePrefix + "textSave('" + Language + "', '" + Data.Text + "', @textid)");
				
				rs = stmt.executeQuery("SELECT @textid AS TextID;");
				rs.first();
				textid = rs.getInt(1);
			}
			
			
			String head = "";
			if (Data.Head == RPG_NpcArmourClass.Diamond)
				head = "DIAMOND_HELMET";
			else if (Data.Head == RPG_NpcArmourClass.Gold)
				head = "GOLD_HELMET";
			else if (Data.Head == RPG_NpcArmourClass.Iron)
				head = "IRON_HELMET";
			else if (Data.Head == RPG_NpcArmourClass.Chain)
				head = "CHAINMAIL_HELMET";
			else if (Data.Head == RPG_NpcArmourClass.Leather)
				head = "LEATHER_HELMET";
			
			String chest = "";
			if (Data.Chest == RPG_NpcArmourClass.Diamond)
				chest = "DIAMOND_CHESTPLATE";
			else if (Data.Chest == RPG_NpcArmourClass.Gold)
				chest = "GOLD_CHESTPLATE";
			else if (Data.Chest == RPG_NpcArmourClass.Iron)
				chest = "IRON_CHESTPLATE";
			else if (Data.Chest == RPG_NpcArmourClass.Chain)
				chest = "CHAINMAIL_CHESTPLATE";
			else if (Data.Chest == RPG_NpcArmourClass.Leather)
				chest = "LEATHER_CHESTPLATE";
			
			String legs = "";
			if (Data.Legs == RPG_NpcArmourClass.Diamond)
				legs = "DIAMOND_LEGGINGS";
			else if (Data.Legs == RPG_NpcArmourClass.Gold)
				legs = "GOLD_LEGGINGS";
			else if (Data.Legs == RPG_NpcArmourClass.Iron)
				legs = "IRON_LEGGINGS";
			else if (Data.Legs == RPG_NpcArmourClass.Chain)
				legs = "CHAINMAIL_LEGGINGS";
			else if (Data.Legs == RPG_NpcArmourClass.Leather)
				legs = "LEATHER_LEGGINGS";
			
			String feet = "";
			if (Data.Feet == RPG_NpcArmourClass.Diamond)
				feet = "DIAMOND_BOOTS";
			else if (Data.Feet == RPG_NpcArmourClass.Gold)
				feet = "GOLD_BOOTS";
			else if (Data.Feet == RPG_NpcArmourClass.Iron)
				feet = "IRON_BOOTS";
			else if (Data.Feet == RPG_NpcArmourClass.Chain)
				feet = "CHAINMAIL_BOOTS";
			else if (Data.Feet == RPG_NpcArmourClass.Leather)
				feet = "LEATHER_BOOTS";
			
			stmt.executeUpdate("INSERT INTO " + SQLTablePrefix + "npctemplates (TemplateName, NPCName, NationID, Level, Money, TextID, ShopID, ItemInHand, ArmorHead, ArmorChest, ArmorLegs, ArmorFeet) " +
					"VALUES ('" + TemplateName + "','" + Data.Name + "',-1," + Data.Level + "," + Data.Money + "," + textid + ",-1,'" + Data.Item + "','" + head + "','" + chest + "','" + legs + "','" + feet + "');");
			
			rs = stmt.executeQuery("select last_insert_id();");
			rs.last();
			int id = rs.getInt(1);
			
			RPG_Core.Log(-1, id, RPG_LogType.Information, "NPCTemplateCreate", "Created");
			
			ReloadNPCTemplate(id);
			
			return id;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "NPCTemplateCreate", ex);
			
			return -1;
		}
	}
	
	public void LoadNPCTemplatesFromDB()
	{
		logger.info(prefix + "Loading NPC templates...");
		
		npctemplates.clear();
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			rs = stmt.executeQuery("SELECT ID, TemplateName, NPCName, NationID, Level, Money, TextID, ShopID, ItemInHand, ArmorHead, ArmorChest, ArmorLegs, ArmorFeet FROM " + 
					SQLTablePrefix + "npctemplates;");
			
			while (rs.next())
			{
				npctemplates.put(rs.getString("TemplateName").toLowerCase(), new RPG_NpcTemplate(rs.getInt("ID"), rs.getString("TemplateName"), rs.getString("NPCName"), rs.getInt("NationID"), rs.getInt("Level"), 
						rs.getInt("Money"), rs.getInt("TextID"), rs.getInt("ShopID"), rs.getString("ItemInHand"), rs.getString("ArmorHead"), rs.getString("ArmorChest"), rs.getString("ArmorLegs"), rs.getString("ArmorFeet")));
				
				RPG_Core.Log(-1, rs.getInt("ID"), RPG_LogType.Information, "NPCTemplate", "Loaded");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "NPCTemplate", ex);
		}
		
		logger.info(prefix + "  Loaded " + npctemplates.size() + " NPC template(s) from database!");
	}
	
	public void ReloadNPCTemplate(int ID)
	{
		if (npctemplates.containsKey(ID))
			npctemplates.remove(ID);
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			rs = stmt.executeQuery("SELECT ID, TemplateName, NPCName, NationID, Level, Money, TextID, ShopID, ItemInHand, ArmorHead, ArmorChest, ArmorLegs, ArmorFeet FROM " + 
					SQLTablePrefix + "npctemplates WHERE ID = " + ID + ";");
			
			if (rs.first())
			{
				npctemplates.put(rs.getString("TemplateName").toLowerCase(), new RPG_NpcTemplate(rs.getInt("ID"), rs.getString("TemplateName"), rs.getString("NPCName"), rs.getInt("NationID"), rs.getInt("Level"), 
						rs.getInt("Money"), rs.getInt("TextID"), rs.getInt("ShopID"), rs.getString("ItemInHand"), rs.getString("ArmorHead"), rs.getString("ArmorChest"), rs.getString("ArmorLegs"), rs.getString("ArmorFeet")));
				
				RPG_Core.Log(-1, rs.getInt("ID"), RPG_LogType.Information, "NPCTemplate", "Reloaded");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "NPCTemplate", ex);
		}
	}
}

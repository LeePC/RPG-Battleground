package rpgQuestManager;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.event.screen.TextFieldChangeEvent;
import org.getspout.spoutapi.gui.WidgetType;

import rpgCore.RPG_Core;
import rpgGUI.RPG_InfoScreenItem;
import rpgInterfaces.RPG_IQuestManager;
import rpgNpc.RPG_Npc;
import rpgOther.RPG_LogType;
import rpgPlayer.RPG_EditMode;
import rpgPlayer.RPG_Player;
import rpgQuest.RPG_Quest;
import rpgQuest.RPG_QuestCreationData;
import rpgQuest.RPG_QuestProgress;
import rpgQuest.RPG_QuestRequest;
import rpgQuest.RPG_QuestType;
import rpgQuest.RPG_Quest_Bring;
import rpgQuest.RPG_Quest_Kill;
import rpgQuest.RPG_Quest_Talk;
import rpgTexts.RPG_Language;

public class RPG_QuestManager extends JavaPlugin implements Listener, RPG_IQuestManager
{
	private String prefix = "[RPG Quest Manager] ";
	private Logger logger = Logger.getLogger("Minecraft");
	private String SQLTablePrefix;
	
	private int questCreationCounter = 0;
	private HashMap<Integer, RPG_QuestCreationData> questsInCreation = new HashMap<Integer, RPG_QuestCreationData>();
	
	private HashMap<Integer, RPG_Quest> quests = new HashMap<Integer, RPG_Quest>();
	private HashMap<Integer, HashMap<Integer, RPG_QuestProgress>> questprogresses = new HashMap<Integer, HashMap<Integer, RPG_QuestProgress>>();
	
	
	// 
	// Main
	// 
	public RPG_QuestManager()
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
		pm.registerEvent(PlayerJoinEvent.class, this, EventPriority.HIGH, RPG_Core.GetEventExecutor(), this);
		pm.registerEvent(AsyncPlayerChatEvent.class, this, EventPriority.NORMAL, RPG_Core.GetEventExecutor(), this);
		pm.registerEvent(PlayerPickupItemEvent.class, this, EventPriority.NORMAL, RPG_Core.GetEventExecutor(), this);
		pm.registerEvent(EntityDamageByEntityEvent.class, this, EventPriority.LOW, RPG_Core.GetEventExecutor(), this);
		pm.registerEvent(EntityDeathEvent.class, this, EventPriority.NORMAL, RPG_Core.GetEventExecutor(), this);
		if (RPG_Core.IsSpoutEnabled())
		{
			pm.registerEvent(ButtonClickEvent.class, this, EventPriority.NORMAL, RPG_Core.GetEventExecutor(), this);
			pm.registerEvent(TextFieldChangeEvent.class, this, EventPriority.NORMAL, RPG_Core.GetEventExecutor(), this);
		}
		else
			logger.info(prefix + "  Skipped Spout events!");
		
		logger.info(prefix + "Adding info screens...");
		
		ArrayList<RPG_InfoScreenItem> items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("new", 		"Create a Quest", 			"rpg.quest.new"));
		items.add(new RPG_InfoScreenItem("edit", 		"Edit a Quest", 			"rpg.quest.edit", 	true));
		items.add(new RPG_InfoScreenItem("del", 		"Delete an existing Quest",	"rpg.quest.del",	true));
		items.add(new RPG_InfoScreenItem("list", 		"List all the Quests", 		"rpg.quest.list"));
		items.add(new RPG_InfoScreenItem("reload", 		"Reload all the Quests", 	"rpg.quest.reload"));
		RPG_Core.AddInfoScreen("rpg.quest", "Quest Manager", items);
		
		items = new ArrayList<RPG_InfoScreenItem>();
		items.add(new RPG_InfoScreenItem("Description:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  Deletes a quest from the database", "", ""));
		items.add(new RPG_InfoScreenItem("Syntax:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  /quest del [id]", "", ""));
		items.add(new RPG_InfoScreenItem("Arguments:", "", ""));
		items.add(new RPG_InfoScreenItem(ChatColor.WHITE + "  [id]", ChatColor.WHITE + "The ID of the quest to delete", ""));
		RPG_Core.AddInfoScreen("rpg.quest.del", "Delete a quest", items);
		
		LoadQuestsFromDB();
		
		logger.info(prefix + "Getting data for already online players...");
        
		for (Player p : getServer().getOnlinePlayers())
		{
			RPG_Player rpg_p = RPG_Core.GetPlayer(p.getName());
			LoadQuestProgressesForPlayer(rpg_p.GetID());
		}
		
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
		
		if (commandLabel.equalsIgnoreCase("quests") || commandLabel.equalsIgnoreCase("quest") || commandLabel.equalsIgnoreCase("q"))
		{			
			if (!RPG_Core.HasPermission(sender, "rpg.quest"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (args.length < 1)
			{
				RPG_Core.ShowInfoScreen("rpg.quest", sender);
			}
			else
			{
				if (args[0].equalsIgnoreCase("new"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.quest.new"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (!RPG_Core.IsPlayer(sender))
					{
						sender.sendMessage(ChatColor.RED + "Only players can create new quests");
						return false;
					}
					
					questCreationCounter++;
					questsInCreation.put(questCreationCounter, new RPG_QuestCreationData());
					
					rpg_p.SetEditMode(RPG_EditMode.Quest_Create);
					rpg_p.SetEditId(questCreationCounter);
					
					if (rpg_p.IsSpoutPlayer())
					{
						RPG_Screen_CreateQuest1 scr = new RPG_Screen_CreateQuest1(this, rpg_p, questsInCreation.get(rpg_p.GetEditId()));
						scr.Show();
					}
					else	
					{
						sender.sendMessage(ChatColor.GOLD + "----------------------");
						sender.sendMessage(ChatColor.BLUE + "Quest Creation Wizard");
						sender.sendMessage(ChatColor.GOLD + "----------------------");
						sender.sendMessage(ChatColor.WHITE + "Welcome to the Quest creation wizard. This wizard will help you create a Quest with a few simple steps!");
						sender.sendMessage(ChatColor.WHITE + "Please simply enter the requested data into the chat. You may exit this wizard at any time using: " + ChatColor.GOLD + "/quest cancel");
						sender.sendMessage(ChatColor.WHITE + "You can move to the next step using " + ChatColor.GOLD + "/quest next " + ChatColor.WHITE + "and you can go back using " + ChatColor.GOLD + "/quest back");
						
						rpg_p.SetEditStep(-1);
						
						onPlayerChat(new AsyncPlayerChatEvent(true, rpg_p.GetPlayer(), "", null));
					}
				}
				else if (args[0].equalsIgnoreCase("create"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.quest.create"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (!RPG_Core.IsPlayer(sender))
					{
						sender.sendMessage(ChatColor.RED + "Only players can create new quests");
						return false;
					}
					
					if (rpg_p.GetEditId() == -1)
					{
						sender.sendMessage(ChatColor.YELLOW + "You are not creating a quest at the moment. Start creating one by using: " + ChatColor.GOLD + "/quest new");
						return false;
					}
					
					if (rpg_p.GetEditStep() < 100)
					{
						sender.sendMessage(ChatColor.YELLOW + "Please first finish all the steps of the quest creation wizard before creating the quest!");
						return false;
					}
					
					int id = AddQuestToDB(questsInCreation.get(rpg_p.GetEditId()), rpg_p.GetLanguage());
					
					if (id >= 0)
						sender.sendMessage(ChatColor.GREEN + "Quest with id " + ChatColor.BLUE + id + ChatColor.GREEN + " created");
					else
						sender.sendMessage(ChatColor.RED + "Quest creation failed!");
					
					sender.sendMessage(ChatColor.GREEN + "Quest created");
					
					questsInCreation.remove(rpg_p.GetEditId());
					rpg_p.SetEditMode(RPG_EditMode.None);
				}
				else if (args[0].equalsIgnoreCase("next"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.quest.next"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (!RPG_Core.IsPlayer(sender))
					{
						sender.sendMessage(ChatColor.RED + "Only players can create new quests");
						return false;
					}
					
					if (rpg_p.GetEditMode() == RPG_EditMode.Quest_Create)
					{						
						if (rpg_p.GetEditStep() < 100)
							onPlayerChat(new AsyncPlayerChatEvent(true, rpg_p.GetPlayer(), "", null));
						else
						{
							sender.sendMessage(ChatColor.RED + "You are already done with the wizard. Please use " + ChatColor.GOLD + "/quest back " + ChatColor.RED + "to go back a step, or use " + 
								ChatColor.GOLD + "/quest create " + ChatColor.RED + "to create your quest. You can use " + ChatColor.GOLD + "/quest cancel " + ChatColor.RED + "to exit the wizard.");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.YELLOW + "You are not creating a quest at the moment. Start creating one by using: " + ChatColor.GOLD + "/quest new");
						return false;
					}
				}
				else if (args[0].equalsIgnoreCase("back"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.quest.back"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (!RPG_Core.IsPlayer(sender))
					{
						sender.sendMessage(ChatColor.RED + "Only players can create new quests");
						return false;
					}
					
					if (rpg_p.GetEditMode() == RPG_EditMode.Quest_Create)
					{						
						if (rpg_p.GetEditStep() > 0)
						{
							rpg_p.SetEditStep(rpg_p.GetEditStep() - 2);
							onPlayerChat(new AsyncPlayerChatEvent(true, rpg_p.GetPlayer(), "", null));
						}
						else
						{
							sender.sendMessage(ChatColor.RED + "You cannot go backwards at the first step! Use " + ChatColor.GOLD + "/quest next " + ChatColor.RED + "to go to the next step, or use " + 
									ChatColor.GOLD + "/quest cancel " + ChatColor.RED + "to exit the wizard!");
						}
					}
					else
					{
						sender.sendMessage(ChatColor.YELLOW + "You are not creating a quest at the moment. Start creating one by using: " + ChatColor.GOLD + "/quest new");
						return false;
					}
				}
				else if (args[0].equalsIgnoreCase("cancel"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.quest.cancel"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(rpg_p.GetID(), -1, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (!RPG_Core.IsPlayer(sender))
					{
						sender.sendMessage(ChatColor.RED + "Only players can create new quests");
						return false;
					}
					
					if (rpg_p.GetEditMode() == RPG_EditMode.Quest_Create)
					{
						if (rpg_p.GetEditId() == -1)
						{
							sender.sendMessage(ChatColor.YELLOW + "You are not creating a quest at the moment. Start creating one by using: " + ChatColor.GOLD + "/quest new");
							return false;
						}
						
						sender.sendMessage(ChatColor.GREEN + "Quest creation canceled");
						
						questsInCreation.remove(rpg_p.GetEditId());
						rpg_p.SetEditMode(RPG_EditMode.None);
					}
					else if (rpg_p.GetEditMode() == RPG_EditMode.Quest_Edit)
					{
						sender.sendMessage(ChatColor.GREEN + "You have stopped editing quests");
						
						rpg_p.SetEditMode(RPG_EditMode.None);
					}
				}
				else if (args[0].equalsIgnoreCase("edit"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.quest.edit"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					// DEBUG: TO BE DONE
					sender.sendMessage(ChatColor.DARK_RED + "Not implemented yet");
				}
				else if (args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("rem") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.quest.del"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (args.length < 2)
					{
						RPG_Core.ShowInfoScreen("rpg.quest.del", sender);
					}
					else
					{
						int id = -1;
						try
						{
							id = Integer.parseInt(args[1]);
						}
						catch (Exception ex)
						{
							sender.sendMessage(ChatColor.RED + "You have entered an invalid ID");
							return false;
						}
						
						if (GetQuest(id) == null)
						{
							sender.sendMessage(ChatColor.RED + "The quest with the ID " + ChatColor.GOLD + id + ChatColor.RED + " does not exist");
							return false;
						}
						
						if (DeleteQuestFromDB(id))
							sender.sendMessage(ChatColor.GREEN + "Quest deleted");
						else
							sender.sendMessage(ChatColor.RED + "Could not delete quest");
					}
				}
				else if (args[0].equalsIgnoreCase("list"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.quest.list"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					sender.sendMessage(ChatColor.BLUE + "Quest List");
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					for (RPG_Quest q : GetQuests())
						sender.sendMessage(ChatColor.GOLD + "  " + q.GetID() + " " + ChatColor.WHITE + q.GetName(rpg_p.GetLanguage()) + " | " + q.GetNPCStartID() + " | " + q.GetRecompletable());
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				}
				else if (args[0].equalsIgnoreCase("reload"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.quest.reload"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					LoadQuestsFromDB();
					
					sender.sendMessage(ChatColor.GREEN + "Quests reloaded");
				}
				else
					sender.sendMessage(ChatColor.RED + "Unknown command");
			}
		}
		else if (commandLabel.equalsIgnoreCase("journal") || commandLabel.equalsIgnoreCase("j"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.journal"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (!RPG_Core.IsPlayer(sender))
			{
				sender.sendMessage(ChatColor.RED + "Only players can access the journal");
				return false;
			}
			
			if (args.length > 0)
			{
				int id = -1;
				
				try
				{
					id = Integer.parseInt(args[0]);
				}
				catch (Exception ex)
				{
					sender.sendMessage(RPG_Core.GetFormattedMessage("journal_invalid_id", sender));
					return false;
				}
				
				RPG_Quest rpg_q = GetQuest(id);
				if (rpg_q == null)
				{
					sender.sendMessage(RPG_Core.GetFormattedMessage("journal_quest_doesnt_exist", sender));
					return false;
				}
				
				sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				sender.sendMessage(ChatColor.BLUE + "Journal - " + ChatColor.AQUA + rpg_q.GetName(rpg_p.GetLanguage()));
				sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				sender.sendMessage(ChatColor.WHITE + "  " + rpg_q.GetDescription(rpg_p.GetLanguage()));
				sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
			}
			else
			{
				sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				sender.sendMessage(RPG_Core.GetFormattedMessage("journal_title", sender));
				sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				
				sender.sendMessage("  " + RPG_Core.GetFormattedMessage("journal_title_current", sender));
				sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				ArrayList<RPG_Quest> qs = rpg_p.GetCurrentQuests();
				for (RPG_Quest rpg_q : qs)
					sender.sendMessage(ChatColor.YELLOW + "    " + rpg_q.GetID() + ". " + rpg_q.GetName(rpg_p.GetLanguage()));
				
				sender.sendMessage("");
				
				sender.sendMessage("  " + RPG_Core.GetFormattedMessage("journal_title_completed", sender));
				sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				qs = rpg_p.GetCompletedQuests();
				for (RPG_Quest rpg_q : qs)
					sender.sendMessage(ChatColor.GREEN + "    " + rpg_q.GetID() + ". " + rpg_q.GetName(rpg_p.GetLanguage()));
				
				sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
			}
		}
		else
			sender.sendMessage(RPG_Core.GetFormattedMessage("unknown_command", sender));
		
		return false;
	}
	
	// 
	// Events
	// 
	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		LoadQuestProgressesForPlayer(RPG_Core.GetPlayer(e.getPlayer().getName()).GetID());
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e)
	{
		RPG_Player rpg_p = RPG_Core.GetPlayer(e.getPlayer().getName());
		
		if (rpg_p.GetEditMode() == RPG_EditMode.Quest_Create)
		{
			e.setCancelled(true);
			
			if (!RPG_Core.HasPermission(rpg_p, "rpg.quest.create"))
			{
				rpg_p.SendMessage(RPG_Core.GetFormattedMessage("no_perm", rpg_p.GetLanguage()));
				RPG_Core.Log(rpg_p.GetID(), -1, RPG_LogType.Warning, "Action", "NoPerm: rpg.quest.create");
				return;
			}
			
			if (rpg_p.GetEditStep() == -1)
			{
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "At first, please enter a " + ChatColor.BLUE + "name " + ChatColor.WHITE + "for your Quest.");
				rpg_p.SendMessage(ChatColor.WHITE + "The " + ChatColor.BLUE + "name " + ChatColor.WHITE + "of a quest is used whenever the quest is " + ChatColor.BLUE + "mentioned ingame" + ChatColor.WHITE + ":");
				
				rpg_p.SetEditStep(0);
			}
			else if (rpg_p.GetEditStep() == 0)
			{
				if (e.getMessage() != "")
				{
					questsInCreation.get(rpg_p.GetEditId()).Name = e.getMessage();
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Name: " + ChatColor.WHITE + e.getMessage());
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Now please enter the " + ChatColor.BLUE + "display name " + ChatColor.WHITE + "of the quest:");
				rpg_p.SendMessage(ChatColor.WHITE + "The " + ChatColor.BLUE + "display name " + ChatColor.WHITE + "is used when the quest is " + ChatColor.BLUE + "listed by an NPC.");
				
				rpg_p.SetEditStep(1);
			}
			else if (rpg_p.GetEditStep() == 1)
			{
				if (e.getMessage() != "")
				{
					questsInCreation.get(rpg_p.GetEditId()).DisplayName = e.getMessage();
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Display name: " + ChatColor.WHITE + e.getMessage());
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Now please enter the " + ChatColor.BLUE + "quest type " + ChatColor.WHITE + "of the quest:");
				rpg_p.SendMessage(ChatColor.WHITE + "The fol1owing values are valid for the quest type:");
				rpg_p.SendMessage(ChatColor.GOLD + "1  " + ChatColor.BLUE + "Bring Quest  " + ChatColor.WHITE + "The player has to collect certain items");
				rpg_p.SendMessage(ChatColor.GOLD + "2  " + ChatColor.BLUE + "Kill Quest   " + ChatColor.WHITE + "The player has to kill a certain amount of mobs");
				rpg_p.SendMessage(ChatColor.GOLD + "3  " + ChatColor.BLUE + "Talk Quest   " + ChatColor.WHITE + "The player has to talk to another NPC");
				
				rpg_p.SetEditStep(2);
			}
			else if (rpg_p.GetEditStep() == 2)
			{
				if (e.getMessage() != "")
				{
					RPG_QuestType type = RPG_QuestType.None;
					
					if (e.getMessage().equalsIgnoreCase("bring") || e.getMessage().equalsIgnoreCase("bring quest") || e.getMessage().equalsIgnoreCase("1"))
						type = RPG_QuestType.Bring;
					else if (e.getMessage().equalsIgnoreCase("kill") || e.getMessage().equalsIgnoreCase("kill quest") || e.getMessage().equalsIgnoreCase("2"))
						type = RPG_QuestType.Kill;
					else if (e.getMessage().equalsIgnoreCase("talk") || e.getMessage().equalsIgnoreCase("talk quest") || e.getMessage().equalsIgnoreCase("3"))
						type = RPG_QuestType.Talk;
					
					if (type != RPG_QuestType.None)
					{
						questsInCreation.get(rpg_p.GetEditId()).Type = type;
						
						rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
						rpg_p.SendMessage(ChatColor.BLUE + "Quest type: " + ChatColor.WHITE + type);
					}
					else
					{
						rpg_p.SendMessage(ChatColor.RED + "You have entered an invalid quest type. Please enter a valid quest type, or type: " + ChatColor.GOLD + "/quest cancel " + ChatColor.RED + "to exit the wizard.");
						return;
					}
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Now please enter the " + ChatColor.BLUE + "ID " + ChatColor.WHITE + "of the start NPC:");
				rpg_p.SendMessage(ChatColor.WHITE + "This is the " + ChatColor.BLUE + "NPC " + ChatColor.WHITE + "where players will be able to " + ChatColor.BLUE + "start " + ChatColor.WHITE + "the quest.");
				rpg_p.SendMessage(ChatColor.WHITE + "You can also " + ChatColor.BLUE + "select " + ChatColor.WHITE + "an " + ChatColor.BLUE + "NPC " + ChatColor.WHITE + "by " + ChatColor.BLUE + "left clicking " + ChatColor.WHITE + "it ingame.");
				rpg_p.SendMessage(ChatColor.WHITE + "Use the " + ChatColor.GOLD + "/npc list " + ChatColor.WHITE + "command if you don't know the ID of a specific NPC.");
				
				rpg_p.SetEditStep(3);
			}
			else if (rpg_p.GetEditStep() == 3)
			{
				if (e.getMessage() != "")
				{
					Integer npcid  = -1;
					
					try
					{
						npcid = Integer.parseInt(e.getMessage());
						questsInCreation.get(rpg_p.GetEditId()).NPCStartID = npcid;
					}
					catch (Exception ex)
					{
						rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect NPC ID, please enter a numeric value, or type: " + ChatColor.GOLD + "/quest cancel " + ChatColor.RED + "to exit the wizard.");
						return;
					}
					
					if (RPG_Core.GetNpcManager().GetNPC(npcid) != null)
					{
						rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
						rpg_p.SendMessage(ChatColor.BLUE + "Start NPC ID: " + ChatColor.WHITE + e.getMessage());
					}
					else
					{
						rpg_p.SendMessage(ChatColor.RED + "An NPC with the ID you have entered does not exist. Please use an existing ID, or type " + ChatColor.GOLD + "/quest cancel " + ChatColor.RED + "to exit the wizard.");
						return;
					}
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Now please enter the " + ChatColor.BLUE + "ID " + ChatColor.WHITE + "of the end NPC:");
				rpg_p.SendMessage(ChatColor.WHITE + "This is the " + ChatColor.BLUE + "NPC " + ChatColor.WHITE + "where players will be able to " + ChatColor.BLUE + "complete " + ChatColor.WHITE + "the quest.");
				rpg_p.SendMessage(ChatColor.WHITE + "You can also " + ChatColor.BLUE + "select " + ChatColor.WHITE + "an " + ChatColor.BLUE + "NPC " + ChatColor.WHITE + "by " + ChatColor.BLUE + "left clicking " + ChatColor.WHITE + "it ingame.");
				rpg_p.SendMessage(ChatColor.WHITE + "Use the " + ChatColor.GOLD + "/npc list " + ChatColor.WHITE + "command if you don't know the ID of a specific NPC.");
				
				rpg_p.SetEditStep(4);
			}
			else if (rpg_p.GetEditStep() == 4)
			{
				if (e.getMessage() != "")
				{
					Integer npcid = -1;
					
					try
					{
						npcid = Integer.parseInt(e.getMessage());
						questsInCreation.get(rpg_p.GetEditId()).NPCEndID = npcid;
					}
					catch (Exception ex)
					{
						rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect NPC ID, please enter a numeric value, or type: " + ChatColor.GOLD + "/quest cancel " + ChatColor.RED + "to exit the wizard.");
						return;
					}
					
					if (RPG_Core.GetNpcManager().GetNPC(npcid) != null)
					{
						rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
						rpg_p.SendMessage(ChatColor.BLUE + "End NPC ID: " + ChatColor.WHITE + e.getMessage());
					}
					else
					{
						rpg_p.SendMessage(ChatColor.RED + "An NPC with the ID you have entered does not exist. Please use an existing ID, or type " + ChatColor.GOLD + "/quest cancel " + ChatColor.RED + "to exit the wizard.");
						return;
					}
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Now please enter the " + ChatColor.BLUE + "start text " + ChatColor.WHITE + "that is told by the NPC when you start the quest:");
				rpg_p.SendMessage(ChatColor.WHITE + "You have currently set your language to " + ChatColor.BLUE + rpg_p.GetLanguage() + ChatColor.WHITE + 
						", which means that you are setting the text that the NPC says in " + ChatColor.BLUE + rpg_p.GetLanguage() + ChatColor.WHITE + ". You can set the other texts later.");
				rpg_p.SendMessage(ChatColor.WHITE + "If you use " + ChatColor.BLUE + "%p_name " + ChatColor.WHITE + "in the text it will be replaced with the players name who is talking to the NPC");
				
				rpg_p.SetEditStep(5);
			}
			else if (rpg_p.GetEditStep() == 5)
			{
				if (e.getMessage() != "")
				{
					questsInCreation.get(rpg_p.GetEditId()).StartText = e.getMessage();
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Start text: " + ChatColor.WHITE + e.getMessage());
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Now please enter the " + ChatColor.BLUE + "end text " + ChatColor.WHITE + "that is told by the NPC when you complete the quest:");
				rpg_p.SendMessage(ChatColor.WHITE + "You have currently set your language to " + ChatColor.BLUE + rpg_p.GetLanguage() + ChatColor.WHITE + 
						", which means that you are setting the text that the NPC says in " + ChatColor.BLUE + rpg_p.GetLanguage() + ChatColor.WHITE + ". You can set the other texts later.");
				rpg_p.SendMessage(ChatColor.WHITE + "If you use " + ChatColor.BLUE + "%p_name " + ChatColor.WHITE + "in the text it will be replaced with the players name who is talking to the NPC");
				
				rpg_p.SetEditStep(6);
			}
			else if (rpg_p.GetEditStep() == 6)
			{
				if (e.getMessage() != "")
				{
					questsInCreation.get(rpg_p.GetEditId()).EndText = e.getMessage();
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "End text: " + ChatColor.WHITE + e.getMessage());
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Next you must specify the " + ChatColor.BLUE + "other quests " + ChatColor.WHITE + "that have to be completed in order to " + ChatColor.BLUE + "start " + ChatColor.WHITE + "this quest:");
				rpg_p.SendMessage(ChatColor.WHITE + "Enter the " + ChatColor.BLUE + "ID of each quest " + ChatColor.WHITE + "that is required, " + ChatColor.BLUE + "seperated by a comma" + ChatColor.WHITE + ".");
				rpg_p.SendMessage(ChatColor.WHITE + "Type " + ChatColor.BLUE + "<none> " + ChatColor.WHITE + "if you don't want any prerequested quests.");
				rpg_p.SendMessage(ChatColor.WHITE + "Use the " + ChatColor.GOLD + "/quest list " + ChatColor.WHITE + "command if you don't know the ID of a specific quest.");
				
				rpg_p.SetEditStep(7);
			}
			else if (rpg_p.GetEditStep() == 7)
			{
				if (e.getMessage() != "")
				{
					try
					{
						String text = "";
						
						if (!e.getMessage().equalsIgnoreCase("<none>") && !e.getMessage().equalsIgnoreCase("none") && !e.getMessage().equalsIgnoreCase("[none]") && !e.getMessage().equalsIgnoreCase("(none)"))
						{
							String[] splits = e.getMessage().split(",");
							for (String s : splits)
							{
								s = s.replace(" ", "");
								int qid = Integer.parseInt(s);
								
								if (GetQuest(qid) != null)
									questsInCreation.get(rpg_p.GetEditId()).ReqQuests.add(qid);
								else
									rpg_p.SendMessage(ChatColor.YELLOW + "The quest with the ID " + ChatColor.BLUE + qid + ChatColor.YELLOW + " does not exist. This entry has been ignored!");
							}
							
							for (Integer qid : questsInCreation.get(rpg_p.GetEditId()).ReqQuests)
								text += qid + ", ";
							
							if (text.length() > 2)
								text = text.substring(0, text.length() - 2);
						}
						
						rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
						rpg_p.SendMessage(ChatColor.BLUE + "Required quests: " + ChatColor.WHITE + text);
					}
					catch (Exception ex)
					{
						rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect list of quest IDs, please enter numeric values, seperated by comma. Type: " + ChatColor.GOLD + "/quest cancel " + ChatColor.RED + "to exit the wizard.");
						return;
					}
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Now please enter the " + ChatColor.BLUE + "required level " + ChatColor.WHITE + "to start this quest:");
				
				rpg_p.SetEditStep(8);
			}
			else if (rpg_p.GetEditStep() == 8)
			{
				if (e.getMessage() != "")
				{
					try
					{
						questsInCreation.get(rpg_p.GetEditId()).ReqLevel = Integer.parseInt(e.getMessage());
						
						rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
						rpg_p.SendMessage(ChatColor.BLUE + "Required level: " + ChatColor.WHITE + e.getMessage());
					}
					catch (Exception ex)
					{
						rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect value, please enter a numeric value, or type: " + ChatColor.GOLD + "/quest cancel " + ChatColor.RED + "to exit the wizard.");
						return;
					}
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Now please enter the " + ChatColor.BLUE + "required money " + ChatColor.WHITE + "to start this quest:");
				
				rpg_p.SetEditStep(9);
			}
			else if (rpg_p.GetEditStep() == 9)
			{
				if (e.getMessage() != "")
				{
					try
					{
						questsInCreation.get(rpg_p.GetEditId()).ReqMoney = Integer.parseInt(e.getMessage());
						
						rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
						rpg_p.SendMessage(ChatColor.BLUE + "Required money: " + ChatColor.WHITE + e.getMessage());
					}
					catch (Exception ex)
					{
						rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect value, please enter a numeric value, or type: " + ChatColor.GOLD + "/quest cancel " + ChatColor.RED + "to exit the wizard.");
						return;
					}
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Now please enter the " + ChatColor.BLUE + "reward exp " + ChatColor.WHITE + "that the player recieves upon completing this quest:");
				
				rpg_p.SetEditStep(10);
			}
			else if (rpg_p.GetEditStep() == 10)
			{
				if (e.getMessage() != "")
				{
					try
					{
						questsInCreation.get(rpg_p.GetEditId()).RewardExp = Integer.parseInt(e.getMessage());
						
						rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
						rpg_p.SendMessage(ChatColor.BLUE + "Reward exp: " + ChatColor.WHITE + e.getMessage());
					}
					catch (Exception ex)
					{
						rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect value, please enter a numeric value, or type: " + ChatColor.GOLD + "/quest cancel " + ChatColor.RED + "to exit the wizard.");
						return;
					}
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Now please enter the " + ChatColor.BLUE + "reward money " + ChatColor.WHITE + "that the player recieves upon completing this quest:");
				
				rpg_p.SetEditStep(11);
			}
			else if (rpg_p.GetEditStep() == 11)
			{
				if (e.getMessage() != "")
				{
					try
					{
						questsInCreation.get(rpg_p.GetEditId()).RewardMoney = Integer.parseInt(e.getMessage());
						
						rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
						rpg_p.SendMessage(ChatColor.BLUE + "Reward money: " + ChatColor.WHITE + e.getMessage());
					}
					catch (Exception ex)
					{
						rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect value, please enter a numeric value, or type: " + ChatColor.GOLD + "/quest cancel " + ChatColor.RED + "to exit the wizard.");
						return;
					}
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Next you must decide if this quest can be completed only once by each player, or over and over again:");
				rpg_p.SendMessage(ChatColor.WHITE + "Use " + ChatColor.BLUE + "false " + ChatColor.WHITE + "if it should only be completed once.");
				rpg_p.SendMessage(ChatColor.WHITE + "Use " + ChatColor.BLUE + "true " + ChatColor.WHITE + "if the player should be able to complete it more than once.");
				
				rpg_p.SetEditStep(12);
			}
			else if (rpg_p.GetEditStep() == 12)
			{
				if (e.getMessage() != "")
				{
					String text = e.getMessage().replace(" ", "");
					
					if (text.equalsIgnoreCase("true"))
						questsInCreation.get(rpg_p.GetEditId()).Recompletable = true;
					else if (text.equalsIgnoreCase("false"))
						questsInCreation.get(rpg_p.GetEditId()).Recompletable = false;
					else
					{
						rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect value, please enter a boolean value, or type: " + ChatColor.GOLD + "/quest cancel " + ChatColor.RED + "to exit the wizard.");
						return;
					}
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Recompletable: " + ChatColor.WHITE + e.getMessage());
				}
				
				RPG_QuestType type = questsInCreation.get(rpg_p.GetEditId()).Type;
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "At last you must enter the quest specific data. This data is dependant on the type of quest you have selected:");
				rpg_p.SendMessage(ChatColor.WHITE + "You have selected to create a " + ChatColor.BLUE + type + ChatColor.WHITE + " quest.");
				
				if (type == RPG_QuestType.Bring)
				{					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.WHITE + "Since you have chosen a " + ChatColor.BLUE + "bring quest " + ChatColor.WHITE + "type you must specify which " + ChatColor.BLUE + "items " + 
							ChatColor.WHITE + "the player must collect.");
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.WHITE + "Please enter the " + ChatColor.BLUE + "ID " + ChatColor.WHITE + "of the item that you want the player to " + ChatColor.BLUE + "collect");
					rpg_p.SendMessage(ChatColor.WHITE + "You can also take the item you wish into your hand, and type " + ChatColor.BLUE + "<hand> " + ChatColor.WHITE + "to select the item:");
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.WHITE + "When you have added all the items needed type " + ChatColor.BLUE + "<done> " + ChatColor.WHITE + "to proceed to creating the quest");
					
					rpg_p.SetEditStep(20);
				}
				else if (type == RPG_QuestType.Kill)
				{
					rpg_p.SetEditStep(30);
				}
				else if (type == RPG_QuestType.Talk)
				{
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.WHITE + "Since you have chosen a " + ChatColor.BLUE + "talk quest " + ChatColor.WHITE + "type there is nothing else you have to specify.");
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.WHITE + "You are almost done, please review your data, and enter " + ChatColor.GOLD + "/quest create " + ChatColor.WHITE + "to create the quest. Or enter " + 
							ChatColor.GOLD + "/quest cancel " + ChatColor.WHITE + "to exit the wizard.");
					RPG_QuestCreationData d = questsInCreation.get(rpg_p.GetEditId());
					String text = "";
					for (Integer qid : d.ReqQuests)
						text += qid + ", ";
					if (text.length() > 2)
						text = text.substring(0, text.length() - 2);
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Name: " + ChatColor.WHITE + d.Name);
					rpg_p.SendMessage(ChatColor.BLUE + "Display name: " + ChatColor.WHITE + d.DisplayName);
					rpg_p.SendMessage(ChatColor.BLUE + "Type: " + ChatColor.WHITE + d.Type);
					rpg_p.SendMessage(ChatColor.BLUE + "NPC start ID: " + ChatColor.WHITE + d.NPCStartID);
					rpg_p.SendMessage(ChatColor.BLUE + "NPC end ID: " + ChatColor.WHITE + d.NPCEndID);
					rpg_p.SendMessage(ChatColor.BLUE + "Start text: " + ChatColor.WHITE + d.StartText);
					rpg_p.SendMessage(ChatColor.BLUE + "End text: " + ChatColor.WHITE + d.EndText);
					rpg_p.SendMessage(ChatColor.BLUE + "Required quest IDs: " + ChatColor.WHITE + text);
					rpg_p.SendMessage(ChatColor.BLUE + "Required level: " + ChatColor.WHITE + d.ReqLevel);
					rpg_p.SendMessage(ChatColor.BLUE + "Required money: " + ChatColor.WHITE + d.ReqMoney);
					rpg_p.SendMessage(ChatColor.BLUE + "Reward Exp: " + ChatColor.WHITE + d.RewardExp);
					rpg_p.SendMessage(ChatColor.BLUE + "Reward money: " + ChatColor.WHITE + d.RewardMoney);
					rpg_p.SendMessage(ChatColor.BLUE + "Recompletable: " + ChatColor.WHITE + d.Recompletable);
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					
					rpg_p.SetEditStep(100);
				}
			}
			else if (rpg_p.GetEditStep() == 20)
			{
				if (e.getMessage() != "")
				{
					int id = -1;
					
					if (e.getMessage().equalsIgnoreCase("<done>") || e.getMessage().equalsIgnoreCase("done"))
					{
						rpg_p.SetEditStep(22);
						onPlayerChat(new AsyncPlayerChatEvent(true, rpg_p.GetPlayer(), "", null));
						return;
					}
					else if (e.getMessage().equalsIgnoreCase("<hand>") || e.getMessage().equalsIgnoreCase("hand"))
						id = rpg_p.GetPlayer().getItemInHand().getTypeId();
					else
					{
						id = Integer.parseInt(e.getMessage());
					}
					
					for (RPG_QuestRequest data : questsInCreation.get(rpg_p.GetEditId()).Data)
					{
						if (data.GetID() == id)
						{
							rpg_p.SendMessage(ChatColor.RED + "There is already an entry for the ID you have entered!");
							return;
						}
					}
					
					questsInCreation.get(rpg_p.GetEditId()).Data.add(new RPG_QuestRequest(id, 0));
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Item ID: " + ChatColor.WHITE + id);
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Now please enter the " + ChatColor.BLUE + "amount " + ChatColor.WHITE + "of the item that you want the player to " + ChatColor.BLUE + "collect" + ChatColor.WHITE + ":");
				
				rpg_p.SetEditStep(21);
			}
			else if (rpg_p.GetEditStep() == 21)
			{
				if (e.getMessage() != "")
				{
					if (e.getMessage().equalsIgnoreCase("<done>") || e.getMessage().equalsIgnoreCase("done"))
					{
						rpg_p.SetEditStep(22);
						onPlayerChat(new AsyncPlayerChatEvent(true, rpg_p.GetPlayer(), "", null));
						return;
					}
					
					int amount = -1;
					try
					{
						amount = Integer.parseInt(e.getMessage());
					}
					catch (Exception ex)
					{
						rpg_p.SendMessage(ChatColor.RED + "You have entered an incorrect amount, please enter a numeric value, or type: " + ChatColor.GOLD + "/quest cancel " + ChatColor.RED + "to exit the wizard.");
						return;
					}
					
					if (amount < 1)
					{
						rpg_p.SendMessage(ChatColor.RED + "It does not make sense to specify an " + ChatColor.BLUE + "item " + ChatColor.WHITE + "if the player doesn't have to " + ChatColor.BLUE + 
								"collect " + ChatColor.WHITE + "any of it!");
						return;
					}
					
					RPG_QuestCreationData qdata = questsInCreation.get(rpg_p.GetEditId()); 
					int id = qdata.Data.get(qdata.Data.size() - 1).GetID();
					qdata.Data.remove(qdata.Data.size() - 1);
					qdata.Data.add(new RPG_QuestRequest(id, amount));
					
					rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					rpg_p.SendMessage(ChatColor.BLUE + "Amount: " + ChatColor.WHITE + amount);
				}
				
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "Please enter the " + ChatColor.BLUE + "ID " + ChatColor.WHITE + "of the item that you want the player to " + ChatColor.BLUE + "collect" + ChatColor.WHITE + ":");
				rpg_p.SendMessage(ChatColor.WHITE + "You can also take the item you wish into your hand, and type " + ChatColor.BLUE + "<hand> " + ChatColor.WHITE + "to select the item.");
				
				rpg_p.SetEditStep(20);
			}
			else if (rpg_p.GetEditStep() == 22)
			{
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.WHITE + "You are almost done, please review your data, and enter " + ChatColor.GOLD + "/quest create " + ChatColor.WHITE + "to create the quest. Or enter " + 
						ChatColor.GOLD + "/quest cancel " + ChatColor.WHITE + "to exit the wizard.");
				RPG_QuestCreationData d = questsInCreation.get(rpg_p.GetEditId());
				String text = "";
				for (Integer qid : d.ReqQuests)
					text += qid + ", ";
				if (text.length() > 2)
					text = text.substring(0, text.length() - 2);
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				rpg_p.SendMessage(ChatColor.BLUE + "Name: " + ChatColor.WHITE + d.Name);
				rpg_p.SendMessage(ChatColor.BLUE + "Display name: " + ChatColor.WHITE + d.DisplayName);
				rpg_p.SendMessage(ChatColor.BLUE + "Type: " + ChatColor.WHITE + d.Type);
				rpg_p.SendMessage(ChatColor.BLUE + "NPC start ID: " + ChatColor.WHITE + d.NPCStartID);
				rpg_p.SendMessage(ChatColor.BLUE + "NPC end ID: " + ChatColor.WHITE + d.NPCEndID);
				rpg_p.SendMessage(ChatColor.BLUE + "Start text: " + ChatColor.WHITE + d.StartText);
				rpg_p.SendMessage(ChatColor.BLUE + "End text: " + ChatColor.WHITE + d.EndText);
				rpg_p.SendMessage(ChatColor.BLUE + "Required quest IDs: " + ChatColor.WHITE + text);
				rpg_p.SendMessage(ChatColor.BLUE + "Required level: " + ChatColor.WHITE + d.ReqLevel);
				rpg_p.SendMessage(ChatColor.BLUE + "Required money: " + ChatColor.WHITE + d.ReqMoney);
				rpg_p.SendMessage(ChatColor.BLUE + "Reward Exp: " + ChatColor.WHITE + d.RewardExp);
				rpg_p.SendMessage(ChatColor.BLUE + "Reward money: " + ChatColor.WHITE + d.RewardMoney);
				rpg_p.SendMessage(ChatColor.BLUE + "Recompletable: " + ChatColor.WHITE + d.Recompletable);
				rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				
				rpg_p.SetEditStep(100);
			}
			else if (rpg_p.GetEditStep() == 30)
			{
				
			}
			else if (rpg_p.GetEditStep() == 98)
			{
				rpg_p.SetEditStep(11);
				onPlayerChat(new AsyncPlayerChatEvent(true, rpg_p.GetPlayer(), "", null));
			}
		}
	}
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent e)
	{ }
	
	@EventHandler (priority = EventPriority.LOW)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e)
	{
		try
		{
			if (e.getCause() == DamageCause.ENTITY_ATTACK && e.getEntityType() == EntityType.PLAYER)
			{
				if (RPG_Core.GetNpcManager().GetIDFromUUID(e.getEntity().getUniqueId()) != -1)
				{
					Player p = (Player)e.getDamager();
					RPG_Npc rpg_npc = RPG_Core.GetNpcManager().GetNPC(e.getEntity().getUniqueId());
					RPG_Player rpg_p = RPG_Core.GetPlayer(p.getName());
					
					if (rpg_p.GetEditMode() == RPG_EditMode.None)
					{						
						if (rpg_p.IsTalking())
							return;
						
						ArrayList<RPG_Quest> qs = rpg_p.GetCurrentQuests();
						for (RPG_Quest rpg_q : qs)
						{							
							if (rpg_q.GetQuestType() == RPG_QuestType.Talk && rpg_q.GetNPCEndID() == rpg_npc.GetID())
							{
								if (!RPG_Core.HasPermission(rpg_p, "rpg.quest.complete"))
								{
									rpg_p.SendMessage(RPG_Core.GetFormattedMessage("no_perm", rpg_p.GetLanguage()));
									RPG_Core.Log(rpg_p.GetID(), -1, RPG_LogType.Warning, "Action", "NoPerm: rpg.quest.complete");
									continue;
								}
							}
							else if (rpg_q.GetQuestType() == RPG_QuestType.Kill)
							{
								boolean completed = true;
								RPG_Quest_Kill rpg_qk = (RPG_Quest_Kill)rpg_q;
								RPG_QuestProgress rpg_qp = GetQuestProgressForQuest(rpg_p.GetID(), rpg_q.GetID());
								
								for (RPG_QuestRequest mob : rpg_qk.GetMobs())
								{
									if (rpg_qp.GetProgress(mob.GetID()) < mob.GetAmount())
									{
										completed = false;
										break;
									}
								}
								
								if (completed)
								{
									if (!RPG_Core.HasPermission(rpg_p, "rpg.quest.complete"))
									{
										rpg_p.SendMessage(RPG_Core.GetFormattedMessage("no_perm", rpg_p.GetLanguage()));
										RPG_Core.Log(rpg_p.GetID(), -1, RPG_LogType.Warning, "Action", "NoPerm: rpg.quest.complete");
										continue;
									}
								}
								else
									continue;
							}
							else if (rpg_q.GetQuestType() == RPG_QuestType.Bring)
							{
								boolean completed = true;
								RPG_Quest_Bring rpg_qb = (RPG_Quest_Bring)rpg_q;
								
								for (RPG_QuestRequest item : rpg_qb.GetItems())
								{
									if (!p.getInventory().containsAtLeast(new ItemStack(item.GetID()), item.GetAmount()))
									{
										completed = false;
										break;
									}
								}
								
								if (!completed)
									continue;
								
								for (RPG_QuestRequest item : rpg_qb.GetItems())
								{
									int num = item.GetAmount();
									while (num > 0)
									{
										ItemStack stack = p.getInventory().getItem(p.getInventory().first(item.GetID()));
										if (stack.getAmount() > num)
										{
											stack.setAmount(stack.getAmount() - num);
											num = 0;
										}
										else if (stack.getAmount() == item.GetAmount())
										{
											p.getInventory().removeItem(stack);
											num = 0;
										}
										else
										{
											num -= stack.getAmount();
											p.getInventory().removeItem(stack);
										}
									}
								}
								
								if (!RPG_Core.HasPermission(rpg_p, "rpg.quest.complete"))
								{
									rpg_p.SendMessage(RPG_Core.GetFormattedMessage("no_perm", rpg_p.GetLanguage()));
									RPG_Core.Log(rpg_p.GetID(), -1, RPG_LogType.Warning, "Action", "NoPerm: rpg.quest.complete");
									continue;
								}
								
								if (!rpg_qb.GetKeepItems())
								{
									for (RPG_QuestRequest item : rpg_qb.GetItems())
										rpg_p.GetPlayer().getInventory().remove(new ItemStack(item.GetID(), item.GetAmount()));
								}
							}
							
							rpg_p.AddMoney(rpg_q.GetRewardMoney());
							rpg_p.AddExp(rpg_q.GetRewardExp());		
							
							rpg_p.SendMessage(ChatColor.BLUE + rpg_npc.GetName() + ": " + ChatColor.WHITE + rpg_q.GetNPCEndText(rpg_p));
							
							rpg_p.EndQuest(rpg_q.GetID());
							
							rpg_p.SendMessage(RPG_Core.GetFormattedMessage("quest_end", rpg_p.GetLanguage(), rpg_q));
							rpg_p.SendMessage(RPG_Core.GetFormattedMessage("quest_reward_get", rpg_p.GetLanguage(), rpg_q));
							
							String title = RPG_Core.FormatTextForAchievementMessage(RPG_Core.GetFormattedMessage("quest_end_short", rpg_p.GetLanguage(), rpg_q));
							String name = RPG_Core.FormatTextForAchievementMessage(rpg_q.GetName(rpg_p.GetLanguage()));
							
							if (rpg_p.IsSpoutPlayer())
								SpoutManager.getPlayer(p).sendNotification(title, name, Material.BOOK_AND_QUILL);
						}
					}
					else if (rpg_p.GetEditMode() == RPG_EditMode.Quest_Create)
					{
						if (rpg_p.GetEditStep() == 3)
						{
							questsInCreation.get(rpg_p.GetEditId()).NPCStartID = rpg_npc.GetID();
							
							if (rpg_p.IsSpoutPlayer())
							{
								RPG_Screen_CreateQuest2 scr = new RPG_Screen_CreateQuest2(this, rpg_p, questsInCreation.get(rpg_p.GetEditId()));
								scr.Show();
							}
							else
							{
								rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
								rpg_p.SendMessage(ChatColor.BLUE + "Start NPC ID: " + ChatColor.WHITE + rpg_npc.GetID());
								
								rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
								rpg_p.SendMessage(ChatColor.WHITE + "Now please enter the " + ChatColor.BLUE + "ID " + ChatColor.WHITE + "of the end NPC:");
								rpg_p.SendMessage(ChatColor.WHITE + "This is the " + ChatColor.BLUE + "NPC " + ChatColor.WHITE + "where players will be able to " + ChatColor.BLUE + "complete " + ChatColor.WHITE + "the quest.");
								rpg_p.SendMessage(ChatColor.WHITE + "You can also " + ChatColor.BLUE + "select " + ChatColor.WHITE + "an " + ChatColor.BLUE + "NPC " + ChatColor.WHITE + "by " + ChatColor.BLUE + "left clicking " + ChatColor.WHITE + "it ingame:");
								
								rpg_p.SetEditStep(4);
							}
						}
						else if (rpg_p.GetEditStep() == 4)
						{
							questsInCreation.get(rpg_p.GetEditId()).NPCEndID = rpg_npc.GetID();
							
							if (rpg_p.IsSpoutPlayer())
							{
								RPG_Screen_CreateQuest2 scr = new RPG_Screen_CreateQuest2(this, rpg_p, questsInCreation.get(rpg_p.GetEditId()));
								scr.Show();
							}
							else
							{							
								rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
								rpg_p.SendMessage(ChatColor.BLUE + "End NPC ID: " + ChatColor.WHITE + rpg_npc.GetID());
								
								rpg_p.SendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
								rpg_p.SendMessage(ChatColor.WHITE + "Now please enter the " + ChatColor.BLUE + "start text " + ChatColor.WHITE + "that is told by the NPC where you start the quest:");
								rpg_p.SendMessage(ChatColor.WHITE + "You have currently set your language to " + ChatColor.BLUE + rpg_p.GetLanguage() + ChatColor.WHITE + 
										", which means that you are setting the text that the NPC says in " + ChatColor.BLUE + rpg_p.GetLanguage() + ChatColor.WHITE + ". You can set the other texts later.");
								rpg_p.SendMessage(ChatColor.WHITE + "If you use " + ChatColor.BLUE + "%p_name " + ChatColor.WHITE + "in the text it will be replaced with the players name who is talking to the NPC");
								
								rpg_p.SetEditStep(5);
							}
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e)
	{
		try
		{
			if (e.getEntityType() != EntityType.PLAYER)
			{
				LivingEntity target = e.getEntity();
				Player p = target.getKiller();
				
				if (p == null)
				{
					if (target.getLastDamageCause() == null)
						return;
					
					if (target.getLastDamageCause().getEntityType() == EntityType.PLAYER)
						p = (Player)target.getLastDamageCause();
					else
						return;
				}
				
				RPG_Player rpg_p = RPG_Core.GetPlayer(p.getName());
				
				ArrayList<RPG_Quest> qs = rpg_p.GetCurrentQuests();
				for (RPG_Quest rpg_q : qs)
				{
					if (rpg_q.GetQuestType() == RPG_QuestType.Kill)
					{
						RPG_Quest_Kill rpg_qk = (RPG_Quest_Kill)rpg_q;
						for (RPG_QuestRequest mob : rpg_qk.GetMobs())
						{
							if (mob.GetID() == target.getEntityId())
							{
								RPG_QuestProgress rpg_qp = GetQuestProgressForQuest(rpg_p.GetID(), rpg_q.GetID());
								rpg_qp.AddProgress(mob.GetID());
								
								rpg_p.SendMessage(RPG_Core.GetFormattedMessage("quest_mob_kill", rpg_p.GetLanguage(), rpg_q));
								
								p.sendMessage(ChatColor.GOLD + "You have killed " + ChatColor.BLUE + 
										rpg_qp.GetProgress(mob.GetID()) + ChatColor.GOLD + "/" + ChatColor.BLUE + mob.GetAmount() + ChatColor.GOLD + " mobs");
								
								String title = RPG_Core.FormatTextForAchievementMessage(RPG_Core.GetFormattedMessage("quest_mob_kill_short", rpg_p.GetLanguage(), rpg_q));
								String text = RPG_Core.FormatTextForAchievementMessage(rpg_q.GetName(rpg_p.GetLanguage()));
								
								SpoutManager.getPlayer(p).sendNotification(title, text, Material.BOOK_AND_QUILL);
							}
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	@EventHandler
	protected void onButtonClick(ButtonClickEvent e)
	{
		RPG_Player rpg_p = RPG_Core.GetPlayer(e.getPlayer().getName());
		RPG_QuestCreationData data = questsInCreation.get(rpg_p.GetEditId());
		
		if (e.getScreen().getClass() == RPG_Screen_CreateQuest1.class)
		{
			RPG_Screen_CreateQuest1 scr_q = (RPG_Screen_CreateQuest1)e.getScreen();
			
			if (e.getButton().getType() == WidgetType.Button)
			{
				data.Name = scr_q.GetQuestName();
				data.DisplayName = scr_q.GetQuestDisplayName();
				data.Type = scr_q.GetQuestType();
				data.Recompletable = scr_q.GetQuestRecompletable();
				data.ReqLevel = scr_q.GetQuestReqLevel();
				data.ReqMoney = scr_q.GetQuestReqMoney();
				data.RewardExp = scr_q.GetQuestRewardExp();
				data.RewardMoney = scr_q.GetQuestRewardMoney();
				data.Description = scr_q.GetQuestDescription();
				
				if (scr_q.IsCancelButton(e.getButton()))
				{
					scr_q.Hide();
					
					rpg_p.SendMessage(ChatColor.GREEN + "Quest creation canceled");
					
					questsInCreation.remove(rpg_p.GetEditId());
					rpg_p.SetEditMode(RPG_EditMode.None);
				}
				else if (scr_q.IsNextButton(e.getButton()))
				{
					scr_q.ResetColors();
					
					if (scr_q.GetQuestName().equalsIgnoreCase(""))
					{
						scr_q.QuestNameNotSpecified();
						return;
					}
					else if (scr_q.GetQuestType() == RPG_QuestType.None)
					{
						scr_q.QuestTypeNotSpecified();
						return;
					}
					
					scr_q.Hide();
					
					RPG_Screen_CreateQuest2 scr = new RPG_Screen_CreateQuest2(this, rpg_p, data);
					scr.Show();
				}
			}
		}
		else if (e.getScreen().getClass() == RPG_Screen_CreateQuest2.class)
		{
			RPG_Screen_CreateQuest2 scr_q = (RPG_Screen_CreateQuest2)e.getScreen();
			
			if (e.getButton().getType() == WidgetType.Button)
			{
				data.ReqQuests = scr_q.GetQuestReqQuests();
				data.NPCStartID = scr_q.GetQuestStartNPCID();
				data.NPCEndID = scr_q.GetQuestEndNPCID();
				
				if (scr_q.IsCancelButton(e.getButton()))
				{
					scr_q.Hide();
					
					rpg_p.SendMessage(ChatColor.GREEN + "Quest creation canceled");
					
					questsInCreation.remove(rpg_p.GetEditId());
					rpg_p.SetEditMode(RPG_EditMode.None);
				}
				else if (scr_q.IsBackButton(e.getButton()))
				{					
					scr_q.Hide();
					
					RPG_Screen_CreateQuest1 scr = new RPG_Screen_CreateQuest1(this, rpg_p, questsInCreation.get(rpg_p.GetEditId()));
					scr.Show();
				}
				else if (scr_q.IsNextButton(e.getButton()))
				{
					scr_q.ResetColors();
					
					if (!RPG_Core.GetNpcManager().IsNPCID(scr_q.GetQuestStartNPCID()))
					{
						scr_q.StartNPCNotSpecified();
						return;
					}
					else if (!RPG_Core.GetNpcManager().IsNPCID(scr_q.GetQuestEndNPCID()))
					{
						scr_q.EndNPCNotSpecified();
						return;
					}
					
					scr_q.Hide();
					
					RPG_Screen_CreateQuest3 scr = new RPG_Screen_CreateQuest3(this, rpg_p, questsInCreation.get(rpg_p.GetEditId()));
					scr.Show();
				}
				else if (scr_q.IsReqsAddButton(e.getButton()))
				{
					scr_q.AddItemToReqs();
				}
				else if (scr_q.IsReqsRemoveButton(e.getButton()))
				{
					scr_q.RemoveItemFromReqs();
				}
				else if (scr_q.IsPickNPCStartButton(e.getButton()))
				{
					scr_q.Hide();
					
					rpg_p.SetEditStep(3);
				}
				else if (scr_q.IsPickNPCEndButton(e.getButton()))
				{
					scr_q.Hide();
					
					rpg_p.SetEditStep(4);
				}
			}
		}
		else if (e.getScreen().getClass() == RPG_Screen_CreateQuest3.class)
		{
			RPG_Screen_CreateQuest3 scr_q = (RPG_Screen_CreateQuest3)e.getScreen();
			
			if (e.getButton().getType() == WidgetType.Button)
			{
				data.StartText = scr_q.GetQuestStartText();
				data.EndText = scr_q.GetQuestEndText();
				
				if (scr_q.IsCancelButton(e.getButton()))
				{
					scr_q.Hide();
					
					rpg_p.SendMessage(ChatColor.GREEN + "Quest creation canceled");
					
					questsInCreation.remove(rpg_p.GetEditId());
					rpg_p.SetEditMode(RPG_EditMode.None);
				}
				else if (scr_q.IsBackButton(e.getButton()))
				{					
					scr_q.Hide();
					
					RPG_Screen_CreateQuest2 scr = new RPG_Screen_CreateQuest2(this, rpg_p, questsInCreation.get(rpg_p.GetEditId()));
					scr.Show();
				}
				else if (scr_q.IsNextButton(e.getButton()))
				{
					scr_q.Hide();
					
					if (questsInCreation.get(rpg_p.GetEditId()).Type == RPG_QuestType.Talk)
					{
						int id = AddQuestToDB(questsInCreation.get(rpg_p.GetEditId()), rpg_p.GetLanguage());
						
						if (id >= 0)
							rpg_p.SendMessage(ChatColor.GREEN + "Quest with id " + ChatColor.BLUE + id + ChatColor.GREEN + " created");
						else
							rpg_p.SendMessage(ChatColor.RED + "Quest creation failed!");
						
						questsInCreation.remove(rpg_p.GetEditId());
						rpg_p.SetEditMode(RPG_EditMode.None);
					}
					else
					{
						RPG_Screen_CreateQuest4 scr = new RPG_Screen_CreateQuest4(this, rpg_p, questsInCreation.get(rpg_p.GetEditId()));
						scr.Show();
					}
				}
			}
		}
		else if (e.getScreen().getClass() == RPG_Screen_CreateQuest4.class)
		{
			RPG_Screen_CreateQuest4 scr_q = (RPG_Screen_CreateQuest4)e.getScreen();
			
			if (e.getButton().getType() == WidgetType.Button)
			{
				data.Data = scr_q.GetQuestData();
				
				if (scr_q.IsCancelButton(e.getButton()))
				{
					scr_q.Hide();
					
					rpg_p.SendMessage(ChatColor.GREEN + "Quest creation canceled");
					
					questsInCreation.remove(rpg_p.GetEditId());
					rpg_p.SetEditMode(RPG_EditMode.None);
				}
				else if (scr_q.IsBackButton(e.getButton()))
				{					
					scr_q.Hide();
					
					RPG_Screen_CreateQuest3 scr = new RPG_Screen_CreateQuest3(this, rpg_p, questsInCreation.get(rpg_p.GetEditId()));
					scr.Show();
				}
				else if (scr_q.IsCreateButton(e.getButton()))
				{
					scr_q.Hide();
					
					int id = AddQuestToDB(questsInCreation.get(rpg_p.GetEditId()), rpg_p.GetLanguage());
					
					if (id >= 0)
						rpg_p.SendMessage(ChatColor.GREEN + "Quest with id " + ChatColor.BLUE + id + ChatColor.GREEN + " created");
					else
						rpg_p.SendMessage(ChatColor.RED + "Quest creation failed!");
					
					questsInCreation.remove(rpg_p.GetEditId());
					rpg_p.SetEditMode(RPG_EditMode.None);
				}
				else if (scr_q.IsAddItemButton(e.getButton()))
				{
					scr_q.AddItemToList();
				}
				else if (scr_q.IsRemoveItemButton(e.getButton()))
				{
					scr_q.RemoveItemFromList();
				}
			}
		}
	}
	
	@EventHandler
	protected void onTextFieldChange(TextFieldChangeEvent e)
	{
		if (e.isCancelled())
			return;
		
		if (e.getScreen().getClass() == RPG_Screen_CreateQuest2.class)
		{
			RPG_Screen_CreateQuest2 scr_q = (RPG_Screen_CreateQuest2)e.getScreen();
			int id = -1;
			if (!e.getNewText().equalsIgnoreCase(""))
				id = Integer.parseInt(e.getNewText());
			
			if (scr_q.IsNPCStartNumField(e.getTextField()))
				scr_q.UpdateStartNPC(id);
			else if (scr_q.IsNPCEndNumField(e.getTextField()))
				scr_q.UpdateEndNPC(id);
		}
		else if (e.getScreen().getClass() == RPG_Screen_CreateQuest4.class)
		{
			RPG_Screen_CreateQuest4 scr_q = (RPG_Screen_CreateQuest4)e.getScreen();
			int id = -1;
			if (!e.getNewText().equalsIgnoreCase(""))
				id = Integer.parseInt(e.getNewText());
			
			if (scr_q.IsNewItemField(e.getTextField()))
				scr_q.UpdateNewItem(id);
		}
	}
	
	// 
	// Quests
	// 
	public RPG_Quest GetQuest(int ID)
	{
		if (quests.containsKey(ID))
			return quests.get(ID);
		else
		{
			logger.warning(prefix + "Quest with id " + ID + " does not exist");
			RPG_Core.Log(-1, ID, RPG_LogType.Warning, "Quest", "Does not exist");
			return null;
		}
	}
	public ArrayList<RPG_Quest> GetQuests()
	{
		return new ArrayList<RPG_Quest>(quests.values());
	}
	
	public int AddQuestToDB(RPG_QuestCreationData Data, RPG_Language Language)
	{
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			
			int type = 0;
			if (Data.Type == RPG_QuestType.Bring)
				type = 1;
			else if (Data.Type == RPG_QuestType.Kill)
				type = 2;
			else if (Data.Type == RPG_QuestType.Talk)
				type = 3;
			
			int nameid = -1;
			stmt.executeUpdate("CALL " + SQLTablePrefix + "textSave('" + Language + "', '" + Data.Name + "', @nameid)");
			
			rs = stmt.executeQuery("SELECT @nameid AS NameID;");
			rs.first();
			nameid = rs.getInt(1);
			
			int dispnameid = -1;
			stmt.executeUpdate("CALL " + SQLTablePrefix + "textSave('" + Language + "', '" + Data.DisplayName + "', @dispnameid)");
			
			rs = stmt.executeQuery("SELECT @dispnameid AS DispNameID;");
			rs.first();
			dispnameid = rs.getInt(1);
			
			int descrid = -1;
			stmt.executeUpdate("CALL " + SQLTablePrefix + "textSave('" + Language + "', '" + Data.Description + "', @descrid)");
			
			rs = stmt.executeQuery("SELECT @descrid AS DescrID;");
			rs.first();
			descrid = rs.getInt(1);
			
			int starttextid = -1;
			stmt.executeUpdate("CALL " + SQLTablePrefix + "textSave('" + Language + "', '" + Data.StartText + "', @starttextid)");
			
			rs = stmt.executeQuery("SELECT @starttextid AS StartTextID;");
			rs.first();
			starttextid = rs.getInt(1);
			
			int endtextid = -1;
			stmt.executeUpdate("CALL " + SQLTablePrefix + "textSave('" + Language + "', '" + Data.EndText + "', @endtextid)");
			
			rs = stmt.executeQuery("SELECT @endtextid AS EndTextID;");
			rs.first();
			endtextid = rs.getInt(1);
			
			String reqquests = "";
			for (Integer id : Data.ReqQuests)
				reqquests += id + RPG_Core.GetDBSeperator();
			if (reqquests.length() > 0)
				reqquests = reqquests.substring(0, reqquests.length() - 2);
			
			String tag = "0" + RPG_Core.GetDBSeperator();		// Do not keep the items
			for (RPG_QuestRequest data : Data.Data)
				tag += data.GetID() + RPG_Core.GetDBSeperator2() + data.GetAmount() + RPG_Core.GetDBSeperator2();
			if (tag.length() > 0)
				tag = tag.substring(0, tag.length() - 1);
			
			stmt.executeUpdate("INSERT INTO " + SQLTablePrefix + "quests (NameID, DisplayNameID, DescriptionID, QuestType, NPCStartID, NPCEndID, NPCStartTextID, NPCEndTextID, " +
					"ReqQuestIDs, ReqMoney, ReqLevel, RewardExp, RewardMoney, Recompletable, Tag) " +
					"VALUES (" + nameid + ", " + dispnameid + ", " + descrid + ", " + type + ", " + Data.NPCStartID + ", " + Data.NPCEndID + ", " + starttextid + ", " + endtextid + 
					", '" + reqquests + "', " + Data.ReqMoney + ", " + Data.ReqLevel + ", " + Data.RewardExp + ", " + Data.RewardMoney + ", " + Data.Recompletable + ", '" + tag + "');");
			
			rs = stmt.executeQuery("select last_insert_id();");
			rs.last();
			int id = rs.getInt(1);
			
			RPG_Core.Log(-1, id, RPG_LogType.Information, "QuestCreate", "Created");
			
			RPG_Core.ReloadText(nameid);
			RPG_Core.ReloadText(dispnameid);
			RPG_Core.ReloadText(descrid);
			RPG_Core.ReloadText(starttextid);
			RPG_Core.ReloadText(endtextid);
			
			ReloadQuest(id);
			
			return id;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			
			RPG_Core.Log(RPG_LogType.Error, "QuestCreate", ex);
			
			return -1;
		}
	}
	public boolean DeleteQuestFromDB(int ID)
	{
		Statement stmt = null;
		
		try
		{
			if (!quests.containsKey(ID))
				return false;
			
			RPG_Quest rpg_q = GetQuest(ID);
			
			stmt = RPG_Core.GetDatabaseStatement();
			stmt.executeUpdate("DELETE FROM " + SQLTablePrefix + "quests WHERE ID = " + ID + ";");
			
			stmt.executeUpdate("DELETE FROM " + SQLTablePrefix + "questprogress WHERE QuestID = " + ID + ";");
			
			stmt.executeUpdate("CALL " + SQLTablePrefix + "textDelete(" + rpg_q.GetNameID() + ");");
			stmt.executeUpdate("CALL " + SQLTablePrefix + "textDelete(" + rpg_q.GetDisplayNameID() + ");");
			stmt.executeUpdate("CALL " + SQLTablePrefix + "textDelete(" + rpg_q.GetDescriptionID() + ");");
			stmt.executeUpdate("CALL " + SQLTablePrefix + "textDelete(" + rpg_q.GetNPCStartTextID() + ");");
			stmt.executeUpdate("CALL " + SQLTablePrefix + "textDelete(" + rpg_q.GetNPCEndTextID() + ");");
			
			quests.remove(ID);
			
			RPG_Core.Log(-1, ID, RPG_LogType.Information, "QuestDelete", "Deleted");
			
			return true;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "QuestDelete", ex);
			return false;
		}
	}
	
	public ArrayList<RPG_Quest> GetQuestsByNPC(int NPCID)
	{
		ArrayList<RPG_Quest> qs = new ArrayList<RPG_Quest>();
		
		for (RPG_Quest q : quests.values())
		{
			if (q.GetNPCStartID() == NPCID)
				qs.add(q);
		}
		
		return qs;
	}
	
	public ArrayList<RPG_Quest> GetCompletedQuestsByPlayer(int PlayerID)
	{
		ArrayList<RPG_Quest> completedQuests = new ArrayList<RPG_Quest>();
		
		for (RPG_QuestProgress rpg_qp : questprogresses.get(PlayerID).values())
		{
			if (rpg_qp.GetCompleted())
				completedQuests.add(GetQuest(rpg_qp.GetQuestID()));
		}
	    
		return completedQuests;
	}
	public ArrayList<RPG_Quest> GetCurrentQuestsByPlayer(int PlayerID)
	{
		ArrayList<RPG_Quest> currentQuests = new ArrayList<RPG_Quest>();
		
		for (RPG_QuestProgress rpg_qp : questprogresses.get(PlayerID).values())
		{
			if (!rpg_qp.GetCompleted())
				currentQuests.add(GetQuest(rpg_qp.GetQuestID()));
		}
	    
		return currentQuests;
	}
	
	public void LoadQuestsFromDB()
	{
		logger.info(prefix + "Loading quests...");
		
		quests.clear();
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			rs = stmt.executeQuery("SELECT ID, NameID, DisplayNameID, DescriptionID, QuestType, NPCStartID, NPCEndID, NPCStartTextID, NPCEndTextID, " +
					"ReqQuestIDs, ReqMoney, ReqLevel, RewardExp, RewardMoney, Recompletable, Tag FROM " + SQLTablePrefix + "quests;");
			
			while (rs.next())
			{
				ArrayList<Integer> list = new ArrayList<Integer>();
				RPG_QuestType type = RPG_QuestType.None;
				
				if (rs.getString("ReqQuestIDs") != null)
				{
					String[] splits = rs.getString("ReqQuestIDs").split(RPG_Core.GetDBSeperator());
					
					try
					{
						for (String line : splits)
						{
							if (!line.equalsIgnoreCase(""))
								list.add(Integer.parseInt(line));
						}
					}
					catch (Exception ex)
					{
						logger.warning(prefix + "  Could not load quest " + rs.getInt("ID") + ": '" + rs.getString("ReqQuestIDs") + "' is not a valid quest list");
						RPG_Core.Log(-1, rs.getInt("ID"), RPG_LogType.Warning, "Quest", rs.getString("ReqQuestIDs") + " is not a valid quest id list");
						continue;
					}
				}
				
				if (rs.getInt("QuestType") == 1)
				{
					type = RPG_QuestType.Bring;
					ArrayList<RPG_QuestRequest> items = new ArrayList<RPG_QuestRequest>();
					boolean keepitems = false;
					String[] subsplits = rs.getString("Tag").split(RPG_Core.GetDBSeperator());
					
					try
					{
						keepitems = Boolean.parseBoolean(subsplits[0]);
						for (int i = 1; i < subsplits.length; i++)
						{
							String[] elements = subsplits[i].split(RPG_Core.GetDBSeperator2());
							items.add(new RPG_QuestRequest(Integer.parseInt(elements[0]), Integer.parseInt(elements[1])));
						}
					}
					catch (Exception ex)
					{
						logger.warning(prefix + "  Could not load quest " + rs.getInt("ID") + ": '" + rs.getString("Tag") + "' is not a valid tag");
						RPG_Core.Log(-1, rs.getInt("ID"), RPG_LogType.Warning, "Quest", rs.getString("Tag") + " is not a valid tag");
						continue;
					}
					
					quests.put(rs.getInt("ID"), new RPG_Quest_Bring(rs.getInt("ID"), rs.getInt("NameID"), rs.getInt("DisplayNameID"), rs.getInt("DescriptionID"), type, 
							rs.getInt("NPCStartID"), rs.getInt("NPCEndID"), rs.getInt("NPCStartTextID"), rs.getInt("NPCEndTextID"), list, rs.getInt("ReqMoney"), 
							rs.getInt("ReqLevel"), rs.getInt("RewardExp"), rs.getInt("RewardMoney"), rs.getBoolean("Recompletable"), items, keepitems));
					
					RPG_Core.Log(-1, rs.getInt("ID"), RPG_LogType.Information, "Quest", "Loaded");
				}
				else if (rs.getInt("QuestType") == 2)
				{
					type = RPG_QuestType.Kill;
					ArrayList<RPG_QuestRequest> mobs = new ArrayList<RPG_QuestRequest>();
					String[] subsplits = rs.getString("Tag").split(RPG_Core.GetDBSeperator());
					
					try
					{
						for (int i = 0; i < subsplits.length; i++)
						{
							String[] elements = subsplits[i].split(RPG_Core.GetDBSeperator2());
							mobs.add(new RPG_QuestRequest(Integer.parseInt(elements[0]), Integer.parseInt(elements[1])));
						}
					}
					catch (Exception ex)
					{
						logger.warning(prefix + "  Could not load quest " + rs.getInt("ID") + ": '" + rs.getString("Tag") + "' is not a valid tag");
						RPG_Core.Log(-1, rs.getInt("ID"), RPG_LogType.Warning, "Quest", rs.getString("Tag") + " is not a valid tag");
						continue;
					}
					
					quests.put(rs.getInt("ID"), new RPG_Quest_Kill(rs.getInt("ID"), rs.getInt("NameID"), rs.getInt("DisplayNameID"), rs.getInt("DescriptionID"), type, 
							rs.getInt("NPCStartID"), rs.getInt("NPCEndID"), rs.getInt("NPCStartTextID"), rs.getInt("NPCEndTextID"), list, rs.getInt("ReqMoney"), 
							rs.getInt("ReqLevel"), rs.getInt("RewardExp"), rs.getInt("RewardMoney"), rs.getBoolean("Recompletable"), mobs));
					
					RPG_Core.Log(-1, rs.getInt("ID"), RPG_LogType.Information, "Quest", "Loaded");
				}
				else if (rs.getInt("QuestType") == 3)
				{
					type = RPG_QuestType.Talk;
					
					quests.put(rs.getInt("ID"), new RPG_Quest_Talk(rs.getInt("ID"), rs.getInt("NameID"), rs.getInt("DisplayNameID"), rs.getInt("DescriptionID"), type, 
							rs.getInt("NPCStartID"), rs.getInt("NPCEndID"), rs.getInt("NPCStartTextID"), rs.getInt("NPCEndTextID"), list, rs.getInt("ReqMoney"), 
							rs.getInt("ReqLevel"), rs.getInt("RewardExp"), rs.getInt("RewardMoney"), rs.getBoolean("Recompletable")));
					
					RPG_Core.Log(-1, rs.getInt("ID"), RPG_LogType.Information, "Quest", "Loaded");
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "Quest", ex);
		}
		
		logger.info(prefix + "  Loaded " + quests.size() + " quest(s) from database!");
	}
	public void ReloadQuest(int ID)
	{		
		if (quests.containsKey(ID))
			quests.remove(ID);
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = RPG_Core.GetDatabaseStatement();
			rs = stmt.executeQuery("SELECT ID, NameID, DisplayNameID, DescriptionID, QuestType, NPCStartID, NPCEndID, NPCStartTextID, NPCEndTextID, " +
					"ReqQuestIDs, ReqMoney, ReqLevel, RewardExp, RewardMoney, Recompletable, Tag FROM " + SQLTablePrefix + "quests WHERE ID = " + ID + ";");
			
			if (rs.first())
			{
				ArrayList<Integer> list = new ArrayList<Integer>();
				RPG_QuestType type = RPG_QuestType.None;
				
				if (rs.getString("ReqQuestIDs") != null)
				{
					String[] splits = rs.getString("ReqQuestIDs").split(RPG_Core.GetDBSeperator());
					
					try
					{
						for (String line : splits)
						{
							if (!line.equalsIgnoreCase(""))
								list.add(Integer.parseInt(line));
						}
					}
					catch (Exception ex)
					{
						logger.warning(prefix + "Could not load quest " + rs.getInt("ID") + ": '" + rs.getString("ReqQuestIDs") + "' is not a valid quest list");
						RPG_Core.Log(-1, rs.getInt("ID"), RPG_LogType.Warning, "Quest", rs.getString("ReqQuestIDs") + " is not a valid quest id list");
						return;
					}
				}
				
				if (rs.getInt("QuestType") == 1)
				{
					type = RPG_QuestType.Bring;
					ArrayList<RPG_QuestRequest> items = new ArrayList<RPG_QuestRequest>();
					boolean keepitems = false;
					String[] subsplits = rs.getString("Tag").split(RPG_Core.GetDBSeperator());
					
					try
					{
						keepitems = Boolean.parseBoolean(subsplits[0]);
						for (int i = 1; i < subsplits.length; i++)
						{
							String[] elements = subsplits[i].split(RPG_Core.GetDBSeperator2());
							items.add(new RPG_QuestRequest(Integer.parseInt(elements[0]), Integer.parseInt(elements[1])));
						}
					}
					catch (Exception ex)
					{
						logger.warning(prefix + "Could not load quest " + rs.getInt("ID") + ": '" + rs.getString("Tag") + "' is not a valid tag");
						RPG_Core.Log(-1, rs.getInt("ID"), RPG_LogType.Warning, "Quest", rs.getString("Tag") + " is not a valid tag");
						return;
					}
					
					quests.put(rs.getInt("ID"), new RPG_Quest_Bring(rs.getInt("ID"), rs.getInt("NameID"), rs.getInt("DisplayNameID"), rs.getInt("DescriptionID"), type, 
							rs.getInt("NPCStartID"), rs.getInt("NPCEndID"), rs.getInt("NPCStartTextID"), rs.getInt("NPCEndTextID"), list, rs.getInt("ReqMoney"), 
							rs.getInt("ReqLevel"), rs.getInt("RewardExp"), rs.getInt("RewardMoney"), rs.getBoolean("Recompletable"), items, keepitems));
					
					RPG_Core.Log(-1, rs.getInt("ID"), RPG_LogType.Information, "Quest", "Loaded");
				}
				else if (rs.getInt("QuestType") == 2)
				{
					type = RPG_QuestType.Kill;
					ArrayList<RPG_QuestRequest> mobs = new ArrayList<RPG_QuestRequest>();
					String[] subsplits = rs.getString("Tag").split(RPG_Core.GetDBSeperator());
					
					try
					{
						for (int i = 0; i < subsplits.length; i++)
						{
							String[] elements = subsplits[i].split(RPG_Core.GetDBSeperator2());
							mobs.add(new RPG_QuestRequest(Integer.parseInt(elements[0]), Integer.parseInt(elements[1])));
						}
					}
					catch (Exception ex)
					{
						logger.warning(prefix + "Could not load quest " + rs.getInt("ID") + ": '" + rs.getString("Tag") + "' is not a valid tag");
						RPG_Core.Log(-1, rs.getInt("ID"), RPG_LogType.Warning, "Quest", rs.getString("Tag") + " is not a valid tag");
						return;
					}
					
					quests.put(rs.getInt("ID"), new RPG_Quest_Kill(rs.getInt("ID"), rs.getInt("NameID"), rs.getInt("DisplayNameID"), rs.getInt("DescriptionID"), type, 
							rs.getInt("NPCStartID"), rs.getInt("NPCEndID"), rs.getInt("NPCStartTextID"), rs.getInt("NPCEndTextID"), list, rs.getInt("ReqMoney"), 
							rs.getInt("ReqLevel"), rs.getInt("RewardExp"), rs.getInt("RewardMoney"), rs.getBoolean("Recompletable"), mobs));
					
					RPG_Core.Log(-1, rs.getInt("ID"), RPG_LogType.Information, "Quest", "Loaded");
				}
				else if (rs.getInt("QuestType") == 3)
				{
					type = RPG_QuestType.Talk;
					
					quests.put(rs.getInt("ID"), new RPG_Quest_Talk(rs.getInt("ID"), rs.getInt("NameID"), rs.getInt("DisplayNameID"), rs.getInt("DescriptionID"), type, 
							rs.getInt("NPCStartID"), rs.getInt("NPCEndID"), rs.getInt("NPCStartTextID"), rs.getInt("NPCEndTextID"), list, rs.getInt("ReqMoney"), 
							rs.getInt("ReqLevel"), rs.getInt("RewardExp"), rs.getInt("RewardMoney"), rs.getBoolean("Recompletable")));
					
					RPG_Core.Log(-1, rs.getInt("ID"), RPG_LogType.Information, "Quest", "Loaded");
				}
			}
			else
				RPG_Core.Log(-1, ID, RPG_LogType.Warning, "Quest", "Could not be reloaded");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			
			RPG_Core.Log(RPG_LogType.Error, "Quest", ex);
		}
	}
	
	// 
	// QuestProgresses
	// 
	public RPG_QuestProgress GetQuestProgressForQuest(int PlayerID, int QuestID)
	{
		if (questprogresses.containsKey(PlayerID))
		{
			if (questprogresses.get(PlayerID).containsKey(QuestID))
				return questprogresses.get(PlayerID).get(QuestID);
			else
			{
				logger.warning(prefix + "QuestProgress for Player with id " + PlayerID + " does not have quest " + QuestID);
				RPG_Core.Log(PlayerID, QuestID, RPG_LogType.Warning, "QuestProgress", "Quest does not exist");
				return null;
			}
		}
		else
		{
			logger.warning(prefix + "QuestProgress for Player with id " + PlayerID + " does not exist");
			RPG_Core.Log(PlayerID, QuestID, RPG_LogType.Warning, "QuestProgress", "Player does not exist");
			return null;
		}
	}
	public HashMap<Integer, RPG_QuestProgress> GetQuestProgresses(int PlayerID)
	{
		if (questprogresses.containsKey(PlayerID))
			return questprogresses.get(PlayerID);
		else
		{
			logger.warning(prefix + "QuestProgress for Player with id " + PlayerID + " does not exist");
			RPG_Core.Log(PlayerID, -1, RPG_LogType.Warning, "QuestProgress", "Does not exist");
			return null;
		}
	}
	
	public void LoadQuestProgressesForPlayer(int PlayerID)
	{
		Statement stmt;
		ResultSet rs;
		
		try
		{
			if (!questprogresses.containsKey(PlayerID))
				questprogresses.put(PlayerID, new HashMap<Integer, RPG_QuestProgress>());
			else
				questprogresses.get(PlayerID).clear();
			
			stmt = RPG_Core.GetDatabaseStatement();
			rs = stmt.executeQuery("SELECT ID, QuestID, PlayerID, Completed, Data FROM " + SQLTablePrefix + "questprogress WHERE PlayerID = '" + PlayerID + "';");
			
			while (rs.next())
			{
				HashMap<Integer, Integer> progresses = new HashMap<Integer, Integer>(); 
				
				if (rs.getString("Data") != null)
				{					
					try
					{
						String[] splits = rs.getString("Data").split(RPG_Core.GetDBSeperator());
						for (String value : splits)
						{
							String[] subsplits = value.split(RPG_Core.GetDBSeperator2());
							if (subsplits.length == 2)
								progresses.put(Integer.parseInt(subsplits[0]), Integer.parseInt(subsplits[1]));
						}
					}
					catch (Exception ex)
					{
						logger.warning(prefix + "Could not load quest progress " + rs.getInt("ID") + ": '" + rs.getString("Data") + "' is not a valid data tag");
						continue;
					}
				}
				
				questprogresses.get(PlayerID).put(rs.getInt("QuestID"), new RPG_QuestProgress(rs.getInt("ID"), rs.getInt("QuestID"), rs.getInt("PlayerID"), rs.getBoolean("Completed"), progresses));
				RPG_Core.Log(PlayerID, rs.getInt("ID"), RPG_LogType.Information, "QuestProgress", "Loaded");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			RPG_Core.Log(RPG_LogType.Error, "QuestProgress", ex);
		}
	}
	public void UnloadQuestProgressForPlayer(int PlayerID)
	{
		if (questprogresses.containsKey(PlayerID))
		{
			questprogresses.remove(PlayerID);
			RPG_Core.Log(PlayerID, -1, RPG_LogType.Information, "QuestProgress", "Unloaded");
		}
		else
		{
			RPG_Core.Log(PlayerID, -1, RPG_LogType.Warning, "QuestProgress", "Does not exist");
		}
	}
}

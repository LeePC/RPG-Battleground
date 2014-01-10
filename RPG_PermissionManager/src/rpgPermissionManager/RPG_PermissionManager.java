package rpgPermissionManager;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;


import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import rpgCore.RPG_Core;
import rpgOther.RPG_LogType;
import rpgOther.RPG_RestrictedItem;
import rpgPlayer.RPG_Player;
import rpgPlayer.RPG_PlayerAction;
import rpgQuest.RPG_Quest;
import rpgQuest.RPG_QuestProgress;

public class RPG_PermissionManager extends JavaPlugin implements Listener
{
	private static String prefix = "[RPG Permission Manager] ";
	private static Logger logger = Logger.getLogger("Minecraft");
	
	private HashMap<Integer, HashMap<RPG_PlayerAction, RPG_RestrictedItem>> items = new HashMap<Integer, HashMap<RPG_PlayerAction, RPG_RestrictedItem>>();
	
	
	public RPG_PermissionManager()
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
	    
		logger.info(prefix + "Loading restricted items...");
		
		UpdateRestrictedItems();
		
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
		
		if (commandLabel.equalsIgnoreCase("pm"))
		{			
			if (!RPG_Core.HasPermission(sender, "rpg.pm"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (args.length <= 0)
			{
				sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				sender.sendMessage(ChatColor.BLUE + "Permission Manager");
				sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				if (RPG_Core.HasPermission(sender, "rpg.pm.ri"))
					sender.sendMessage(ChatColor.GOLD + " ri      " + ChatColor.WHITE + "Restricted Items");
				sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
			}
			else
			{
				if (args[0].equalsIgnoreCase("ri"))
				{
					if (!RPG_Core.HasPermission(sender, "rpg.pm.ri"))
					{
						sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
						RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
						return false;
					}
					
					if (args.length <= 1)
					{
						sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
						sender.sendMessage(ChatColor.BLUE + "RPG Commands - Permission Manager - Restricted Items");
						sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
						if (RPG_Core.HasPermission(sender, "rpg.pm.ri.add"))
							sender.sendMessage(ChatColor.GOLD + " add     " + ChatColor.WHITE + "Add a new restricted item");
						if (RPG_Core.HasPermission(sender, "rpg.pm.ri.del"))
							sender.sendMessage(ChatColor.GOLD + " del      " + ChatColor.WHITE + "Delete an existing restricted item");
						if (RPG_Core.HasPermission(sender, "rpg.pm.ri.list"))
							sender.sendMessage(ChatColor.GOLD + " list     " + ChatColor.WHITE + "Lists all the restricted items");
						if (RPG_Core.HasPermission(sender, "rpg.pm.ri.reload"))
							sender.sendMessage(ChatColor.GOLD + " reload  " + ChatColor.WHITE + "Reload the restricted items");
						sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					}
					else
					{
						if (args[1].equalsIgnoreCase("add"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.pm.ri.add"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (args.length <= 2)
							{
								sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
								sender.sendMessage(ChatColor.BLUE + "RPG Commands - Add a restricted item");
								sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
								sender.sendMessage(ChatColor.BLUE + " Description:");
								sender.sendMessage(ChatColor.WHITE + "  Adds an item to the list of restricted items. These items can");
								sender.sendMessage(ChatColor.WHITE + "  only be used by players under certain conditions");
								sender.sendMessage(ChatColor.BLUE + " Syntax:");
								sender.sendMessage(ChatColor.GOLD + "  /pm ri add [ItemID] [ReqLevel] [ActionIDs] [QuestIDs]");
								sender.sendMessage(ChatColor.BLUE + " Arguments:");
								sender.sendMessage(ChatColor.GOLD + "  [ItemID]   " + ChatColor.WHITE + "The ID of the restricted item");
								sender.sendMessage(ChatColor.GOLD + "  [ReqLevel] " + ChatColor.WHITE + "The required level to use this item");
								sender.sendMessage(ChatColor.GOLD + "  [Actions]  " + ChatColor.WHITE + "The actions that this rule applies to, seperated by ';'");
								sender.sendMessage(ChatColor.GOLD + "             " + ChatColor.WHITE + "   0   Place");
								sender.sendMessage(ChatColor.GOLD + "             " + ChatColor.WHITE + "   1   Damage");
								sender.sendMessage(ChatColor.GOLD + "             " + ChatColor.WHITE + "   2   Destroy");
								sender.sendMessage(ChatColor.GOLD + "             " + ChatColor.WHITE + "   3   Interact");
								sender.sendMessage(ChatColor.GOLD + "  [QuestIDs] " + ChatColor.WHITE + "The list of all the QuestIDs that need to be completed to use this item, seperated by ';'");
								sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
							}
							else
							{
								int itemid = -1;
								int reqlvl = -1; 
								String aids = args[4];
								String qids = args[5];
								
								try
								{
									itemid = Integer.parseInt(args[2]);
								}
								catch (Exception ex)
								{ }
								
								if (itemid < 0)
								{
									sender.sendMessage(ChatColor.RED + "Invalid item ID!");
									return false;
								}
								
								try
								{
									reqlvl = Integer.parseInt(args[3]);
								}
								catch (Exception ex)
								{ }
								
								if (reqlvl < 0)
								{
									sender.sendMessage(ChatColor.RED + "Invalid required level!");
									return false;
								}
								
								try
								{
									/*Statement stmt = RPG_Core.GetDatabaseStatement();
									stmt.executeUpdate("INSERT INTO " + RPG_Core.SQLTablePrefix + "restricteditems (ItemID, ReqLevel, ActionIDs, QuestIDs) VALUES (" + 
											itemid + ", " + reqlvl + ", '" + aids + "', '" + qids + "');");
									
									sender.sendMessage(ChatColor.GREEN + "Item added to the restricted item list!");*/
									sender.sendMessage(ChatColor.RED + "DEBUG: NOT IMPLEMENTED YET!");
								}
								catch (Exception ex)
								{
									sender.sendMessage(ChatColor.RED + "Could not add item!");
									ex.printStackTrace();
								}
							}
						}
						else if (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("remove") ||
								args[1].equalsIgnoreCase("del") || args[1].equalsIgnoreCase("rem"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.pm.ri.del"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							if (args.length <= 2)
							{
								sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
								sender.sendMessage(ChatColor.BLUE + "RPG Commands - Delete a restricted item");
								sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
								sender.sendMessage(ChatColor.BLUE + " Description:");
								sender.sendMessage(ChatColor.WHITE + "  Removes a restricted item for the list, so that it can be ");
								sender.sendMessage(ChatColor.WHITE + "  used again without preconditions.");
								sender.sendMessage(ChatColor.BLUE + " Syntax:");
								sender.sendMessage(ChatColor.GOLD + "  /pm ri delete [ItemID] [Action]");
								sender.sendMessage(ChatColor.GOLD + "  /pm ri remove [ItemID] [Action]");
								sender.sendMessage(ChatColor.GOLD + "  /pm ri del [ItemID] [Action]");
								sender.sendMessage(ChatColor.GOLD + "  /pm ri rem [ItemID] [Action]");
								sender.sendMessage(ChatColor.BLUE + " Arguments:");
								sender.sendMessage(ChatColor.GOLD + "  [ItemID]   " + ChatColor.WHITE + "The ID of the restricted item.");
								sender.sendMessage(ChatColor.GOLD + "  [Action]   " + ChatColor.WHITE + "The action that this rule applies to.");
								sender.sendMessage(ChatColor.GOLD + "             " + ChatColor.WHITE + "   0   Place");
								sender.sendMessage(ChatColor.GOLD + "             " + ChatColor.WHITE + "   1   Damage");
								sender.sendMessage(ChatColor.GOLD + "             " + ChatColor.WHITE + "   2   Destroy");
								sender.sendMessage(ChatColor.GOLD + "             " + ChatColor.WHITE + "   3   Interact");
								sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
							}
							else
							{
								int itemid = -1;
								int actionid = -1;
								
								try
								{
									itemid = Integer.parseInt(args[2]);
								}
								catch (Exception ex)
								{ }
								
								if (itemid < 0)
								{
									sender.sendMessage(ChatColor.RED + "Invalid item ID!");
									return false;
								}
								
								try
								{
									actionid = Integer.parseInt(args[3]);
								}
								catch (Exception ex)
								{ }
								
								if (actionid < 0 || actionid > 3)
								{
									sender.sendMessage(ChatColor.RED + "Invalid action!");
									return false;
								}
								
								RPG_PlayerAction action = RPG_Core.GetPlayerAction(actionid);
								
								if (!items.containsKey(itemid) || !items.get(itemid).containsKey(action))
								{
									sender.sendMessage(ChatColor.RED + "There is no restricted item with the specified item ID & action combination!");
									return false;
								}
								
								try
								{
									/*Statement stmt = RPG_Core.GetDatabaseStatement();
									stmt.executeUpdate("DELETE FROM " + RPG_Core.SQLTablePrefix + "restricteditems WHERE (ItemID = " + itemid + " AND Action = " + actionid + ");");
									
									sender.sendMessage(ChatColor.GREEN + "Item removed from the restricted item list!");*/
									sender.sendMessage(ChatColor.RED + "DEBUG: NOT IMPLEMENTED YET!");
								}
								catch (Exception ex)
								{
									sender.sendMessage(ChatColor.RED + "Could not remove item!");
									ex.printStackTrace();
								}
							}
						}
						else if (args[1].equalsIgnoreCase("list"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.pm.ri.list"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							sender.sendMessage(ChatColor.BLUE + "ID  Action  ReqLvl  QuestIDs");
							
							for (HashMap<RPG_PlayerAction, RPG_RestrictedItem> map : items.values())
							{
								for (RPG_RestrictedItem item : map.values())
									sender.sendMessage(ChatColor.GOLD + "" + item.GetItemID() + "  " + ChatColor.WHITE + item.GetAction() + "  " + item.GetRequiredLevel() + "  " + item.GetQuestIDs());
							}
						}
						else if (args[1].equalsIgnoreCase("reload"))
						{
							if (!RPG_Core.HasPermission(sender, "rpg.pm.ri.reload"))
							{
								sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
								RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
								return false;
							}
							
							RPG_Core.LoadRestrictedItemsFromDB();
							UpdateRestrictedItems();
							
							sender.sendMessage(ChatColor.GREEN + "Reloaded restricted items");
						}
						else
							sender.sendMessage(ChatColor.RED + "Unknown command");
					}
				}
				else
					sender.sendMessage(ChatColor.RED + "Unknown command");
			}
		}
		else
			sender.sendMessage(ChatColor.RED + "Unknown command");
		
		return true;
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockPlace(BlockPlaceEvent e)
	{
		int tid = e.getBlockPlaced().getTypeId();
		
		if (items.containsKey(tid))
		{
			if (items.get(tid).containsKey(RPG_PlayerAction.Place))
			{
				RPG_RestrictedItem rpg_item = items.get(tid).get(RPG_PlayerAction.Place);
				RPG_Player rpg_p = RPG_Core.GetPlayer(e.getPlayer().getName());
				
				if (rpg_p.GetLevel() < rpg_item.GetRequiredLevel())
				{
					rpg_p.SendMessage(RPG_Core.GetFormattedMessage("ri_minlvl", rpg_p.GetLanguage(), rpg_item));
					e.setCancelled(true);
					return;
				}
				
				boolean completed = true;
				for (Integer qid : rpg_item.GetQuestIDs())
				{
					if (!RPG_Core.GetQuestManager().GetQuestProgressForQuest(rpg_p.GetID(), qid).GetCompleted())
					{
						completed = false;
						break;
					}
				}
				
				if (!completed)
				{
					e.setCancelled(true);
					rpg_p.SendMessage(RPG_Core.GetFormattedMessage("ri_minqs", rpg_p.GetLanguage(), rpg_item));
					
					for (Integer qid : rpg_item.GetQuestIDs())
					{
						RPG_Quest rpg_q = RPG_Core.GetQuestManager().GetQuest(qid);
						
						if (rpg_q != null)
						{
							RPG_QuestProgress rpg_qp = RPG_Core.GetQuestManager().GetQuestProgressForQuest(rpg_p.GetID(), rpg_q.GetID());
							if (rpg_qp == null)
								rpg_p.SendMessage(ChatColor.RED + " - " + rpg_q.GetDisplayName(rpg_p.GetLanguage()));
							else if (rpg_qp.GetCompleted())
								rpg_p.SendMessage(ChatColor.GREEN + " - " + rpg_q.GetDisplayName(rpg_p.GetLanguage()));
							else
								rpg_p.SendMessage(ChatColor.YELLOW + " - " + rpg_q.GetDisplayName(rpg_p.GetLanguage()));

						}
						else
							logger.warning(prefix + "The quest with the ID " + qid + " is precondition to place item ID " + rpg_item.GetID() + " but was not found!");
					}
				}
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent e)
	{
		int tid = e.getBlock().getTypeId();
		
		if (items.containsKey(e.getBlock().getTypeId()))
		{
			if (items.get(tid).containsKey(RPG_PlayerAction.Destroy))
			{
				RPG_RestrictedItem rpg_item = items.get(tid).get(RPG_PlayerAction.Destroy);
				RPG_Player rpg_p = RPG_Core.GetPlayer(e.getPlayer().getName());
				
				if (rpg_p.GetLevel() < rpg_item.GetRequiredLevel())
				{
					rpg_p.SendMessage(RPG_Core.GetFormattedMessage("ri_minlvl", rpg_p.GetLanguage(), rpg_item));
					e.setCancelled(true);
					return;
				}
				
				boolean completed = true;
				for (Integer qid : rpg_item.GetQuestIDs())
				{
					if (!RPG_Core.GetQuestManager().GetQuestProgressForQuest(rpg_p.GetID(), qid).GetCompleted())
					{
						completed = false;
						break;
					}
				}
				
				if (!completed)
				{
					e.setCancelled(true);
					rpg_p.SendMessage(RPG_Core.GetFormattedMessage("ri_minqs", rpg_p.GetLanguage(), rpg_item));
					
					for (Integer qid : rpg_item.GetQuestIDs())
					{
						RPG_Quest rpg_q = RPG_Core.GetQuestManager().GetQuest(qid);
						
						if (rpg_q != null)
						{
							RPG_QuestProgress rpg_qp = RPG_Core.GetQuestManager().GetQuestProgressForQuest(rpg_p.GetID(), rpg_q.GetID());
							if (rpg_qp == null)
								rpg_p.SendMessage(ChatColor.RED + " - " + rpg_q.GetDisplayName(rpg_p.GetLanguage()));
							else if (rpg_qp.GetCompleted())
								rpg_p.SendMessage(ChatColor.GREEN + " - " + rpg_q.GetDisplayName(rpg_p.GetLanguage()));
							else
								rpg_p.SendMessage(ChatColor.YELLOW + " - " + rpg_q.GetDisplayName(rpg_p.GetLanguage()));

						}
						else
							logger.warning(prefix + "The quest with the ID " + qid + " is precondition to place item ID " + rpg_item.GetID() + " but was not found!");
					}
				}
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockDamage(BlockDamageEvent e)
	{
		int tid = e.getBlock().getTypeId();
		
		if (items.containsKey(e.getBlock().getTypeId()))
		{
			if (items.get(tid).containsKey(RPG_PlayerAction.Damage))
			{
				RPG_RestrictedItem rpg_item = items.get(tid).get(RPG_PlayerAction.Damage);
				RPG_Player rpg_p = RPG_Core.GetPlayer(e.getPlayer().getName());
				
				if (rpg_p.GetLevel() < rpg_item.GetRequiredLevel())
				{
					rpg_p.SendMessage(RPG_Core.GetFormattedMessage("ri_minlvl", rpg_p.GetLanguage(), rpg_item));
					e.setCancelled(true);
					return;
				}
				
				boolean completed = true;
				for (Integer qid : rpg_item.GetQuestIDs())
				{
					if (!RPG_Core.GetQuestManager().GetQuestProgressForQuest(rpg_p.GetID(), qid).GetCompleted())
					{
						completed = false;
						break;
					}
				}
				
				if (!completed)
				{
					e.setCancelled(true);
					rpg_p.SendMessage(RPG_Core.GetFormattedMessage("ri_minqs", rpg_p.GetLanguage(), rpg_item));
					
					for (Integer qid : rpg_item.GetQuestIDs())
					{
						RPG_Quest rpg_q = RPG_Core.GetQuestManager().GetQuest(qid);
						
						if (rpg_q != null)
						{
							RPG_QuestProgress rpg_qp = RPG_Core.GetQuestManager().GetQuestProgressForQuest(rpg_p.GetID(), rpg_q.GetID());
							if (rpg_qp == null)
								rpg_p.SendMessage(ChatColor.RED + " - " + rpg_q.GetDisplayName(rpg_p.GetLanguage()));
							else if (rpg_qp.GetCompleted())
								rpg_p.SendMessage(ChatColor.GREEN + " - " + rpg_q.GetDisplayName(rpg_p.GetLanguage()));
							else
								rpg_p.SendMessage(ChatColor.YELLOW + " - " + rpg_q.GetDisplayName(rpg_p.GetLanguage()));

						}
						else
							logger.warning(prefix + "The quest with the ID " + qid + " is precondition to place item ID " + rpg_item.GetID() + " but was not found!");
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteractEvent(PlayerInteractEvent e)
	{
		if (e.getClickedBlock() != null)
		{
			int tid = e.getClickedBlock().getTypeId();
			
			if (items.containsKey(e.getClickedBlock().getTypeId()))
			{
				if (items.get(tid).containsKey(RPG_PlayerAction.Interact))
				{
					RPG_RestrictedItem rpg_item = items.get(tid).get(RPG_PlayerAction.Interact);
					RPG_Player rpg_p = RPG_Core.GetPlayer(e.getPlayer().getName());
					
					if (rpg_p.GetLevel() < rpg_item.GetRequiredLevel())
					{
						rpg_p.SendMessage(RPG_Core.GetFormattedMessage("ri_minlvl", rpg_p.GetLanguage(), rpg_item));
						e.setCancelled(true);
						return;
					}
					
					boolean completed = true;
					for (Integer qid : rpg_item.GetQuestIDs())
					{
						if (!RPG_Core.GetQuestManager().GetQuestProgressForQuest(rpg_p.GetID(), qid).GetCompleted())
						{
							completed = false;
							break;
						}
					}
					
					if (!completed)
					{
						e.setCancelled(true);
						rpg_p.SendMessage(RPG_Core.GetFormattedMessage("ri_minqs", rpg_p.GetLanguage(), rpg_item));
						
						for (Integer qid : rpg_item.GetQuestIDs())
						{
							RPG_Quest rpg_q = RPG_Core.GetQuestManager().GetQuest(qid);
							
							if (rpg_q != null)
							{
								RPG_QuestProgress rpg_qp = RPG_Core.GetQuestManager().GetQuestProgressForQuest(rpg_p.GetID(), rpg_q.GetID());
								if (rpg_qp == null)
									rpg_p.SendMessage(ChatColor.RED + " - " + rpg_q.GetDisplayName(rpg_p.GetLanguage()));
								else if (rpg_qp.GetCompleted())
									rpg_p.SendMessage(ChatColor.GREEN + " - " + rpg_q.GetDisplayName(rpg_p.GetLanguage()));
								else
									rpg_p.SendMessage(ChatColor.YELLOW + " - " + rpg_q.GetDisplayName(rpg_p.GetLanguage()));

							}
							else
								logger.warning(prefix + "The quest with the ID " + qid + " is precondition to place item ID " + rpg_item.GetID() + " but was not found!");
						}
					}
				}
			}
		}
	}
	
	public void UpdateRestrictedItems()
	{		
		items.clear();
		
		ArrayList<RPG_RestrictedItem> items_raw = RPG_Core.GetRestrictedItems();
		for (RPG_RestrictedItem item : items_raw)
		{
			if (items.containsKey(item.GetItemID()))
			{
				items.get(item.GetItemID()).put(item.GetAction(), item);
			}
			else
			{
				items.put(item.GetItemID(), new HashMap<RPG_PlayerAction, RPG_RestrictedItem>());
				items.get(item.GetItemID()).put(item.GetAction(), item);
			}
		}
	}
}

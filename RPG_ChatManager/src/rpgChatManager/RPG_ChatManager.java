package rpgChatManager;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import rpgCore.RPG_Core;
import rpgGuild.RPG_Guild;
import rpgNation.RPG_Nation;
import rpgOther.RPG_ChatType;
import rpgOther.RPG_LogType;
import rpgPlayer.RPG_EditMode;
import rpgPlayer.RPG_Player;
import rpgWorld.RPG_World;

public class RPG_ChatManager extends JavaPlugin implements Listener
{
	private String prefix = "[RPG Chat Manager] ";
	
	private Logger logger = Logger.getLogger("Minecraft");
	
	private double talkdist = 30.0d;
	private ChatColor color_server = ChatColor.LIGHT_PURPLE;
	private ChatColor color_world = ChatColor.YELLOW;
	private ChatColor color_nation = ChatColor.AQUA;
	private ChatColor color_guild = ChatColor.GREEN;
	private ChatColor color_region = ChatColor.GRAY;
	private ChatColor color_private = ChatColor.ITALIC;
	
	
	// 
	// Main
	// 
	public RPG_ChatManager()
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
		
		logger.info(prefix + "Loading config...");
		
		getConfig().addDefault("regional_talkdistance", 30.0d);
		getConfig().addDefault("color_server", ChatColor.LIGHT_PURPLE.getChar());
		getConfig().addDefault("color_world", ChatColor.YELLOW.getChar());
		getConfig().addDefault("color_nation", ChatColor.AQUA.getChar());
		getConfig().addDefault("color_guild", ChatColor.GREEN.getChar());
		getConfig().addDefault("color_region", ChatColor.GRAY.getChar());
		getConfig().addDefault("color_private", ChatColor.ITALIC.getChar());
		getConfig().options().copyDefaults(true);
		saveConfig();
	    
		talkdist = getConfig().getDouble("regional_talkdistance");
		color_server = ChatColor.getByChar(getConfig().getString("color_server"));
		color_world = ChatColor.getByChar(getConfig().getString("color_world"));
		color_nation = ChatColor.getByChar(getConfig().getString("color_nation"));
		color_guild = ChatColor.getByChar(getConfig().getString("color_guild"));
		color_region = ChatColor.getByChar(getConfig().getString("color_region"));
		color_private = ChatColor.getByChar(getConfig().getString("color_private"));
		
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
		
		if (commandLabel.equalsIgnoreCase("s"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.chat.server"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (args.length > 0)
			{
				String text = args[0];
				for (int i = 1; i < args.length; i++)
					text += " " + args[i];
				
				RPG_Core.LogChatMessage(sender, RPG_ChatType.Server, text);
				logger.info(color_server + "[Server] " + ChatColor.BLUE + sender.getName() + ": " + ChatColor.WHITE + text);
				
				for (RPG_Player rpg_pl : RPG_Core.GetOnlinePlayers())
					rpg_pl.SendMessage(color_server + "[Server] " + ChatColor.BLUE + sender.getName() + ": " + ChatColor.WHITE + text);
			}
			else if (RPG_Core.IsPlayer(sender))
				sender.sendMessage(ChatColor.GREEN + "You are now talking in the " + ChatColor.BLUE + "server " + ChatColor.GREEN + "chat");
			
			if (RPG_Core.IsPlayer(sender))
				rpg_p.SetLastChat(RPG_ChatType.Server);
		}
		else if (commandLabel.equalsIgnoreCase("worldon") || commandLabel.equalsIgnoreCase("won"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.chat.world.on"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (!RPG_Core.IsPlayer(sender))
			{
				sender.sendMessage(ChatColor.RED + "Only players can use the chat settings");
				return false;
			}
			
			rpg_p.SetChatWorld(true);
			sender.sendMessage(ChatColor.GOLD + "You have turned " + ChatColor.BLUE + "WORLD " + ChatColor.GOLD + "chat messages " + ChatColor.BLUE + "ON" + ChatColor.GOLD + "!");
		}
		else if (commandLabel.equalsIgnoreCase("worldoff") || commandLabel.equalsIgnoreCase("woff"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.chat.world.off"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (!RPG_Core.IsPlayer(sender))
			{
				sender.sendMessage(ChatColor.RED + "Only players can use the chat settings");
				return false;
			}
			
			rpg_p.SetChatWorld(false);
			sender.sendMessage(ChatColor.GOLD + "You have turned " + ChatColor.BLUE + "WORLD " + ChatColor.GOLD + "chat messages " + ChatColor.BLUE + "OFF" + ChatColor.GOLD + "!");
		}
		else if (commandLabel.equalsIgnoreCase("w"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.chat.world"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (RPG_Core.IsPlayer(sender))
			{				
				if (args.length > 0)
				{
					String text = args[0];
					for (int i = 1; i < args.length; i++)
						text += " " + args[i];
					
					RPG_Core.LogChatMessage(sender, RPG_ChatType.World, text);
					logger.info(color_world + "[World] " + ChatColor.BLUE + sender.getName() + ": " + ChatColor.WHITE + text);
					
					for (RPG_Player rpg_pl : RPG_Core.GetOnlinePlayers())
					{
						if (!rpg_pl.GetChatWorld())
							continue;
						
						if (!rpg_pl.GetPlayer().getWorld().getUID().equals(rpg_p.GetPlayer().getWorld().getUID()))
							continue;
						
						rpg_pl.SendMessage(color_world + "[World] " + ChatColor.BLUE + sender.getName() + ": " + ChatColor.WHITE + text);
					}
				}
				else
					sender.sendMessage(ChatColor.GREEN + "You are now talking in the " + ChatColor.BLUE + "world " + ChatColor.GREEN + "chat");
				
				rpg_p.SetLastChat(RPG_ChatType.World);
			}
			else
			{
				if (args.length < 2)
				{
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					sender.sendMessage(ChatColor.BLUE + "RPG Commands - Send chat to World");
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					sender.sendMessage(ChatColor.BLUE + " Description:");
					sender.sendMessage(ChatColor.WHITE + "  Sends a chat message to every player in the specified world");
					sender.sendMessage(ChatColor.BLUE + " Syntax:");
					sender.sendMessage(ChatColor.GOLD + "  /w [World] [Text]");
					sender.sendMessage(ChatColor.BLUE + " Arguments:");
					sender.sendMessage(ChatColor.GOLD + "  [World]    " + ChatColor.WHITE + "The name of the world");
					sender.sendMessage(ChatColor.GOLD + "  [Text]     " + ChatColor.WHITE + "The text that is sent to the players");
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				}
				else
				{
					String world = args[0];
					
					String text = args[1];
					for (int i = 2; i < args.length; i++)
						text += " " + args[i];
					
					RPG_World rpg_w = RPG_Core.GetWorldManager().GetWorld(world);
					if (rpg_w == null)
					{
						sender.sendMessage(ChatColor.RED + "The world '" + world + "' does not exist");
						return false;
					}
					
					RPG_Core.LogChatMessage(sender, RPG_ChatType.World, text);
					logger.info(color_world + "[World] " + ChatColor.BLUE + sender.getName() + ": " + ChatColor.WHITE + text);
					
					for (RPG_Player rpg_pl : RPG_Core.GetOnlinePlayers())
					{
						if (!rpg_pl.GetChatWorld())
							continue;
						
						if (rpg_pl.GetPlayer().getWorld().getName() != rpg_w.GetName())
							continue;
						
						rpg_pl.SendMessage(ChatColor.YELLOW + "[World] " + ChatColor.BLUE + sender.getName() + ": " + ChatColor.WHITE + text);
					}
				}
			}
		}
		else if (commandLabel.equalsIgnoreCase("nationon") || commandLabel.equalsIgnoreCase("non"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.chat.nation.on"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (!RPG_Core.IsPlayer(sender))
			{
				sender.sendMessage(ChatColor.RED + "Only players can use the chat settings");
				return false;
			}
			
			rpg_p.SetChatNation(true);
			sender.sendMessage(ChatColor.GOLD + "You have turned " + ChatColor.BLUE + "NATION " + ChatColor.GOLD + "chat messages " + ChatColor.BLUE + "ON" + ChatColor.GOLD + "!");
		}
		else if (commandLabel.equalsIgnoreCase("nationoff") || commandLabel.equalsIgnoreCase("noff"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.chat.nation.off"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (!RPG_Core.IsPlayer(sender))
			{
				sender.sendMessage(ChatColor.RED + "Only players can use the chat settings");
				return false;
			}
			
			rpg_p.SetChatNation(false);
			sender.sendMessage(ChatColor.GOLD + "You have turned " + ChatColor.BLUE + "NATION " + ChatColor.GOLD + "chat messages " + ChatColor.BLUE + "OFF" + ChatColor.GOLD + "!");
		}
		else if (commandLabel.equalsIgnoreCase("n"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.chat.nation"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (RPG_Core.IsPlayer(sender))
			{
				if (args.length > 0)
				{
					String text = args[0];
					for (int i = 1; i < args.length; i++)
						text += " " + args[i];
					
					RPG_Core.LogChatMessage(sender, RPG_ChatType.Nation, text);
					logger.info(color_nation + "[Nation] " + ChatColor.BLUE + sender.getName() + ": " + ChatColor.WHITE + text);
					
					for (RPG_Player rpg_pl : RPG_Core.GetOnlinePlayers())
					{
						if (!rpg_pl.GetChatNation() || rpg_pl.GetNationId() != rpg_p.GetNationId())
							continue;
						
						rpg_pl.SendMessage(color_nation + "[Nation] " + ChatColor.BLUE + sender.getName() + ": " + ChatColor.WHITE + text);
					}
				}
				else
					sender.sendMessage(ChatColor.GREEN + "You are now talking in the " + ChatColor.BLUE + "nation " + ChatColor.GREEN + "chat");
				
				rpg_p.SetLastChat(RPG_ChatType.Nation);
			}
			else
			{
				if (args.length < 2)
				{
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					sender.sendMessage(ChatColor.BLUE + "RPG Commands - Send chat to Nation");
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					sender.sendMessage(ChatColor.BLUE + " Description:");
					sender.sendMessage(ChatColor.WHITE + "  Sends a chat message to every player in the specified Nation");
					sender.sendMessage(ChatColor.BLUE + " Syntax:");
					sender.sendMessage(ChatColor.GOLD + "  /n [NationID] [Text]");
					sender.sendMessage(ChatColor.BLUE + " Arguments:");
					sender.sendMessage(ChatColor.GOLD + "  [NationID] " + ChatColor.WHITE + "The ID of the Nation");
					sender.sendMessage(ChatColor.GOLD + "  [Text]     " + ChatColor.WHITE + "The text that is sent to the players");
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				}
				else
				{
					int nation = -1;
					try
					{
						nation = Integer.parseInt(args[0]);
					}
					catch (Exception ex)
					{
						sender.sendMessage(ChatColor.RED + "The nation '" + args[0] + "' does not exist");
						return false;
					}
					
					String text = args[1];
					for (int i = 2; i < args.length; i++)
						text += " " + args[i];
					
					RPG_Nation rpg_n = RPG_Core.GetNation(nation);
					if (rpg_n == null)
					{
						sender.sendMessage(ChatColor.RED + "The nation '" + args[0] + "' does not exist");
						return false;
					}
					
					RPG_Core.LogChatMessage(sender, RPG_ChatType.Nation, text);
					logger.info(color_nation + "[Nation] " + ChatColor.BLUE + sender.getName() + ": " + ChatColor.WHITE + text);
					
					for (RPG_Player rpg_pl : RPG_Core.GetOnlinePlayers())
					{
						if (!rpg_pl.GetChatWorld() || rpg_pl.GetNationId() != nation)
							continue;
						
						rpg_pl.SendMessage(color_nation + "[Nation] " + ChatColor.BLUE + sender.getName() + ": " + ChatColor.WHITE + text);
					}
				}
			}
		}
		else if (commandLabel.equalsIgnoreCase("guildon") || commandLabel.equalsIgnoreCase("gon"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.chat.guild.on"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (!RPG_Core.IsPlayer(sender))
			{
				sender.sendMessage(ChatColor.RED + "Only players can use the chat settings");
				return false;
			}
			
			rpg_p.SetChatGuild(true);
			sender.sendMessage(ChatColor.GOLD + "You have turned " + ChatColor.BLUE + "GUILD " + ChatColor.GOLD + "chat messages " + ChatColor.BLUE + "ON" + ChatColor.GOLD + "!");
		}
		else if (commandLabel.equalsIgnoreCase("guildoff") || commandLabel.equalsIgnoreCase("goff"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.chat.guild.off"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (!RPG_Core.IsPlayer(sender))
			{
				sender.sendMessage(ChatColor.RED + "Only players can use the chat settings");
				return false;
			}
			
			rpg_p.SetChatGuild(false);
			sender.sendMessage(ChatColor.GOLD + "You have turned " + ChatColor.BLUE + "GUILD " + ChatColor.GOLD + "chat messages " + ChatColor.BLUE + "OFF" + ChatColor.GOLD + "!");
		}
		else if (commandLabel.equalsIgnoreCase("g"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.chat.guild"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (RPG_Core.IsPlayer(sender))
			{				
				if (args.length > 0)
				{
					if (rpg_p.GetGuildId() == -1)
					{
						rpg_p.SendMessage(ChatColor.RED + "You are not in a guild!");
						return false;
					}
					
					String text = args[0];
					for (int i = 1; i < args.length; i++)
						text += " " + args[i];
					
					RPG_Core.LogChatMessage(sender, RPG_ChatType.Guild, text);
					logger.info(color_guild + "[Guild] " + ChatColor.BLUE + sender.getName() + ": " + ChatColor.WHITE + text);
					
					for (RPG_Player rpg_pl : RPG_Core.GetOnlinePlayers())
					{
						if (!rpg_pl.GetChatGuild() || rpg_pl.GetGuildId() != rpg_p.GetGuildId())
							continue;
						
						rpg_pl.SendMessage(color_guild + "[Guild] " + ChatColor.BLUE + rpg_p.GetUsername() + ": " + ChatColor.WHITE + text);
					}
				}
				else
					sender.sendMessage(ChatColor.GREEN + "You are now talking in the " + ChatColor.BLUE + "guild " + ChatColor.GREEN + "chat");
				
				rpg_p.SetLastChat(RPG_ChatType.Guild);
			}
			else
			{
				if (args.length < 2)
				{			
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					sender.sendMessage(ChatColor.BLUE + "RPG Commands - Send chat to Guild");
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
					sender.sendMessage(ChatColor.BLUE + " Description:");
					sender.sendMessage(ChatColor.WHITE + "  Sends a chat message to every player in the specified Guild");
					sender.sendMessage(ChatColor.BLUE + " Syntax:");
					sender.sendMessage(ChatColor.GOLD + "  /g [GuildID] [Text]");
					sender.sendMessage(ChatColor.BLUE + " Arguments:");
					sender.sendMessage(ChatColor.GOLD + "  [GuildID]  " + ChatColor.WHITE + "The ID of the Guild");
					sender.sendMessage(ChatColor.GOLD + "  [Text]     " + ChatColor.WHITE + "The text that is sent to the players");
					sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				}
				else
				{
					int guild = -1;
					try
					{
						guild = Integer.parseInt(args[0]);
					}
					catch (Exception ex)
					{
						sender.sendMessage(ChatColor.RED + "The Guild '" + args[0] + "' does not exist");
						return false;
					}
					
					String text = args[1];
					for (int i = 2; i < args.length; i++)
						text += " " + args[i];
					
					RPG_Guild rpg_g = RPG_Core.GetGuild(guild);
					if (rpg_g == null)
					{
						sender.sendMessage(ChatColor.RED + "The Guild '" + args[0] + "' does not exist");
						return false;
					}
					
					RPG_Core.LogChatMessage(sender, RPG_ChatType.Guild, text);
					logger.info(color_guild + "[Guild] " + ChatColor.BLUE + sender.getName() + ": " + ChatColor.WHITE + text);
					
					for (RPG_Player rpg_pl : RPG_Core.GetOnlinePlayers())
					{
						if (!rpg_pl.GetChatGuild() || rpg_pl.GetGuildId() != guild)
							continue;
						
						rpg_pl.SendMessage(color_guild + "[Guild] " + ChatColor.BLUE + sender.getName() + ": " + ChatColor.WHITE + text);
					}
				}
			}
		}
		else if (commandLabel.equalsIgnoreCase("regionon") || commandLabel.equalsIgnoreCase("ron"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.chat.region.on"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (!RPG_Core.IsPlayer(sender))
			{
				sender.sendMessage(ChatColor.RED + "Only players can use the chat settings");
				return false;
			}
			
			rpg_p.SetChatRegion(true);
			sender.sendMessage(ChatColor.GOLD + "You have turned " + ChatColor.BLUE + "REGION " + ChatColor.GOLD + "chat messages " + ChatColor.BLUE + "ON" + ChatColor.GOLD + "!");
		}
		else if (commandLabel.equalsIgnoreCase("regionoff") || commandLabel.equalsIgnoreCase("roff"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.chat.region.off"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (!RPG_Core.IsPlayer(sender))
			{
				sender.sendMessage(ChatColor.RED + "Only players can use the chat settings");
				return false;
			}
			
			rpg_p.SetChatRegion(false);
			sender.sendMessage(ChatColor.GOLD + "You have turned " + ChatColor.BLUE + "REGION " + ChatColor.GOLD + "chat messages " + ChatColor.BLUE + "OFF" + ChatColor.GOLD + "!");
		}
		else if (commandLabel.equalsIgnoreCase("r"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.chat.region"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (!RPG_Core.IsPlayer(sender))
			{
				sender.sendMessage(ChatColor.RED + "Only players can use the chat settings");
				return false;
			}
			
			if (args.length > 0)
			{
				String text = args[0];
				for (int i = 1; i < args.length; i++)
					text += " " + args[i];
				
				RPG_Core.LogChatMessage(sender, RPG_ChatType.Region, text);
				logger.info(color_region + "[Region] " + ChatColor.BLUE + sender.getName() + ": " + ChatColor.WHITE + text);
				
				for (RPG_Player rpg_pl : RPG_Core.GetOnlinePlayers())
				{
					if (!rpg_pl.GetChatGuild())
						continue;
					
					if (rpg_p.GetPlayer().getLocation().distance(rpg_pl.GetPlayer().getLocation()) > talkdist)
						continue;
					
					rpg_pl.SendMessage(color_region + "[Region] " + ChatColor.BLUE + rpg_p.GetPlayer().getDisplayName() + ": " + ChatColor.WHITE + text);
				}
			}
			else
				sender.sendMessage(ChatColor.GREEN + "You are now talking in the " + ChatColor.BLUE + "region " + ChatColor.GREEN + "chat");
			
			rpg_p.SetLastChat(RPG_ChatType.Region);
		}
		else if (commandLabel.equalsIgnoreCase("privateon") || commandLabel.equalsIgnoreCase("pon"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.chat.private.on"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (!RPG_Core.IsPlayer(sender))
			{
				sender.sendMessage(ChatColor.RED + "Only players can use the chat settings");
				return false;
			}
			
			rpg_p.SetChatPrivate(true);
			sender.sendMessage(ChatColor.GOLD + "You have turned " + ChatColor.BLUE + "PRIVATE " + ChatColor.GOLD + "chat messages " + ChatColor.BLUE + "ON" + ChatColor.GOLD + "!");
		}
		else if (commandLabel.equalsIgnoreCase("privateoff") || commandLabel.equalsIgnoreCase("poff"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.chat.private.off"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (!RPG_Core.IsPlayer(sender))
			{
				sender.sendMessage(ChatColor.RED + "Only players can use the chat settings");
				return false;
			}
			
			rpg_p.SetChatPrivate(false);
			sender.sendMessage(ChatColor.GOLD + "You have turned " + ChatColor.BLUE + "PRIVATE " + ChatColor.GOLD + "chat messages " + ChatColor.BLUE + "OFF" + ChatColor.GOLD + "!");
		}
		else if (commandLabel.equalsIgnoreCase("p"))
		{
			if (!RPG_Core.HasPermission(sender, "rpg.chat.private"))
			{
				sender.sendMessage(RPG_Core.GetFormattedMessage("no_perm", sender));
				RPG_Core.Log(sender, RPG_LogType.Warning, "Command", "NoPerm");
				return false;
			}
			
			if (args.length < 2)
			{
				sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				sender.sendMessage(ChatColor.BLUE + "RPG Commands - Send private chat");
				sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
				sender.sendMessage(ChatColor.BLUE + " Description:");
				sender.sendMessage(ChatColor.WHITE + "  Sends a chat private message to the specified player");
				sender.sendMessage(ChatColor.BLUE + " Syntax:");
				sender.sendMessage(ChatColor.GOLD + "  /p [Player] [Text]");
				sender.sendMessage(ChatColor.BLUE + " Arguments:");
				sender.sendMessage(ChatColor.GOLD + "  [Player]  " + ChatColor.WHITE + "The name of the player");
				sender.sendMessage(ChatColor.GOLD + "  [Text]     " + ChatColor.WHITE + "The text that is sent to the player");
				sender.sendMessage(ChatColor.GOLD + RPG_Core.GetTextSeperator());
			}
			else
			{
				ArrayList<RPG_Player> rpg_pls = RPG_Core.GetPlayersForPartialName(args[0]);
				if (rpg_pls.size() == 0)
				{
					sender.sendMessage(ChatColor.RED + "No player with the name '" + args[0] + "' could be found");
					return false;
				}
				
				if (rpg_pls.size() > 1)
				{
					sender.sendMessage(ChatColor.RED + "The name '" + args[0] + "' can stand for the following players:");
					for (RPG_Player pl : rpg_pls)
						sender.sendMessage(ChatColor.RED + "  " + pl.GetUsername());
					return false;
				}
				
				RPG_Player rpg_pl = rpg_pls.get(0);
				
				String text = args[1];
				for (int i = 2; i < args.length; i++)
					text += " " + args[i];
				
				RPG_Core.LogChatMessage(sender, rpg_pl.GetID(), RPG_ChatType.Private, text);
				logger.info(color_private + "[Private] " + ChatColor.BLUE + sender.getName() + ": " + ChatColor.WHITE + text);
				
				rpg_pl.GetPlayer().sendMessage(color_private + "[Private] " + ChatColor.BLUE + sender.getName() + ": " + ChatColor.WHITE + text);
			}
			
			if (RPG_Core.IsPlayer(sender))
				rpg_p.SetLastChat(RPG_ChatType.Private);
		}
		else
			sender.sendMessage(ChatColor.RED + "Unknown command");
		
		return false;
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e)
	{
		RPG_Player rpg_p = RPG_Core.GetPlayer(e.getPlayer().getName());
		
		if (rpg_p.IsTalking())
			return;
		
		if (rpg_p.GetEditMode() != RPG_EditMode.None)
			return;
		
		e.getRecipients().clear();
		
		if (rpg_p.GetLastChat() == RPG_ChatType.Server)
		{
			if (!RPG_Core.HasPermission(rpg_p, "rpg.chat.server"))
			{
				rpg_p.SendMessage(RPG_Core.GetFormattedMessage("no_perm", rpg_p.GetLanguage()));
				e.setCancelled(true);
				return;
			}
			
			e.setFormat(color_server + "[Server] " + ChatColor.BLUE + "%s: " + ChatColor.WHITE + "%s");
			
			for (Player pl : getServer().getOnlinePlayers())
				e.getRecipients().add(pl);
		}
		else if (rpg_p.GetLastChat() == RPG_ChatType.World)
		{
			if (!RPG_Core.HasPermission(rpg_p, "rpg.chat.world"))
			{
				rpg_p.SendMessage(RPG_Core.GetFormattedMessage("no_perm", rpg_p.GetLanguage()));
				e.setCancelled(true);
				return;
			}
			
			e.setFormat(color_world + "[World] " + ChatColor.BLUE + "%s: " + ChatColor.WHITE + "%s");
			
			for (RPG_Player rpg_pl : RPG_Core.GetOnlinePlayers())
			{
				if (rpg_pl.GetChatWorld())
					continue;
				
				if (rpg_p.GetPlayer().getWorld().getUID().equals(rpg_pl.GetPlayer().getWorld().getUID()))
					e.getRecipients().add(rpg_pl.GetPlayer());
			}
		}
		else if (rpg_p.GetLastChat() == RPG_ChatType.Nation)
		{
			if (!RPG_Core.HasPermission(rpg_p, "rpg.chat.nation"))
			{
				rpg_p.SendMessage(RPG_Core.GetFormattedMessage("no_perm", rpg_p.GetLanguage()));
				e.setCancelled(true);
				return;
			}
			
			e.setFormat(color_nation + "[Nation] " + ChatColor.BLUE + "%s: " + ChatColor.WHITE + "%s");
			
			for (RPG_Player rpg_pl : RPG_Core.GetOnlinePlayers())
			{
				if (rpg_pl.GetChatNation() && rpg_pl.GetNationId() == rpg_p.GetNationId())
					e.getRecipients().add(rpg_pl.GetPlayer());
			}
		}
		else if (rpg_p.GetLastChat() == RPG_ChatType.Guild)
		{
			if (!RPG_Core.HasPermission(rpg_p, "rpg.chat.guild"))
			{
				rpg_p.SendMessage(RPG_Core.GetFormattedMessage("no_perm", rpg_p.GetLanguage()));
				e.setCancelled(true);
				return;
			}
			
			if (rpg_p.GetGuildId() == -1)
			{
				rpg_p.SendMessage(ChatColor.RED + "You are not in a guild!");
				return;
			}
			
			e.setFormat(color_guild + "[Guild] " + ChatColor.BLUE + "%s: " + ChatColor.WHITE + "%s");
			
			for (RPG_Player rpg_pl : RPG_Core.GetOnlinePlayers())
			{
				if (rpg_pl.GetChatGuild() && rpg_pl.GetNationId() == rpg_p.GetNationId())
					e.getRecipients().add(rpg_pl.GetPlayer());
			}
		}
		else if (rpg_p.GetLastChat() == RPG_ChatType.Region)
		{
			if (!RPG_Core.HasPermission(rpg_p, "rpg.chat.region"))
			{
				rpg_p.SendMessage(RPG_Core.GetFormattedMessage("no_perm", rpg_p.GetLanguage()));
				e.setCancelled(true);
				return ;
			}
			
			e.setFormat(color_region + "[Region] " + ChatColor.BLUE + "%s: " + ChatColor.WHITE + "%s");
			
			for (RPG_Player rpg_pl : RPG_Core.GetOnlinePlayers())
			{
				if (rpg_p.GetPlayer().getLocation().getWorld() != rpg_pl.GetPlayer().getLocation().getWorld() || rpg_p.GetPlayer().getLocation().distance(rpg_pl.GetPlayer().getLocation()) > talkdist)
					continue;
				
				if (rpg_pl.GetChatRegion())
					e.getRecipients().add(rpg_pl.GetPlayer());
			}
		}
		else if (rpg_p.GetLastChat() == RPG_ChatType.Private)
		{
			if (!RPG_Core.HasPermission(rpg_p, "rpg.chat.private"))
			{
				rpg_p.SendMessage(RPG_Core.GetFormattedMessage("no_perm", rpg_p.GetLanguage()));
				e.setCancelled(true);
				return ;
			}
		}
		
		RPG_Core.LogChatMessage(rpg_p.GetID(), rpg_p.GetLastChat(), e.getMessage());
	}
}

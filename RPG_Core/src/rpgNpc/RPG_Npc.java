package rpgNpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.server.v1_7_R1.EntityLiving;
import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.EnumGamemode;
import net.minecraft.server.v1_7_R1.Packet;
import net.minecraft.server.v1_7_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_7_R1.PlayerInteractManager;
import net.minecraft.server.v1_7_R1.WorldServer;

import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.inventory.ItemStack;

import net.minecraft.util.com.mojang.authlib.GameProfile;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import rpgCore.RPG_Core;
import rpgOther.RPG_LogType;
import rpgOther.RPG_Teleport;
import rpgPlayer.RPG_Player;
import rpgQuest.RPG_Quest;
import rpgShop.RPG_Shop;
import rpgTexts.RPG_Language;
import rpgTexts.RPG_Text;

public class RPG_Npc
{
	private RPG_NpcData data;
	private net.minecraft.server.v1_7_R1.EntityPlayer player;
	private WorldServer worldServer;
	
	public int GetID()
	{
		return data.GetID();
	}
	public UUID GetUUID()
	{
		return player.getUniqueID();
	}
	public String GetName()
	{
		return data.GetName();
	}
	public Location GetLocation()
	{
		return data.GetLocation();
	}
	public int GetLevel()
	{
		return data.GetLevel();
	}
	public int GetMoney()
	{
		return data.GetMoney();
	}
	public int GetNationID()
	{
		return data.GetNationID();
	}
	public int GetShopID()
	{
		return data.GetShopID();
	}
	public String GetItemInHand()
	{
		return data.GetItemInHand();
	}
	public String GetArmorHead()
	{
		return data.GetArmorHead();
	}
	public String GetArmorChest()
	{
		return data.GetArmorChest();
	}
	public String GetArmorLegs()
	{
		return data.GetArmorLegs();
	}
	public String GetArmorFeet()
	{
		return data.GetArmorFeet();
	}
	public RPG_Text GetStandardText()
	{
		return data.GetStandardText();
	}
	public String GetFormattedStandardTextInLanguage(RPG_Language Language, RPG_Player Player, RPG_Npc NPC)
	{
		return data.GetFormattedStandardTextInLanguage(Language, Player, NPC);
	}
	
    
	public RPG_Npc(RPG_NpcData Data)
	{
		try
		{
			this.data = Data;
			worldServer = ((CraftWorld)Data.GetLocation().getWorld()).getHandle();
			PlayerInteractManager pim = new PlayerInteractManager(worldServer);
			this.player = new EntityPlayer(worldServer.getServer().getServer(), worldServer, new GameProfile(UUID.randomUUID().toString(), Data.GetName()), pim);
		    pim.setGameMode(EnumGamemode.SURVIVAL);
		    this.player.setPositionRotation(Data.GetLocation().getX(), Data.GetLocation().getY(), Data.GetLocation().getZ(), Data.GetLocation().getYaw(), Data.GetLocation().getPitch());
		    this.player.getBukkitEntity().setSleepingIgnored(true);
		    
		    RPG_NetworkSocket socket = new RPG_NetworkSocket();
		    RPG_NetworkManager conn = null;
		    try
		    {
		    	conn = new RPG_NetworkManager(false);
			    player.playerConnection = new RPG_NetworkHandler(worldServer.getServer().getServer(), conn, player);
			    conn.a(player.playerConnection);
		    }
		    catch (IOException ex)
		    { }
		    
		    try
		    {
		      socket.close();
		    }
		    catch (IOException ex)
		    {}
		    
		    EntityLiving entity = (EntityLiving)player;
		    entity.yaw = Data.GetLocation().getYaw();
		    entity.aP = Data.GetLocation().getYaw();
		    entity.aQ = Data.GetLocation().getYaw();
		    entity.pitch = Data.GetLocation().getPitch();
		    
		    this.player.getBukkitEntity().getInventory().setItemInHand(new ItemStack(Material.getMaterial(Data.GetItemInHand())));
		    this.player.getBukkitEntity().getInventory().setHelmet(new ItemStack(Material.getMaterial(Data.GetArmorHead())));
		    this.player.getBukkitEntity().getInventory().setChestplate(new ItemStack(Material.getMaterial(Data.GetArmorChest())));
		    this.player.getBukkitEntity().getInventory().setLeggings(new ItemStack(Material.getMaterial(Data.GetArmorLegs())));
		    this.player.getBukkitEntity().getInventory().setBoots(new ItemStack(Material.getMaterial(Data.GetArmorFeet())));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void SetName(String Name)
	{
		RPG_Core.GetNpcManager().EditNPCProperty(data.GetID(), "Name", Name);
	}
	public void SetNationID(int ID)
	{
		RPG_Core.GetNpcManager().EditNPCProperty(data.GetID(), "NationID", ID + "");
	}
	public void SetLevel(int Level)
	{
		RPG_Core.GetNpcManager().EditNPCProperty(data.GetID(), "Level", Level + "");
	}
	public void SetMoney(int Amount)
	{
		RPG_Core.GetNpcManager().EditNPCProperty(data.GetID(), "Money", Amount + "");
	}
	public void SetShopID(int ID)
	{
		RPG_Core.GetNpcManager().EditNPCProperty(data.GetID(), "ShopID", ID + "");
	}
	public void SetPosition(int ID)
	{
	}
	public void SetText(int ID)
	{
	}
	public void SetItemInHand(String item)
	{
		RPG_Core.GetNpcManager().EditNPCProperty(data.GetID(), "ItemInHand", item);
	}
	public void SetHeadArmor(String item)
	{
		RPG_Core.GetNpcManager().EditNPCProperty(data.GetID(), "ArmorHead", item);
	}
	public void SetChestArmor(String item)
	{
		RPG_Core.GetNpcManager().EditNPCProperty(data.GetID(), "ArmorChest", item);
	}
	public void SetLegsArmor(String item)
	{
		RPG_Core.GetNpcManager().EditNPCProperty(data.GetID(), "ArmorLegs", item);
	}
	public void SetFeetArmor(String item)
	{
		RPG_Core.GetNpcManager().EditNPCProperty(data.GetID(), "ArmorFeet", item);
	}
	
	public void Spawn()
	{
		worldServer.addEntity(player);
		worldServer.players.remove(player);
	    
	    RPG_Core.Log(-1, data.GetID(), RPG_LogType.Information, "NPC", "Spawned");
	}
	public void DeSpawn()
	{
		worldServer.removeEntity(player);
		
		Packet packet = new PacketPlayOutEntityDestroy(player.getId());
		RPG_Core.SendPacketToPlayers(packet);
		
        player.die();
        
	    RPG_Core.Log(-1, data.GetID(), RPG_LogType.Information, "NPC", "Despawned");
	} 
	
	
	public int GetTeleportCount()
	{
		return RPG_Core.GetTeleportCountByNPC(data.GetID());
	}
	public ArrayList<RPG_Teleport> GetTeleports()
	{
		return RPG_Core.GetTeleportsByNPC(data.GetID());
	}
	
	public ArrayList<RPG_Quest> GetQuestStarts()
	{
		if (!Bukkit.getPluginManager().isPluginEnabled("RPG Quest Manager"))
			return new ArrayList<RPG_Quest>();
		
		return RPG_Core.GetQuestManager().GetQuestsByNPC(data.GetID());
	}
	public ArrayList<RPG_Quest> GetQuestStartsForPlayer(RPG_Player Player)
	{
		if (!Bukkit.getPluginManager().isPluginEnabled("RPG Quest Manager"))
			return new ArrayList<RPG_Quest>();
		
		ArrayList<RPG_Quest> qs = RPG_Core.GetQuestManager().GetQuestsByNPC(data.GetID());
		ArrayList<RPG_Quest> realqs = new ArrayList<RPG_Quest>();
		ArrayList<RPG_Quest> currentqs = Player.GetCurrentQuests();
		ArrayList<RPG_Quest> completedqs = Player.GetCompletedQuests();
		
		for (RPG_Quest q : qs)
		{
			if (q.GetReqLevel() > Player.GetLevel())
				continue;
			
			if (q.GetReqMoney() > Player.GetMoney())
				continue;
			
			boolean currentOrCompleted = false;
			for (RPG_Quest currq : currentqs)
			{
				if (currq.GetID() == q.GetID())
				{
					currentOrCompleted = true;
					break;
				}
			}
			
			if (currentOrCompleted)
				continue;
			
			for (RPG_Quest completedq : completedqs)
			{
				if (completedq.GetID() == q.GetID())
				{
					currentOrCompleted = true;
					break;
				}
			}
			
			if (currentOrCompleted)
				continue;
			
			boolean completed = true;
			for (Integer qid : q.GetReqQuestIDs())
			{
				boolean contains = false;
				for (RPG_Quest compq : completedqs)
				{
					if (compq.GetID() == qid)
					{
						contains = true;
						break;
					}
				}
				
				if (!contains)
				{
					completed = false;
					break;
				}
			}
			
			if (!completed)
				continue;
			
			realqs.add(q);
		}
		
		return realqs;
	}
	
	public RPG_Shop GetShop()
	{
		return RPG_Core.GetShop(data.GetShopID());
	}
}

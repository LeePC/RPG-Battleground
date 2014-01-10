package rpgBank;

import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

import rpgCore.RPG_Core;
import rpgPlayer.RPG_Player;

public class RPG_BankAccount
{
	private int id;
	private int playerid;
	private int gold;
	private HashMap<Integer, ItemStack> items = new HashMap<Integer, ItemStack>();
	
	public int GetID()
	{
		return id;
	}
	public int GetPlayerID()
	{
		return playerid;
	}
	public int GetGold()
	{
		return gold;
	}
	public HashMap<Integer, ItemStack> GetItems()
	{
		return items;
	}
	
	
	public RPG_BankAccount(int ID, int PlayerID, int Gold, HashMap<Integer, ItemStack> Items)
	{
		this.id = ID;
		this.playerid = PlayerID;
		this.gold = Gold;
		this.items = Items;
	}
	
	public RPG_Player GetPlayer()
	{
		return RPG_Core.GetPlayer(playerid);
	}
	
	public void Deposit(ItemStack Stack)
	{
		int type = Stack.getTypeId();
		
		if (items.containsKey(type))
			items.get(type).setAmount(items.get(type).getAmount() + Stack.getAmount());
		else
			items.put(type, Stack);
	}
	public ItemStack Withdraw(int TypeID)
	{
		if (items.containsKey(TypeID))
		{
			ItemStack stack = items.get(TypeID);
			items.remove(TypeID);
			return stack;
		}
		
		return null;
	}
	public int GetAmount(int TypeID)
	{
		return TypeID;
	}
}

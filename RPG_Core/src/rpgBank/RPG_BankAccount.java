package rpgBank;

import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

import rpgCore.RPG_Core;
import rpgPlayer.RPG_Player;

public class RPG_BankAccount
{
	private int id;
	private int playerId;
	private int gold;
	private HashMap<String, ItemStack> items = new HashMap<String, ItemStack>();
	
	public int GetID()
	{
		return id;
	}
	public int GetPlayerID()
	{
		return playerId;
	}
	public int GetGold()
	{
		return gold;
	}
	public HashMap<String, ItemStack> GetItems()
	{
		return items;
	}
	
	
	public RPG_BankAccount(int id, int playerId, int gold, HashMap<String, ItemStack> items)
	{
		this.id = id;
		this.playerId = playerId;
		this.gold = gold;
		this.items = items;
	}
	
	public RPG_Player GetPlayer()
	{
		return RPG_Core.GetPlayer(playerId);
	}
	
	public void Deposit(ItemStack stack)
	{
		String type = stack.getType().name();
		
		if (items.containsKey(type))
			items.get(type).setAmount(items.get(type).getAmount() + stack.getAmount());
		else
			items.put(type, stack);
	}
	public ItemStack Withdraw(int typeId)
	{
		if (items.containsKey(typeId))
		{
			ItemStack stack = items.get(typeId);
			items.remove(typeId);
			return stack;
		}
		
		return null;
	}
	public int GetAmount(int typeId)
	{
		return typeId;
	}
}

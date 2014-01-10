package rpgMobs;

import org.bukkit.inventory.ItemStack;

public class RPG_MobDrop
{
	private ItemStack item;
	private float chance;
	
	public ItemStack GetItem()
	{
		return item;
	}
	public float GetChance()
	{
		return chance;
	}
	
	
	public RPG_MobDrop (ItemStack Item, float Chance)
	{
		this.item = Item;
		
		if (Chance > 1.0)
			Chance = 1.0f;
		if (Chance < 0)
			Chance = 0.0f;
		this.chance = Chance;
	}
}

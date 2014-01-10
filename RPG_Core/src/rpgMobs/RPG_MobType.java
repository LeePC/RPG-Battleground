package rpgMobs;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.inventory.ItemStack;

public class RPG_MobType
{
	private int id;
	private int nameid;
	private int descrid;
	private int mobtype;
	private int level;
	private ArrayList<RPG_MobDrop> drops = new ArrayList<RPG_MobDrop>();
	
	public int GetID()
	{
		return id;
	}
	public int GetNameID()
	{
		return nameid;
	}
	public int GetDescriptionID()
	{
		return descrid;
	}
	public int GetMobType()
	{
		return mobtype;
	}
	public int GetLevel()
	{
		return level;
	}
	
	
	public RPG_MobType(int ID, int NameID, int DescriptionID, int MobType, int Level, ArrayList<RPG_MobDrop> Drops)
	{
		this.id = ID;
		this.nameid = NameID;
		this.descrid = DescriptionID;
		this.mobtype = MobType;
		this.level = Level;
		this.drops = Drops;
	}
	
	public ArrayList<ItemStack> GetRandomDrops()
	{
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		Random rand = new Random();
		
		for (RPG_MobDrop drop : drops)
		{
			if (rand.nextDouble() < drop.GetChance())
				items.add(drop.GetItem().clone());
		}
		
		return items;
	}
}

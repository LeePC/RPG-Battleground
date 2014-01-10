package rpgMobs;

import java.util.Collection;
import java.util.HashMap;

public class RPG_MobSpawner
{
	private int id;
	private HashMap<RPG_MobType, Integer> spawndefs = new HashMap<RPG_MobType, Integer>();
	
	public int GetID()
	{
		return id;
	}
	public Collection<RPG_MobType> GetSpawningMobs()
	{
		return spawndefs.keySet();
	}
	
	
	public RPG_MobSpawner(int ID, HashMap<RPG_MobType, Integer> SpawnDefs)
	{
		this.id = ID;
		this.spawndefs = SpawnDefs;
	}
}

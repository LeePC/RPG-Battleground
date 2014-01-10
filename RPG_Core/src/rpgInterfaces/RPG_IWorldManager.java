package rpgInterfaces;

import java.util.Collection;

import rpgWorld.RPG_World;
import rpgWorld.RPG_WorldCreationData;

public interface RPG_IWorldManager
{
	// 
	// Worlds
	// 
	public RPG_World GetWorld(int ID);
	public RPG_World GetWorld(String Name);
	public Collection<RPG_World> GetWorlds();
	public int AddWorldToDB(RPG_WorldCreationData Data);
	public void LoadWorldsFromDB();
	public void LoadWorlds();
}

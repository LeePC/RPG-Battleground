package rpgBattlefield;

import java.util.Collection;
import java.util.HashMap;

public class RPG_BattlefieldRegion
{
	private int id;
	private String name;
	private HashMap<Integer, RPG_Outpost> outposts = new HashMap<Integer, RPG_Outpost>();
	
	public int GetID()
	{
		return id;
	}
	public String GetName()
	{
		return name;
	}
	public Collection<RPG_Outpost> GetOutposts()
	{
		return outposts.values();
	}
	
	
	public RPG_BattlefieldRegion()
	{
		
	}
}

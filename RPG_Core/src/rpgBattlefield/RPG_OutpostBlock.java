package rpgBattlefield;

import org.bukkit.Location;
import org.bukkit.Material;

import rpgCore.RPG_Core;

public class RPG_OutpostBlock
{
	private int id;
	private int outpostid;
	private String type;
	private Location loc;
	
	public int GetID()
	{
		return id;
	}
	public int GetOutpostID()
	{
		return outpostid;
	}
	public String GetType()
	{
		return type;
	}
	public Location GetLocation()
	{
		return loc;
	}
	
	
	public RPG_OutpostBlock(int id, int outpostId, String type, Location location)
	{
		this.id = id;
		this.outpostid = outpostId;
		this.type = type;
		this.loc = location;
	}
	
	public RPG_Outpost GetOutpost()
	{
		return RPG_Core.GetBattlefieldManager().GetOutpost(outpostid);
	}
	public void RecreateBlock()
	{		
		loc.getBlock().setType(Material.getMaterial(type));
	}
}

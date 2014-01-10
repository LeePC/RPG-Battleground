package rpgBattlefield;

import org.bukkit.Location;

import rpgCore.RPG_Core;

public class RPG_OutpostBlock
{
	private int id;
	private int outpostid;
	private int type;
	private byte data;
	private Location loc;
	
	public int GetID()
	{
		return id;
	}
	public int GetOutpostID()
	{
		return outpostid;
	}
	public int GetType()
	{
		return type;
	}
	public byte GetData()
	{
		return data;
	}
	public Location GetLocation()
	{
		return loc;
	}
	
	
	public RPG_OutpostBlock(int ID, int OutpostID, int Type, byte Data, Location Loc)
	{
		this.id = ID;
		this.outpostid = OutpostID;
		this.data = Data;
		this.type = Type;
		this.loc = Loc;
	}
	
	public RPG_Outpost GetOutpost()
	{
		return RPG_Core.GetBattlefieldManager().GetOutpost(outpostid);
	}
	public void RecreateBlock()
	{		
		loc.getBlock().setTypeIdAndData(type, data, false);
	}
}

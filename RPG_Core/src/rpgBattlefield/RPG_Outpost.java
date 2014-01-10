package rpgBattlefield;

import java.util.ArrayList;

import rpgCore.RPG_Core;
import rpgNation.RPG_Nation;
import rpgOther.RPG_Region;

public class RPG_Outpost
{
	private int id;
	private int nameid;
	private RPG_Region region;
	private int flagid;
	private ArrayList<RPG_OutpostBlock> blocks = new ArrayList<RPG_OutpostBlock>();
	
	public int GetID()
	{
		return id;
	}
	public int GetNameID()
	{
		return nameid;
	}
	public RPG_Region GetRegion()
	{
		return region;
	}
	public int GetFlagID()
	{
		return flagid;
	}
	public ArrayList<RPG_OutpostBlock> GetBlocks()
	{
		return blocks;
	}
	
	public RPG_Outpost(int ID, int NameID, RPG_Region Region, int FlagID, ArrayList<RPG_OutpostBlock> Blocks)
	{
		this.id = ID;
		this.nameid = NameID;
		this.region = Region;
		this.flagid = FlagID;
		this.blocks = Blocks;
	}
	
	public RPG_Flag GetFlag()
	{
		return RPG_Core.GetBattlefieldManager().GetFlag(flagid);
	}
	public int GetNationID()
	{
		return GetFlag().GetNationID();
	}
	public RPG_Nation GetNation()
	{
		return GetFlag().GetNation();
	}
	public void Rebuild()
	{
		for (RPG_OutpostBlock b : blocks)
			b.RecreateBlock();
	}
}

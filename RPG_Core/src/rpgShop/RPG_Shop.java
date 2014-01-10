package rpgShop;

import java.util.ArrayList;

import org.bukkit.Location;

public class RPG_Shop
{
	private int id;
	private int nameid;
	private Location pos;
	private int reqlvl;
	private ArrayList<Integer> qids = new ArrayList<Integer>();
	
	public int GetID()
	{
		return id;
	}
	public int GetNameID()
	{
		return nameid;
	}
	public Location GetPosition()
	{
		return pos;
	}
	public int GetReqLevel()
	{
		return reqlvl;
	}
	public ArrayList<Integer> GetPreReqQuestIDs()
	{
		return qids;
	}
	
	
	public RPG_Shop(int ID, int NameID, Location Position, int RequiredLevel, ArrayList<Integer> PreReqQuestIDs)
	{
		this.id = ID;
		this.nameid = NameID;
		this.pos = Position;
		this.reqlvl = RequiredLevel;
		this.qids = PreReqQuestIDs;
	}
}

package rpgOther;

import org.bukkit.Location;

import rpgCore.RPG_Core;
import rpgTexts.RPG_Language;

public class RPG_Teleport
{
	private int id;
	private int npcid;
	private Location targetpos;
	private int targettextid;
	private int level;
	private int cost;
	
	public int GetID()
	{
		return id;
	}
	public int GetNPCID()
	{
		return npcid;
	}
	public Location GetTargetPos()
	{
		return targetpos;
	}
	public int GetTargetTextID()
	{
		return targettextid;
	}
	public int GetLevel()
	{
		return level;
	}
	public int GetCost()
	{
		return cost;
	}
	
	
	public RPG_Teleport(int ID, int NPCID, Location TargetPos, int TargetTextID, int Level, int Cost)
	{
		this.id = ID;
		this.npcid = NPCID;
		this.targetpos = TargetPos;
		this.targettextid = TargetTextID;
		this.level = Level;
		this.cost = Cost;
	}
	
	public String GetTargetNameInLanguage(RPG_Language Language)
	{
		return RPG_Core.GetTextInLanguage(targettextid, Language);
	}
}

package rpgBattlefield;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Location;

import rpgCore.RPG_Core;
import rpgNation.RPG_Nation;
import rpgTexts.RPG_Language;

public class RPG_Flag
{
	private int id;
	private int nameid;
	private int nationid;
	private Location loc;
	private float captureradius;
	private int capturetime;
	private int capturevalue;
	private HashMap<Integer, Integer> players = new HashMap<Integer, Integer>();
	
	
	public int GetID()
	{
		return id;
	}
	public int GetNameID()
	{
		return nameid;
	}
	public int GetNationID()
	{
		return nationid;
	}
	public Location GetLocation()
	{
		return loc;
	}
	public float GetCaptureRadius()
	{
		return captureradius;
	}
	public int GetCaptureTime()
	{
		return capturetime;
	}
	public int GetCaptureValue()
	{
		return capturevalue;
	}
	
	
	public RPG_Flag(int ID, int NameID, int NationID, Location Loc, float CaptureRadius, int CaptureTime)
	{
		this.id = ID;
		this.nameid = NameID;
		this.nationid = NationID;
		this.loc = Loc;
		this.captureradius = CaptureRadius;
		this.capturetime = CaptureTime;
	}
	
	public RPG_Nation GetNation()
	{
		return RPG_Core.GetNation(nationid);
	}
	public String GetName(RPG_Language Language)
	{
		return RPG_Core.GetTextInLanguage(nameid, Language);
	}
	
	public void AddPlayerFromNation(int NationID)
	{
		if (players.containsKey(NationID))
			players.put(NationID, players.get(NationID) + 1);
		else
			players.put(NationID, 1);
	}
	public void ResetPlayersFromNations()
	{
		players.clear();
	}
	public void UpdateCaptureValue()
	{
		int mostid = -1;
		int mostvalue = 0;
		boolean istie = false;
		
		for (Entry<Integer, Integer> entry : players.entrySet())
		{
			if (entry.getValue() > mostvalue)
			{
				mostid = entry.getKey();
				mostvalue = entry.getValue();
				istie = false;
			}
			else if (entry.getValue() == mostvalue)
				istie = true;
		}
		
		if (istie)
			return;
		
		if (nationid != mostid)
			capturevalue -= mostvalue;
		else
			capturevalue += mostvalue;
		
		if (capturevalue < 0)
		{
			nationid = mostid;
			capturevalue = -capturevalue;
		}
		
		if (capturevalue > capturetime)
			capturevalue = (int)capturetime;
	}
}

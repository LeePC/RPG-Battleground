package rpgQuest;

import java.util.HashMap;

import rpgCore.RPG_Core;
import rpgPlayer.RPG_Player;

public class RPG_QuestProgress
{
	private int id;
	private int questid;
	private int playerid;
	private boolean completed;
	private HashMap<Integer, Integer> progresses = new HashMap<Integer, Integer>();
	
	public int GetID()
	{
		return id;
	}
	public int GetQuestID()
	{
		return questid;
	}
	public int GetPlayerID()
	{
		return playerid;
	}
	public boolean GetCompleted()
	{
		return completed;
	}
	public Integer GetProgress(int ID)
	{
		return progresses.get(ID);
	}
	
	
	public RPG_QuestProgress(int ID, int QuestID, int PlayerID, boolean Completed, HashMap<Integer, Integer> Progresses)
	{
		this.id = ID;
		this.questid = QuestID;
		this.playerid = PlayerID;
		this.completed = Completed;
		this.progresses = Progresses;
	}
	
	public RPG_Quest GetQuest()
	{
		return RPG_Core.GetQuestManager().GetQuest(questid);
	}
	public RPG_Player GetPlayer()
	{
		return RPG_Core.GetPlayer(playerid);
	}
	public void SetProgress(int ID, int Progress)
	{
		if (progresses.containsKey(ID))
			this.progresses.remove(ID);
		
		this.progresses.put(ID, Progress);
	}	
	public void AddProgress(int ID)
	{
		if (progresses.containsKey(ID))
		{
			int oldval = progresses.get(ID);
			progresses.remove(ID);
			progresses.put(ID, oldval + 1);
		}
		else
			progresses.put(ID, 1);
	}
	public void AddProgress(int ID, int Amount)
	{
		if (progresses.containsKey(ID))
		{
			int oldval = progresses.get(ID);
			progresses.remove(ID);
			progresses.put(ID, oldval + Amount);
		}
		else
			progresses.put(ID, Amount);
	}
}

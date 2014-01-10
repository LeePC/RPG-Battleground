package rpgOther;

import java.util.ArrayList;

import rpgPlayer.RPG_PlayerAction;

public class RPG_RestrictedItem
{
	private int id;
	private int iid;
	private RPG_PlayerAction action;
	private int reqlvl;
	private ArrayList<Integer> qids = new ArrayList<Integer>();
	
	public int GetID()
	{
		return id;
	}
	public int GetItemID()
	{
		return iid;
	}
	public RPG_PlayerAction GetAction()
	{
		return action;
	}
	public int GetRequiredLevel()
	{
		return reqlvl;
	}
	public ArrayList<Integer> GetQuestIDs()
	{
		return qids;
	}
	
	
	public RPG_RestrictedItem(int ID, int ItemID, RPG_PlayerAction Action, int RequiredLevel, ArrayList<Integer> QuestIDs)
	{
		this.id = ID;
		this.iid = ItemID;
		this.action = Action;
		this.reqlvl = RequiredLevel;
		this.qids = QuestIDs;
	}
}

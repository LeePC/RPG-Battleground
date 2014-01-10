package rpgQuest;

import java.util.ArrayList;

import rpgOther.RPG_CreationData;

public class RPG_QuestCreationData extends RPG_CreationData
{
	public String Name = "";
	public String DisplayName = "";
	public String Description = "";
	public RPG_QuestType Type = RPG_QuestType.None;
	public int NPCStartID = -1;
	public int NPCEndID = -1;
	public String StartText = "";
	public String EndText = "";
	public ArrayList<Integer> ReqQuests = new ArrayList<Integer>();
	public int ReqLevel = 0;
	public int ReqMoney = 0;
	public int RewardExp = 0;
	public int RewardMoney = 0;
	public boolean Recompletable = false;
	public ArrayList<RPG_QuestRequest> Data = new ArrayList<RPG_QuestRequest>();
	
	
	public RPG_QuestCreationData()
	{ }
}

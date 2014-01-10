package rpgQuest;

import java.util.ArrayList;

public class RPG_Quest_Kill extends RPG_Quest
{
	private ArrayList<RPG_QuestRequest> mobs = new ArrayList<RPG_QuestRequest>();
	
	public ArrayList<RPG_QuestRequest> GetMobs()
	{
		return mobs;
	}
	
	
	public RPG_Quest_Kill(int ID, int NameID, int DisplayNameID, int DescriptionID, RPG_QuestType Type, int NPCStartID, int NPCEndID, int NPCStartTextID, int NPCEndTextID,
			ArrayList<Integer> ReqQuestIDs, int ReqMoney, int ReqLevel, int RewardExp, int RewardMoney, boolean Recompletable, ArrayList<RPG_QuestRequest> Mobs)
	{
		super(ID, NameID, DisplayNameID, DescriptionID, Type, NPCStartID, NPCEndID, NPCStartTextID, NPCEndTextID, ReqQuestIDs, ReqMoney, ReqLevel, RewardExp, RewardMoney, 
				Recompletable);
		
		this.mobs = Mobs;
	}
}

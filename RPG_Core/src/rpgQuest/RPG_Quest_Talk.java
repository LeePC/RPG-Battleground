package rpgQuest;

import java.util.ArrayList;

public class RPG_Quest_Talk extends RPG_Quest
{
	public RPG_Quest_Talk(int ID, int NameID, int DisplayNameID, int DescriptionID, RPG_QuestType Type, int NPCStartID, int NPCEndID, int NPCStartTextID, int NPCEndTextID, 
			ArrayList<Integer> ReqQuestIDs, int ReqMoney, int ReqLevel, int RewardExp, int RewardMoney, boolean Recompletable)
	{
		super(ID, NameID, DisplayNameID, DescriptionID, Type, NPCStartID, NPCEndID, NPCStartTextID, NPCEndTextID, ReqQuestIDs, ReqMoney, ReqLevel, RewardExp, RewardMoney, 
				Recompletable);
	}
}

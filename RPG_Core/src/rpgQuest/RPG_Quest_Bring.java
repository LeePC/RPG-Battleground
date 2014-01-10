package rpgQuest;

import java.util.ArrayList;

public class RPG_Quest_Bring extends RPG_Quest
{
	private ArrayList<RPG_QuestRequest> items = new ArrayList<RPG_QuestRequest>();
	private boolean keepitems;
	
	public ArrayList<RPG_QuestRequest> GetItems()
	{
		return items;
	}
	public boolean GetKeepItems()
	{
		return keepitems;
	}
	
	
	public RPG_Quest_Bring(int ID, int NameID, int DisplayNameID, int DescriptionID, RPG_QuestType Type, int NPCStartID, int NPCEndID, int NPCStartTextID, int NPCEndTextID,
			ArrayList<Integer> ReqQuestIDs, int ReqMoney, int ReqLevel, int RewardExp, int RewardMoney, boolean Recompletable, ArrayList<RPG_QuestRequest> Items, boolean KeepItems)
	{
		super(ID, NameID, DisplayNameID, DescriptionID, Type, NPCStartID, NPCEndID, NPCStartTextID, NPCEndTextID, ReqQuestIDs, ReqMoney, ReqLevel, RewardExp, RewardMoney, 
				Recompletable);
		
		this.items = Items;
		this.keepitems = KeepItems;
	}
}

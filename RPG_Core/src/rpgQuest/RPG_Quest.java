package rpgQuest;

import java.util.ArrayList;

import rpgCore.RPG_Core;
import rpgPlayer.RPG_Player;
import rpgTexts.RPG_Language;

public abstract class RPG_Quest
{
	private int id;
	private int nameid;
	private int displaynameid;
	private int descrid;
	private RPG_QuestType questtype;
	private int npcstartid;
	private int npcendid;
	private int npcstarttextid;
	private int npcendtextid;
	private ArrayList<Integer> reqqids = new ArrayList<Integer>();
	private int reqmoney;
	private int reqlevel;
	private int rewardexp;
	private int rewardmoney;
	private String tag;
	private boolean recompletable;
	
	public int GetID()
	{
		return id;
	}
	public int GetNameID()
	{
		return nameid;
	}
	public int GetDisplayNameID()
	{
		return displaynameid;
	}
	public int GetDescriptionID()
	{
		return descrid;
	}
	public RPG_QuestType GetQuestType()
	{
		return questtype;
	}
	public int GetNPCStartID()
	{
		return npcstartid;
	}
	public int GetNPCEndID()
	{
		return npcendid;
	}
	public int GetNPCStartTextID()
	{
		return npcstarttextid;
	}
	public int GetNPCEndTextID()
	{
		return npcendtextid;
	}
	public ArrayList<Integer> GetReqQuestIDs()
	{
		return reqqids;
	}
	public int GetReqMoney()
	{
		return reqmoney;
	}
	public int GetReqLevel()
	{
		return reqlevel;
	}
	public int GetRewardExp()
	{
		return rewardexp;
	}
	public int GetRewardMoney()
	{
		return rewardmoney;
	}
	public String GetTag()
	{
		return tag;
	}
	public boolean GetRecompletable()
	{
		return recompletable;
	}
	
	
	public RPG_Quest(int ID, int NameID, int DisplayNameID, int DescriptionID, RPG_QuestType Type, int NPCStartID, int NPCEndID, int NPCStartTextID, int NPCEndTextID,
			ArrayList<Integer> ReqQuestIDs, int ReqMoney, int ReqLevel, int RewardExp, int RewardMoney, boolean Recompletable)
	{
		this.id = ID;
		this.nameid = NameID;
		this.displaynameid = DisplayNameID;
		this.descrid = DescriptionID;
		this.questtype = Type;
		this.npcstartid = NPCStartID;
		this.npcendid = NPCEndID;
		this.npcstarttextid = NPCStartTextID;
		this.npcendtextid = NPCEndTextID;
		this.reqqids = ReqQuestIDs;
		this.reqmoney = ReqMoney;
		this.reqlevel = ReqLevel;
		this.rewardexp = RewardExp;
		this.rewardmoney = RewardMoney;
		this.recompletable = Recompletable;
	}
	
	public String GetName(RPG_Language Language)
	{
		return RPG_Core.GetTextInLanguage(nameid, Language);
	}
	public String GetDisplayName(RPG_Language Language)
	{
		return RPG_Core.GetTextInLanguage(displaynameid, Language);
	}
	public String GetDescription(RPG_Language Language)
	{
		return RPG_Core.GetTextInLanguage(descrid, Language);
	}
	public String GetNPCStartText(RPG_Player Player)
	{
		return RPG_Core.GetFormattedText(npcstarttextid, Player.GetLanguage(), null, Player, this, null, null);
	}
	public String GetNPCEndText(RPG_Player Player)
	{
		return RPG_Core.GetFormattedText(npcendtextid, Player.GetLanguage(), null, Player, this, null, null);
	}
}

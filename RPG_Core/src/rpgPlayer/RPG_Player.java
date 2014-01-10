package rpgPlayer;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import rpgCore.RPG_Core;
import rpgNpc.RPG_Npc;
import rpgOther.RPG_ChatType;
import rpgOther.RPG_LogType;
import rpgQuest.RPG_Quest;
import rpgTexts.RPG_Language;

public class RPG_Player
{
	private int id;
	private String un;
	private Player p;
	private int nationID;
	private int guildID;
	private int lvl;
	private int xp;
	private int money;
	private RPG_PlayerState state;
	
	private int talknpc;
	private ArrayList<RPG_Quest> qstartable;
	private RPG_ChatType lastChat = RPG_ChatType.Region;
	private boolean chatWorld = true;
	private boolean chatNation = true;
	private boolean chatGuild = true;
	private boolean chatRegion = true;
	private boolean chatPrivate = true;
	private RPG_Language lang = RPG_Language.EN;
	private RPG_EditMode mode = RPG_EditMode.None;
	private int step = -1; 
	private int editid = -1;
	
	public int GetID()
	{
		return id;
	}
	public String GetUsername()
	{
		return un;
	}
	public Player GetPlayer()
	{
		return p;
	}
	public int GetNationId()
	{
		return nationID;
	}
	public int GetGuildId()
	{
		return guildID;
	}
	public int GetLevel()
	{
		return lvl;
	}
	public int GetExp()
	{
		return xp;
	}
	public int GetMoney()
	{
		return money;
	}
	public RPG_PlayerState GetState()
	{
		return state;
	}
	
	public int GetTalking()
	{
		return talknpc;
	}
	public boolean IsTalking()
	{
		if (talknpc > -1)
			return true;
		return false;
	}
	public ArrayList<RPG_Quest> GetStartableQuests()
	{
		return qstartable;
	}
	public boolean GetChatWorld()
	{
		return chatWorld;
	}
	public boolean GetChatNation()
	{
		return chatNation;
	}
	public boolean GetChatGuild()
	{
		return chatGuild;
	}
	public boolean GetChatRegion()
	{
		return chatRegion;
	}
	public boolean GetChatPrivate()
	{
		return chatPrivate;
	}
	public RPG_ChatType GetLastChat()
	{
		return lastChat;
	}
	public RPG_Language GetLanguage()
	{
		return lang;
	}
	public RPG_EditMode GetEditMode()
	{
		return mode;
	}
	public int GetEditStep()
	{
		return step;
	}
	public int GetEditId()
	{
		return editid;
	}
	
	public void SetChatWorld(boolean chat)
	{
		this.chatWorld = chat;
	}
	public void SetChatNation(boolean chat)
	{
		this.chatNation = chat;
	}
	public void SetChatGuild(boolean chat)
	{
		this.chatGuild = chat;
	}
	public void SetChatRegion(boolean chat)
	{
		this.chatRegion = chat;
	}
	public void SetChatPrivate(boolean chat)
	{
		this.chatPrivate = chat;
	}	
	public void SetLastChat(RPG_ChatType type)
	{
		this.lastChat = type;
	}
	public void SetTalking(int npcId)
	{
		this.talknpc = npcId;
		
		RPG_Core.Log(id, npcId, RPG_LogType.Information, "Player", "Talking");
	}
	public void SetStartableQuests(ArrayList<RPG_Quest> startableQuests)
	{
		this.qstartable = startableQuests;
	}
	public void SetLanguage(RPG_Language language)
	{
		this.lang = language;
	}
	public void SetEditMode(RPG_EditMode editMode)
	{
		this.mode = editMode;
		this.step = -1;
		this.editid = -1;
	}
	public void SetEditStep(int step)
	{
		this.step = step;
	}
	public void SetEditId(int id)
	{
		this.editid = id;
	}
	
	
	public RPG_Player(int id, String userName, Player player, int nationId, int guildId, int level, int exp, int money, 
			RPG_Language language, RPG_PlayerState state)
	{
		this.id = id;
		this.un = userName;
		this.p = player;
		this.nationID = nationId;
		this.guildID = guildId;
		this.lvl = level;
		this.xp = exp;
		this.money = money;
		this.lang = language;
		this.state = state;
		this.talknpc = -1;
		this.qstartable = new ArrayList<RPG_Quest>();
		this.lastChat = RPG_ChatType.Region;
	}
	
	public boolean IsSpoutPlayer()
	{
		return RPG_Core.IsSpoutPlayer(p);
	}
	
	public void SendMessage(String message)
	{
		p.sendMessage(message);
	}
	
	public ArrayList<RPG_Quest> GetCompletedQuests()
	{
		return RPG_Core.GetQuestManager().GetCompletedQuestsByPlayer(id);
	}
	public ArrayList<RPG_Quest> GetCurrentQuests()
	{
		return RPG_Core.GetQuestManager().GetCurrentQuestsByPlayer(id);
	}
	
	public RPG_Npc GetTalkingToNpc()
	{
		return RPG_Core.GetNpcManager().GetNPC(talknpc);
	}
	
	public void StartQuest(int questId)
	{
		RPG_Core.PlayerStartsQuest(id, questId);
	}
	public void EndQuest(int questId)
	{
		RPG_Core.PlayerEndsQuest(id, questId);
	}
	
	public void AddMoney(int amount)
	{
		money += amount;
	}
	public void AddExp(int amount)
	{
		xp += amount;
	}
}

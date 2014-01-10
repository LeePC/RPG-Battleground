package rpgInterfaces;

import java.util.ArrayList;
import java.util.HashMap;

import rpgQuest.RPG_Quest;
import rpgQuest.RPG_QuestCreationData;
import rpgQuest.RPG_QuestProgress;
import rpgTexts.RPG_Language;

public interface RPG_IQuestManager
{
	// 
	// Quests
	// 
	public RPG_Quest GetQuest(int ID);
	public ArrayList<RPG_Quest> GetQuests();
	public int AddQuestToDB(RPG_QuestCreationData Data, RPG_Language Language);
	public boolean DeleteQuestFromDB(int ID);
	public ArrayList<RPG_Quest> GetQuestsByNPC(int NPCID);
	public ArrayList<RPG_Quest> GetCompletedQuestsByPlayer(int PlayerID);
	public ArrayList<RPG_Quest> GetCurrentQuestsByPlayer(int PlayerID);
	public void LoadQuestsFromDB();
	public void ReloadQuest(int ID);
	
	// 
	// QuestProgresses
	// 
	public RPG_QuestProgress GetQuestProgressForQuest(int PlayerID, int QuestID);
	public HashMap<Integer, RPG_QuestProgress> GetQuestProgresses(int PlayerID);
	public void LoadQuestProgressesForPlayer(int PlayerID);
	public void UnloadQuestProgressForPlayer(int PlayerID);
}

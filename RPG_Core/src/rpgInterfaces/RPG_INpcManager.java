package rpgInterfaces;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.Location;

import rpgNpc.RPG_Npc;
import rpgNpc.RPG_NpcCreationData;
import rpgNpc.RPG_NpcData;
import rpgNpc.RPG_NpcTemplate;
import rpgTexts.RPG_Language;

public interface RPG_INpcManager
{
	// 
	// NPC
	//
	public boolean IsNPCID(int ID);
	public RPG_Npc GetNPC(int ID);
	public RPG_Npc GetNPC(UUID uuid);
	public RPG_NpcData GetNPCData(int ID);
	public Collection<RPG_Npc> GetNPCs();
	public int GetIDFromUUID(UUID uuid);
	public int AddNPCToDB(RPG_NpcCreationData Data, Location Position, RPG_Language Language);
	public int AddNPCToDB(RPG_NpcTemplate Template, Location Position);
	public boolean RemoveNPCFromDB(int ID);
	public boolean EditNPCProperty(int ID, String Name, String Value);
	public boolean MoveNPC(int ID, Location NewPosition);
	public boolean EditNPCText(int ID, int TextID, RPG_Language Language, String Text);
	public void AddNPC(RPG_Npc NPC);
	public void RemoveNPC(int ID);
	public void SpawnNPCs();
	public void DeSpawnNPCs();
	public void LoadNPCDataFromDB();
	public void LoadNPCsFromData();
	public void ReloadNPCs();
	public void ReloadNPC(int ID);
	
	// 
	// NPC Template
	// 
	public RPG_NpcTemplate GetNPCTemplate(int ID);
	public RPG_NpcTemplate GetNPCTemplate(String TemplateName);
	public Collection<RPG_NpcTemplate> GetTemplates();
	public int AddNPCTemplateToDB(String TemplateName, RPG_NpcCreationData Data, RPG_Language Language);
	public void LoadNPCTemplatesFromDB();
	public void ReloadNPCTemplate(int ID);
}

package rpgInterfaces;

import java.util.Collection;

import org.bukkit.Location;

import rpgBattlefield.RPG_Flag;
import rpgBattlefield.RPG_FlagCreationData;
import rpgBattlefield.RPG_Outpost;
import rpgTexts.RPG_Language;

public interface RPG_IBattlefieldManager
{
	// 
	// Flags
	// 
	public RPG_Flag GetFlag(int ID);
	public Collection<RPG_Flag> GetFlags();
	public void FlagCaptured(int FlagID, int NationID);
	public int AddFlagToDB(RPG_FlagCreationData Data, Location Position, RPG_Language Language);
	public boolean RemoveFlagFromDB(int ID);
	public void LoadFlagsFromDB();
	
	// 
	// Outposts
	// 
	public RPG_Outpost GetOutpost(int ID);
	public Collection<RPG_Outpost> GetOutposts();
	public void LoadOutpostsFromDB();
}

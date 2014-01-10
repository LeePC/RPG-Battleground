package rpgBattlefield;

import org.bukkit.Location;

public class RPG_ProtectedBlock
{
	private Location loc;
	private int hp;
	private boolean destroyable;
	
	public Location GetLocation()
	{
		return loc;
	}
	public int GetHP()
	{
		return hp;
	}
	public boolean GetDestroyable()
	{
		return destroyable;
	}
	
	
	public RPG_ProtectedBlock(Location Loc, int HP, boolean Destroyable)
	{
		this.loc = Loc;
		this.hp = HP;
		this.destroyable = Destroyable;
	}
}

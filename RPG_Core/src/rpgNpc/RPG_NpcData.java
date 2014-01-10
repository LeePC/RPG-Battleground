package rpgNpc;

import org.bukkit.Location;

import rpgCore.RPG_Core;
import rpgPlayer.RPG_Player;
import rpgTexts.RPG_Language;
import rpgTexts.RPG_Text;

public class RPG_NpcData
{
	private int id;
	private String name;
	private int nationid;
	private Location pos;
	private int lvl;
	private int money;
	private int standardtextid;
	private int shopid;
	private String iteminhand;
	private String armorhead;
	private String armorchest;
	private String armorlegs;
	private String armorfeet;
	
	public int GetID()
	{
		return id;
	}
	public String GetName()
	{
		return name;
	}
	public int GetNationID()
	{
		return nationid;
	}
	public Location GetLocation()
	{
		return pos;
	}
	public int GetLevel()
	{
		return lvl;
	}
	public int GetMoney()
	{
		return money;
	}
	public int GetStandardTextID()
	{
		return standardtextid;
	}
	public int GetShopID()
	{
		return shopid;
	}
	public String GetItemInHand()
	{
		return iteminhand;
	}
	public String GetArmorHead()
	{
		return armorhead;
	}
	public String GetArmorChest()
	{
		return armorchest;
	}
	public String GetArmorLegs()
	{
		return armorlegs;
	}
	public String GetArmorFeet()
	{
		return armorfeet;
	}
	
	
	public RPG_NpcData(int id, String name, int nationId, Location position, int level, int money, int standardTextId, int shopId, String itemInHand, 
			String armorHead, String armorChest, String armorLegs, String armorFeet)
	{
		this.id = id;
		this.name = name;
		this.nationid = nationId;
		this.pos = position;
		this.lvl = level;
		this.money = money;
		this.standardtextid = standardTextId;
		this.shopid = shopId;
		this.iteminhand = itemInHand;
		this.armorhead = armorHead;
		this.armorchest = armorChest;
		this.armorlegs = armorLegs;
		this.armorfeet = armorFeet;
	}
	
	public RPG_Text GetStandardText()
	{
		return RPG_Core.GetText(standardtextid);
	}
	public String GetFormattedStandardTextInLanguage(RPG_Language language, RPG_Player player, RPG_Npc npc)
	{
		return RPG_Core.GetFormattedText(standardtextid, language, npc, player, null, null, null);
	}
}

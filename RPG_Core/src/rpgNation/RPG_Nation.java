package rpgNation;

import org.bukkit.Material;

import rpgCore.RPG_Core;
import rpgTexts.RPG_Language;

public class RPG_Nation
{
	private int id;
	private String prefix;
	private String name;
	private int displayNameId;
	private int money;
	private Material blockMaterial;
	
	public int GetID()
	{
		return id;
	}
	public String GetPrefix()
	{
		return prefix;
	}
	public String GetName()
	{
		return name;
	}
	public int GetDisplayNameID()
	{
		return displayNameId;
	}
	public int GetMoney()
	{
		return money;
	}
	public Material GetBlockMaterial()
	{
		return blockMaterial;
	}
	
	
	public RPG_Nation(int id, String prefix, String name, int displayNameId, int money, Material blockMaterial)
	{
		this.id = id;
		this.prefix = prefix;
		this.name = name;
		this.displayNameId = displayNameId;
		this.money = money;
		this.blockMaterial = blockMaterial;
	}
	
	public String GetDisplayName(RPG_Language Language)
	{
		return RPG_Core.GetTextInLanguage(displayNameId, Language);
	}
}

package rpgNpc;

public class RPG_NpcTemplate
{
	private int id;
	private String templatename;
	private String npcname;
	private int nationid;
	private int lvl;
	private int money;
	private int textid;
	private int shopid;
	private String item;
	private String head;
	private String chest;
	private String legs;
	private String feet;
	
	public int GetID()
	{
		return id;
	}
	public String GetTemplateName()
	{
		return templatename;
	}
	public String GetName()
	{
		return npcname;
	}
	public int GetNationID()
	{
		return nationid;
	}
	public int GetLevel()
	{
		return lvl;
	}
	public int GetMoney()
	{
		return money;
	}
	public int GetTextID()
	{
		return textid;
	}
	public int GetShopID()
	{
		return shopid;
	}
	public String GetItemInHand()
	{
		return item;
	}
	public String GetArmorHead()
	{
		return head;
	}
	public String GetArmorChest()
	{
		return chest;
	}
	public String GetArmorLegs()
	{
		return legs;
	}
	public String GetArmorFeet()
	{
		return feet;
	}
	
	
	public RPG_NpcTemplate(int ID, String TemplateName, String NPCName, int NationID, int Level, int Money, int TextID, int ShopID, 
			String ItemInHand, String ArmorHead, String ArmorChest, String ArmorLegs, String ArmorFeet)
	{
		this.id = ID;
		this.templatename = TemplateName;
		this.npcname = NPCName;
		this.nationid = NationID;
		this.lvl = Level;
		this.money = Money;
		this.textid = TextID;
		this.shopid = ShopID;
		this.item = ItemInHand;
		this.head = ArmorHead;
		this.chest = ArmorChest;
		this.legs = ArmorLegs;
		this.feet = ArmorFeet;
	}
}

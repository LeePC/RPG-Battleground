package rpgGuild;

public class RPG_Guild
{
	private int id;
	private String prefix;
	private String name;
	private String description;
	private int founderID;
	
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
	public String GetDescription()
	{
		return description;
	}
	public int GetFounderID()
	{
		return founderID;
	}
	
	
	public RPG_Guild(int ID, String Prefix, String Name, String Description, int FounderID)
	{
		this.id = ID;
		this.prefix = Prefix;
		this.name = Name;
		this.description = Description;
		this.founderID = FounderID;
	}
}

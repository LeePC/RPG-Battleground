package rpgGUI;

public class RPG_InfoScreenItem
{
	public String Name;
	public String Description;
	public String Perms;
	public String LinkScreen;
	
	
	public RPG_InfoScreenItem(String Name, String Description)
	{
		this.Name = Name;
		this.Description = Description;
	}
	public RPG_InfoScreenItem(String Name, String Description, String Perms)
	{
		this(Name, Description);
		
		this.Perms = Perms;
	}
	public RPG_InfoScreenItem(String Name, String Description, String Perms, boolean HasLink)
	{
		this(Name, Description, Perms);
		
		if (HasLink)
			this.LinkScreen = Perms;
	}
}

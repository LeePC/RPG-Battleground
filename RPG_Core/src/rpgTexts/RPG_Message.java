package rpgTexts;

public class RPG_Message
{
	private String name;
	private String de;
	private String en;
	private String fr;
	
	public String GetName()
	{
		return name;
	}
	public String GetDE()
	{
		return de;
	}
	public String GetEN()
	{
		return en;
	}
	public String GetFR()
	{
		return fr;
	}
	
	
	public RPG_Message (String Name, String DE, String EN, String FR)
	{
		this.name = Name;
		this.de = DE;
		this.en = EN;
		this.fr = FR;
	}
}

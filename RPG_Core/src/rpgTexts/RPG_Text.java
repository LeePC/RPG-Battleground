package rpgTexts;

public class RPG_Text
{
	private int id;
	private String de;
	private String en;
	private String fr;
	
	public int GetID()
	{
		return id;
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
	
	
	public RPG_Text(int ID, String DE, String EN, String FR)
	{
		this.id = ID;
		this.de = DE;
		this.en = EN;
		this.fr = FR;
	}
}

package rpgWorld;

public class RPG_World
{
	private int id;
	private String name;
	private boolean loaded;
	private boolean loadonstart;
	
	public int GetID()
	{
		return id;
	}
	public String GetName()
	{
		return name;
	}
	public boolean GetLoadOnStart()
	{
		return loadonstart;
	}
	public boolean IsLoaded()
	{
		return loaded;
	}
	
	public void SetLoaded(boolean Loaded)
	{
		this.loaded = Loaded;
	}
	
	
	public RPG_World(int ID, String Name, boolean Loaded, boolean LoadOnStart)
	{
		this.id = ID;
		this.name = Name;
		this.loaded = Loaded;
		this.loadonstart = LoadOnStart;
	}
}

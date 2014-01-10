package rpgQuest;

public class RPG_QuestRequest
{
	private int id;
	private int amount;
	
	public int GetID()
	{
		return id;
	}
	public int GetAmount()
	{
		return amount;
	}
	
	
	public RPG_QuestRequest(int ID, int Amount)
	{
		this.id = ID;
		this.amount = Amount;
	}
}

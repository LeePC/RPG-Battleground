package rpgGUI;

import java.util.ArrayList;

public class RPG_InfoScreenData
{
	public String Title;
	public ArrayList<RPG_InfoScreenItem> Items;
	
	
	public RPG_InfoScreenData(String Title, ArrayList<RPG_InfoScreenItem> Items)
	{
		this.Title = Title;
		this.Items = Items;
	}
}

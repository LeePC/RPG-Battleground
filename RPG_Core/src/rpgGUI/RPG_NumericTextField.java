package rpgGUI;

import org.getspout.spoutapi.event.screen.TextFieldChangeEvent;
import org.getspout.spoutapi.gui.GenericTextField;

public class RPG_NumericTextField extends GenericTextField
{
	public int GetValue()
	{
		if (text.equalsIgnoreCase(""))
			return 0;
		
		return Integer.parseInt(text);
	}
	
	
	public RPG_NumericTextField()
	{
		super();	
	}
	
	@Override
	public void onTextFieldChange(TextFieldChangeEvent e)
	{
		if (e.getNewText().equalsIgnoreCase(""))
			return;
		
		try
		{
			Integer.parseInt(e.getNewText());
		}
		catch (Exception ex)
		{
			e.setCancelled(true);
		}
	}
}

package rpgOther;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import rpgCore.RPG_Core;

public class RPG_EventExecutor implements EventExecutor
{
	@Override
	public void execute(Listener arg0, Event arg1) throws EventException
	{
		try
		{
			Method[] methods;
			if (RPG_Core.IsSpoutEnabled())
				methods = arg0.getClass().getDeclaredMethods();
			else
				methods = arg0.getClass().getMethods();
			
			for (Method m : methods)
			{
				if (m.getParameterTypes().length > 0 && m.getParameterTypes()[0] == arg1.getClass())
				{
					m.invoke(arg0, arg1);
					break;
				}
			}
		}
		catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{ e.printStackTrace(); }
	}
}

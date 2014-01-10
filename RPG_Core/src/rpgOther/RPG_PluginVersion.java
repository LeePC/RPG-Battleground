package rpgOther;

public class RPG_PluginVersion
{
	private int major = 0;
	private int minor = 0;
	private int build = 0;
	
	public int GetMajor()
	{
		return major;
	}
	public int GetMinor()
	{
		return minor;
	}
	public int GetBuild()
	{
		return build;
	}
	public String GetVersion()
	{
		return major + "." + minor + "." + build;
	}
	
	
	public RPG_PluginVersion(int Major, int Minor, int Build)
	{
		this.major = Major;
		this.minor = Minor;
		this.build = Build;
	}
	public RPG_PluginVersion(String Version)
	{		
		try
		{
			String[] splits = Version.split("\\.");
			this.major = Integer.parseInt(splits[0]);
			this.minor = Integer.parseInt(splits[1]);
			this.build = Integer.parseInt(splits[2]);
		}
		catch (Exception ex)
		{ }
	}
	
	public boolean IsLarger(RPG_PluginVersion Version2)
	{
		if (major > Version2.GetMajor())
			return true;
		else if (major == Version2.GetMajor())
		{
			if (minor > Version2.GetMinor())
				return true;
			else if (minor == Version2.GetMinor())
			{
				if (build > Version2.GetBuild())
					return true;
				
				return false;
			}
			
			return false;
		}
		
		return false;
	}
	public static boolean IsLarger(RPG_PluginVersion Version1, RPG_PluginVersion Version2)
	{
		if (Version1.GetMajor() > Version2.GetMajor())
			return true;
		else if (Version1.GetMajor() == Version2.GetMajor())
		{
			if (Version1.GetMinor() > Version2.GetMinor())
				return true;
			else if (Version1.GetMinor() == Version2.GetMinor())
			{
				if (Version1.GetBuild() > Version2.GetBuild())
					return true;
				
				return false;
			}
			
			return false;
		}
		
		return false;
	}
}

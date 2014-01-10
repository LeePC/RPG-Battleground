package rpgOther;

import org.bukkit.Location;
import org.bukkit.World;

public class RPG_Region
{
	private World world;
	private double[] min;
	private double[] max;
	
	public World GetWorld()
	{
		return world;
	}
	public double GetMinX()
	{
		return min[0];
	}
	public double GetMinY()
	{
		return min[1];
	}
	public double GetMinZ()
	{
		return min[2];
	}
	public double GetMaxX()
	{
		return max[0];
	}
	public double GetMaxY()
	{
		return max[1];
	}
	public double GetMaxZ()
	{
		return max[2];
	}
	public Location GetMin()
	{
		return new Location(world, min[0], min[1], min[2]);
	}
	public Location GetMax()
	{
		return new Location(world, max[0], max[1], max[2]);
	}
	
	
	public RPG_Region(Location Min, Location Max)
	{
		if (Min.getWorld() != Max.getWorld())
			return;
		
		min = new double[3];
		max = new double[3];
		
		world = Min.getWorld();
		
		if (Min.getX() < Max.getX())
		{
			min[0] = Min.getX();
			max[0] = Max.getX();
		}
		else
		{
			min[0] = Max.getX();
			max[0] = Min.getX();
		}
		
		if (Min.getY() < Max.getY())
		{
			min[1] = Min.getY();
			max[1] = Max.getY();
		}
		else
		{
			min[1] = Max.getY();
			max[1] = Min.getY();
		}
		
		if (Min.getZ() < Max.getZ())
		{
			min[2] = Min.getZ();
			max[2] = Max.getZ();
		}
		else
		{
			min[2] = Max.getZ();
			max[2] = Min.getZ();
		}
	}
	
	public double[] GetDimensions()
	{
		double[] diff = new double[3];
		diff[0] = max[0] - min[0];
		diff[1] = max[1] - min[1];
		diff[2] = max[2] - min[2];
		return diff;
	}
	public double GetDimensionX()
	{
		return max[0] - min[0];
	}
	public double GetDimensionY()
	{
		return max[1] - min[1];
	}
	public double GetDimensionZ()
	{
		return max[2] - min[2];
	}
}

package  l2s.Phantoms.objects;

public class ConfigLevelGroup
{
	public int _level_min;
	public int _level_max;
	public int _min;
	public int _max;
	
	public ConfigLevelGroup(int level_min,int level_max,int min,int max)
	{
		_level_min = level_min;
		_level_max = level_max;
		_min = min;
		_max = max;
	}
}

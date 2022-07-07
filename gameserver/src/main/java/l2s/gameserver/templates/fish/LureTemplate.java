package l2s.gameserver.templates.fish;

import java.util.ArrayList;
import java.util.List;

public final class LureTemplate
{
	private final int _id;
	private final int _durationMin;
	private final int _durationMax;
	private final List<FishTemplate> _fishes = new ArrayList<>();

	public LureTemplate(int id, double failChance, int durationMin, int durationMax)
	{
		_id = id;
		_durationMin = durationMin;
		_durationMax = durationMax;
		if(failChance > 0.0)
			_fishes.add(new FishTemplate(0, failChance, 0));
	}

	public int getId()
	{
		return _id;
	}

	public void addFish(FishTemplate fish)
	{
		_fishes.add(fish);
	}

	public List<FishTemplate> getFishes()
	{
		return _fishes;
	}

	public int getDurationMin()
	{
		return _durationMin;
	}

	public int getDurationMax()
	{
		return _durationMax;
	}
}

package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.item.data.AttendanceRewardData;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

import java.util.Collection;

public final class AttendanceRewardHolder extends AbstractHolder
{
	private static final AttendanceRewardHolder _instance;
	private final IntObjectMap<AttendanceRewardData> _normalRewards;
	private final IntObjectMap<AttendanceRewardData> _premiumRewards;

	public AttendanceRewardHolder()
	{
		_normalRewards = new TreeIntObjectMap<>();
		_premiumRewards = new TreeIntObjectMap<>();
	}

	public static AttendanceRewardHolder getInstance()
	{
		return _instance;
	}

	public void addNormalReward(AttendanceRewardData reward)
	{
		_normalRewards.put(_normalRewards.size() + 1, reward);
	}

	public void addPremiumReward(AttendanceRewardData reward)
	{
		_premiumRewards.put(_premiumRewards.size() + 1, reward);
	}

	public Collection<AttendanceRewardData> getRewards(boolean premium)
	{
		return premium ? _premiumRewards.values() : _normalRewards.values();
	}

	public AttendanceRewardData getReward(int index, boolean premium)
	{
		return premium ? _premiumRewards.get(index) : _normalRewards.get(index);
	}

	@Override
	public int size()
	{
		return _normalRewards.size() + _premiumRewards.size();
	}

	@Override
	public void clear()
	{
		_normalRewards.clear();
		_premiumRewards.clear();
	}

	static
	{
		_instance = new AttendanceRewardHolder();
	}
}

package l2s.gameserver.model.actor.instances.player;

import com.google.common.flogger.FluentLogger;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.CharacterHennaDAO;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.BaseStats;
import l2s.gameserver.network.l2.s2c.ExPeriodicHenna;
import l2s.gameserver.network.l2.s2c.HennaInfoPacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.HennaTemplate;
import l2s.gameserver.utils.ItemFunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @author Bonux
**/
public class HennaList
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	public static final int MAX_SIZE = 3;

	private static final int MAX_STAT_VALUE = 15;

	private List<Henna> _hennaList = Collections.emptyList();
	private Henna _premiumHenna = null;

	private final Map<BaseStats, Integer> hennaBaseStats = new ConcurrentHashMap<>();

	private Future<?> _removeTask;

	private final Player _owner;
	private final TIntObjectMap<SkillEntry> _skills = new TIntObjectHashMap<SkillEntry>();

	public HennaList(Player owner)
	{
		_owner = owner;
	}

	public void restore()
	{
		_hennaList = new ArrayList<Henna>();
		_premiumHenna = null;

		List<Henna> hennas = CharacterHennaDAO.getInstance().select(_owner);
		for(Henna henna : hennas)
		{
			if(henna.isPremium())
			{
				if(_premiumHenna != null)
					_log.atWarning().log( "%s: Contains more than one premium henna!", this );

				if(henna.getTemplate().getPeriod() == 0)
					_log.atWarning().log( "%s: Contains no premium henna in premium slot!", this );

				_premiumHenna = henna;
			}
			else
				_hennaList.add(henna);
		}

		Collections.sort(_hennaList);

		if(_hennaList.size() > MAX_SIZE)
		{
			_log.atWarning().log( "%s: Contains more than three henna\'s!", this );

			for(int i = MAX_SIZE; i < _hennaList.size(); i++)
				_hennaList.remove(i);
		}

		refreshStats(false);
		_owner.sendPacket(new HennaInfoPacket(_owner));

		stopHennaRemoveTask();
		startHennaRemoveTask();
	}

	public Henna get(int symbolId)
	{
		for(Henna henna : values(true))
		{
			if(henna.getTemplate().getSymbolId() == symbolId)
				return henna;
		}
		return null;
	}

	public int size()
	{
		return _hennaList.size();
	}

	public int getFreeSize()
	{
		return Math.max(0, MAX_SIZE - size());
	}

	public Henna[] values(boolean withPremium)
	{
		if(!withPremium)
			return _hennaList.toArray(new Henna[_hennaList.size()]);

		List<Henna> hennas = new ArrayList<Henna>(_hennaList);
		if(_premiumHenna != null)
			hennas.add(_premiumHenna);

		return hennas.toArray(new Henna[hennas.size()]);
	}

	public Henna getPremiumHenna()
	{
		return _premiumHenna;
	}

	public boolean isFull()
	{
		return getFreeSize() == 0;
	}

	public boolean canAdd(Henna henna)
	{
		if(!henna.isPremium() && isFull())
			return false;

		if(henna.isPremium() && (!Config.EX_USE_PREMIUM_HENNA_SLOT || _premiumHenna != null))
			return false;

		return true;
	}

	public boolean add(Henna henna)
	{
		if(!canAdd(henna))
			return false;

		if(CharacterHennaDAO.getInstance().insert(_owner, henna))
		{
			if(!henna.isPremium())
			{
				_hennaList.add(henna);
				Collections.sort(_hennaList);
			}
			else
				_premiumHenna = henna;

			if(refreshStats(true))
				_owner.sendSkillList();

			return true;
		}
		return false;
	}

	public boolean remove(Henna henna)
	{
		if(!remove0(henna))
			return false;

		if(refreshStats(true))
			_owner.sendSkillList();

		if(!henna.isPremium())
		{
			long removeCount = henna.getTemplate().getRemoveCount();
			if(removeCount > 0)
				ItemFunctions.addItem(_owner, henna.getTemplate().getDyeId(), henna.getTemplate().getRemoveCount(), true);
		}

		return true;
	}

	private boolean remove0(Henna henna)
	{
		if(!_hennaList.remove(henna))
		{
			if(_premiumHenna != henna)
				return false;

			stopHennaRemoveTask();
			_premiumHenna = null;
		}

		Collections.sort(_hennaList);

		return CharacterHennaDAO.getInstance().delete(_owner, henna);
	}

	public boolean isActive(Henna henna)
	{
		if(!henna.getTemplate().isForThisClass(_owner))
			return false;

		if(henna.isPremium() && !Config.EX_USE_PREMIUM_HENNA_SLOT)
			return false;

		return true;
	}

	public boolean refreshStats(boolean send)
	{
		hennaBaseStats.clear();

		boolean updateSkillList = false;
		for(int skillId : _skills.keys())
		{
			if(_owner.removeSkill(skillId, false) != null)
				updateSkillList = true;
		}

		_skills.clear();

		for(Henna henna : values(true))
		{
			if(!isActive(henna))
				continue;

			HennaTemplate template = henna.getTemplate();

			template.getBaseStats().forEach((key, value) ->
					hennaBaseStats.merge(key, value, Integer::sum));

			for(TIntIntIterator iterator = template.getSkills().iterator(); iterator.hasNext();)
			{
				iterator.advance();

				SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, iterator.key(), iterator.value());
				if(skillEntry == null)
					continue;

				SkillEntry tempSkillEntry = _skills.get(skillEntry.getId());
				if(tempSkillEntry == null || tempSkillEntry.getLevel() < skillEntry.getLevel())
					_skills.put(skillEntry.getId(), skillEntry);
			}
		}

		for(SkillEntry skillEntry : _skills.valueCollection())
			_owner.addSkill(skillEntry, false);

		if(!_skills.isEmpty())
			updateSkillList = true;

		hennaBaseStats.entrySet().forEach(entry -> {
			if (entry.getValue() > MAX_STAT_VALUE) {
				entry.setValue(MAX_STAT_VALUE);
			}
		});

		if(send)
		{
			_owner.sendPacket(new HennaInfoPacket(_owner));
			_owner.sendUserInfo(true);
		}

		return updateSkillList;
	}

	public void stopHennaRemoveTask()
	{
		if(_removeTask != null)
		{
			_removeTask.cancel(false);
			_removeTask = null;
		}
	}

	private void startHennaRemoveTask()
	{
		if(_premiumHenna == null)
			return;

		if(_removeTask != null)
			return;

		_removeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> 
		{
			if(_premiumHenna.getLeftTime() <= 0)
			{
				if(remove0(_premiumHenna))
				{
					if(refreshStats(true))
						_owner.sendSkillList();
				}
			}
			else
				_owner.sendPacket(new ExPeriodicHenna(_owner));
		}, 0, 60000L);
	}

	/**
	 * @param stat
	 * @return the henna bonus of specified base stat
	 */
	public int getValue(BaseStats stat)
	{
		return hennaBaseStats.getOrDefault(stat, 0);
	}

	@Override
	public String toString()
	{
		return "HennaList[owner=" + _owner.getName() + "]";
	}
}

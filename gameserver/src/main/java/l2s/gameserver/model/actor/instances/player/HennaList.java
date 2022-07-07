package l2s.gameserver.model.actor.instances.player;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.CharacterHennaDAO;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExPeriodicHenna;
import l2s.gameserver.network.l2.s2c.HennaInfoPacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.HennaTemplate;
import l2s.gameserver.utils.ItemFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

public class HennaList
{
	public static final int MAX_SIZE = 3;
	private static final Logger _log;
	private static final int MAX_STAT_VALUE = 15;
	private List<Henna> _hennaList;
	private Henna _premiumHenna;
	private int _str;
	private int _int;
	private int _dex;
	private int _men;
	private int _wit;
	private int _con;
	private Future<?> _removeTask;
	private final Player _owner;
	private final TIntObjectMap<SkillEntry> _skills;

	public HennaList(Player owner)
	{
		_hennaList = Collections.emptyList();
		_premiumHenna = null;
		_skills = new TIntObjectHashMap<>();
		_owner = owner;
	}

	public void restore()
	{
		_hennaList = new ArrayList<>();
		_premiumHenna = null;
		List<Henna> hennas = CharacterHennaDAO.getInstance().select(_owner);
		for(Henna henna : hennas)
			if(henna.isPremium())
			{
				if(_premiumHenna != null)
					_log.warn(this + ": Contains more than one premium henna!");
				if(henna.getTemplate().getPeriod() == 0)
					_log.warn(this + ": Contains no premium henna in premium slot!");
				_premiumHenna = henna;
			}
			else
				_hennaList.add(henna);
		Collections.sort(_hennaList);
		if(_hennaList.size() > 3)
		{
			_log.warn(this + ": Contains more than three henna's!");
			for(int i = 3; i < _hennaList.size(); ++i)
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
			if(henna.getTemplate().getSymbolId() == symbolId)
				return henna;
		return null;
	}

	public int size()
	{
		return _hennaList.size();
	}

	public int getFreeSize()
	{
		return Math.max(0, 3 - size());
	}

	public Henna[] values(boolean withPremium)
	{
		if(!withPremium)
			return _hennaList.toArray(new Henna[0]);
		List<Henna> hennas = new ArrayList<>(_hennaList);
		if(_premiumHenna != null)
			hennas.add(_premiumHenna);
		return hennas.toArray(new Henna[0]);
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
		return (henna.isPremium() || !isFull()) && (!henna.isPremium() || Config.EX_USE_PREMIUM_HENNA_SLOT && _premiumHenna == null);
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
			if(removeCount > 0L)
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
		return henna.getTemplate().isForThisClass(_owner) && (!henna.isPremium() || Config.EX_USE_PREMIUM_HENNA_SLOT);
	}

	public boolean refreshStats(boolean send)
	{
		_int = 0;
		_str = 0;
		_con = 0;
		_men = 0;
		_wit = 0;
		_dex = 0;
		boolean updateSkillList = false;
		for(int skillId : _skills.keys())
			if(_owner.removeSkill(skillId, false) != null)
				updateSkillList = true;
		_skills.clear();
		for(Henna henna : values(true))
			if(isActive(henna))
			{
				HennaTemplate template = henna.getTemplate();
				_int += template.getStatINT();
				_str += template.getStatSTR();
				_men += template.getStatMEN();
				_con += template.getStatCON();
				_wit += template.getStatWIT();
				_dex += template.getStatDEX();
				TIntIntIterator iterator = template.getSkills().iterator();
				while(iterator.hasNext())
				{
					iterator.advance();
					SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(iterator.key(), iterator.value());
					if(skillEntry == null)
						continue;
					SkillEntry tempSkillEntry = _skills.get(skillEntry.getId());
					if(tempSkillEntry != null && tempSkillEntry.getLevel() >= skillEntry.getLevel())
						continue;
					_skills.put(skillEntry.getId(), skillEntry);
				}
			}
		for(SkillEntry skillEntry2 : _skills.valueCollection())
			_owner.addSkill(skillEntry2, false);
		if(!_skills.isEmpty())
			updateSkillList = true;
		_int = Math.min(_int, 15);
		_str = Math.min(_str, 15);
		_con = Math.min(_con, 15);
		_men = Math.min(_men, 15);
		_wit = Math.min(_wit, 15);
		_dex = Math.min(_dex, 15);
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
		_removeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if(_premiumHenna.getLeftTime() <= 0)
			{
				if(remove0(_premiumHenna) && refreshStats(true))
					_owner.sendSkillList();
			}
			else
				_owner.sendPacket(new ExPeriodicHenna(_owner));
		}, 0L, 60000L);
	}

	public int getINT()
	{
		return _int;
	}

	public int getSTR()
	{
		return _str;
	}

	public int getCON()
	{
		return _con;
	}

	public int getMEN()
	{
		return _men;
	}

	public int getWIT()
	{
		return _wit;
	}

	public int getDEX()
	{
		return _dex;
	}

	@Override
	public String toString()
	{
		return "HennaList[owner=" + _owner.getName() + "]";
	}

	static
	{
		_log = LoggerFactory.getLogger(HennaList.class);
	}
}

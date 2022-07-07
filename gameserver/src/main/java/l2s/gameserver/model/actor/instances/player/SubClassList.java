package l2s.gameserver.model.actor.instances.player;

import l2s.gameserver.dao.CharacterSubclassDAO;
import l2s.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

public class SubClassList
{
	private static final Logger _log;
	public static final int MAX_SUB_COUNT = 6;
	private final TreeMap<Integer, SubClass> _listByIndex;
	private final TreeMap<Integer, SubClass> _listByClassId;
	private final Player _owner;
	private SubClass _baseSubClass;
	private SubClass _activeSubClass;

	public SubClassList(Player owner)
	{
		_listByIndex = new TreeMap<>();
		_listByClassId = new TreeMap<>();
		_baseSubClass = null;
		_activeSubClass = null;
		_owner = owner;
	}

	public void restore()
	{
		_listByIndex.clear();
		_listByClassId.clear();
		List<SubClass> subclasses = CharacterSubclassDAO.getInstance().restore(_owner);
		int index = 2;

		for(SubClass sub : subclasses)
		{
			if(sub == null)
				continue;
			if(size() >= MAX_SUB_COUNT)
			{
				_log.warn("SubClassList:restore: Limit is subclass! Player: " + _owner.getName() + "(" + _owner.getObjectId() + ")");
				break;
			}
			if(sub.isActive())
				_activeSubClass = sub;
			if(sub.isBase())
				(_baseSubClass = sub).setIndex(1);
			else
			{
				sub.setIndex(index);
				++index;
			}
			if(_listByIndex.containsKey(sub.getIndex()))
				_log.warn("SubClassList:restore: Duplicate index in player subclasses! Player: " + _owner.getName() + "(" + _owner.getObjectId() + ")");
			_listByIndex.put(sub.getIndex(), sub);
			if(_listByClassId.containsKey(sub.getClassId()))
				_log.warn("SubClassList:restore: Duplicate class_id in player subclasses! Player: " + _owner.getName() + "(" + _owner.getObjectId() + ")");
			_listByClassId.put(sub.getClassId(), sub);
		}
		if(_listByIndex.size() != _listByClassId.size())
			_log.warn("SubClassList:restore: The size of the lists do not match! Player: " + _owner.getName() + "(" + _owner.getObjectId() + ")");
	}

	public Collection<SubClass> values()
	{
		return _listByIndex.values();
	}

	public SubClass getByClassId(int classId)
	{
		return _listByClassId.get(classId);
	}

	public SubClass getByIndex(int index)
	{
		return _listByIndex.get(index);
	}

	public void removeByClassId(int classId)
	{
		if(!_listByClassId.containsKey(classId))
			return;
		int index = _listByClassId.get(classId).getIndex();
		_listByIndex.remove(index);
		_listByClassId.remove(classId);
	}

	public SubClass getActiveSubClass()
	{
		return _activeSubClass;
	}

	public SubClass getBaseSubClass()
	{
		return _baseSubClass;
	}

	public boolean isBaseClassActive()
	{
		return _activeSubClass == _baseSubClass;
	}

	public boolean haveSubClasses()
	{
		return size() > 1;
	}

	public boolean changeSubClassId(int oldClassId, int newClassId)
	{
		if(!_listByClassId.containsKey(oldClassId))
			return false;
		if(_listByClassId.containsKey(newClassId))
			return false;
		SubClass sub = _listByClassId.get(oldClassId);
		sub.setClassId(newClassId);
		_listByClassId.remove(oldClassId);
		_listByClassId.put(sub.getClassId(), sub);
		return true;
	}

	public boolean add(SubClass sub)
	{
		if(sub == null)
			return false;
		if(size() >= MAX_SUB_COUNT)
			return false;
		if(_listByClassId.containsKey(sub.getClassId()))
			return false;
		int index;
		for(index = 1; _listByIndex.containsKey(index); ++index)
		{}
		sub.setIndex(index);
		_listByIndex.put(sub.getIndex(), sub);
		_listByClassId.put(sub.getClassId(), sub);
		return true;
	}

	public SubClass changeActiveSubClass(int classId)
	{
		if(!_listByClassId.containsKey(classId))
			return null;
		if(_activeSubClass == null)
			return null;
		_activeSubClass.setActive(false);
		SubClass sub = _listByClassId.get(classId);
		sub.setActive(true);
		return _activeSubClass = sub;
	}

	public boolean containsClassId(int classId)
	{
		return _listByClassId.containsKey(classId);
	}

	public int size()
	{
		return _listByIndex.size();
	}

	@Override
	public String toString()
	{
		return "SubClassList[owner=" + _owner.getName() + "]";
	}

	static
	{
		_log = LoggerFactory.getLogger(SubClassList.class);
	}

	public TreeMap<Integer, SubClass> getListByClassId()
	{
		return _listByClassId;
	}

	//TODO
	public void restorePhantom(List<SubClass> subclasses)
	{
		_listByIndex.clear();
		_listByClassId.clear();
		int index = 2;

		for(SubClass sub : subclasses)
		{
			if(sub == null)
				continue;
			if(size() >= MAX_SUB_COUNT)
			{
				_log.warn("SubClassList:restore: Limit is subclass! Player: " + _owner.getName() + "(" + _owner.getObjectId() + ")");
				break;
			}
			if(sub.isActive())
				_activeSubClass = sub;
			if(sub.isBase())
				(_baseSubClass = sub).setIndex(1);
			else
			{
				sub.setIndex(index);
				++index;
			}
			if(_listByIndex.containsKey(sub.getIndex()))
				_log.warn("SubClassList:restore: Duplicate index in player subclasses! Player: " + _owner.getName() + "(" + _owner.getObjectId() + ")");
			_listByIndex.put(sub.getIndex(), sub);
			if(_listByClassId.containsKey(sub.getClassId()))
				_log.warn("SubClassList:restore: Duplicate class_id in player subclasses! Player: " + _owner.getName() + "(" + _owner.getObjectId() + ")");
			_listByClassId.put(sub.getClassId(), sub);
		}
		if(_listByIndex.size() != _listByClassId.size())
			_log.warn("SubClassList:restore: The size of the lists do not match! Player: " + _owner.getName() + "(" + _owner.getObjectId() + ")");
	}
}

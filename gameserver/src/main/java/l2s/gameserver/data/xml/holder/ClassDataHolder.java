package l2s.gameserver.data.xml.holder;

import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.player.ClassData;

public final class ClassDataHolder extends AbstractHolder
{
	private static final ClassDataHolder _instance;
	private final TIntObjectHashMap<ClassData> _classDataList;

	public ClassDataHolder()
	{
		_classDataList = new TIntObjectHashMap();
	}

	public static ClassDataHolder getInstance()
	{
		return _instance;
	}

	public void addClassData(ClassData classData)
	{
		_classDataList.put(classData.getClassId(), classData);
	}

	public ClassData getClassData(int classId)
	{
		return _classDataList.get(classId);
	}

	@Override
	public int size()
	{
		return _classDataList.size();
	}

	@Override
	public void clear()
	{
		_classDataList.clear();
	}

	static
	{
		_instance = new ClassDataHolder();
	}
}

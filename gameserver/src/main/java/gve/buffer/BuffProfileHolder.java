package gve.buffer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class BuffProfileHolder
{
	private final List<Integer> buffs;
	private String name;

	public BuffProfileHolder(String name, List<Integer> buffs)
	{
		this.name = name;
		this.buffs = new CopyOnWriteArrayList<>(buffs);
	}

	public void setName(String newName)
	{
		name = newName;
	}

	public boolean isDefault(String name)
	{
		return this.name.equals(name);
	}

	public boolean hasBuff(int id)
	{
		return buffs.contains(id);
	}

	public boolean addBuff(int id)
	{
		return buffs.add(id);
	}

	public String getName()
	{
		return name;
	}

	public List<Integer> getBuffs()
	{
		return Collections.unmodifiableList(buffs);
	}

	public int buffsCount()
	{
		return buffs.size();
	}

	public void clear()
	{
		buffs.clear();
	}

	public void removeBuff(int id)
	{
		if (buffs.contains(id))
			buffs.remove(Integer.valueOf(id));
	}

	public void removeBuffs(Collection<Integer> ids)
	{
		buffs.removeAll(ids);
	}
}

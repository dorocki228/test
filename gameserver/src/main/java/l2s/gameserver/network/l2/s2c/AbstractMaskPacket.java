package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.s2c.updatetype.IUpdateTypeComponent;

public abstract class AbstractMaskPacket<T extends IUpdateTypeComponent> extends L2GameServerPacket
{
	protected static final byte[] DEFAULT_FLAG_ARRAY;

	protected abstract byte[] getMasks();

	protected abstract void onNewMaskAdded(T p0);

	public void addComponentType(T... updateComponents)
	{
		for(T component : updateComponents)
			if(!containsMask(component))
			{
				byte[] masks = getMasks();
				int n = component.getMask() >> 3;
				masks[n] |= DEFAULT_FLAG_ARRAY[component.getMask() & 0x7];
                onNewMaskAdded(component);
			}
	}

	public boolean containsMask(T component)
	{
		return containsMask(component.getMask());
	}

	public boolean containsMask(int mask)
	{
		return (getMasks()[mask >> 3] & DEFAULT_FLAG_ARRAY[mask & 0x7]) != 0x0;
	}

	static
	{
		DEFAULT_FLAG_ARRAY = new byte[] { -128, 64, 32, 16, 8, 4, 2, 1 };
	}
}

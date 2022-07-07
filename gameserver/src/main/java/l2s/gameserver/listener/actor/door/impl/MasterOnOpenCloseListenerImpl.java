package l2s.gameserver.listener.actor.door.impl;

import l2s.gameserver.listener.actor.door.OnOpenCloseListener;
import l2s.gameserver.model.instances.DoorInstance;

public class MasterOnOpenCloseListenerImpl implements OnOpenCloseListener
{
	private final DoorInstance _door;

	public MasterOnOpenCloseListenerImpl(DoorInstance door)
	{
		_door = door;
	}

	@Override
	public void onOpen(DoorInstance doorInstance)
	{
		_door.openMe();
	}

	@Override
	public void onClose(DoorInstance doorInstance)
	{
		_door.closeMe();
	}
}

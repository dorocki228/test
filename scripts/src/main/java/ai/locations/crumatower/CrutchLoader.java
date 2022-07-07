package ai.locations.crumatower;

import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.actor.listener.NpcListenerList;

/**
 * @author KanuToIIIKa
 */

public class CrutchLoader implements OnInitScriptListener
{

	@Override
	public void onInit()
	{
		NpcListenerList.addGlobal(new FirstFloorCrutch());
		NpcListenerList.addGlobal(new SecondFloorCrutch());
	}

}

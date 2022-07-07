package l2s.Phantoms.ai.tasks.other;

import l2s.Phantoms.Utils.PhantomUtils;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;

public class SoeTask extends RunnableImpl
{
	public Player phantom;
	boolean _forcibly;
	Skill _skill;

	public SoeTask(Player ph, boolean forcibly, Skill skill)
	{
		phantom = ph;
		_forcibly = forcibly;
		_skill = skill;
	}

	@Override
	public void runImpl()
	{
		if(phantom == null/* || phantom.isInPvPEvent()*/)
			return; //2013
		/*if (!phantom.isInPeaceZone())
		{
			if (!phantom.isCastingNow())
			{
				phantom.getAI().Cast(_skill, phantom);
			}
			phantom.phantom_params.initSoeTask(new SoeTask(phantom,_forcibly, _skill), 5000); // не в мирной зоне-запустим повторно 
		}else*/
		// в мирной зоне - запустим таск на поиск новой локации фарма
		ThreadPoolManager.getInstance().PhantomOtherSchedule(new EndPeaceCooldownTask(phantom), Rnd.get((int) (phantom.phantom_params.getPeaceCooldown() * 750), (int) (phantom.phantom_params.getPeaceCooldown() * 1250)));
		if(_forcibly)
			PhantomUtils.giveClothes(phantom, null);

	}
}

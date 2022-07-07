package l2s.Phantoms.ai.tasks.other;

import java.util.ArrayList;
import java.util.List;

import l2s.Phantoms.Utils.PhantomUtils;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;

public class ChangeClothesAndProfession extends RunnableImpl
{
	public Player phantom;

	public ChangeClothesAndProfession(Player ph)
	{
		phantom = ph;
	}

	@Override
	public void runImpl()
	{
		switch(phantom.getLevel())// поменяем профу
		{
			case 20:
			case 40:
			case 76:
			{
				phantom.phantom_params.getPhantomAI().abortAITask();
				ClassId classId = phantom.getClassId();
				List<Integer> list_class_id = new ArrayList<Integer>();

				for(ClassId cid : ClassId.values())
				{
					if(cid.childOf(classId) && cid.getClassLevel().ordinal() == classId.getClassLevel().ordinal() + 1)
						list_class_id.add(cid.getId());
				}
				Integer new_class = Rnd.get(list_class_id);
				if(!list_class_id.isEmpty())
				{
					phantom.setClassId(new_class, false, false);
					phantom.broadcastUserInfo(true);
				}
				PhantomUtils.initializePhantom(phantom, new_class, null);
				break;
			}
			case 10:
			case 52:
			case 61:
			case 80:
			case 84:
			{
				PhantomUtils.giveClothes(phantom, null);
				break;
			}

		}
		PhantomUtils.checkLevelAndSetFarmLoc(phantom, phantom, false);
	}
}
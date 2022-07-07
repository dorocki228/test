package l2s.Phantoms.ai.tasks.other;

import java.util.Comparator;
import java.util.Optional;

import l2s.Phantoms.Utils.PhantomUtils;
import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.OutpostInstance;
import l2s.gameserver.model.instances.TeleporterInstance;
import l2s.gameserver.tables.GmListTable;
import l2s.gameserver.utils.Location;
import l2s.gameserver.model.base.Fraction;

//подбегаем к баферу или гк
public class MoveToGkTask extends RunnableImpl
{
	public Player phantom;

	public MoveToGkTask(Player ph)
	{
		phantom = ph;
	}

	@Override
	public void runImpl()
	{
		if(phantom == null || phantom.getOlympiadGame() != null /*|| phantom.isInPvPEvent() */ || phantom.getReflectionId() != 0)
			return;
		if (phantom.phantom_params.getGmLog())
			GmListTable.broadcastMessageToGMs("MoveToGkTask 2");
			// ищем ближайший телепорт
			Optional<NpcInstance> gk = phantom.getAroundNpc(2500, 700).stream().filter(npc -> npc != null
					&& (npc.getTemplate().getInstanceClass() == TeleporterInstance.class || npc.getNpcId() ==  (phantom.getFraction() == Fraction.FIRE ? OutpostInstance.FIRE_FLAG : OutpostInstance.WATER_FLAG))).min(Comparator.comparingDouble(phantom::getDistance));

			if (gk.isPresent())// нашли
			{
				if (phantom.phantom_params.getGmLog())
				GmListTable.broadcastMessageToGMs(phantom + " MoveToGkTask 3"  + gk.get());
				//ищем стабильною точку и бежим к гк используя патчфинд
				Location loc = Location.findAroundPosition(gk.get(), 50,100); 
				if (PhantomUtils.availabilityCheck(phantom,loc))
				{
					if (phantom.phantom_params.getGmLog())
					GmListTable.broadcastMessageToGMs(phantom + " MoveToGkTask 4"  + gk.get());
					phantom.moveToLocation(loc, 0, true);
					if (!phantom.phantom_params.getEndPeaceCooldownTask())
					{
						phantom.phantom_params.initEndPeaceCooldownTask(phantom.phantom_params.getPeaceCooldown() *1000);
					}
				}
				else
				{
					phantom.kick();
					//if (phantom.phantom_params.getGmLog())
					//GmListTable.broadcastMessageToGMs("MoveToGkTask 5");
					// пройти невозможно, добавить варианты
				}

			} else // телепорта нет, или деспавним или ищем ближайший маршрут
			{
				phantom.kick();
				//if (phantom.phantom_params.getGmLog())
				//GmListTable.broadcastMessageToGMs("MoveToGkTask 6");
			}
	}
}

package  l2s.Phantoms.ai.tasks.party;

import  l2s.Phantoms.ai.abstracts.PhantomDefaultPartyAI;
import  l2s.Phantoms.enums.PartyState;
import  l2s.Phantoms.enums.RouteType;
import  l2s.Phantoms.manager.PartyManager;
import  l2s.Phantoms.objects.TrafficScheme.PhantomRoute;
import  l2s.Phantoms.taskmanager.BSoeTask;
import  l2s.commons.threading.RunnableImpl;
import  l2s.commons.util.Rnd;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.utils.Location;

public class ChangePartyStateTask extends RunnableImpl
{
	private int id;
	private int state;
	
	public ChangePartyStateTask(int id, int state)
	{
		this.id = id;
		this.state = state;
	}
	
	@Override
	public void runImpl()
	{
		PhantomDefaultPartyAI party = PartyManager.getInstance().getPartyAIByID(id);
		if (state == 1)
		{
			// выдадим новый маршрут
			PhantomRoute route =null;//PhantomRoutesParser.getInstance().getRndRoute(RouteType.STANDART_CP);
			party.setRoute(route);

			for (Player member : party.getAllMembers())
			{
				// бот не в мирной зоне, хилы умерли и бот не мертвый
				if (!member.isInPeaceZone() && !member.isDead())
				{
					if (!member.phantom_params.getSoeTask())
						member.phantom_params.initSoeTask(new BSoeTask(member), Rnd.get(1, 5) * 1000);
					continue;
				}
				// бот мертвый
				if (member.isDead())
				{
					member.decayMe();
					member.doRevive(); // воскрешаем
					// баф фантомов
					member.phantom_params.getPhantomAI().startBuffTask(100);
					// p.fullHeal(); // хилим
					member.teleToClosestTown();
					continue;
				}
			}
			party.initSubTask(new ChangePartyStateTask(party.getPartyId(), 2), Rnd.get(20, 240) * 1000); // в отстойник
		}
		else if (state == 2)
		{
			Location rnd_loc = Location.coordsRandomize(new Location (-113144, -244744, -15536), 100, 50000);
			for (Player member : party.getAllMembers())
			{
			if (member.isDead())
				member.doRevive(100.);
			member.teleToLocation(rnd_loc); // кидаем в отстойник
			}
			party.initSubTask(new ChangePartyStateTask(party.getPartyId(), 3), Rnd.get(20, 240) * 1000); // запустим повторный таск, телепортируем с отстойника в стартовою локу маршрута
			
		}else if (state == 3)
		{
			Location loc = party.getRoute().getTask().get(0).getPoints().get(0).getLoc();
			for (Player member : party.getAllMembers())
			{
				if (member.isDead())
					member.doRevive(100.);
				member.teleToLocation(Location.coordsRandomize(loc, 20, 80));
			}
			party.changePartyState(PartyState.route);
		}
	}
	
}

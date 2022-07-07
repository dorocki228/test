package l2s.gameserver.model.entity.olympiad;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;

public class OlympiadGameTask implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(OlympiadGameTask.class);
	private final OlympiadGame _game;
	private final BattleStatus _status;
	private final int _count;
	private final long _time;
	private boolean _terminated = false;

	public boolean isTerminated()
	{
		return _terminated;
	}

	public BattleStatus getStatus()
	{
		return _status;
	}

	public int getCount()
	{
		return _count;
	}

	public OlympiadGame getGame()
	{
		return _game;
	}

	public long getTime()
	{
		return _count;
	}

	public ScheduledFuture<?> shedule()
	{
		return ThreadPoolManager.getInstance().schedule(this, _time);
	}

	public OlympiadGameTask(OlympiadGame game, BattleStatus status, int count, long time)
	{
		_game = game;
		_status = status;
		_count = count;
		_time = time;
	}

	@Override
	public void run()
	{
		if(_game == null || _terminated)
			return;

        int gameId = _game.getId();

		try
		{
			if(!Olympiad.inCompPeriod())
				return;

			if(!_game.checkPlayersOnline() && _status != BattleStatus.ValidateWinner && _status != BattleStatus.Ending)
			{
				String messagePattern = "Player is offline for game {}, status: {}";
				ParameterizedMessage message = new ParameterizedMessage(messagePattern, gameId, _status);
				LogService.getInstance().log(LoggerType.OLYMPIAD, message);

				_game.endGame(1, true);
				return;
			}
            OlympiadGameTask task = null;
            switch(_status)
			{
				case Begining:
				{
					task = new OlympiadGameTask(_game, BattleStatus.Begin_Countdown, Config.OLYMPIAD_BEGINIG_DELAY, 100);
					break;
				}
				case Begin_Countdown:
				{
					SystemMsg msg = SystemMsg.YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S;
					_game.broadcastPacket(new SystemMessage(msg).addNumber(_count), true, false);
					switch (_count)
					{
						case 120:
							task = new OlympiadGameTask(_game, BattleStatus.Begin_Countdown, 60, 60000);
							break;
						case 60:
							task = new OlympiadGameTask(_game, BattleStatus.Begin_Countdown, 30, 30000);
							break;
						case 30:
							task = new OlympiadGameTask(_game, BattleStatus.Begin_Countdown, 15, 15000);
							break;
						case 15:
							task = new OlympiadGameTask(_game, BattleStatus.Begin_Countdown, 5, 10000);
							break;
						case 5:
							task = new OlympiadGameTask(_game, BattleStatus.Begin_Countdown, 4, 1000);
							break;
						case 4:
							task = new OlympiadGameTask(_game, BattleStatus.Begin_Countdown, 3, 1000);
							break;
						case 3:
							task = new OlympiadGameTask(_game, BattleStatus.Begin_Countdown, 2, 1000);
							break;
						case 2:
							task = new OlympiadGameTask(_game, BattleStatus.Begin_Countdown, 1, 1000);
							break;
						case 1:
							task = new OlympiadGameTask(_game, BattleStatus.PortPlayers, 0, 1000);
							break;
					}

					break;
				}
				case PortPlayers:
				{
					/*if(!_game.validatePlayers())
					{
						LogService.add("Player is dont valid for game " + gameId + ", status: " + _status, "olympiad");
						_game.endGame(1, true);
						return;
					}*/

					_game.portPlayersToArena();
					_game.managerShout();

					task = new OlympiadGameTask(_game, BattleStatus.Started, 60, 1000);
					break;
				}
				case Started:
				{
                    _game.preparePlayers();
                    _game.addBuffers();
                    _game.broadcastPacket(new SystemMessage(SystemMsg.THE_MATCH_WILL_START_IN_S1_SECONDS).addNumber(_count), true, true);
                    task = new OlympiadGameTask(_game, BattleStatus.Heal, 55, 5000);
					break;
				}
                case Heal:
                {
                    task = new OlympiadGameTask(_game, BattleStatus.CountDown, 50, 5000);
                    break;
                }
				case CountDown:
				{
					_game.broadcastPacket(new SystemMessage(SystemMsg.THE_GAME_WILL_START_IN_S1_SECOND_S).addNumber(_count), true, true);

                    switch (_count)
                    {
                        case 50:
							_game.heal();
                            task = new OlympiadGameTask(_game, BattleStatus.CountDown, 40, 10000);
                            break;
                        case 40:
							task = new OlympiadGameTask(_game, BattleStatus.CountDown, 30, 10000);
                            break;
                        case 30:
                            task = new OlympiadGameTask(_game, BattleStatus.CountDown, 20, 10000);
                            break;
                        case 20:
                            task = new OlympiadGameTask(_game, BattleStatus.CountDown, 10, 10000);
                            break;
                        case 10:
                            _game.openDoors();
                            task = new OlympiadGameTask(_game, BattleStatus.CountDown, 5, 5000);
                            break;
                        case 5:
                            task = new OlympiadGameTask(_game, BattleStatus.CountDown, 4, 1000);
                            break;
                        case 4:
                            task = new OlympiadGameTask(_game, BattleStatus.CountDown, 3, 1000);
                            break;
                        case 3:
                            task = new OlympiadGameTask(_game, BattleStatus.CountDown, 2, 1000);
                            break;
                        case 2:
                            task = new OlympiadGameTask(_game, BattleStatus.CountDown, 1, 1000);
                            break;
                        case 1:
                            task = new OlympiadGameTask(_game, BattleStatus.StartComp, 0, 1000);
                            break;
                    }
					break;
				}
				case StartComp:
				{
					_game.deleteBuffers();
					_game.setState(2);
					_game.broadcastPacket(SystemMsg.THE_MATCH_HAS_STARTED, true, true);
					_game.broadcastInfo(null, null, false);

					task = new OlympiadGameTask(_game, BattleStatus.InComp, 120, 180000); // 300 total
					break;
				}
                case InComp:
                {
                    if (_game.getState() == 0) // game finished
                        return;
                    _game.broadcastPacket(new SystemMessage(SystemMsg.THE_GAME_WILL_END_IN_S1_SECONDS_).addNumber(_count), true, true);
                    switch (_count)
                    {
                        case 120:
                            task = new OlympiadGameTask(_game, BattleStatus.InComp, 60, 60000);
                            break;
                        case 60:
                            task = new OlympiadGameTask(_game, BattleStatus.InComp, 30, 30000);
                            break;
                        case 30:
                            task = new OlympiadGameTask(_game, BattleStatus.InComp, 10, 20000);
                            break;
                        case 10:
                            task = new OlympiadGameTask(_game, BattleStatus.InComp, 5, 5000);
                            break;
                        case 5:
                            task = new OlympiadGameTask(_game, BattleStatus.ValidateWinner, 0, 5000);
                            break;
                    }
                    break;
                }
				case ValidateWinner:
				{
					try
					{
						_game.validateWinner(_count > 0);
					}
					catch(Exception e)
					{
						_log.error("", e);
					}

					task = new OlympiadGameTask(_game, BattleStatus.PortBack, 20, 100);
					break;
				}
				case PortBack:
				{
                    _game.broadcastPacket(new SystemMessage(SystemMsg.YOU_WILL_BE_MOVED_BACK_TO_TOWN_IN_S1_SECONDS).addNumber(_count), true, false);

                    switch (_count)
                    {
                        case 20:
                            task = new OlympiadGameTask(_game, BattleStatus.PortBack, 10, 10000);
                            break;
                        case 10:
                            task = new OlympiadGameTask(_game, BattleStatus.PortBack, 5, 5000);
                            break;
                        case 5:
                            task = new OlympiadGameTask(_game, BattleStatus.PortBack, 4, 1000);
                            break;
                        case 4:
                            task = new OlympiadGameTask(_game, BattleStatus.PortBack, 3, 1000);
                            break;
                        case 3:
                            task = new OlympiadGameTask(_game, BattleStatus.PortBack, 2, 1000);
                            break;
                        case 2:
                            task = new OlympiadGameTask(_game, BattleStatus.PortBack, 1, 1000);
                            break;
                        case 1:
                            task = new OlympiadGameTask(_game, BattleStatus.Ending, 0, 1000);
                            break;
                    }
                    break;
				}
				case Ending:
				{
					_game.collapse();
					_terminated = true;

					if(Olympiad._manager != null)
						Olympiad._manager.freeOlympiadInstance(_game.getId());
					return;
				}
			}

			if(task == null)
			{
				String messagePattern = "task == null for game {}";
				ParameterizedMessage message = new ParameterizedMessage(messagePattern, gameId);
				LogService.getInstance().log(LoggerType.OLYMPIAD, message);
				Thread.dumpStack();

				_game.endGame(1, true);
				return;
			}

			_game.sheduleTask(task);
		}
		catch(Exception e)
		{
			_log.error("", e);
			_game.endGame(1, true);
		}
	}
}

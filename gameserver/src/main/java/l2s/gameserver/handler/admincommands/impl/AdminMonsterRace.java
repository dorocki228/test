package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.MonsterRace;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.DeleteObjectPacket;
import l2s.gameserver.network.l2.s2c.MonRaceInfoPacket;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.utils.Location;

public class AdminMonsterRace implements IAdminCommandHandler
{
	protected static int state;

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if("admin_mons".equalsIgnoreCase(fullString))
		{
			if(!activeChar.getPlayerAccess().MonsterRace)
				return false;
			handleSendPacket(activeChar);
		}
		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void handleSendPacket(Player activeChar)
	{
		int[][] codes = { { -1, 0 }, { 0, 15322 }, { 13765, -1 }, { -1, 0 } };
		MonsterRace race = MonsterRace.getInstance();
		if(state == -1)
		{
			++state;
			race.newRace();
			race.newSpeeds();
			activeChar.broadcastPacket(new MonRaceInfoPacket(codes[state][0], codes[state][1], race.getMonsters(), race.getSpeeds()));
		}
		else if(state == 0)
		{
			++state;
			activeChar.sendPacket(SystemMsg.THEYRE_OFF);
			activeChar.broadcastPacket(new PlaySoundPacket("S_Race"));
			activeChar.broadcastPacket(new PlaySoundPacket(PlaySoundPacket.Type.SOUND, "ItemSound2.race_start", 1, 121209259, new Location(12125, 182487, -3559)));
			activeChar.broadcastPacket(new MonRaceInfoPacket(codes[state][0], codes[state][1], race.getMonsters(), race.getSpeeds()));
			ThreadPoolManager.getInstance().schedule(new RunRace(codes, activeChar), 5000L);
		}
	}

	static
	{
		state = -1;
	}

	private enum Commands
	{
		admin_mons
	}

	class RunRace implements Runnable
	{
		private final int[][] codes;
		private final Player activeChar;

		public RunRace(int[][] codes, Player activeChar)
		{
			this.codes = codes;
			this.activeChar = activeChar;
		}

		@Override
		public void run()
		{
			activeChar.broadcastPacket(new MonRaceInfoPacket(codes[2][0], codes[2][1], MonsterRace.getInstance().getMonsters(), MonsterRace.getInstance().getSpeeds()));
			ThreadPoolManager.getInstance().schedule(new RunEnd(activeChar), 30000L);
		}
	}

	class RunEnd implements Runnable
	{
		private final Player activeChar;

		public RunEnd(Player activeChar)
		{
			this.activeChar = activeChar;
		}

		@Override
		public void run()
		{
			for(int i = 0; i < 8; ++i)
			{
				NpcInstance obj = MonsterRace.getInstance().getMonsters()[i];
				activeChar.broadcastPacket(new DeleteObjectPacket(obj));
			}
			state = -1;
		}
	}
}

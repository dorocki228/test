package l2s.gameserver.network.l2.s2c;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;

import java.util.Calendar;
import java.util.Locale;

public final class SendStatus extends L2GameServerPacket
{
	private static final long MIN_UPDATE_PERIOD = 30000L;
	private static int online_players;
	private static int max_online_players;
	private static int online_priv_store;
	private static long last_update;

	public SendStatus()
	{
		if(System.currentTimeMillis() - last_update < 30000L)
			return;
		last_update = System.currentTimeMillis();
		if(!Config.ENABLE_L2_TOP_OVERONLINE)
		{
			int i = 0;
			int j = 0;
			for(Player player : GameObjectsStorage.getPlayers())
			{
				++i;
				if((player.isInStoreMode() || player.isPrivateBuffer()) && (!Config.SENDSTATUS_TRADE_JUST_OFFLINE || player.isInOfflineMode()))
					++j;
			}
			online_players = i;
			online_priv_store = (int) Math.floor(j * Config.SENDSTATUS_TRADE_MOD);
			max_online_players = Math.max(max_online_players, online_players);
		}
		else
		{
			max_online_players = Config.L2TOP_MAX_ONLINE;
			int hour = Calendar.getInstance(new Locale("ru", "RU")).get(11);
			if(hour >= 0 && hour < 6)
				online_players = Rnd.get(Config.MIN_ONLINE_0_5_AM, Config.MAX_ONLINE_0_5_AM);
			else if(hour >= 6 && hour < 12)
				online_players = Rnd.get(Config.MIN_ONLINE_6_11_AM, Config.MAX_ONLINE_6_11_AM);
			else if(hour >= 12 && hour < 19)
				online_players = Rnd.get(Config.MIN_ONLINE_12_6_PM, Config.MAX_ONLINE_12_6_PM);
			else
				online_players = Rnd.get(Config.MIN_ONLINE_7_11_PM, Config.MAX_ONLINE_7_11_PM);
			int weekDay = Calendar.getInstance().get(7) + 1;
			online_players += weekDay < 5 ? Config.ADD_ONLINE_ON_SIMPLE_DAY : Config.ADD_ONLINE_ON_WEEKEND;
			online_priv_store = Rnd.get(Config.L2TOP_MIN_TRADERS, Config.L2TOP_MAX_TRADERS);
		}
	}

	@Override
	protected final boolean writeOpcodes()
	{
        writeC(0);
		return true;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(1);
        writeD(max_online_players);
        writeD(online_players);
        writeD(online_players);
        writeD(online_priv_store);
		writeD(2883632);
		for(int x = 0; x < 10; ++x)
			writeH(41 + Rnd.get(17));
		writeD(43 + Rnd.get(17));
		int z = 36219 + Rnd.get(1987);
		writeD(z);
		writeD(z);
		writeD(37211 + Rnd.get(2397));
		writeD(0);
		writeD(2);
	}

	static
	{
		online_players = 0;
		max_online_players = 0;
		online_priv_store = 0;
		last_update = 0L;
	}
}

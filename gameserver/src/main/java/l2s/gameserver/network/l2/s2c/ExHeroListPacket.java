package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.templates.StatsSet;

import java.util.Collection;

public class ExHeroListPacket extends L2GameServerPacket
{
	private final Collection<StatsSet> _heroList = Hero.getInstance().getHeroes().values();

	@Override
	protected final void writeImpl()
	{
		writeD(_heroList.size());
		for(StatsSet hero : _heroList)
		{
			writeS(hero.getString("char_name"));
			writeD(hero.getInteger("class_id"));
			writeS(hero.getString("clan_name", ""));
			writeD(hero.getInteger("clan_crest", 0));
			writeS(hero.getString("ally_name", ""));
			writeD(hero.getInteger("ally_crest", 0));
			writeD(hero.getInteger("count"));
			writeD(0);
		}
	}
}

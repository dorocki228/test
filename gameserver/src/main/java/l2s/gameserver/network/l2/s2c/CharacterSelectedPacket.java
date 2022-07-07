package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.time.GameTimeService;
import l2s.gameserver.utils.Location;

public class CharacterSelectedPacket extends L2GameServerPacket
{
	private final int _sessionId;
	private final int char_id;
	private final int clan_id;
	private final int sex;
	private final int race;
	private final int class_id;
	private final String _name;
	private final String _title;
	private final Location _loc;
	private final double curHp;
	private final double curMp;
	private final int level;
	private final int karma;
	private final int _pk;
	private final long _exp;
	private final long _sp;

	public CharacterSelectedPacket(Player cha, int sessionId)
	{
		_sessionId = sessionId;
		_name = cha.getName();
		char_id = cha.getObjectId();
		_title = cha.getTitle();
		clan_id = cha.getClanId();
		sex = cha.getSex().ordinal();
		race = cha.getRace().ordinal();
		class_id = cha.getClassId().getId();
		_loc = cha.getLoc();
		curHp = cha.getCurrentHp();
		curMp = cha.getCurrentMp();
		_sp = cha.getSp();
		_exp = cha.getExp();
		level = cha.getLevel();
		karma = cha.getKarma();
		_pk = cha.getPkKills();
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_name);
        writeD(char_id);
		writeS(_title);
        writeD(_sessionId);
        writeD(clan_id);
        writeD(0);
        writeD(sex);
        writeD(race);
        writeD(class_id);
        writeD(1);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
		writeF(curHp);
		writeF(curMp);
		writeQ(_sp);
		writeQ(_exp);
        writeD(level);
        writeD(karma);
        writeD(_pk);
        writeD(GameTimeService.INSTANCE.getGameTime());
        writeD(0);
        writeD(0);
        writeD(0);
        writeD(0);
        writeD(0);
        writeD(0);
		writeB(new byte[64]);
        writeD(0);
	}
}

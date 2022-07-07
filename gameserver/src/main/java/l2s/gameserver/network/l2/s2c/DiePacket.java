package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.instancemanager.gve.GvePortalManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.packet.PacketThrottler;
import l2s.gameserver.model.pledge.Clan;

import java.util.HashMap;
import java.util.Map;

public class DiePacket extends L2GameServerPacket {
	private final int _objectId;
	private final boolean _hideDieAnimation;
	private boolean _sweepable;
	private int _blessingFeatherDelay;
	private final Map<RestartType, Boolean> _types;
	private final boolean playable;

	public DiePacket(Creature cha, boolean hideDieAnimation) {
		_sweepable = false;
		_blessingFeatherDelay = 0;
		_types = new HashMap<>(RestartType.VALUES.length);
		_hideDieAnimation = hideDieAnimation;
		_objectId = cha.getObjectId();
		playable = cha.isPlayable();
		if (cha.isMonster())
			_sweepable = ((MonsterInstance) cha).isSweepActive();
		else if (cha.isPlayer()) {
			Player player = (Player) cha;

			put(RestartType.FIXED, player.canFixedRessurect());
			put(RestartType.AGATHION, player.isAgathionResAvailable());
			put(RestartType.TO_VILLAGE, true);
			put(RestartType.ADVENTURES_SONG, player.getAbnormalList().containsEffects(22410) || player.getAbnormalList().containsEffects(22411));
			put(RestartType.TO_PORTAL, player.getEvents().isEmpty() && GvePortalManager.getInstance().showToFlagOnDie(player));
			put(RestartType.TO_FORTRESS, isInFactionFortressSiege(player));

			for (Abnormal effect : player.getAbnormalList().getEffects())
				if(effect.getSkill().getId() == 7008)
				{
					_blessingFeatherDelay = effect.getTimeLeft();
					break;
				}
			Clan clan = null;
			if(get(RestartType.TO_VILLAGE))
				clan = player.getClan();
			if(clan != null)
			{
				put(RestartType.TO_CLANHALL, clan.getHasHideout() != 0);
			}
			for(Event e : cha.getEvents())
				e.checkRestartLocs(player, _types);

			for(Castle c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
			{
				if(c.getSiegeEvent() != null && c.getSiegeEvent().isInProgress() && (c.getSiegeEvent().getPlayersInZone().contains(cha) || c.getFraction() == cha.getFraction()))
				{
					put(RestartType.TO_CASTLE, true);
					break;
				}
			}
		}
	}

	public DiePacket(Creature cha)
	{
		this(cha, false);
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_objectId);
        writeD(get(RestartType.TO_VILLAGE));
        writeD(get(RestartType.TO_CLANHALL));
        writeD(get(RestartType.TO_CASTLE));
        writeD(get(RestartType.TO_PORTAL));
        writeD(_sweepable ? 1 : 0);
        writeD(get(RestartType.FIXED));
        writeD(get(RestartType.TO_FORTRESS));
        writeD(_blessingFeatherDelay);
        writeD(get(RestartType.ADVENTURES_SONG));
        writeC(_hideDieAnimation ? 1 : 0);
        writeD(get(RestartType.AGATHION));
		int itemsCount = 0;
        writeD(itemsCount);
		for(int i = 0; i < itemsCount; ++i)
            writeD(0);
	}

	private void put(RestartType t, boolean b)
	{
		_types.put(t, b);
	}

	private boolean get(RestartType t)
	{
		Boolean b = _types.get(t);
		return b != null && b;
	}

	private boolean isInFactionFortressSiege(Player player) {
		FortressSiegeEvent fortressEvent = (FortressSiegeEvent) player.getZoneEvents().stream()
				.filter(e -> e instanceof FortressSiegeEvent)
				.findFirst()
				.orElse(null);
		if (fortressEvent != null) {
			return fortressEvent.getOwnerFraction() == player.getFraction();
		} else {
			return false;
		}
	}

	@Override
	public boolean isInPacketRange(final Creature sender, final Player recipient) {
		if (playable)
			return true;
		return PacketThrottler.MAX_PACKET_RANGE * PacketThrottler.MAX_PACKET_RANGE > sender.getXYDeltaSq(recipient.getX(), recipient.getY());
	}

	@Override
	public void onSendPacket(Player player) {
		player.getPacketThrottler().onSendPacket();
	}
}

package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.model.*;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SummonPortal extends Skill
{
	// TODO вынести в кастомные сообщения
	private static final String CANT_SUMMON_PORTAL_EN = "You cannot use a factional portal if another portal" +
			" is already installed in the near radius.";
	private static final String CANT_SUMMON_PORTAL_RU = "Вы не можете использовать фракционный портал" +
			" если в ближайшем радиусе уже установлен другой портал.";

	private final TeleportType _teleportType;

	public SummonPortal(StatsSet set)
	{
		super(set);
		_teleportType = Enum.valueOf(TeleportType.class, set.getString("teleportType", "PERSONAL").toUpperCase());
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;
		Player player = activeChar.getPlayer();
		if(player == null)
			return false;

		if(player.isProcessingRequest())
		{
			player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}

		if(player.isInZone(Zone.ZoneType.no_portal))
		{
			player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}

		if(!player.getReflection().isMain())
		{
			player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}

		if(World.getAroundNpc(player, 1100, 200).stream().anyMatch(GameObject::isPortal))
		{
			player.sendMessage(player.isLangRus() ? CANT_SUMMON_PORTAL_RU : CANT_SUMMON_PORTAL_EN);
			return false;
		}
		return true;
	}

	@Override
	public void onEndCast(Creature caster, List<Creature> targets)
	{
		super.onEndCast(caster, targets);
		Player activeChar = caster.getPlayer();
		if(activeChar == null)
			return;
		int id = -1;
		Fraction f = activeChar.getFraction();
		switch(_teleportType)
		{
			case PERSONAL:
			{
				id = f == Fraction.FIRE ? 40045 : 40047;
				break;
			}
			case FRACTION:
			{
				id = activeChar.getFraction() == Fraction.FIRE ? 40046 : 40048;
				break;
			}
		}

		if(id > 0)
		{
			NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(id);
			NpcInstance npc = npcTemplate.getNewInstance();
			npc.setCurrentHp(npc.getMaxHp(), false);
			npc.setCurrentMp(npc.getMaxMp());
			npc.setHeading(activeChar.getHeading());
			npc.setReflection(activeChar.getReflection());
			npc.setFraction(f);
			npc.setTitle(activeChar.getName());
			npc.setOwner(activeChar);
			Location location = Location.findAroundPosition(activeChar.getLoc(), 10, 30, activeChar.getGeoIndex());
			npc.spawnMe(location);
			npc.startDeleteTask(TimeUnit.MINUTES.toMillis(5));
		}

	}

	public enum TeleportType
	{
		PERSONAL,
		FRACTION
	}
}

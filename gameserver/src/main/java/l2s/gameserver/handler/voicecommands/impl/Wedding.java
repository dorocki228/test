package l2s.gameserver.handler.voicecommands.impl;

import l2s.commons.dbutils.DbUtils;
import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.instancemanager.CoupleManager;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.Couple;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ConfirmDlgPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.utils.Location;
import org.napile.pair.primitive.IntObjectPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Wedding implements IVoicedCommandHandler
{
	private static final Logger _log;
	private static final String[] _voicedCommands;

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if(!Config.ALLOW_VOICED_COMMANDS)
			return false;
		if(!Config.ALLOW_WEDDING)
			return false;
		if(command.startsWith("engage"))
			return engage(player);
		if(command.startsWith("divorce"))
			return divorce(player);
		return command.startsWith("gotolove") && goToLove(player);
	}

	public boolean divorce(Player activeChar)
	{
		if(activeChar.getPartnerId() == 0)
			return false;
		int _partnerId = activeChar.getPartnerId();
		long AdenaAmount = 0L;
		if(activeChar.isMaried())
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.Divorced"));
			AdenaAmount = Math.abs(activeChar.getAdena() / 100L * Config.WEDDING_DIVORCE_COSTS - 10L);
			activeChar.reduceAdena(AdenaAmount, true);
		}
		else
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.Disengaged"));
		activeChar.setMaried(false);
		activeChar.setPartnerId(0);
		Couple couple = CoupleManager.getInstance().getCouple(activeChar.getCoupleId());
		couple.divorce();
		couple = null;
		Player partner = GameObjectsStorage.getPlayer(_partnerId);
		if(partner != null)
		{
			partner.setPartnerId(0);
			if(partner.isMaried())
				partner.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PartnerDivorce"));
			else
				partner.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PartnerDisengage"));
			partner.setMaried(false);
			if(AdenaAmount > 0L)
				partner.addAdena(AdenaAmount);
		}
		return true;
	}

	public boolean engage(Player activeChar)
	{
		if(activeChar.getTarget() == null)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.NoneTargeted"));
			return false;
		}
		if(!activeChar.getTarget().isPlayer())
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.OnlyAnotherPlayer"));
			return false;
		}
		if(activeChar.getPartnerId() != 0)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.AlreadyEngaged"));
			if(Config.WEDDING_PUNISH_INFIDELITY)
			{
				activeChar.startAbnormalEffect(AbnormalEffect.BIG_HEAD);
				int skillLevel = 1;
				if(activeChar.getLevel() > 40)
					skillLevel = 2;
				int skillId;
				if(activeChar.isMageClass())
					skillId = 4361;
				else
					skillId = 4362;
				Skill skill = SkillHolder.getInstance().getSkill(skillId, skillLevel);
				if(!activeChar.getAbnormalList().containsEffects(skill))
				{
					skill.getEffects(activeChar, activeChar);
					activeChar.sendPacket(new SystemMessagePacket(SystemMsg.S1S_EFFECT_CAN_BE_FELT).addSkillName(skillId, skillLevel));
				}
			}
			return false;
		}
		Player ptarget = (Player) activeChar.getTarget();
		if(ptarget.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.EngagingYourself"));
			return false;
		}
		if(ptarget.isMaried())
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PlayerAlreadyMarried"));
			return false;
		}
		if(ptarget.getPartnerId() != 0)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PlayerAlreadyEngaged"));
			return false;
		}
		IntObjectPair<OnAnswerListener> entry = ptarget.getAskListener(false);
		if(entry != null && entry.getValue() instanceof CoupleAnswerListener)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PlayerAlreadyAsked"));
			return false;
		}
		if(ptarget.getPartnerId() != 0)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PlayerAlreadyEngaged"));
			return false;
		}
		if(ptarget.getSex() == activeChar.getSex() && !Config.WEDDING_SAMESEX)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.SameSex"));
			return false;
		}
		boolean FoundOnFriendList = false;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id=?");
			statement.setInt(1, ptarget.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				int objectId = rset.getInt("friend_id");
				if(objectId == activeChar.getObjectId())
				{
					FoundOnFriendList = true;
					break;
				}
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		if(!FoundOnFriendList)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.NotInFriendlist"));
			return false;
		}
		ConfirmDlgPacket packet = new ConfirmDlgPacket(SystemMsg.S1, 60000).addString("Player " + activeChar.getName() + " asking you to engage. Do you want to start new relationship?");
		ptarget.ask(packet, new CoupleAnswerListener(activeChar, ptarget));
		return true;
	}

	public boolean goToLove(Player activeChar)
	{
		if(!activeChar.isMaried())
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.YoureNotMarried"));
			return false;
		}
		if(activeChar.getPartnerId() == 0)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PartnerNotInDB"));
			return false;
		}
		Player partner = GameObjectsStorage.getPlayer(activeChar.getPartnerId());
		if(partner == null)
		{
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PartnerOffline"));
			return false;
		}
		if(activeChar.isMovementDisabled() || activeChar.isMuted(null) || activeChar.containsEvent(SingleMatchEvent.class) || partner.containsEvent(SingleMatchEvent.class) || partner.isInZone(Zone.ZoneType.no_summon))
		{
			activeChar.sendMessage(new CustomMessage("common.TryLater"));
			return false;
		}
		if(activeChar.getTeleMode() != 0 || !activeChar.getReflection().isMain())
		{
			activeChar.sendMessage(new CustomMessage("common.TryLater"));
			return false;
		}
		if(partner.isInOlympiadMode() || activeChar.isInOlympiadMode() || partner.isInZoneBattle() || partner.isInZone(Zone.ZoneType.SIEGE) || partner.isInZone(Zone.ZoneType.no_restart) || activeChar.isInZoneBattle() || activeChar.isInZone(Zone.ZoneType.SIEGE) || activeChar.isInZone(Zone.ZoneType.no_restart) || !partner.getReflection().isMain() || partner.isInZone(Zone.ZoneType.no_summon) || activeChar.isInObserverMode() || partner.isInObserverMode())
		{
			activeChar.sendPacket(SystemMsg.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}
		if(!activeChar.reduceAdena(Config.WEDDING_TELEPORT_PRICE, true))
		{
			activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return false;
		}
		int teleportTimer = Config.WEDDING_TELEPORT_INTERVAL;
		activeChar.abortAttack(true, true);
		activeChar.abortCast(true, true);
		activeChar.sendActionFailed();
		activeChar.stopMove();
		activeChar.startParalyzed();
		activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.Teleport").addNumber(teleportTimer / 60));
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 1050, 1, teleportTimer, 0L));
		ThreadPoolManager.getInstance().schedule(new EscapeFinalizer(activeChar, partner.getLoc()), teleportTimer * 1000L);
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}

	public void onLoad()
	{
		VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}

	static
	{
		_log = LoggerFactory.getLogger(Wedding.class);
		_voicedCommands = new String[] { "divorce", "engage", "gotolove" };
	}

	private static class CoupleAnswerListener implements OnAnswerListener
	{
		private final HardReference<Player> _playerRef1;
		private final HardReference<Player> _playerRef2;

		public CoupleAnswerListener(Player player1, Player player2)
		{
			_playerRef1 = player1.getRef();
			_playerRef2 = player2.getRef();
		}

		@Override
		public void sayYes()
		{
			Player player1;
			Player player2;
			if((player1 = _playerRef1.get()) == null || (player2 = _playerRef2.get()) == null)
				return;
			CoupleManager.getInstance().createCouple(player1, player2);
			player1.sendMessage(new CustomMessage("l2s.gameserver.model.L2Player.EngageAnswerYes"));
		}

		@Override
		public void sayNo()
		{
			Player player1;
			Player player2;
			if((player1 = _playerRef1.get()) == null || (player2 = _playerRef2.get()) == null)
				return;
			player1.sendMessage(new CustomMessage("l2s.gameserver.model.L2Player.EngageAnswerNo"));
		}
	}

	static class EscapeFinalizer implements Runnable
	{
		private final Player _activeChar;
		private final Location _loc;

		EscapeFinalizer(Player activeChar, Location loc)
		{
			_activeChar = activeChar;
			_loc = loc;
		}

		@Override
		public void run()
		{
			if(_activeChar == null)
				return;
			_activeChar.stopParalyzed();
			if(_activeChar.isDead())
				return;
			_activeChar.teleToLocation(_loc);
		}
	}
}

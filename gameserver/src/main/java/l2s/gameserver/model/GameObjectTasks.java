package l2s.gameserver.model;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.network.l2.s2c.MagicSkillLaunchedPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.utils.Util;

import java.util.List;

public class GameObjectTasks
{
	public static class DeleteTask implements Runnable
	{
		private final HardReference<? extends Creature> _ref;

		public DeleteTask(Creature c)
		{
			_ref = c.getRef();
		}

		@Override
		public void run()
		{
			Creature c = _ref.get();
			if(c != null)
				c.deleteMe();
		}
	}

	public static class SoulConsumeTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public SoulConsumeTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.setConsumedSouls(player.getConsumedSouls() + 1, null);
		}
	}

	public static class PvPFlagTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public PvPFlagTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			long diff = Math.abs(System.currentTimeMillis() - player.getLastPvPAttack());
			if(diff > Config.PVP_TIME)
				player.stopPvPFlag();
			else if(diff > Config.PVP_TIME - 20000)
				player.updatePvPFlag(2);
			else
				player.updatePvPFlag(1);
		}
	}

	public static class HourlyTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public HourlyTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			int hoursInGame = player.getHoursInGame();
			player.sendPacket(new SystemMessage(764).addNumber(hoursInGame));
		}
	}

	public static class WaterTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public WaterTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			if(player.isDead() || !player.isInWater())
			{
				player.stopWaterTask();
				return;
			}
			double reduceHp = player.getMaxHp() < 100 ? 1.0 : player.getMaxHp() / 100;
			player.reduceCurrentHp(reduceHp, player, null, false, true, true, false, false, false, false);
			player.sendPacket(new SystemMessage(297).addNumber((long) reduceHp));
		}
	}

	public static class KickTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public KickTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.setOfflineMode(false);
			player.kick();
		}
	}

	public static class UnJailTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public UnJailTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.fromJail();
		}
	}

	public static class EndSitDownTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public EndSitDownTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.sittingTaskLaunched = false;
			player.getAI().clearNextAction();
		}
	}

	public static class EndStandUpTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public EndStandUpTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.setSitting(player.sittingTaskLaunched = false);
			if(!player.getAI().setNextIntention())
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}

	public static class EndBreakFakeDeathTask implements Runnable
	{
		private final HardReference<Player> _playerRef;

		public EndBreakFakeDeathTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.setFakeDeath(false);
			if(!player.getAI().setNextIntention())
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}

	public static class CastEndTimeTask implements Runnable
	{
		private final HardReference<? extends Creature> _charRef;
		private final List<Creature> _targets;

		public CastEndTimeTask(Creature character, List<Creature> targets)
		{
			_charRef = character.getRef();
			_targets = targets;
		}

		@Override
		public void run()
		{
			Creature character = _charRef.get();
			if(character == null)
				return;
			character.onCastEndTime(_targets, true);
		}
	}

	public static class HitTask implements Runnable
	{
		boolean _crit;
		boolean _miss;
		boolean _shld;
		boolean _soulshot;
		boolean _unchargeSS;
		boolean _notify;
		int _damage;
		int _sAtk;
		private final HardReference<? extends Creature> _charRef;
		private final HardReference<? extends Creature> _targetRef;

		public HitTask(Creature cha, Creature target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS, boolean notify, int sAtk)
		{
			_charRef = cha.getRef();
			_targetRef = target.getRef();
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
			_unchargeSS = unchargeSS;
			_notify = notify;
			_sAtk = sAtk;
		}

		@Override
		public void run()
		{
			Creature character;
			Creature target;
			if((character = _charRef.get()) == null || (target = _targetRef.get()) == null)
				return;
			if(character.isAttackAborted())
				return;
			character.onHitTimer(target, _damage, _crit, _miss, _soulshot, _shld, _unchargeSS);
			if(_notify)
				ThreadPoolManager.getInstance().schedule(new NotifyAITask(character, CtrlEvent.EVT_READY_TO_ACT), _sAtk / 2);
		}
	}

	public static class MagicUseTask implements Runnable
	{
		public boolean _forceUse;
		private final HardReference<? extends Creature> _charRef;

		public MagicUseTask(Creature cha, boolean forceUse)
		{
			_charRef = cha.getRef();
			_forceUse = forceUse;
		}

		@Override
		public void run()
		{
			Creature character = _charRef.get();
			if(character == null)
				return;
			Skill castingSkill = character.getCastingSkill();
			Creature castingTarget = character.getCastingTarget();
			if(castingSkill == null || castingTarget == null)
			{
				character.clearCastVars();
				return;
			}
			character.onMagicUseTimer(castingTarget, castingSkill, _forceUse);
		}
	}

	public static class MagicLaunchedTask implements Runnable
	{
		public boolean _forceUse;
		private final HardReference<? extends Creature> _charRef;

		public MagicLaunchedTask(Creature cha, boolean forceUse)
		{
			_charRef = cha.getRef();
			_forceUse = forceUse;
		}

		@Override
		public void run() {
			Creature character = _charRef.get();
			if (character == null)
				return;
			Skill castingSkill = character.getCastingSkill();
			Creature castingTarget = character.getCastingTarget();
			if (castingSkill == null || castingTarget == null) {
				character.clearCastVars();
				return;
			}
			if (castingSkill.isNotBroadcastable())
				return;
			List<Creature> targets = castingSkill.getTargets(character, castingTarget, _forceUse);
			int[] objectIds = Util.objectToIntArray(targets);
			character.broadcastPacket(new MagicSkillLaunchedPacket(character.getObjectId(), castingSkill.getDisplayId(), castingSkill.getDisplayLevel(), objectIds));
		}
	}

	public static class NotifyAITask implements Runnable
	{
		private final CtrlEvent _evt;
		private final Object _agr0;
		private final Object _agr1;
		private final Object _agr2;
		private final HardReference<? extends Creature> _charRef;

		public NotifyAITask(Creature cha, CtrlEvent evt, Object agr0, Object agr1, Object agr2)
		{
			_charRef = cha.getRef();
			_evt = evt;
			_agr0 = agr0;
			_agr1 = agr1;
			_agr2 = agr2;
		}

		public NotifyAITask(Creature cha, CtrlEvent evt, Object arg0)
		{
			this(cha, evt, arg0, null, null);
		}

		public NotifyAITask(Creature cha, CtrlEvent evt)
		{
			this(cha, evt, null, null, null);
		}

		@Override
		public void run()
		{
			Creature character = _charRef.get();
			if(character == null || !character.hasAI())
				return;
			character.getAI().notifyEvent(_evt, _agr0, _agr1, _agr2);
		}
	}
}

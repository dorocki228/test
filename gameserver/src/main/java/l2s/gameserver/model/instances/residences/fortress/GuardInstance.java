package l2s.gameserver.model.instances.residences.fortress;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Config;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.handler.onshiftaction.OnShiftActionHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.MTPPacket;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.time.Duration;
import java.util.Set;

public class GuardInstance extends NpcInstance
{
	public GuardInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
		setHasChatWindow(false);
	}

	@Override
	public boolean isSiegeGuard()
	{
		return true;
	}

	@Override
	public int getAggroRange()
	{
		return 1200;
	}

	@Override
	public boolean isAttackable(Creature attacker) {
		return isAutoAttackable(attacker);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		Player player = attacker.getPlayer();
		if(player == null)
			return false;

		return getFraction().canAttack(attacker.getFraction());
	}

	@Override
	public Clan getClan()
	{
		return null;
	}

	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);

		FortressSiegeEvent siege = getEvent(FortressSiegeEvent.class);
		if(siege != null && !siege.isInProgress())
			siege.startEvent();
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

		FortressSiegeEvent siege = getEvent(FortressSiegeEvent.class);
		if(siege != null)
		{
			setFraction(siege.getResidence().getFraction());
			broadcastCharInfo();
		}
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(!isTargetable(player))
		{
			player.sendActionFailed();
			return;
		}

		if(player.getTarget() != this)
		{
			player.setNpcTarget(this);
			return;
		}

		if(shift && OnShiftActionHolder.getInstance().callShiftAction(player, NpcInstance.class, this, true))
			return;

		if(isAutoAttackable(player))
		{
			FortressSiegeEvent siege = getEvent(FortressSiegeEvent.class);
			if(siege != null && !siege.isInProgress())
			{
				if(!siege.canStart())
				{
					Duration duration = siege.getTimeToStartSiege().abs();
					long minutes = duration.toMinutes();
					String message = "Siege will be available in " + minutes + " minutes.";

					player.sendMessage(message);

					HtmlMessage html = new HtmlMessage(getObjectId());
					html.setHtml(message);
					player.sendPacket(html);

					return;
				}
			}

			player.getAI().Attack(this, false, shift);
			return;
		}

		if(!checkInteractionDistance(player))
		{
			if(player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
			return;
		}

		if(!Config.ALLOW_TALK_WHILE_SITTING && player.isSitting() || player.isActionsDisabled())
		{
			player.sendActionFailed();
			return;
		}

		player.sendActionFailed();
		if(player.isMoving())
			player.stopMove();
		player.sendPacket(new MTPPacket(player, this, 200));
		if(isBusy())
			showBusyWindow(player);
		else if(isHasChatWindow())
		{
			boolean flag = false;
			Set<Quest> quests = getTemplate().getEventQuests(QuestEventType.NPC_FIRST_TALK);
			if(quests != null)
				for(Quest quest : quests)
				{
					QuestState qs = player.getQuestState(quest);
					if((qs == null || !qs.isCompleted()) && quest.notifyFirstTalk(this, player))
						flag = true;
				}
			if(!flag)
			{
				showChatWindow(player, 0, true);
				if(Config.NPC_DIALOG_PLAYER_DELAY > 0)
					player.setNpcDialogEndTime((int) (System.currentTimeMillis() / 1000L) + Config.NPC_DIALOG_PLAYER_DELAY);
			}
		}
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean isPeaceNpc()
	{
		return false;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}
}
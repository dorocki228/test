package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.dao.MailDAO;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.handler.admincommands.impl.AdminEffects;
import l2s.gameserver.instancemanager.CoupleManager;
import l2s.gameserver.instancemanager.OfflineBufferManager;
import l2s.gameserver.instancemanager.PetitionManager;
import l2s.gameserver.instancemanager.PlayerMessageStack;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2s.gameserver.listener.hooks.ListenerHook;
import l2s.gameserver.listener.hooks.ListenerHookType;
import l2s.gameserver.model.*;
import l2s.gameserver.model.actor.CreatureSkillCast;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.authcomm.gs2as.ChangeAllowedHwid;
import l2s.gameserver.network.authcomm.gs2as.ChangeAllowedIp;
import l2s.gameserver.network.l2.ConnectionState;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.network.l2.s2c.updatetype.NpcInfoType;
import l2s.gameserver.skills.AbnormalVisualEffect;
import l2s.gameserver.skills.SkillCastingType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.triggers.TriggerType;
import l2s.gameserver.utils.GameStats;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.TradeHelper;
import org.napile.primitive.pair.IntObjectPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EnterWorld implements IClientIncomingPacket
{
	private static final Object _lock = new Object();

	private static final Logger _log = LoggerFactory.getLogger(EnterWorld.class);

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		//packet.readS(); - ???????????? ???????????? ???????????????????? ???????????? "narcasse"
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();

		if(activeChar == null)
		{
			client.closeNow();
			return;
		}
		
		GameStats.incrementPlayerEnterGame();

		client.setConnectionState(ConnectionState.IN_GAME);

		onEnterWorld(activeChar);
	}

	public static void onEnterWorld(Player activeChar)
	{
		boolean first = activeChar.entering;

		activeChar.sendPacket(ExLightingCandleEvent.DISABLED);
		//TODO: activeChar.sendPacket(new ExChannlChatEnterWorld(activeChar));
		//TODO: activeChar.sendPacket(new ExChannlChatPlegeInfo(activeChar));
		activeChar.sendPacket(new ExEnterWorldPacket());
		activeChar.sendPacket(new ExOneDayRewardInfo(activeChar));
		activeChar.sendPacket(new ExPeriodicHenna(activeChar));
		activeChar.sendPacket(new HennaInfoPacket(activeChar));

		activeChar.getMacroses().sendMacroses();

		activeChar.sendPacket(new EtcStatusUpdatePacket(activeChar));

		activeChar.sendItemList(false);
		activeChar.sendPacket(new ExAdenaInvenCount(activeChar));
		activeChar.sendPacket(new ExBloodyCoinCount(activeChar));

		activeChar.sendPacket(new ShortCutInitPacket(activeChar));
		activeChar.sendPacket(new ExBasicActionList(activeChar));

		activeChar.sendSkillList();

		List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
		for(Castle c : castleList)
			activeChar.sendPacket(new ExCastleState(c));

		activeChar.sendPacket(new UIPacket(activeChar));
		activeChar.sendPacket(new ExUserInfoInvenWeight(activeChar));
		activeChar.sendPacket(new ExUserInfoEquipSlot(activeChar));
		activeChar.sendPacket(new ExUserInfoCubic(activeChar));
		activeChar.sendPacket(new ExUserInfoAbnormalVisualEffect(activeChar));

		activeChar.sendPacket(new QuestListPacket(activeChar));

		// Send SubClass Info
		//player.sendPacket(new ExSubjobInfo(player, SubclassInfoType.NO_CHANGES));

		activeChar.sendPacket(SystemMsg.WELCOME_TO_THE_WORLD_OF_LINEAGE_II);

		double mpCostDiff = activeChar.getMPCostDiff(Skill.SkillMagicType.PHYSIC);
		if(mpCostDiff != 0)
			activeChar.sendPacket(new ExChangeMPCost(Skill.SkillMagicType.PHYSIC, mpCostDiff));

		mpCostDiff = activeChar.getMPCostDiff(Skill.SkillMagicType.MAGIC);
		if(mpCostDiff != 0)
			activeChar.sendPacket(new ExChangeMPCost(Skill.SkillMagicType.MAGIC, mpCostDiff));

		mpCostDiff = activeChar.getMPCostDiff(Skill.SkillMagicType.MUSIC);
		if(mpCostDiff != 0)
			activeChar.sendPacket(new ExChangeMPCost(Skill.SkillMagicType.MUSIC, mpCostDiff));

		activeChar.initActiveAutoShots();

		activeChar.sendPacket(new ExTutorialList());
		activeChar.sendPacket(new QuestListPacket(activeChar));

		activeChar.sendPacket(new ExUserBookMark(activeChar));

		Announcements.getInstance().showAnnouncements(activeChar);

		if(first)
		{
			activeChar.setOnlineStatus(true);
			if(activeChar.getPlayerAccess().GodMode && !Config.SHOW_GM_LOGIN && !Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			{
				activeChar.setGMInvisible(true);
				activeChar.startAbnormalEffect(AbnormalVisualEffect.STEALTH);
			}

			if (!activeChar.isInPvPEvent()) {
				activeChar.setNonAggroTime(Long.MAX_VALUE);
				activeChar.setNonPvpTime(System.currentTimeMillis() + Config.NONPVP_TIME_ONTELEPORT);
			}

			if(activeChar.isInBuffStore())
			{
				activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
			}
			else if(activeChar.isInStoreMode())
			{
				if(!TradeHelper.validateStore(activeChar))
				{
					activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
					activeChar.storePrivateStore();
				}
			}

			activeChar.setRunning();
			activeChar.standUp();
			activeChar.spawnMe();
			activeChar.startTimers();
		}

		activeChar.sendPacket(new ExBR_PremiumStatePacket(activeChar, activeChar.hasPremiumAccount()));

		activeChar.sendPacket(new ExSetCompassZoneCode(activeChar));
		//TODO: ?????????????????? ???????????????????? ????????????.
		activeChar.sendPacket(new MagicAndSkillList(activeChar, 3503292, 730502));
		activeChar.sendPacket(new ExStorageMaxCount(activeChar));
		activeChar.getAttendanceRewards().onEnterWorld();
		activeChar.sendPacket(new ExShowPostFriend(activeChar));

		if(Config.ALLOW_WORLD_CHAT)
			activeChar.sendPacket(new ExWorldChatCnt(activeChar));

		if(Config.EX_USE_PRIME_SHOP)
		{
			activeChar.sendPacket(new ExBR_ExistNewProductAck(activeChar));
			activeChar.sendPacket(new ExVipInfo(activeChar));
		}

		activeChar.sendPacket(new ElementalSpiritInfo(activeChar, 2));

		if(!Config.EX_COSTUME_DISABLE)
			activeChar.sendPacket(new ExCostumeShortcutList(activeChar));

		checkNewMail(activeChar);

		if(first)
			activeChar.getListeners().onEnter();

		activeChar.checkAndDeleteOlympiadItems();

		if(activeChar.getClan() != null)
		{
			activeChar.getClan().loginClanCond(activeChar, true);

			activeChar.sendPacket(activeChar.getClan().listAll());
			activeChar.sendPacket(new ExPledgeSkillList(activeChar.getClan()));
		}
		else
			activeChar.sendPacket(new ExPledgeCount(0));

		// engage and notify Partner
		if(first && Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance().engage(activeChar);
			CoupleManager.getInstance().notifyPartner(activeChar);
		}

		if(first)
		{
			activeChar.getFriendList().notifyFriends(true);
			//activeChar.restoreDisableSkills(); ?????????? ???????????? ?????????????????? ?????????? ???????????????
		}

		activeChar.checkHpMessages(activeChar.getMaxHp(), activeChar.getCurrentHp());
		activeChar.checkDayNightMessages();

		if(Config.SHOW_HTML_WELCOME)
		{
			String html = HtmCache.getInstance().getHtml("welcome.htm", activeChar);
			HtmlMessage msg = new HtmlMessage(5);
			msg.setHtml(HtmlUtils.bbParse(html));
			activeChar.sendPacket(msg);
		}

		if(Config.PETITIONING_ALLOWED)
			PetitionManager.getInstance().checkPetitionMessages(activeChar);

		if(!first)
		{
			CreatureSkillCast skillCast = activeChar.getSkillCast(SkillCastingType.NORMAL);
			if(skillCast.isCastingNow())
			{
				Creature castingTarget = skillCast.getTarget();
				SkillEntry castingSkillEntry = skillCast.getSkillEntry();
				long animationEndTime = skillCast.getAnimationEndTime();
				if(castingSkillEntry != null && !castingSkillEntry.getTemplate().isNotBroadcastable() && castingTarget != null && castingTarget.isCreature() && animationEndTime > 0)
					activeChar.sendPacket(new MagicSkillUse(activeChar, castingTarget, castingSkillEntry.getId(), castingSkillEntry.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0, SkillCastingType.NORMAL));
			}

			skillCast = activeChar.getSkillCast(SkillCastingType.NORMAL_SECOND);
			if(skillCast.isCastingNow())
			{
				Creature castingTarget = skillCast.getTarget();
				SkillEntry castingSkillEntry = skillCast.getSkillEntry();
				long animationEndTime = skillCast.getAnimationEndTime();
				if(castingSkillEntry != null && !castingSkillEntry.getTemplate().isNotBroadcastable() && castingTarget != null && castingTarget.isCreature() && animationEndTime > 0)
					activeChar.sendPacket(new MagicSkillUse(activeChar, castingTarget, castingSkillEntry.getId(), castingSkillEntry.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0, SkillCastingType.NORMAL_SECOND));
			}

			if(activeChar.isInBoat())
				activeChar.sendPacket(activeChar.getBoat().getOnPacket(activeChar, activeChar.getInBoatPosition()));

			if(activeChar.getMovement().isMoving() || activeChar.getMovement().isFollow())
				activeChar.sendPacket(activeChar.movePacket());

			if(activeChar.getMountNpcId() != 0)
				activeChar.sendPacket(new RidePacket(activeChar));

			if(activeChar.isFishing())
				activeChar.getFishing().stop();
		}

		activeChar.entering = false;

		if(activeChar.isSitting())
			activeChar.sendPacket(new ChangeWaitTypePacket(activeChar, ChangeWaitTypePacket.WT_SITTING));
		if(activeChar.isInStoreMode())
			activeChar.sendPacket(activeChar.getPrivateStoreMsgPacket(activeChar));

		activeChar.unsetVar("offline");
		activeChar.unsetVar("offlinebuff");
		activeChar.unsetVar("offlinebuff_price");
		activeChar.unsetVar("offlinebuff_skills");
		activeChar.unsetVar("offlinebuff_title");

		OfflineBufferManager.getInstance().getBuffStores().remove(activeChar.getObjectId());

		// ???? ???????????? ????????????
		activeChar.sendActionFailed();

		if(first && activeChar.isGM() && Config.SAVE_GM_EFFECTS && activeChar.getPlayerAccess().CanUseGMCommand)
		{
			//silence
			if(activeChar.getVarBoolean("gm_silence"))
			{
				activeChar.setMessageRefusal(true);
				activeChar.sendPacket(SystemMsg.MESSAGE_REFUSAL_MODE);
			}
			//invul
			if(activeChar.getVarBoolean("gm_invul"))
			{
				activeChar.getFlags().getInvulnerable().start();
				activeChar.startAbnormalEffect(AbnormalVisualEffect.INVINCIBILITY);
				activeChar.sendMessage(activeChar.getName() + " is now immortal.");
			}
			//undying
			if(activeChar.getVarBoolean("gm_undying"))
			{
				activeChar.setGMUndying(true);
				activeChar.sendMessage("Undying state has been enabled.");
			}

			int gmspeed = activeChar.getVarInt("gm_gmspeed", 0);
			AdminEffects.addGmSpeedStats(activeChar, gmspeed);
		}

		PlayerMessageStack.getInstance().CheckMessages(activeChar);

		IntObjectPair<OnAnswerListener> entry = activeChar.getAskListener(false);
		if(entry != null && entry.getValue() instanceof ReviveAnswerListener)
			activeChar.sendPacket(new ConfirmDlgPacket(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0).addString("Other player").addString("some"));

		if(!first)
		{
			//???????????????? ?????????????? ???? ?????????? ??????????????????
			if(activeChar.isInObserverMode())
			{
				if(activeChar.getObserverMode() == Player.OBSERVER_LEAVING)
					activeChar.returnFromObserverMode();
				else
					activeChar.leaveObserverMode();
			}
			else if(activeChar.isVisible())
				World.showObjectsToPlayer(activeChar);

			final List<Servitor> servitors = activeChar.getServitors();

			for(Servitor servitor : servitors)
				activeChar.sendPacket(new PetInfo(servitor));

			if(activeChar.isInParty())
			{
				Party party = activeChar.getParty();
				Player leader = party.getPartyLeader();
				if(leader != null) // ??????????????????, ???? ?????????? NPE.
				{
					//sends new member party window for all members
					//we do all actions before adding member to a list, this speeds things up a little
					activeChar.sendPacket(new PartySmallWindowAllPacket(party, leader, activeChar));

					RelationChangedPacket rcp = new RelationChangedPacket();
					for(Player member : party.getPartyMembers())
					{
						if(member != activeChar)
						{
							activeChar.sendPacket(new PartySpelledPacket(member, true));

							for(Servitor servitor : servitors)
								activeChar.sendPacket(new PartySpelledPacket(servitor, true));

							rcp.add(member, activeChar);
							for(Servitor servitor : member.getServitors())
								rcp.add(servitor, activeChar);

							for(Servitor servitor : servitors)
								servitor.broadcastCharInfoImpl(activeChar, NpcInfoType.VALUES);
						}
					}

					activeChar.sendPacket(rcp);

					// ???????? ???????????? ?????? ?? ????, ???? ?????????? ?????????????????? ???????????????? ?????????? ???????????????? ???????? ????
					if(party.isInCommandChannel())
						activeChar.sendPacket(ExOpenMpccPacket.STATIC);
				}
			}

			activeChar.sendActiveAutoShots();

			for(Abnormal e : activeChar.getAbnormalList())
			{
				if(e.getSkill().isToggle() && !e.getSkill().isNotBroadcastable())
					activeChar.sendPacket(new MagicSkillLaunchedPacket(activeChar.getObjectId(), e.getSkill().getId(), e.getSkill().getLevel(), activeChar, SkillCastingType.NORMAL));
			}

			activeChar.broadcastCharInfo();
		}

		if(activeChar.isDead())
			activeChar.sendPacket(new DiePacket(activeChar));

		activeChar.updateAbnormalIcons();
		activeChar.updateStats();

		if(Config.ALT_PCBANG_POINTS_ENABLED)
		{
			if(!Config.ALT_PCBANG_POINTS_ONLY_PREMIUM || activeChar.hasPremiumAccount())
				activeChar.sendPacket(new ExPCCafePointInfo(activeChar, 0, 1, 2, 12));
		}
		
		activeChar.checkLevelUpReward(true);

		if(first)
		{
			activeChar.useTriggers(activeChar, TriggerType.ON_ENTER_WORLD, null, null, 0);

			for(ListenerHook hook : ListenerHook.getGlobalListenerHooks(ListenerHookType.PLAYER_ENTER_GAME))
				hook.onPlayerEnterGame(activeChar);

			if(Config.ALLOW_IP_LOCK && Config.AUTO_LOCK_IP_ON_LOGIN)
				GameServer.getInstance().getAuthServerCommunication().sendPacket(new ChangeAllowedIp(activeChar.getAccountName(), activeChar.getIP()));

			if(Config.ALLOW_HWID_LOCK && Config.AUTO_LOCK_HWID_ON_LOGIN)
			{
				GameClient client = activeChar.getNetConnection();
				if(client != null)
					GameServer.getInstance().getAuthServerCommunication().sendPacket(new ChangeAllowedHwid(activeChar.getAccountName(), client.getHWID()));
			}
		}

		activeChar.getInventory().checkItems();
	}

	private static void checkNewMail(Player activeChar)
	{
		activeChar.sendPacket(new ExUnReadMailCount(activeChar));
		for(Mail mail : MailDAO.getInstance().getReceivedMailByOwnerId(activeChar.getObjectId()))
		{
			if(mail.isUnread())
			{
				activeChar.sendPacket(ExNoticePostArrived.STATIC_FALSE);
				break;
			}
		}
	}
}
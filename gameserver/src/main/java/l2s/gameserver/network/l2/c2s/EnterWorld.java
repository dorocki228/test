package l2s.gameserver.network.l2.c2s;

import gve.util.GveMessageUtil;
import java.util.Calendar;
import java.util.List;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.dao.MailDAO;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.string.StringsHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.handler.bbs.BbsHandlerHolder;
import l2s.gameserver.instancemanager.CoupleManager;
import l2s.gameserver.instancemanager.PetitionManager;
import l2s.gameserver.instancemanager.PlayerMessageStack;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.base.SpecialEffectState;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ChangeWaitTypePacket;
import l2s.gameserver.network.l2.s2c.ConfirmDlgPacket;
import l2s.gameserver.network.l2.s2c.DiePacket;
import l2s.gameserver.network.l2.s2c.EtcStatusUpdatePacket;
import l2s.gameserver.network.l2.s2c.ExAdenaInvenCount;
import l2s.gameserver.network.l2.s2c.ExBR_PremiumStatePacket;
import l2s.gameserver.network.l2.s2c.ExBasicActionList;
import l2s.gameserver.network.l2.s2c.ExCastleState;
import l2s.gameserver.network.l2.s2c.ExChangeMPCost;
import l2s.gameserver.network.l2.s2c.ExConnectedTimeAndGettableReward;
import l2s.gameserver.network.l2.s2c.ExGetBookMarkInfoPacket;
import l2s.gameserver.network.l2.s2c.ExLightingCandleEvent;
import l2s.gameserver.network.l2.s2c.ExNoticePostArrived;
import l2s.gameserver.network.l2.s2c.ExOpenMPCCPacket;
import l2s.gameserver.network.l2.s2c.ExPCCafePointInfoPacket;
import l2s.gameserver.network.l2.s2c.ExPeriodicHenna;
import l2s.gameserver.network.l2.s2c.ExPledgeCount;
import l2s.gameserver.network.l2.s2c.ExReceiveShowPostFriend;
import l2s.gameserver.network.l2.s2c.ExSetCompassZoneCode;
import l2s.gameserver.network.l2.s2c.ExStorageMaxCountPacket;
import l2s.gameserver.network.l2.s2c.ExUnReadMailCount;
import l2s.gameserver.network.l2.s2c.ExUserInfoAbnormalVisualEffect;
import l2s.gameserver.network.l2.s2c.ExUserInfoCubic;
import l2s.gameserver.network.l2.s2c.ExUserInfoEquipSlot;
import l2s.gameserver.network.l2.s2c.ExUserInfoInvenWeight;
import l2s.gameserver.network.l2.s2c.ExWorldChatCnt;
import l2s.gameserver.network.l2.s2c.HennaInfoPacket;
import l2s.gameserver.network.l2.s2c.MagicAndSkillList;
import l2s.gameserver.network.l2.s2c.MagicSkillLaunchedPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.PartySmallWindowAllPacket;
import l2s.gameserver.network.l2.s2c.PartySpelledPacket;
import l2s.gameserver.network.l2.s2c.PetInfoPacket;
import l2s.gameserver.network.l2.s2c.PledgeSkillListPacket;
import l2s.gameserver.network.l2.s2c.QuestListPacket;
import l2s.gameserver.network.l2.s2c.RelationChangedPacket;
import l2s.gameserver.network.l2.s2c.RidePacket;
import l2s.gameserver.network.l2.s2c.ShortCutInitPacket;
import l2s.gameserver.network.l2.s2c.UIPacket;
import l2s.gameserver.network.l2.s2c.updatetype.NpcInfoType;
import l2s.gameserver.stats.triggers.TriggerType;
import l2s.gameserver.utils.GameStats;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.TradeHelper;
import org.napile.pair.primitive.IntObjectPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnterWorld extends L2GameClientPacket
{
	private static final Object _lock = new Object();
	private static final Logger _log = LoggerFactory.getLogger(EnterWorld.class);

	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
		{
			client.closeNow(false);
			return;
		}
		GameStats.incrementPlayerEnterGame();
		boolean first = activeChar.entering;
		activeChar.sendPacket(ExLightingCandleEvent.DISABLED);
		activeChar.sendPacket(new ExPeriodicHenna(activeChar));
		activeChar.sendPacket(new HennaInfoPacket(activeChar));
		List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
		for(Castle c : castleList)
			activeChar.sendPacket(new ExCastleState(c));
		activeChar.sendSkillList();
		activeChar.sendPacket(new EtcStatusUpdatePacket(activeChar));
		activeChar.sendPacket(new UIPacket(activeChar));
		activeChar.sendPacket(new ExUserInfoInvenWeight(activeChar));
		activeChar.sendPacket(new ExUserInfoEquipSlot(activeChar));
		activeChar.sendPacket(new ExUserInfoCubic(activeChar));
		activeChar.sendPacket(new ExUserInfoAbnormalVisualEffect(activeChar));
		activeChar.sendPacket(SystemMsg.WELCOME_TO_THE_WORLD_OF_LINEAGE_II);
		double mpCostDiff = activeChar.getMPCostDiff(Skill.SkillMagicType.PHYSIC);
		if(mpCostDiff != 0.0)
			activeChar.sendPacket(new ExChangeMPCost(Skill.SkillMagicType.PHYSIC, mpCostDiff));
		mpCostDiff = activeChar.getMPCostDiff(Skill.SkillMagicType.MAGIC);
		if(mpCostDiff != 0.0)
			activeChar.sendPacket(new ExChangeMPCost(Skill.SkillMagicType.MAGIC, mpCostDiff));
		mpCostDiff = activeChar.getMPCostDiff(Skill.SkillMagicType.MUSIC);
		if(mpCostDiff != 0.0)
			activeChar.sendPacket(new ExChangeMPCost(Skill.SkillMagicType.MUSIC, mpCostDiff));
		activeChar.sendPacket(new QuestListPacket(activeChar));

		activeChar.initActiveAutoShots();

		activeChar.sendPacket(new ExGetBookMarkInfoPacket(activeChar));
		activeChar.sendItemList(false);
		activeChar.sendPacket(new ExAdenaInvenCount(activeChar));
		activeChar.sendPacket(new ShortCutInitPacket(activeChar));
		activeChar.sendPacket(new ExBasicActionList(activeChar));
		activeChar.getMacroses().sendMacroses();
		Announcements.getInstance().showAnnouncements(activeChar);
		if(first)
		{
			activeChar.setOnlineStatus(true);
			if(activeChar.getPlayerAccess().GodMode && !Config.SHOW_GM_LOGIN)
				activeChar.setGMInvisible(true);

			activeChar.setNonAggroTime(Long.MAX_VALUE);
			activeChar.setNonPvpTime(System.currentTimeMillis() + Config.NONPVP_TIME_ONTELEPORT);
			if(activeChar.isInStoreMode() && !TradeHelper.validateStore(activeChar))
			{
				activeChar.setPrivateStoreType(0);
				activeChar.standUp();
			}
			activeChar.setRunning();
			activeChar.standUp();
			activeChar.spawnMe();
			activeChar.startTimers();
			activeChar.enterWorld();
		}
		activeChar.sendPacket(new ExBR_PremiumStatePacket(activeChar, activeChar.hasPremiumAccount()));
		activeChar.sendPacket(new ExSetCompassZoneCode(activeChar));
		activeChar.sendPacket(new MagicAndSkillList(activeChar, 3503292, 730502));
		activeChar.sendPacket(new ExStorageMaxCountPacket(activeChar));
		activeChar.getAttendanceRewards().onEnterWorld();
		activeChar.sendPacket(new ExReceiveShowPostFriend(activeChar));
		if(Config.ALLOW_FRACTION_WORLD_CHAT)
			activeChar.sendPacket(new ExWorldChatCnt(activeChar));
		checkNewMail(activeChar);
		if(first)
			activeChar.getListeners().onEnter();
		if(first && activeChar.getCreateTime() > 0L)
		{
			Calendar create = Calendar.getInstance();
			create.setTimeInMillis(activeChar.getCreateTime());
			Calendar now = Calendar.getInstance();
			int day = create.get(5);
			if(create.get(2) == 1 && day == 29)
				day = 28;
			int myBirthdayReceiveYear = activeChar.getVarInt("MyBirthdayReceiveYear", 0);
			if(create.get(2) == now.get(2) && create.get(5) == day && (myBirthdayReceiveYear == 0 && create.get(1) != now.get(1) || myBirthdayReceiveYear > 0 && myBirthdayReceiveYear != now.get(1)))
			{
				Mail mail = new Mail();
				mail.setSenderId(1);
				mail.setSenderName(StringsHolder.getInstance().getString(activeChar, "birthday.npc"));
				mail.setReceiverId(activeChar.getObjectId());
				mail.setReceiverName(activeChar.getName());
				mail.setTopic(StringsHolder.getInstance().getString(activeChar, "birthday.title"));
				mail.setBody(StringsHolder.getInstance().getString(activeChar, "birthday.text"));
				ItemInstance item = ItemFunctions.createItem(21169);
				item.setLocation(ItemInstance.ItemLocation.MAIL);
				item.setCount(1L);
				item.save();
				mail.addAttachment(item);
				mail.setUnread(true);
				mail.setType(Mail.SenderType.BIRTHDAY);
				mail.setExpireTime(2592000 + (int) (System.currentTimeMillis() / 1000L));
				mail.save();
				activeChar.setVar("MyBirthdayReceiveYear", String.valueOf(now.get(1)), -1L);
			}
		}

		if(activeChar.getClan() != null)
		{
			activeChar.getClan().loginClanCond(activeChar, true);
			activeChar.sendPacket(activeChar.getClan().listAll());
			activeChar.sendPacket(new PledgeSkillListPacket(activeChar.getClan()));
		}
		else
			activeChar.sendPacket(new ExPledgeCount(0));
		if(first && Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance().engage(activeChar);
			CoupleManager.getInstance().notifyPartner(activeChar);
		}
		if(first)
			activeChar.getFriendList().notifyFriends(true);
		activeChar.checkHpMessages(activeChar.getMaxHp(), activeChar.getCurrentHp());
		activeChar.checkDayNightMessages();
		if(Config.SHOW_HTML_WELCOME && !activeChar.getVarBoolean("welcome_html_shown", false))
		{
			String html = HtmCache.getInstance().getHtml("welcome.htm", activeChar);
			HtmlMessage msg = new HtmlMessage(5);
			msg.setHtml(HtmlUtils.bbParse(html));
			activeChar.sendPacket(msg);

			activeChar.setVar("welcome_html_shown", true);
		}
		if(Config.PETITIONING_ALLOWED)
			PetitionManager.getInstance().checkPetitionMessages(activeChar);
		if(!first)
		{
			if(activeChar.isCastingNow())
			{
				Creature castingTarget = activeChar.getCastingTarget();
				Skill castingSkill = activeChar.getCastingSkill();
				long animationEndTime = activeChar.getAnimationEndTime();
				if(castingSkill != null && !castingSkill.isNotBroadcastable() && castingTarget != null && castingTarget.isCreature() && animationEndTime > 0L)
                    sendPacket(new MagicSkillUse(activeChar, castingTarget, castingSkill.getId(), castingSkill.getLevel(), (int) (animationEndTime - System.currentTimeMillis()), 0L));
			}
			if(activeChar.isInBoat())
				activeChar.sendPacket(activeChar.getBoat().getOnPacket(activeChar, activeChar.getInBoatPosition()));
			if(activeChar.isMoving() || activeChar.isFollowing())
                sendPacket(activeChar.movePacket());
			if(activeChar.getMountNpcId() != 0)
                sendPacket(new RidePacket(activeChar));
			if(activeChar.isFishing())
				activeChar.getFishing().stop();
		}
		activeChar.entering = false;
		if(activeChar.isSitting())
			activeChar.sendPacket(new ChangeWaitTypePacket(activeChar, 0));
		if(activeChar.isInStoreMode())
			activeChar.sendPacket(activeChar.getPrivateStoreMsgPacket(activeChar));
		activeChar.unsetVar("offline");
		activeChar.sendActionFailed();

		String heroPeriod = activeChar.getVar("HeroPeriod");
		if(heroPeriod != null && Long.parseLong(heroPeriod) > System.currentTimeMillis())
		{
			activeChar.setHero(true);
			activeChar.broadcastUserInfo(true);
		}

		if(first && activeChar.isGM() && Config.SAVE_GM_EFFECTS && activeChar.getPlayerAccess().CanUseGMCommand)
		{
			if(activeChar.getVarBoolean("gm_silence"))
			{
				activeChar.setMessageRefusal(true);
				activeChar.sendPacket(SystemMsg.MESSAGE_REFUSAL_MODE);
			}
			if(activeChar.getVarBoolean("gm_invul"))
			{
				activeChar.setInvul(true);
				activeChar.sendMessage(activeChar.getName() + " is now immortal.");
			}
			if(activeChar.getVarBoolean("gm_undying"))
			{
				activeChar.setUndying(SpecialEffectState.GM);
				activeChar.sendMessage("Undying state has been enabled.");
			}
			try
			{
				int var_gmspeed = Integer.parseInt(activeChar.getVar("gm_gmspeed"));
				if(var_gmspeed >= 1 && var_gmspeed <= 4)
					activeChar.doCast(SkillHolder.getInstance().getSkillEntry(7029, var_gmspeed), activeChar, true);
			}
			catch(Exception ex)
			{}
		}
		PlayerMessageStack.getInstance().CheckMessages(activeChar);
		IntObjectPair<OnAnswerListener> entry = activeChar.getAskListener(false);
		if(entry != null && entry.getValue() instanceof ReviveAnswerListener)
            sendPacket(new ConfirmDlgPacket(SystemMsg.C1_IS_MAKING_AN_ATTEMPT_TO_RESURRECT_YOU_IF_YOU_CHOOSE_THIS_PATH_S2_EXPERIENCE_WILL_BE_RETURNED_FOR_YOU, 0).addString("Other player").addString("some"));
		if(!first)
		{
			if(activeChar.isInObserverMode())
			{
				if(activeChar.getObserverMode() == 2)
					activeChar.returnFromObserverMode();
				else
					activeChar.leaveObserverMode();
			}
			else if(activeChar.isVisible())
				World.showObjectsToPlayer(activeChar);
			List<Servitor> servitors = activeChar.getServitors();
			for(Servitor servitor : servitors)
                sendPacket(new PetInfoPacket(servitor));
			if(activeChar.isInParty())
			{
                sendPacket(new PartySmallWindowAllPacket(activeChar.getParty(), activeChar));
				RelationChangedPacket rcp = new RelationChangedPacket();
				for(Player member : activeChar.getParty().getPartyMembers())
					if(member != activeChar)
					{
                        sendPacket(new PartySpelledPacket(member, true));
						for(Servitor servitor2 : servitors)
                            sendPacket(new PartySpelledPacket(servitor2, true));
						rcp.add(member, activeChar);
						for(Servitor servitor2 : member.getServitors())
							rcp.add(servitor2, activeChar);
						for(Servitor servitor2 : servitors)
							servitor2.broadcastCharInfoImpl(activeChar, NpcInfoType.VALUES);
					}
                sendPacket(rcp);
				if(activeChar.getParty().isInCommandChannel())
                    sendPacket(ExOpenMPCCPacket.STATIC);
			}

			for(Abnormal e : activeChar.getAbnormalList().getFirstEffects())
				if(e.getSkill().isToggle() && !e.getSkill().isNotBroadcastable())
                    sendPacket(new MagicSkillLaunchedPacket(activeChar.getObjectId(), e.getSkill().getId(), e.getSkill().getLevel(), activeChar.getObjectId()));
			activeChar.broadcastCharInfo();
		}
		if(activeChar.isDead())
            sendPacket(new DiePacket(activeChar));

		activeChar.sendPacket(new ExConnectedTimeAndGettableReward(activeChar));
		GveMessageUtil.updateProtectMessage(activeChar);
		activeChar.updateEffectIcons();
		activeChar.updateStats();
		if(Config.ALT_PCBANG_POINTS_ENABLED && (!Config.ALT_PCBANG_POINTS_ONLY_PREMIUM || activeChar.hasPremiumAccount()))
			activeChar.sendPacket(new ExPCCafePointInfoPacket(activeChar, 0, 1, 2, 12));
		activeChar.checkLevelUpReward(true);
		if(first)
			activeChar.useTriggers(activeChar, TriggerType.ON_ENTER_WORLD, null, null, 0.0);

		if (activeChar.getFraction() != Fraction.NONE) {
			BbsHandlerHolder.getInstance()
				.getCommunityHandler("_bbshome")
				.onBypassCommand(activeChar, "_bbshome");
		}
	}

	private void checkNewMail(Player activeChar)
	{
		activeChar.sendPacket(new ExUnReadMailCount(activeChar));
		for(Mail mail : MailDAO.getInstance().getReceivedMailByOwnerId(activeChar.getObjectId()))
			if(mail.isUnread())
			{
				activeChar.sendPacket(ExNoticePostArrived.STATIC_FALSE);
				break;
			}
	}
}

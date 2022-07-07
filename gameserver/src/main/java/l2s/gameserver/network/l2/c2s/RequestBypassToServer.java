package l2s.gameserver.network.l2.c2s;

import java.lang.reflect.Method;
import java.util.StringTokenizer;
import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.handler.admincommands.AdminCommandHandler;
import l2s.gameserver.handler.bbs.BbsHandlerHolder;
import l2s.gameserver.handler.bbs.IBbsHandler;
import l2s.gameserver.handler.bypass.BypassHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.instancemanager.MatchingRoomManager;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ListPartyWatingPacket;
import l2s.gameserver.network.l2.s2c.ShowBoardPacket;
import l2s.gameserver.service.PaidActionsStatsService;
import l2s.gameserver.service.PaidActionsStatsService.PaidActionType;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.BypassStorage;
import l2s.gameserver.utils.BypassStorage.BypassType;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.NpcUtils;
import l2s.gameserver.utils.WarehouseFunctions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestBypassToServer extends L2GameClientPacket
{
	private static final Logger _log;
	private String _bypass;

	public RequestBypassToServer()
	{
		_bypass = null;
	}

	@Override
	protected void readImpl()
	{
		_bypass = readS();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _bypass.isEmpty())
			return;
		BypassStorage.ValidBypass bp = activeChar.getBypassStorage().validate(_bypass);
		if(bp == null)
		{
			_log.debug("RequestBypassToServer: Unexpected bypass : " + _bypass + " client : " + getClient() + "!");
			return;
		}
		NpcInstance npc = activeChar.getLastNpc();
		try
		{
			if(_bypass.startsWith("admin_"))
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, _bypass);
			else if(_bypass.startsWith("pcbang?"))
			{
				String command = _bypass.substring(7).trim();
				StringTokenizer st = new StringTokenizer(command, "_");
				String cmd = st.nextToken();
				if("multisell".equalsIgnoreCase(cmd))
				{
					int multisellId = Integer.parseInt(st.nextToken());
					if(!Config.ALT_ALLOWED_MULTISELLS_IN_PCBANG.contains(multisellId))
					{
						_log.warn("Unknown multisell list use in PC-Bang shop! List ID: " + multisellId + ", player ID: " + activeChar.getObjectId() + ", player name: " + activeChar.getName());
						return;
					}
					MultiSellHolder.getInstance().SeparateAndSend(multisellId, activeChar,
							npc != null ? npc.getObjectId() : -1, 0.0);
				}
			}
			else if(_bypass.startsWith("scripts_"))
				_log.error("Trying to call script bypass: " + _bypass + " " + activeChar);
			else if(_bypass.startsWith("htmbypass_"))
			{
				String command = _bypass.substring(10).trim();
				String word = command.split("\\s+")[0];
                Pair<Object, Method> b = BypassHolder.getInstance().getBypass(word);
				if(b != null)
					try
					{
                        String args = command.substring(word.length()).trim();
                        b.getValue().invoke(b.getKey(), activeChar, npc, StringUtils.isEmpty(args) ? new String[0] : args.split("\\s+"));
					}
					catch(Exception e)
					{
						_log.error("Exception: " + e, e);
					}
				else
					_log.warn("Cannot find html bypass: " + command);
			}
			else if(_bypass.startsWith("user_"))
			{
				String command = _bypass.substring(5).trim();
				String word = command.split("\\s+")[0];
				String args = command.substring(word.length()).trim();
				IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(word);
				if(vch != null)
					vch.useVoicedCommand(word, activeChar, args);
				else
					_log.warn("Unknow voiced command '" + word + "'");
			}
			else if(_bypass.startsWith("npc_"))
			{
				int endOfId = _bypass.indexOf(95, 5);
				String id;
				if(endOfId > 0)
					id = _bypass.substring(4, endOfId);
				else
					id = _bypass.substring(4);

				GameObject object = activeChar.getVisibleObject(Integer.parseInt(id));
				if(object != null && object.isNpc() && endOfId > 0 && activeChar.checkInteractionDistance(object))
				{
					activeChar.setLastNpc((NpcInstance) object);
					((NpcInstance) object).onBypassFeedback(activeChar, _bypass.substring(endOfId + 1));
				}
			} else if (_bypass.startsWith("teleport_to_loc")) {
				String command = _bypass.substring(15).trim();
				if (!command.isEmpty()) {
					String[] params = command.split(" ");
					if (params.length == 3) {
						int x = Integer.parseInt(params[0]);
						int y = Integer.parseInt(params[1]);
						int z = Integer.parseInt(params[2]);
						Location loc = new Location(x, y, z);
						activeChar.teleToLocation(loc);
					} else if (params.length == 5) {
						int x = Integer.parseInt(params[0]);
						int y = Integer.parseInt(params[1]);
						int z = Integer.parseInt(params[2]);
						Location loc = new Location(x, y, z);
						int itemId = Integer.parseInt(params[3]);
						int count = Integer.parseInt(params[4]);
						if (ItemFunctions.deleteItem(activeChar, itemId, count, true)) {
							activeChar.teleToLocation(loc);
							PaidActionsStatsService.getInstance()
								.updateStats(PaidActionType.TELEPORT_BYPASS, count);
						} else {
							ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
							if (template != null) {
								CustomMessage message = new CustomMessage("bypass.teleport.error.item").addString(template.getName());
								activeChar.sendMessage(message);
							}
						}
					}
				}
			} else if (_bypass.startsWith("open_party_matching")) {
				final int region = MatchingRoomManager.getInstance().getLocation(activeChar);
				BbsHandlerHolder.getInstance()
					.getCommunityHandler("_bbshome")
					.onBypassCommand(activeChar, "_bbshome");
				activeChar.sendPacket(new ListPartyWatingPacket(region, true, 1, activeChar));
			} else if (_bypass.startsWith("npc?")) {
				if (npc != null && npc.canBypassCheck(activeChar)) {
					String command = _bypass.substring(4).trim();
					npc.onBypassFeedback(activeChar, command);
				}
			} else if (!_bypass.startsWith("item?")) {
				if (_bypass.startsWith("class_change?")) {
					String command = _bypass.substring(13).trim();
					if (command.startsWith("class_name=") && npc != null && npc.canBypassCheck(activeChar)) {
						int classId = Integer.parseInt(command.substring(11).trim());
						npc.onChangeClassBypass(activeChar, classId);
					}
				} else if (_bypass.startsWith("_olympiad?")) {
					NpcInstance manager = NpcUtils.canPassPacket(activeChar, this, _bypass.split("&")[0]);
					if (manager != null) {
						manager.onBypassFeedback(activeChar, _bypass);
					}
				} else if (_bypass.startsWith("manor_menu_select?")) {
					GameObject object = activeChar.getTarget();
					if (object != null && object.isNpc()) {
						((NpcInstance) object).onBypassFeedback(activeChar, _bypass);
					}
				} else if (_bypass.startsWith("menu_select?")) {
					if (npc != null && npc.canBypassCheck(activeChar)) {
						String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
						StringTokenizer st = new StringTokenizer(params, "&");
						int ask = Integer.parseInt(st.nextToken().split("=")[1]);
						int reply = Integer.parseInt(st.nextToken().split("=")[1]);
						npc.onMenuSelect(activeChar, ask, reply);
					}
				} else if ("talk_select".equals(_bypass)) {
					if (npc != null && npc.canBypassCheck(activeChar)) {
						npc.showQuestWindow(activeChar);
					}
				} else if ("teleport_request".equals(_bypass)) {
					if (npc != null && npc.canBypassCheck(activeChar)) {
						npc.onTeleportRequest(activeChar);
					}
				} else if ("deposit".equals(_bypass)) {
					if (npc != null && npc.canBypassCheck(activeChar)) {
						WarehouseFunctions.showDepositWindow(activeChar);
					}
				} else if ("withdraw".equals(_bypass)) {
					if (npc != null && npc.canBypassCheck(activeChar)) {
						WarehouseFunctions.showRetrieveWindow(activeChar);
					}
				} else if ("deposit_pledge".equals(_bypass)) {
					if (npc != null && npc.canBypassCheck(activeChar)) {
						WarehouseFunctions.showDepositWindowClan(activeChar);
					}
				} else if ("withdraw_pledge".equals(_bypass)) {
					if (npc != null && npc.canBypassCheck(activeChar)) {
						WarehouseFunctions.showWithdrawWindowClan(activeChar);
					}
				} else if (_bypass.startsWith("Quest ")) {
					_log.warn("Trying to call Quest bypass: " + _bypass + ", player: " + activeChar);
				} else if (bp.type == BypassType.BBS) {
					if (!Config.COMMUNITYBOARD_ENABLED) {
						activeChar.sendPacket(SystemMsg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
					} else {
						if (activeChar.getFraction() == Fraction.NONE) {
							activeChar.sendPacket(SystemMsg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
							return;
						}

						if ((activeChar.isDead() && !Config.BBS_CHECK_DEATH
							|| activeChar.isMovementDisabled() && !Config.BBS_CHECK_MOVEMENT_DISABLE
							|| activeChar.isInSiegeZone() && !Config.BBS_CHECK_ON_SIEGE_FIELD
							|| activeChar.isInCombat() && !Config.BBS_CHECK_IN_COMBAT
							|| activeChar.isAttackingNow() && !Config.BBS_CHECK_ATTACKING_NOW
							|| /*activeChar.isInOlympiadMode() && !Config.BBS_CHECK_IN_OLYMPIAD_MODE
								||*/ activeChar.getVar("jailed") != null
							|| activeChar.isFlying() && !Config.BBS_CHECK_FLYING
							|| /*activeChar.isInDuel() && !Config.BBS_CHECK_IN_DUEL
								||*/ activeChar.getReflectionId() > 0 && !Config.BBS_CHECK_IN_INSTANCE
							|| activeChar.isOutOfControl() && !Config.BBS_CHECK_OUT_OF_CONTROL
							|| (activeChar.getEvent(SingleMatchEvent.class) != null && activeChar
							.getEvent(SingleMatchEvent.class).isInProgress() && !Config.BBS_CHECK_IN_EVENT)
							|| !activeChar.isInPeaceZone() && !activeChar.hasPremiumAccount()
							&& Config.BBS_CHECK_OUT_OF_TOWN_ONLY_FOR_PREMIUM)) {
							activeChar.sendPacket(new ExShowScreenMessage(
								new CustomMessage("communityboard.checkcondition.false.screen").toString(activeChar),
								5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true));
							activeChar.sendMessage(new CustomMessage("communityboard.checkcondition.false.chat"));

							String html = HtmCache.getInstance()
								.getHtml(Config.BBS_PATH + "/bbs_terms.htm", activeChar);
							String isTrue =
								"<font color=\"66FF33\">" + new CustomMessage("common.allowed").toString(activeChar)
									+ "</font>";
							String isFalse =
								"<font color=\"FF0000\">" + new CustomMessage("common.prohibited").toString(activeChar)
									+ "</font>";
							String onlyPremium =
								"<font color=\"LEVEL\">" + new CustomMessage("common.need.premium").toString(activeChar)
									+ "</font>";

							html = html.replace("%config_isInZonePeace%",
								Config.BBS_CHECK_OUT_OF_TOWN_ONLY_FOR_PREMIUM ? onlyPremium : isTrue);
							html = html.replace("%config_isDead%", Config.BBS_CHECK_DEATH ? isTrue : isFalse);
							html = html.replace("%config_isMovementDisabled%",
								Config.BBS_CHECK_MOVEMENT_DISABLE ? isTrue : isFalse);
							html = html
								.replace("%config_isOnSiegeField%", Config.BBS_CHECK_ON_SIEGE_FIELD ? isTrue : isFalse);
							html = html.replace("%config_isInCombat%", Config.BBS_CHECK_IN_COMBAT ? isTrue : isFalse);
							html = html
								.replace("%config_isAttackingNow%", Config.BBS_CHECK_ATTACKING_NOW ? isTrue : isFalse);
							html = html.replace("%config_isInOlympiadMode%",
								Config.BBS_CHECK_IN_OLYMPIAD_MODE ? isTrue : isFalse);
							html = html.replace("%config_isFlying%", Config.BBS_CHECK_FLYING ? isTrue : isFalse);
							html = html.replace("%config_isInDuel%", Config.BBS_CHECK_IN_DUEL ? isTrue : isFalse);
							html = html
								.replace("%config_isInInstance%", Config.BBS_CHECK_IN_INSTANCE ? isTrue : isFalse);
							html = html.replace("%config_isInJailed%", Config.BBS_CHECK_IN_JAILED ? isTrue : isFalse);
							html = html
								.replace("%config_isOutOfControl%", Config.BBS_CHECK_OUT_OF_CONTROL ? isTrue : isFalse);
							html = html.replace("%config_isInEvent%", Config.BBS_CHECK_IN_EVENT ? isTrue : isFalse);

							String check = " <font color=\"LEVEL\">*</font>";
							html = html.replace("%check_isInZonePeace%",
								!activeChar.isInPeaceZone() && !activeChar.hasPremiumAccount()
									&& Config.BBS_CHECK_OUT_OF_TOWN_ONLY_FOR_PREMIUM ? check : "");
							html = html
								.replace("%check_isDead%", activeChar.isDead() && !Config.BBS_CHECK_DEATH ? check : "");
							html = html.replace("%check_isMovementDisabled%",
								activeChar.isMovementDisabled() && !Config.BBS_CHECK_MOVEMENT_DISABLE ? check : "");
							html = html.replace("%check_isOnSiegeField%",
								activeChar.isInSiegeZone() && !Config.BBS_CHECK_ON_SIEGE_FIELD ? check : "");
							html = html.replace("%check_isInCombat%",
								activeChar.isInCombat() && !Config.BBS_CHECK_IN_COMBAT ? check : "");
							html = html.replace("%check_isAttackingNow%",
								activeChar.isAttackingNow() && !Config.BBS_CHECK_ATTACKING_NOW ? check : "");
							//							html = html.replace("%check_isInOlympiadMode%", activeChar.isInOlympiadMode() && !Config.BBS_CHECK_IN_OLYMPIAD_MODE ? check : "");
							html = html.replace("%check_isFlying%",
								activeChar.isFlying() && !Config.BBS_CHECK_FLYING ? check : "");
							//							html = html.replace("%check_isInDuel%", activeChar.isInDuel() && !Config.BBS_CHECK_IN_DUEL ? check : "");
							html = html.replace("%check_isInInstance%",
								activeChar.getReflectionId() > 0 && !Config.BBS_CHECK_IN_INSTANCE ? check : "");
							html = html.replace("%check_isInJailed%",
								activeChar.getVar("jailed") != null && !Config.BBS_CHECK_IN_JAILED ? check : "");
							html = html.replace("%check_isOutOfControl%",
								activeChar.isOutOfControl() && !Config.BBS_CHECK_OUT_OF_CONTROL ? check : "");
							html = html.replace("%check_isInEvent%",
								activeChar.getEvent(SingleMatchEvent.class) != null && activeChar
									.getEvent(SingleMatchEvent.class).isInProgress() && !Config.BBS_CHECK_IN_EVENT
									? check : "");

							ShowBoardPacket.separateAndSend(html, activeChar);
							return;
						}

						for (Event e : activeChar.getEvents()) {
							if (!e.canUseCommunityFunctions(activeChar)) {
								activeChar.sendPacket(SystemMsg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
								return;
							}
						}
						IBbsHandler handler = BbsHandlerHolder.getInstance().getCommunityHandler(_bypass);
						if (handler != null) {
							handler.onBypassCommand(activeChar, _bypass);
						}
					}
				}
			}
		}
		catch(Exception e2)
		{
			String st3 = "Error while handling bypass: " + _bypass;
			if(npc != null)
				st3 = st3 + " via NPC " + npc;
			_log.error(st3, e2);
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(RequestBypassToServer.class);
	}
}

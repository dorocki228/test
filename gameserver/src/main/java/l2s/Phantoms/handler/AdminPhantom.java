package l2s.Phantoms.handler;

import java.util.ArrayList;
import java.util.List;
import gve.zones.GveZoneManager;
import l2s.gameserver.model.base.Fraction;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gve.zones.model.GveZone;
import l2s.Phantoms.PhantomPlayers;
import l2s.Phantoms.PhantomVariables;
import l2s.Phantoms.Utils.PhantomUtils;
import l2s.Phantoms.ai.abstracts.PhantomDefaultAI;
import l2s.Phantoms.enums.PartyState;
import l2s.Phantoms.enums.PhantomType;
import l2s.Phantoms.objects.Nickname;
import l2s.Phantoms.objects.PhantomPartyObject;
import l2s.Phantoms.objects.TrafficScheme.PhantomRoute;
import l2s.Phantoms.parsers.Craft.ItemsForCraftParser;

import l2s.Phantoms.parsers.PhantomRouteParser;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.handler.admincommands.AdminCommandHandler;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ShowBoardPacket;
import l2s.gameserver.tables.GmListTable;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.Location;

public class AdminPhantom implements IAdminCommandHandler {
	protected static final Logger _log = LoggerFactory.getLogger(AdminPhantom.class);

	static enum Commands {
		admin_phantom, admin_movepoint, admin_pvis, admin_phantom_spawn, admin_phantom_ai, admin_phantom_enchant,
		admin_start_record, admin_stop_record, admin_dump_skills, admin_setcast, admin_spawnptown, admin_getservantmode,
		admin_getpartystate, admin_gethunterTask, admin_starthunterTask, admin_setbot, admin_unsetbot, admin_isbot,
		admin_getpartylist, admin_getphantomtype, admin_trafficscheme, admin_phantom_log, admin_getpoolmanager,
		admin_getmaster, admin_phantomgetai, admin_startai, admin_stopai, admin_show_phantom, admin_spawnphantom,
		admin_recallallphantom, admin_p_matchingroom, admin_phantom_start_oly, admin_phantom_stop_oly, admin_spawnparty,
		admin_gettargetp, admin_setp1, admin_setp2, admin_setp3, admin_setp4, admin_test1, admin_test2, admin_test5,
		admin_pfortreg, admin_pforttest, admin_pfortspawn, admin_pcastlespawn, admin_pcastlereg, admin_pcastletest,

		admin_displayts, // простые маршруты //команда нейм
		admin_displaysiege, // форты //команда идфорта
		admin_displayroute_party, // пати // команда нейм
		admin_test6, admin_despawnallphantom, admin_testpathfind, admin_getgeoindex, admin_set_route, admin_getgvezone;
	}

	@SuppressWarnings("rawtypes")
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar) {
		Commands command = (Commands) comm;
		if (activeChar.getPlayerAccess().Menu) {
			Player fantom = null;
			GameObject target = activeChar.getTarget();
			switch (command) {
			case admin_test1: {
				if (target == null || !target.isPlayer())
					return false;
				Player t_player = target.getPlayer();
				activeChar.sendMessage("total:" + t_player.getAdenaReward());
				activeChar.sendMessage("********************");
				activeChar.sendMessage("itemReward:" + t_player.get_itemReward().getAdena());
				activeChar.sendMessage("pvpReward:" + t_player.get_pvpReward().getAdena());
				activeChar.sendMessage("setReward:" + t_player.get_setReward().getAdena());
				activeChar.sendMessage("enchantReward:" + t_player.get_enchantReward().getAdena());
				activeChar.sendMessage("nobleReward:" + t_player.get_nobleReward().getAdena());
				activeChar.sendMessage("heroReward:" + t_player.get_heroReward().getAdena());
				activeChar.sendMessage("********************");

				break;
			}
			case admin_getgvezone: {
				for (GveZone zone : GveZoneManager.getInstance().getActiveZones()) {
					activeChar.sendMessage(zone.getInGameName() + " " + zone.getType());
				}
				break;
			}

			case admin_testpathfind: {
				activeChar.moveToLocation(147464, 1624, -336, 0, true);
				break;
			}
			case admin_movepoint: {
				activeChar.moveToLocation(147464, 1624, -336, 0, true);
				break;
			}
			case admin_pcastletest: {
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_pcastlereg " + wordList[1]);
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar,
						"admin_quick_siege_start " + wordList[1]);
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar,
						"admin_pcastlespawn " + wordList[1]);
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar,
						"admin_displaysiege SIEGE_CASTLE_ATTACK " + wordList[1]);
				break;
			}

			case admin_pforttest: {
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_pfortreg 101");
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_quick_siege_start 101");
				AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_pfortspawn 101");
				// AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar,
				// "admin_displaysiege 101");
				break;
			}
			case admin_set_route: {
				if (target != null && target.isPhantom()) {
					Player phantom = target.getPlayer();
					PhantomRoute _route = PhantomRouteParser.getInstance().getSchemeByName(wordList[1]);
					if (_route == null)
						activeChar.sendAdminMessage("Route == null");

					if (_route != null) {
						phantom.phantom_params.getPhantomAI().setRoute(_route);
						Location loc = _route.getTask().get(0).getPoints().get(0).getLoc();
						phantom.teleToLocation(loc);
						activeChar.teleToLocation(loc);
						phantom.phantom_params.changeState(PartyState.route);
						activeChar.sendAdminMessage("Start route...");
					}
				}
				break;
			}
			case admin_test6: {
				activeChar.sendAdminMessage(target.getPlayer().phantom_params.getState().name());
				// String dialog =
				// HtmCache.getInstance().getNotNull("admin/cb/phantom/PhantomShift.htm",
				// activeChar);
				// ShowBoard.separateAndSend(dialog, activeChar);
				break;
			}
			case admin_test2: {
				activeChar.sendMessage("isTeleporting " + target.getPlayer().isTeleporting());
				break;
			}
			case admin_test5: {
				final Location pos = activeChar.getLoc().correctGeoZ();
				activeChar.sendMessage("correctGeoZ " + pos);

				break;
			}
			case admin_pvis: {
				if (target != null) {
					long time_n1 = System.nanoTime();
					boolean b1 = PhantomUtils.availabilityCheck(activeChar, target.getLoc());
					long time_n1_end = System.nanoTime();
					long time_n2 = System.nanoTime();
					boolean b2 = PhantomUtils.availabilityCheck(activeChar, target.getLoc());
					long time_n2_end = System.nanoTime();
					activeChar.sendMessage("availabilityCheck: " + b1 + " " + (time_n1_end - time_n1));
					activeChar.sendMessage("availabilityCheck2: " + b2 + " " + (time_n2_end - time_n2));
				}
				break;
			}
			case admin_gettargetp: {
				if (target != null && target.isCreature()) {
					activeChar.sendMessage("getAttackTarget: " + ((Creature) target).getAI().getAttackTarget());
					activeChar.sendMessage("getAggressionTarget: " + ((Creature) target).getAggressionTarget());
					activeChar.sendMessage("getTarget: " + ((Creature) target).getTarget());
				}
				break;
			}
			case admin_setp1: {
				if (target != null && target.isPlayer() && target.getPlayer().isPhantom()
						&& target.getPlayer().getPhantomType() == PhantomType.PHANTOM_CLAN_MEMBER) {
					Player player = target.getPlayer();
					PhantomPartyObject party = player.phantom_params.getPhantomPartyAI();
					party.setp1(Integer.parseInt(wordList[1]));
					activeChar.sendMessage("1:" + party.getp1() + " 2:" + party.getp2() + " 3:" + party.getp3() + " 4:"
							+ +party.getp4());
				}
				break;
			}
			case admin_setp2: {
				if (target != null && target.isPlayer() && target.getPlayer().isPhantom()
						&& target.getPlayer().getPhantomType() == PhantomType.PHANTOM_CLAN_MEMBER) {
					Player player = target.getPlayer();
					PhantomPartyObject party = player.phantom_params.getPhantomPartyAI();
					party.setp2(Integer.parseInt(wordList[1]));
					activeChar.sendMessage("1:" + party.getp1() + " 2:" + party.getp2() + " 3:" + party.getp3() + " 4:"
							+ +party.getp4());
				}
				break;
			}
			case admin_setp3: {
				if (target != null && target.isPlayer() && target.getPlayer().isPhantom()
						&& target.getPlayer().getPhantomType() == PhantomType.PHANTOM_CLAN_MEMBER) {
					Player player = target.getPlayer();
					PhantomPartyObject party = player.phantom_params.getPhantomPartyAI();
					party.setp3(Integer.parseInt(wordList[1]));
					activeChar.sendMessage("1:" + party.getp1() + " 2:" + party.getp2() + " 3:" + party.getp3() + " 4:"
							+ +party.getp4());
				}
				break;
			}
			case admin_setp4: {
				if (target != null && target.isPlayer() && target.getPlayer().isPhantom()
						&& target.getPlayer().getPhantomType() == PhantomType.PHANTOM_CLAN_MEMBER) {
					Player player = target.getPlayer();
					PhantomPartyObject party = player.phantom_params.getPhantomPartyAI();
					party.setp4(Integer.parseInt(wordList[1]));
					activeChar.sendMessage("1:" + party.getp1() + " 2:" + party.getp2() + " 3:" + party.getp3() + " 4:"
							+ +party.getp4());
				}
				break;
			}
			case admin_start_record: {
				activeChar.tScheme_record.setLogging(true);
				activeChar.tScheme_record.newRecord();
				break;
			}
			case admin_stop_record: {
				activeChar.tScheme_record.stopRecord(false);
				activeChar.tScheme_record.setLogging(false);
				PhantomRouteParser.getInstance().SavePhantomRoute();
				break;
			}

			case admin_phantom_enchant: {
				String dialog = HtmCache.getInstance().getHtml("admin/cb/phantom/PhantomEnchant.htm", activeChar);
				if (wordList.length >= 2) {
					if (wordList[1].equalsIgnoreCase("EnabledEnchSkill")) {
						if (wordList[2].equalsIgnoreCase("on"))
							PhantomUtils.SetActive("PEnchSkill", true);
						else if (wordList[2].equalsIgnoreCase("of"))
							PhantomUtils.SetActive("PEnchSkill", false);
					}
					if (wordList[1].equalsIgnoreCase("RndEnchSkill")) {
						if (wordList[2].equalsIgnoreCase("on"))
							PhantomUtils.SetActive("PRndEnchSkill", true);
						else if (wordList[2].equalsIgnoreCase("of"))
							PhantomUtils.SetActive("PRndEnchSkill", false);
					}
					if (wordList[1].equalsIgnoreCase("Max15EnchLevel")) {
						int Plimit = Integer.parseInt(wordList[2]);
						if (Plimit > 15)
							Plimit = 15;
						if (Plimit < 1)
							Plimit = 1;
						PhantomVariables.set("SetMax15EnchLevel", Plimit);
					}
					if (wordList[1].equalsIgnoreCase("Max30EnchLevel")) {
						int Plimit = Integer.parseInt(wordList[2]);
						if (Plimit > 30)
							Plimit = 30;
						if (Plimit < 1)
							Plimit = 1;
						PhantomVariables.set("SetMax30EnchLevel", Plimit);
					}

					if (wordList[1].equalsIgnoreCase("PhantomHardMinAttArmor")) {
						int Plimit = Integer.parseInt(wordList[2]);
						if (Plimit > 120)
							Plimit = 120;
						if (Plimit < 0)
							Plimit = 0;
						PhantomVariables.set("PhantomHardMinAttArmor", Plimit);
					}

					if (wordList[1].equalsIgnoreCase("PhantomHardMaxAttArmor")) {
						int Plimit = Integer.parseInt(wordList[2]);
						if (Plimit > 120)
							Plimit = 120;
						if (Plimit < 0)
							Plimit = 0;
						PhantomVariables.set("PhantomHardMaxAttArmor", Plimit);
					}
					if (wordList[1].equalsIgnoreCase("PhantomHardMinEnchW")) {
						int Plimit = Integer.parseInt(wordList[2]);
						if (Plimit > 120)
							Plimit = 120;
						if (Plimit < 0)
							Plimit = 0;
						PhantomVariables.set("PhantomHardMinEnchW", Plimit);
					}

					if (wordList[1].equalsIgnoreCase("PhantomHardMaxEnchW")) {
						int Plimit = Integer.parseInt(wordList[2]);
						if (Plimit > 20)
							;// Config.ENCHANT_MAX_WEAPON)
						Plimit = 20;// Config.ENCHANT_MAX_WEAPON;
						if (Plimit < 0)
							Plimit = 0;
						PhantomVariables.set("PhantomHardMaxEnchW", Plimit);
					}

					if (wordList[1].equalsIgnoreCase("PhantomHardMinEnchArmor")) {
						int Plimit = Integer.parseInt(wordList[2]);
						if (Plimit > 20)
							;// Config.ENCHANT_MAX_ARMOR)
						Plimit = 20;// Config.ENCHANT_MAX_ARMOR;
						if (Plimit < 0)
							Plimit = 0;
						PhantomVariables.set("PhantomHardMinEnchArmor", Plimit);
					}

					if (wordList[1].equalsIgnoreCase("PhantomHardMaxEnchArmor")) {
						int Plimit = Integer.parseInt(wordList[2]);
						if (Plimit > 20)
							;// Config.ENCHANT_MAX_ARMOR)
						Plimit = 20;// Config.ENCHANT_MAX_ARMOR;
						if (Plimit < 0)
							Plimit = 0;
						PhantomVariables.set("PhantomHardMaxEnchArmor", Plimit);
					}

					if (wordList[1].equalsIgnoreCase("PhantomHardAttWeapon")) {
						int Plimit = Integer.parseInt(wordList[2]);
						if (Plimit > 300)
							Plimit = 300;
						if (Plimit < 20)
							Plimit = 20;
						PhantomVariables.set("PhantomHardAttWeapon", Plimit);
					}
				}

				dialog = dialog.replaceFirst("%VisualRndEnchSkill%",
						PhantomUtils.IsActive("PRndEnchSkill") ? "On" : "Off");
				dialog = dialog.replaceFirst("%VisualEnchSkill%", PhantomUtils.IsActive("PEnchSkill") ? "On" : "Off");
				dialog = dialog.replaceFirst("%VisualEnch15Skill%",
						String.valueOf(PhantomVariables.getInt("SetMax15EnchLevel", 0)));
				dialog = dialog.replaceFirst("%VisualEnch30Skill%",
						String.valueOf(PhantomVariables.getInt("SetMax30EnchLevel", 0)));

				dialog = dialog.replaceFirst("%VisualPhantomHardMinAttArmor%",
						String.valueOf(PhantomVariables.getInt("PhantomHardMinAttArmor", 0)));
				dialog = dialog.replaceFirst("%VisualPhantomHardMaxAttArmor%",
						String.valueOf(PhantomVariables.getInt("PhantomHardMaxAttArmor", 0)));
				dialog = dialog.replaceFirst("%VisualPhantomHardAttWeapon%",
						String.valueOf(PhantomVariables.getInt("PhantomHardAttWeapon", 0)));

				dialog = dialog.replaceFirst("%VisualPhantomHardMinEnchW%",
						String.valueOf(PhantomVariables.getInt("PhantomHardMinEnchW", 0)));
				dialog = dialog.replaceFirst("%VisualPhantomHardMaxEnchW%",
						String.valueOf(PhantomVariables.getInt("PhantomHardMaxEnchW", 0)));

				dialog = dialog.replaceFirst("%VisualPhantomHardMinEnchArmor%",
						String.valueOf(PhantomVariables.getInt("PhantomHardMinEnchArmor", 0)));
				dialog = dialog.replaceFirst("%VisualPhantomHardMaxEnchArmor%",
						String.valueOf(PhantomVariables.getInt("PhantomHardMaxEnchArmor", 0)));

				ShowBoardPacket.separateAndSend(dialog, activeChar);
				break;
			}
			case admin_phantom_ai: {
				String dialog = HtmCache.getInstance().getHtml("admin/cb/phantom/PhantomAi.htm", activeChar);
				if (wordList.length >= 2) {
					if (wordList[1].equalsIgnoreCase("BAttackPlayerPvpFlag")) {
						if (wordList[2].equalsIgnoreCase("on"))
							PhantomUtils.SetActive("AttackPlayerPvpFlag", true);
						else if (wordList[2].equalsIgnoreCase("of"))
							PhantomUtils.SetActive("AttackPlayerPvpFlag", false);
					}
					if (wordList[1].equalsIgnoreCase("BChanceTargetPlayerPvpFlag")) {
						int Plimit = Integer.parseInt(wordList[2]);
						if (Plimit > 100)
							Plimit = 100;
						if (Plimit < 0)
							Plimit = 0;
						PhantomVariables.set("ChanceTargetPlayerPvpFlag", Plimit);
					}
					if (wordList[1].equalsIgnoreCase("BChanceTargetPhantomPvpFlag")) {
						int Plimit = Integer.parseInt(wordList[2]);
						if (Plimit > 100)
							Plimit = 100;
						if (Plimit < 0)
							Plimit = 0;
						PhantomVariables.set("ChanceTargetPhantomPvpFlag", Plimit);
					}
				}

				dialog = dialog.replaceFirst("%VisualAttackPlayerPvpFlag%",
						PhantomUtils.IsActive("AttackPlayerPvpFlag") ? "On" : "Off");
				dialog = dialog.replaceFirst("%VisualChanceTargetPlayerPvpFlag%",
						String.valueOf(PhantomVariables.getInt("ChanceTargetPlayerPvpFlag", 0)));
				dialog = dialog.replaceFirst("%VisualChanceTargetPhantomPvpFlag%",
						String.valueOf(PhantomVariables.getInt("ChanceTargetPhantomPvpFlag", 0)));

				ShowBoardPacket.separateAndSend(dialog, activeChar);
				break;
			}
			case admin_phantom: {
				activeChar.sendMessage("admin_phantom");

				String dialog = HtmCache.getInstance().getHtml("admin/cb/phantom/Phantom.htm", activeChar);
				if (wordList.length >= 2) {
					if (wordList[1].equalsIgnoreCase("MaxEquipGradeServant")) {
						PhantomVariables.set("MaxEquipGradeServant", wordList[2]);
					}

					if (wordList[1].equalsIgnoreCase("MaxEquipGrade")) {
						PhantomVariables.set("MaxEquipGrade", wordList[2]);
					}
				}

				dialog = dialog.replaceFirst("%MaxEquipGrade_S%",
						String.valueOf(PhantomVariables.getString("MaxEquipGrade", "S")));
				dialog = dialog.replaceFirst("%MaxEquipGrade_SS%",
						String.valueOf(PhantomVariables.getString("MaxEquipGradeServant", "S")));
				ShowBoardPacket.separateAndSend(dialog, activeChar);
				break;
			}
			case admin_setcast: {
				if (target != null && target.isPlayer()) {
					Player p1 = target.getPlayer();
					if (p1.isPhantom()) {
						p1.phantom_params.setChanceOlyCast(Double.parseDouble(wordList[1]));
						activeChar.sendMessage("Chance: " + Double.parseDouble(wordList[1]));
					}
				}
				break;
			}
			/*
			 * case admin_spawnptown: { Nickname PhantomObjId =
			 * PhantomPlayers.getInstance().getRandomPhantomNext(Integer.parseInt(wordList[1
			 * ])); fantom = PhantomPlayers.getInstance().createPhantom(PhantomObjId, -1,
			 * PhantomType.PHANTOM, Integer.parseInt(wordList[1])); if (fantom == null) {
			 * return false; } // fantom.decayMe(); activeChar.sendMessage("нейм1." +
			 * wordList[2]); List<PhantomRoute> scheme =
			 * PhantomRouteParser.getInstance().getAllPhantomRoute().stream().filter(s -> s
			 * != null && PhantomUtils.equals(s.getName(),
			 * wordList[2])).collect(Collectors.toList()); if (scheme != null &&
			 * scheme.size() > 0) { activeChar.sendMessage("нейм." +
			 * scheme.get(0).getName()); fantom.phantom_params.getPhantomAI().abortAITask();
			 * fantom.setPhantomType(PhantomType.PHANTOM_TOWNS_PEOPLE);
			 * fantom.phantom_params.setTrafficScheme(scheme.get(0),
			 * fantom.phantom_params.getTrafficScheme(), false);
			 * fantom.phantom_params.setPhantomTownsAI();
			 * fantom.teleToLocation(scheme.get(0).getPointsRnd().get(0).getLoc());
			 * fantom.phantom_params.getPhantomAI().startAITask(Config.PHANTOM_AI_DELAY);
			 * activeChar.sendMessage("Фантом " + fantom + " добавлен в мир.");
			 * activeChar.teleToLocation(fantom.getLoc()); } break; }
			 */
			case admin_getpartystate: {
				if (target.isPlayer() && target.getPlayer().isPhantom()) {
					PhantomPartyObject party = target.getPlayer().phantom_params.getPhantomPartyAI();
					activeChar.sendMessage("PartyState: " + party.getPartyState().name());
				}
				break;
			}
			case admin_getphantomtype: {
				activeChar.sendMessage("PhantomType: " + target.getPlayer().getPhantomType().name());
				break;
			}
			case admin_getpartylist: {
				if (target.isPlayer()) {
					PhantomPartyObject party = target.getPlayer().phantom_params.getPhantomPartyAI();
					for (Player member : party.getAllMembers()) {
						activeChar.sendMessage("Игрок: " + member);
					}
				}
				break;
			}
			case admin_getpoolmanager:
				_log.info(ThreadPoolManager.getInstance().getStats().toString());
				break;
			case admin_phantom_start_oly:
				PhantomPlayers.getInstance().startRegOlympiadTask(120000);
				break;
			case admin_phantom_stop_oly:
				PhantomPlayers.getInstance().abortRegOlympiadTask();
				break;
			case admin_trafficscheme:
				if (target.getPlayer().isPhantom()) {
					PhantomRoute Scheme = target.getPlayer().phantom_params.getTrafficScheme();
					if (Scheme != null) {
						activeChar.sendMessage("Traffic Scheme: " + Scheme.getName());
					}
				}
				break;
			case admin_phantom_log:
				if (target.getPlayer().isPhantom()) {
					if (target.getPlayer().phantom_params.getGmLog()) {
						target.getPlayer().phantom_params.setGmLog(false);
					} else {
						target.getPlayer().phantom_params.setGmLog(true);
					}
				}
				break;
			case admin_phantom_spawn:
				String dialog = HtmCache.getInstance().getHtml("admin/cb/phantom/PhantomSpawn.htm", activeChar);
				if (wordList.length >= 2) {

					if (wordList[1].equalsIgnoreCase("CfgPhantomLimit")) {
						int Plimit = Integer.parseInt(wordList[2]);
						PhantomVariables.set("CfgPhantomLimit", Plimit);
					}
					if (wordList[1].equalsIgnoreCase("CfgPhantomMaxLvl")) {
						int Plimit = Integer.parseInt(wordList[2]);
						if (Plimit > 80)
							Plimit = 80;
						if (Plimit < 19)
							Plimit = 19;
						PhantomVariables.set("GreatePhantomMaxLvl", Plimit);
					}

					if (wordList[1].equalsIgnoreCase("PhantomSpawnCraftOrTrade")) {
						if (wordList[2].equalsIgnoreCase("on")) {
							PhantomUtils.SetActive("PhantomSpawnCraftOrTrade", true);
							PhantomPlayers.getInstance().FantomSpawnTraderTask();
						} else if (wordList[2].equalsIgnoreCase("of")) {
							PhantomUtils.SetActive("PhantomSpawnCraftOrTrade", false);
							for (Player phantom : GameObjectsStorage.getPlayers()) {
								if (phantom.getPhantomType() == PhantomType.PHANTOM_CRAFTER
										|| phantom.getPhantomType() == PhantomType.PHANTOM_TRADER) {
									phantom.setOnlineStatus(false);
									PhantomDefaultAI ai = phantom.phantom_params.getPhantomAI();
									if (ai != null) {
										phantom.phantom_params.getPhantomAI().abortBuffTask();
										ai.abortAITask();
									}
									phantom.kick();
								}
							}
							// ItemsForSaleBuyParser.getInstance().reload();
							ItemsForCraftParser.getInstance().reload();
						}
					}
					if (wordList[1].equalsIgnoreCase("PhantomBasicSpawn")) {
						if (wordList[2].equalsIgnoreCase("on")) {
							PhantomUtils.SetActive("PhantomBasicSpawn", true);
							PhantomPlayers.getInstance().startSpawnTask(Config.PHANTOM_PLAYERS_DELAY_SPAWN);
						} else if (wordList[2].equalsIgnoreCase("of")) {
							PhantomUtils.SetActive("PhantomBasicSpawn", false);
							PhantomPlayers.getInstance().abortSpawnTask();
						}
					}
					if (wordList[1].equalsIgnoreCase("PCount_merchants")) {
						int Plimit = Integer.parseInt(wordList[2]);
						PhantomVariables.set("PCount_merchants", Plimit);
					}

					for (GveZone z : GveZoneManager.getInstance().getZones().values()) 
					{
						
						if (wordList[1].equalsIgnoreCase(z.getName())) 
						{
							if (wordList[2].equalsIgnoreCase("on")) 
								PhantomUtils.SetActive("Status_" + z.getName(), true);
							 else if (wordList[2].equalsIgnoreCase("of")) 
								PhantomUtils.SetActive("Status_" + z.getName(), false);
						}

						if (wordList[1].equalsIgnoreCase("set_" + z.getName())) 
						{
							int limit = NumberUtils.toInt(wordList[2], 100);
							PhantomVariables.set(z.getName(), limit);
						}
					}
				}

				dialog = dialog.replace("%list_zone%", String.valueOf(getZonesHtmlP()));
				dialog = dialog.replaceFirst("%SpawnCraftOrTrade%",
						PhantomUtils.IsActive("PhantomSpawnCraftOrTrade") ? "On" : "Off");
				dialog = dialog.replaceFirst("%PhantomBasicSpawn%",
						PhantomUtils.IsActive("PhantomBasicSpawn") ? "On" : "Off");
				dialog = dialog.replaceFirst("%CfgPhantomLimit%",
						String.valueOf(PhantomVariables.getInt("CfgPhantomLimit", 0)));
				dialog = dialog.replaceFirst("%PhantomMaxLvL%",
						String.valueOf(PhantomVariables.getInt("GreatePhantomMaxLvl", 0)));
				dialog = dialog.replaceFirst("%PCount_merchants%",
						String.valueOf(PhantomVariables.getInt("PCount_merchants", 0)));

				_log.info(dialog);

				ShowBoardPacket.separateAndSend(dialog, activeChar);
				break;
			case admin_phantomgetai:
				if (target == null) {
					activeChar.sendMessage("Укажите ник фантома.");
				} else if (!target.isPlayer()) {
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
				} else {
					Player fantom1 = (Player) target;
					activeChar.sendMessage("Ai:" + fantom1.phantom_params.getPhantomAI().StatusAITask());
				}
				break;
			case admin_startai:
				if (target == null) {
					activeChar.sendMessage("Укажите ник фантома.");
				} else if (!target.isPlayer()) {
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
				} else {
					Player fantomAI = (Player) target;
					fantomAI.phantom_params.getPhantomAI().startAITask(Config.PHANTOM_AI_DELAY);
					fantomAI.phantom_params.getPhantomAI().startBuffTask(100);
				}
				break;
			case admin_stopai:
				if (target == null) {
					activeChar.sendMessage("Укажите ник фантома.");
				} else if (!target.isPlayer()) {
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
				} else {
					Player fantomstopAI = (Player) target;
					fantomstopAI.phantom_params.getPhantomAI().abortAITask();
					fantomstopAI.phantom_params.getPhantomAI().abortBuffTask();
				}
				break;
			case admin_show_phantom:
				try {
					int page = Integer.parseInt(wordList[1]);
					listPhantom(activeChar, page);
				} catch (StringIndexOutOfBoundsException localStringIndexOutOfBoundsException) {
				}
				break;
			case admin_spawnphantom:
				Nickname PhantomObjId = PhantomPlayers.getInstance()
						.getRandomPhantomNext(Integer.parseInt(wordList[1]));
				fantom = PhantomPlayers.getInstance().createPhantom(PhantomObjId, -1, PhantomType.PHANTOM,
						Integer.parseInt(wordList[1]), null);
				if (fantom == null) {
					return false;
				}
				fantom.decayMe();
				Location loc1 = activeChar.getLoc();
				fantom.spawnMe(loc1);
				activeChar.sendMessage("Фантом " + fantom + " добавлен в мир.");
				break;
			case admin_despawnallphantom:
				for (Player phantom : GameObjectsStorage.getPlayers()) {
					if (phantom.isPhantom()) {
						if (phantom.getPhantomType() != PhantomType.PHANTOM_CRAFTER
								|| phantom.getPhantomType() != PhantomType.PHANTOM_TRADER) {
							phantom.setOnlineStatus(false);
							phantom.kick();
						}
					}
				}
				break;
			case admin_recallallphantom:
				for (Player player : GameObjectsStorage.getPlayers()) {
					if (player.isPhantom()) {
						if (player.getPhantomType() != PhantomType.PHANTOM_CRAFTER
								|| player.getPhantomType() != PhantomType.PHANTOM_TRADER) {
							Location loc = activeChar.getLoc();
							player.teleToLocation(loc);
						}
					}
				}
				break;
			default:
				break;
			}
		}
		return false;
	}

	public String getZonesHtmlP() {
		StringBuilder builder = new StringBuilder();
		GveZoneManager.getInstance().getZoneTypesStream().forEach(zoneType -> {
			List<GveZone> gveZones = GveZoneManager.getInstance().getZones().get(zoneType);
			for (int i = 0; i < gveZones.size(); i++) {
				if ((i + 1) % 2 != 0) {
					String dialog = HtmCache.getInstance().getHtml("admin/cb/phantom/SpawnZoneParam.htm",
							Language.ENGLISH);
					builder.append("<tr>");
					dialog = dialog.replace("%zone_name%", gveZones.get(i).getName());
					dialog = dialog.replace("%status%",PhantomUtils.IsActive("Status_" + gveZones.get(i).getName()) ? "On" : "Off");
					dialog = dialog.replace("%zone_limit%",
							String.valueOf(PhantomVariables.getInt(gveZones.get(i).getName(), 0)));
					dialog = dialog.replace("%zone_name_p%", gveZones.get(i).getName());
					builder.append(dialog);
					if (gveZones.size() == (i + 1)) {
						builder.append("<td width=5>|</td>");
						builder.append(
								"<td width=100></td><td width=20></td><td width=30></td><td width=30></td><td width=30></td><td width=30></td><td width=30></td><td width=30></td>");
						builder.append("</tr>");
					}
				} else {
					String dialog = HtmCache.getInstance().getHtml("admin/cb/phantom/SpawnZoneParam.htm",
							Language.ENGLISH);
					builder.append("<td width=5>|</td>");
					dialog = dialog.replace("%zone_name%", gveZones.get(i).getName());
					dialog = dialog.replace("%status%",
							PhantomUtils.IsActive("Status_" + gveZones.get(i).getName()) ? "On" : "Off");
					dialog = dialog.replace("%zone_limit%",
							String.valueOf(PhantomVariables.getInt(gveZones.get(i).getName(), 0)));
					dialog = dialog.replace("%zone_name_p%", gveZones.get(i).getName());
					builder.append(dialog);
					builder.append("</tr>");
				}
			}

		});
		return builder.toString();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum[] getAdminCommandEnum() {
		return Commands.values();
	}

	private void listPhantom(Player activeChar, int page) {
		List<Player> players = new ArrayList<Player>(GameObjectsStorage.getPlayers(player -> player.isPhantom()));
		String color;
		String has_bonus;
		int MaxCharactersPerPage = 15;
		int MaxPages = players.size() / MaxCharactersPerPage;
		if (players.size() > MaxCharactersPerPage * MaxPages)
			MaxPages++;
		// Check if number of users changed
		if (page > MaxPages)
			page = MaxPages;
		int CharactersStart = MaxCharactersPerPage * page;
		int CharactersEnd = players.size();
		if (CharactersEnd - CharactersStart > MaxCharactersPerPage)
			CharactersEnd = CharactersStart + MaxCharactersPerPage;
		StringBuilder replyMSG = new StringBuilder(
				"<html><title>Список фантомов Rebellion Team</title><body><center><table width=275 >");
		replyMSG.append(
				"<tr><td><button value=\"Главная\" action=\"bypass -h admin_admin\" width=80 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append(
				"<td><button value=\"Ивенты\" action=\"bypass -h admin_show_html events/events.htm\" width=80 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append(
				"<td><button value=\"Телепорт\" action=\"bypass -h admin_show_moves\" width=80 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append(
				"<td><button value=\"Персонажи\" action=\"bypass -h admin_char_manage\" width=80 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append(
				"<td><button value=\"Фантомы\" action=\"bypass -h admin_phantom\" width=80 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		replyMSG.append(
				"<td><button value=\"Сервер\" action=\"bypass -h admin_server admserver.htm\" width=80 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr></table><br><br>");
		replyMSG.append(
				"<table width=680 background=\"L2UI_CH3.refinewnd_back_Pattern\"><tr><td width=640 height=445>");
		replyMSG.append("<tr><br>");
		replyMSG.append("<center>");
		replyMSG.append("<table><tr>");
		replyMSG.append("<td valign=\"top\" align=\"center\"><font color=22FF00>Всего в игре</font>: "
				+ GameObjectsStorage.getAllPlayersSize() + "</td>");
		replyMSG.append("<td valign=\"top\" align=\"center\"><font color=B59A75>Офлайн трейд</font>: "
				+ GameObjectsStorage.getAllOfflineCount() + "</td>");
		replyMSG.append("<td valign=\"top\" align=\"center\"><font color=0999FF>Phantom</font>: "
				+ GameObjectsStorage.getAllPhantomCount() + "</td>");
		replyMSG.append("<td valign=\"top\" align=\"center\"><font color=FFFFFF>Игроки</font>: "
				+ (GameObjectsStorage.getAllPlayersSize() - GameObjectsStorage.getAllOfflineCount()
						- GameObjectsStorage.getAllPhantomCount())
				+ "</td>");
		replyMSG.append("</tr></table></center>");
		replyMSG.append("<center><table width=600>");
		replyMSG.append(
				"<tr><td width=600>Вы можете найти фантома написав его имя и нажать на поиск ниже.<font color=LEVEL> Имена должны быть написаны с учетом регистра.</font></td></tr>");
		replyMSG.append("</table></center><br>");
		replyMSG.append("<center><table><tr><td>");
		replyMSG.append(
				"<edit var=\"character_name\" width=80></td><td><button value=\"Найти\" action=\"bypass -h admin_find_character $character_name\" width=45 height=15 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">");
		replyMSG.append("</td></tr></table></center></center><br><br>");
		replyMSG.append("<center>");
		replyMSG.append("<table><tr>");
		if (players.size() > MaxCharactersPerPage) {
			int pgs = 0;
			for (int ar = 0; ar < players.size(); ar += MaxCharactersPerPage) {
				pgs++;
			}
			int sh = 0;
			if (page > 2) {
				sh = page - 2;
				if (page > (pgs - 3)) {
					sh = (pgs - 5);
				}
				if (sh < 0) {
					sh = 0;
				}
			}
			for (int ut = 0; ut < players.size(); ut += MaxCharactersPerPage) {
				int pag = ut / MaxCharactersPerPage;
				if (sh <= pag && sh + 5 > pag) {
					if (page == pag) {
						replyMSG.append("<td valign=\"top\" align=\"center\" width=30 align=center>["
								+ String.valueOf(pag + 1) + "]</td>");
					} else {
						replyMSG.append("<td width=30 align=center><button value=" + String.valueOf(pag + 1)
								+ " action=\"bypass -h admin_show_phantom " + pag
								+ " \" width=28 height=19 back=L2UI_CT1.ListCTRL_DF_Title_Down fore=L2UI_CT1.ListCTRL_DF_Title></td>");
					}
				}
			}
		}
		replyMSG.append("</tr></table></center>");
		replyMSG.append("<br>");
		replyMSG.append("<center>");
		replyMSG.append("<table width=460>");
		replyMSG.append(
				"<tr><td width=100>Ник:</td><td width=110>Класс:</td><td width=60>Уровень:</td><td width=30>PA:</td></tr>");
		for (int i = CharactersStart; i < CharactersEnd; i++) {
			Player p = players.get(i);
			if (p.getPhantomType() == PhantomType.PHANTOM_CRAFTER || p.getPhantomType() == PhantomType.PHANTOM_TRADER)
				color = "808080";
			else if (p.isInZone(ZoneType.peace_zone) || p.isInZone(ZoneType.RESIDENCE))
				color = "FFFFFF";
			else if (p.phantom_params.getTrafficScheme() != null)
				color = "FFFF00";
			else if (p.getFraction() == Fraction.FIRE)
				color = Fraction.FIRE.getButtonColor();
			else if (p.getFraction() == Fraction.WATER)
				color = Fraction.WATER.getButtonColor();
			else
				color = "FFFFFF";
			has_bonus = "<font color=B59A75>-</font>";
			replyMSG.append(
					"<tr><td width=100><a action=\"bypass -h admin_character_list " + p.getName() + "\"><font color="
							+ color + ">" + p.getName() + "</font></a></td><td width=110>" + p.getClassId().name()
							+ "</td><td width=40>" + p.getLevel() + "</td><td width=30>" + has_bonus + "</td></tr>");
		}
		replyMSG.append("</table></td></tr></table>");
		replyMSG.append("</center>");
		replyMSG.append("</body></html>");
		ShowBoardPacket.separateAndSend(replyMSG.toString(), activeChar);
	}

}

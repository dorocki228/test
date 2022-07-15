package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.features.foursepulchers.instances.SepulcherNpcInstance;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.features.foursepulchers.FourSepulchersHall;
import l2s.gameserver.features.foursepulchers.FourSepulchersManager;

/**
 * @author pchayka
 */
public final class FourSepulchersGatekeeperInstance extends SepulcherNpcInstance {
	public FourSepulchersGatekeeperInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
		super(objectId, template, set);
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state) {
		if(ask == -2000) {
			if(reply == 1) {
				tryEntry(player, false);
			} else if(reply == 2) {
				tryEntry(player, true);
			}
		}
		super.onMenuSelect(player, ask, reply, state);
	}

	private synchronized void tryEntry(Player player, boolean unstable)
	{
		int npcId = getNpcId();
		if(npcId < 31921 || npcId > 31924)
			return;

		if(unstable || !FourSepulchersManager.Companion.isEntryTime())
		{
			showChatWindow(player, SepulcherNpcInstance.HTML_FILE_PATH + getNpcId() + "-NE.htm", false);
			return;
		}

		FourSepulchersHall fourSepulchersHall = FourSepulchersManager.Companion.getHalls().get(npcId - 31920);
		if (fourSepulchersHall == null) {
			return;
		}

		if(fourSepulchersHall.getInUse().get())
		{
			showChatWindow(player, SepulcherNpcInstance.HTML_FILE_PATH + getNpcId() + "-FULL.htm", false);
			return;
		}

		Party party = player.getParty();
		if(party != null) {
			if (party.getMemberCount() < 4) {
				showChatWindow(player, SepulcherNpcInstance.HTML_FILE_PATH + getNpcId() + "-SP.htm", false);
				return;
			}

			if (!party.isLeader(player)) {
				showChatWindow(player, SepulcherNpcInstance.HTML_FILE_PATH + getNpcId() + "-NL.htm", false);
				return;
			}

			for (Player member : party.getPartyMembers()) {
				if (member.getInventory().getItemByItemId(FourSepulchersManager.Companion.getENTRANCE_PASS()) == null) {
					showChatWindow(player, SepulcherNpcInstance.HTML_FILE_PATH + getNpcId() + "-SE.htm", false);
					return;
				}

				/*if (!member.isQuestContinuationPossible(true))
					return;*/

				if (member.isDead() || !member.isInRange(player, 700) || member.getLevel() < 80) { // TODO: Пересмотреть сообщение.
					showChatWindow(player, SepulcherNpcInstance.HTML_FILE_PATH + getNpcId() + "-SP.htm", false);
					return;
				}
			}
		} else if(!player.isGM()) {
			showChatWindow(player, SepulcherNpcInstance.HTML_FILE_PATH + getNpcId() + "-SP.htm", false);
			return;
		}

		fourSepulchersHall.entry(player, unstable);
	}
}
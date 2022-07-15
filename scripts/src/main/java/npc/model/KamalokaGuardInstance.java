package npc.model;

import instances.custom.Kamaloka;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.AbnormalVisualEffect;
import l2s.gameserver.templates.InstantZone;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * @author Java-man
 */
public final class KamalokaGuardInstance extends NpcInstance
{
	public KamalokaGuardInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.startsWith("kamaloka"))
		{
			int val = Integer.parseInt(command.substring(9));
			if ((val >= 57 && val <= 79) || val == 134)
				ReflectionUtils.simpleEnterInstancedZone(player, Kamaloka.class, val);
		}
		else if(command.startsWith("escape"))
		{
			if(player.getParty() == null || !player.getParty().isLeader(player))
			{
				showChatWindow(player, "not_party_leader.htm", false);
				return;
			}

			Reflection reflection = player.getReflection();
			if(reflection != null && !reflection.isDefault()) {
				for(Player p : reflection.getPlayers()) {
					escapePlayer(p);
				}
			}
		}
		else if(command.startsWith("return"))
		{
			escapePlayer(player);
		}
		else {
			Reflection reflection = getReflection();
			if(command.startsWith("unlock_miniboss"))
			{
				if (reflection.getVariable("miniboss_unlocked", false)) {
					player.sendMessage("Boss already unlocked.");
					return;
				}

				if (!ItemFunctions.deleteItem(player, 7260, 1)) {
					player.sendMessage("You don't have a key.");
				}

				reflection.setVariable("miniboss_unlocked", true);

				InstantZone iz = reflection.getInstancedZone();
				if (iz != null) {
					int boss = iz.getAddParams().getInteger("mini_boss", -1);
					if (boss != -1) {
						unlockBoss(reflection, boss);
					}
				}
			}
			else if(command.startsWith("unlock_finalboss"))
			{
				if (!reflection.getVariable("mini_boss_killed", false)) {
					player.sendMessage("You need to kill mini boss first.");
					return;
				}

				if (reflection.getVariable("finalboss_unlocked", false)) {
					player.sendMessage("Boss already unlocked.");
					return;
				}

				if (!ItemFunctions.deleteItem(player, 7260, 1)) {
					player.sendMessage("You don't have a key.");
				}

				reflection.setVariable("finalboss_unlocked", true);

				InstantZone iz = reflection.getInstancedZone();
				if (iz != null) {
					int boss = iz.getAddParams().getInteger("final_boss", -1);
					if (boss != -1) {
						unlockBoss(reflection, boss);
					}
				}
			}
			else
				super.onBypassFeedback(player, command);
		}
	}

	private void unlockBoss(Reflection reflection, int npcId) {
		reflection.getNpcs(true, npcId).forEach(npc -> {
			npc.stopAbnormalEffect(AbnormalVisualEffect.INVINCIBILITY);
			npc.getFlags().getInvulnerable().stop();
		});
	}

	private void escapePlayer(Player player) {
		Reflection r = player.getReflection();
		if(r.getReturnLoc() != null)
			player.teleToLocation(r.getReturnLoc(), ReflectionManager.MAIN);
		else
			player.setReflection(ReflectionManager.MAIN);
	}

	@Override
	public String getHtmlDir(String filename, Player player)
	{
		return "instance/kamaloka/";
	}
}
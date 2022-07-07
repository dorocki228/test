package l2s.gameserver.handler.admincommands.impl;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.*;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.EarthQuakePacket;
import l2s.gameserver.network.l2.s2c.ExShowUsmPacket;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.network.l2.s2c.TutorialShowHtmlPacket;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.utils.Util;

import java.util.List;

public class AdminEffects implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().GodMode)
			return false;
        GameObject target = activeChar.getTarget();
		switch(command)
		{
			case admin_invis:
			case admin_vis:
			{
				if(activeChar.isGMInvisible())
				{
					activeChar.setGMInvisible(false);
					activeChar.sendUserInfo(true);
					List<Player> players = World.getAroundObservers(activeChar);
					for(Player p : players)
						p.sendPacket(p.addVisibleObject(activeChar, null));
					for(Servitor servitor : activeChar.getServitors())
					{
						for(Player p2 : players)
							p2.sendPacket(p2.addVisibleObject(servitor, null));
					}
					break;
				}
				activeChar.setGMInvisible(true);
				activeChar.sendUserInfo(true);
				World.removeObjectFromPlayers(activeChar);
				for(Servitor servitor2 : activeChar.getServitors())
				{
					World.removeObjectFromPlayers(servitor2);
				}
				break;
			}
			case admin_gmspeed:
			{
				int val;
				if(wordList.length < 2)
					val = 0;
				else
					try
					{
						val = Integer.parseInt(wordList[1]);
					}
					catch(Exception e2)
					{
						activeChar.sendMessage("USAGE: //gmspeed value=[0..4]");
						return false;
					}
				int sh_level = 0;
				for(Abnormal e : activeChar.getAbnormalList().getEffects())
					if(e.getSkill().getId() == 7029)
					{
						sh_level = e.getSkill().getLevel();
						break;
					}
				if(val == 0)
				{
					if(sh_level != 0)
						activeChar.doCast(SkillHolder.getInstance().getSkillEntry(7029, sh_level), activeChar, true);
					activeChar.unsetVar("gm_gmspeed");
					break;
				}
				if(val < 1 || val > 4)
				{
					activeChar.sendMessage("USAGE: //gmspeed value=[0..4]");
					break;
				}
				if(Config.SAVE_GM_EFFECTS)
					activeChar.setVar("gm_gmspeed", String.valueOf(val), -1L);
				if(val != sh_level)
				{
					if(sh_level != 0)
						activeChar.doCast(SkillHolder.getInstance().getSkillEntry(7029, sh_level), activeChar, true);
					activeChar.doCast(SkillHolder.getInstance().getSkillEntry(7029, val), activeChar, true);
					break;
				}
				break;
			}
			case admin_invul:
			{
				handleInvul(activeChar, activeChar);
				if(!activeChar.isGM() || !activeChar.isInvul())
				{
					activeChar.unsetVar("gm_invul");
					break;
				}
				if(Config.SAVE_GM_EFFECTS)
				{
					activeChar.setVar("gm_invul", "true", -1L);
					break;
				}
				break;
			}
		}
		if(!activeChar.isGM())
			return false;
		switch(command)
		{
			case admin_offline_vis:
			{
				for(Player player : GameObjectsStorage.getPlayers())
					if(player != null && player.isInOfflineMode())
					{
						player.setInvisible(false);
						player.decayMe();
						player.spawnMe();
					}
				break;
			}
			case admin_offline_invis:
			{
				for(Player player : GameObjectsStorage.getPlayers())
					if(player != null && player.isInOfflineMode())
					{
						player.setInvisible(true);
						player.decayMe();
					}
				break;
			}
			case admin_earthquake:
			{
				try
				{
					int intensity = Integer.parseInt(wordList[1]);
					int duration = Integer.parseInt(wordList[2]);
					activeChar.broadcastPacket(new EarthQuakePacket(activeChar.getLoc(), intensity, duration));
					break;
				}
				catch(Exception e2)
				{
					activeChar.sendMessage("USAGE: //earthquake intensity duration");
					return false;
				}
			}
			case admin_block:
			{
				if(target == null || !target.isCreature())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				if(((Creature) target).isBlocked())
					return false;
				((Creature) target).abortAttack(true, false);
				((Creature) target).abortCast(true, false);
				((Creature) target).block();
				activeChar.sendMessage("Target blocked.");
				break;
			}
			case admin_unblock:
			{
				if(target == null || !target.isCreature())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				if(!((Creature) target).isBlocked())
					return false;
				((Creature) target).unblock();
				activeChar.sendMessage("Target unblocked.");
				break;
			}
			case admin_changename:
			{
				if(wordList.length < 2)
				{
					activeChar.sendMessage("USAGE: //changename newName");
					return false;
				}
				if(target == null)
					target = activeChar;
				if(!target.isCreature())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				String oldName = target.getName();
				String newName = Util.joinStrings(" ", wordList, 1);
				((Creature) target).setName(newName);
				((Creature) target).broadcastCharInfo();
				activeChar.sendMessage("Changed name from " + oldName + " to " + newName + ".");
				break;
			}
			case admin_setinvul:
			{
				if(target == null || !target.isPlayer())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				handleInvul(activeChar, (Player) target);
				break;
			}
			case admin_getinvul:
			{
				if(target != null && target.isCreature())
				{
					activeChar.sendMessage("Target " + target.getName() + "(object ID: " + target.getObjectId() + ") is " + (((Creature) target).isInvul() ? "" : "NOT ") + "invul");
					break;
				}
				break;
			}
			case admin_social:
			{
				int val;
				if(wordList.length < 2)
					val = Rnd.get(1, 7);
				else
					try
					{
						val = Integer.parseInt(wordList[1]);
					}
					catch(NumberFormatException nfe)
					{
						activeChar.sendMessage("USAGE: //social value");
						return false;
					}
				if(target == null || target == activeChar)
				{
					activeChar.broadcastPacket(new SocialActionPacket(activeChar.getObjectId(), val));
					break;
				}
				if(target.isCreature())
				{
					((Creature) target).broadcastPacket(new SocialActionPacket(target.getObjectId(), val));
					break;
				}
				break;
			}
			case admin_abnormal:
			{
                AbnormalEffect ae = AbnormalEffect.NONE;
                try
				{
					if(wordList.length > 1)
						ae = AbnormalEffect.VALUES[Integer.parseInt(wordList[1])];
				}
				catch(Exception e3)
				{
					activeChar.sendMessage("USAGE: //abnormal id");
					activeChar.sendMessage("//abnormal - Clears all abnormal effects");
					return false;
				}
				Creature effectTarget = target == null ? activeChar : (Creature) target;
				if(ae == AbnormalEffect.NONE)
				{
					effectTarget.startAbnormalEffect(AbnormalEffect.NONE);
					effectTarget.sendMessage("Abnormal effects clearned by admin.");
					if(effectTarget != activeChar)
					{
						effectTarget.sendMessage("Abnormal effects clearned.");
						break;
					}
					break;
				}
				else
				{
					effectTarget.startAbnormalEffect(ae);
					effectTarget.sendMessage("Admin added abnormal effect: " + ae.getName());
					if(effectTarget != activeChar)
					{
						effectTarget.sendMessage("Added abnormal effect: " + ae.getName());
						break;
					}
					break;
				}
			}
			case admin_showmovie:
			{
				if(wordList.length < 2)
				{
					activeChar.sendMessage("USAGE: //showmovie id");
					return false;
				}
				int id;
				try
				{
					id = Integer.parseInt(wordList[1]);
				}
				catch(NumberFormatException e4)
				{
					activeChar.sendMessage("You must specify id");
					return false;
				}
				activeChar.startScenePlayer(id);
				break;
			}
			case admin_showusm:
			{
				if(wordList.length < 2)
				{
					activeChar.sendMessage("USAGE: //showusm id");
					return false;
				}
				int usmId;
				try
				{
					usmId = Integer.parseInt(wordList[1]);
				}
				catch(NumberFormatException e5)
				{
					activeChar.sendMessage("You must specify id");
					return false;
				}
				activeChar.sendPacket(new ExShowUsmPacket(usmId));
				break;
			}
			case admin_showchtml:
			{
				if(wordList.length < 2)
				{
					activeChar.sendMessage("USAGE: //showhtml file_name");
					return false;
				}
				String fileName = fullString.replace(wordList[0] + " ", "");
				activeChar.sendPacket(new TutorialShowHtmlPacket(TutorialShowHtmlPacket.LARGE_WINDOW, "..\\L2text\\" + fileName + ".htm"));
				break;
			}
		}
		return true;
	}

	private void handleInvul(Player activeChar, Player target)
	{
		if(target.isInvul())
		{
			target.setInvul(false);
			for(Servitor servitor : target.getServitors())
				servitor.setInvul(false);

			activeChar.sendMessage(target.getName() + " is now mortal.");
		}
		else
		{
			target.setInvul(true);
			for(Servitor servitor : target.getServitors())
				servitor.setInvul(true);

			activeChar.sendMessage(target.getName() + " is now immortal.");
		}
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private enum Commands
	{
		admin_invis,
		admin_vis,
		admin_offline_vis,
		admin_offline_invis,
		admin_earthquake,
		admin_block,
		admin_unblock,
		admin_changename,
		admin_gmspeed,
		admin_invul,
		admin_setinvul,
		admin_getinvul,
		admin_social,
		admin_abnormal,
		admin_showmovie,
		admin_showusm,
		admin_showchtml
    }
}

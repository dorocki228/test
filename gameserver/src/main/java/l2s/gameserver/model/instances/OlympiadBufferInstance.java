/*
 * Decompiled with CFR 0_122.
 * 
 * Could not load the following classes:
 *  gnu.trove.set.hash.TIntHashSet
 */
package l2s.gameserver.model.instances;

import gnu.trove.set.hash.TIntHashSet;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.handler.onshiftaction.OnShiftActionHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.MyTargetSelectedPacket;
import l2s.gameserver.network.l2.s2c.ValidateLocationPacket;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class OlympiadBufferInstance extends NpcInstance
{
	private final TIntHashSet buffs = new TIntHashSet();

	public OlympiadBufferInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onAction(Player player, boolean shift)
	{
		if(shift && OnShiftActionHolder.getInstance().callShiftAction(player, OlympiadBufferInstance.class, this, true))
			return;

		if(this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new ValidateLocationPacket(this));
		}
		else
		{
			player.sendPacket(new MyTargetSelectedPacket(player, this));
			if(!checkInteractionDistance(player))
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else if(buffs.size() > 4)
			{
				showChatWindow(player, 1, false);
			}
			else
			{
				showChatWindow(player, 0, false);
			}
			player.sendActionFailed();
		}
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player))
			return;

		if(buffs.size() > 4)
		{
			showChatWindow(player, 1, false);
		}

		StringTokenizer st = new StringTokenizer(command);
		String cmd = st.nextToken();
		if("buff".equalsIgnoreCase(cmd))
		{
			int id = Integer.parseInt(st.nextToken());
			int lvl = Integer.parseInt(st.nextToken());
			boolean checked = false;
			for(int[] buff : Olympiad.BUFFS_LIST)
			{
				if(buff.length != 2 || buff[0] != id || buff[1] != lvl)
					continue;
				checked = true;
				break;
			}
			if(!checked)
			{
				showChatWindow(player, 0, false);
				return;
			}
			Skill skill = SkillHolder.getInstance().getSkill(id, lvl);
			ArrayList<Creature> target = new ArrayList<>();
			target.add(player);
			broadcastPacket(new MagicSkillUse(this, player, id, lvl, 0, 0));
			callSkill(skill, target, true, false);
			buffs.add(id);
			if(buffs.size() > 4)
			{
				showChatWindow(player, 1, false);
			}
			else
			{
				showChatWindow(player, 0, false);
			}
		}
		else
		{
			showChatWindow(player, 0, false);
		}
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
	{
		String fileName = "olympiad/buffer";

		if(val > 0)
			fileName = fileName + "-" + val;

		fileName = fileName + ".htm";
		player.sendPacket(new HtmlMessage(this, fileName).setPlayVoice(firstTalk));
	}

    @Override
    public boolean isAttackable(Creature attacker)
    {
        return false;
    }

    @Override
    public boolean isAutoAttackable(Creature attacker)
    {
        return false;
    }
}

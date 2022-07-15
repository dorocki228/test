package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.stats.DoubleStat;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.CreatureTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.util.List;

public class AdminFightCalculator implements IAdminCommandHandler
{
	private enum Commands
	{
		admin_fight_calculator,
		admin_fight_calculator_show,
		admin_fcs
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		switch(command)
		{
			case admin_fight_calculator:
				handleStart(activeChar, wordList);
				break;
			case admin_fight_calculator_show:
				handleShow(activeChar, wordList);
				break;
			case admin_fcs:
				handleShow(activeChar, wordList);
				break;
		}

		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void handleStart(Player activeChar, String[] params)
	{
		int lvl1 = 0;
		int lvl2 = 0;
		int mid1 = 0;
		int mid2 = 0;
		for (String param : params) {
			if (param.equals("lvl1"))
			{
				lvl1 = Integer.parseInt(param);
				continue;
			}
			else if (param.equals("lvl2"))
			{
				lvl2 = Integer.parseInt(param);
				continue;
			}
			else if (param.equals("mid1"))
			{
				mid1 = Integer.parseInt(param);
				continue;
			}
			else if (param.equals("mid2"))
			{
				mid2 = Integer.parseInt(param);
				continue;
			}
		}

		NpcTemplate npc1 = null;
		if (mid1 != 0)
		{
			npc1 = NpcHolder.getInstance().getTemplate(mid1);
		}
		NpcTemplate npc2 = null;
		if (mid2 != 0)
		{
			npc2 = NpcHolder.getInstance().getTemplate(mid2);
		}

		final String replyMSG;

		if ((npc1 != null) && (npc2 != null))
		{
			replyMSG = "<html><title>Selected mobs to fight</title><body><table><tr><td>First</td><td>Second</td></tr><tr><td>level " + lvl1 + "</td><td>level " + lvl2 + "</td></tr>" + "<tr><td>id " + npc1.getId() + "</td><td>id " + npc2.getId() + "</td></tr>" + "<tr><td>" + npc1.getName() + "</td><td>" + npc2.getName() + "</td></tr>" + "</table>" + "<center><br><br><br>" + "<button value=\"OK\" action=\"bypass -h admin_fight_calculator_show " + npc1.getId() + " " + npc2.getId() + "\"  width=100 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "</center>" + "</body></html>";
		}
		else if ((lvl1 != 0) && (npc1 == null))
		{
			final List<NpcTemplate> npcs = NpcHolder.getInstance().getAllOfLevel(lvl1);
			final StringBuilder sb = new StringBuilder(50 + (npcs.size() * 200));
			sb.append("<html><title>Select first mob to fight</title><body><table>");

			for (NpcTemplate n : npcs)
			{
				sb.append("<tr><td><a action=\"bypass -h admin_fight_calculator lvl1 " + lvl1 + " lvl2 " + lvl2 + " mid1 " + n.getId() + " mid2 " + mid2 + "\">" + n.getName() + "</a></td></tr>");
			}

			sb.append("</table></body></html>");
			replyMSG = sb.toString();
		}
		else if ((lvl2 != 0) && (npc2 == null))
		{
			final List<NpcTemplate> npcs = NpcHolder.getInstance().getAllOfLevel(lvl2);
			final StringBuilder sb = new StringBuilder(50 + (npcs.size() * 200));
			sb.append("<html><title>Select second mob to fight</title><body><table>");

			for (NpcTemplate n : npcs)
			{
				sb.append("<tr><td><a action=\"bypass -h admin_fight_calculator lvl1 " + lvl1 + " lvl2 " + lvl2 + " mid1 " + mid1 + " mid2 " + n.getId() + "\">" + n.getName() + "</a></td></tr>");
			}

			sb.append("</table></body></html>");
			replyMSG = sb.toString();
		}
		else
		{
			replyMSG = "<html><title>Select mobs to fight</title>" + "<body>" + "<table>" + "<tr><td>First</td><td>Second</td></tr>" + "<tr><td><edit var=\"lvl1\" width=80></td><td><edit var=\"lvl2\" width=80></td></tr>" + "</table>" + "<center><br><br><br>" + "<button value=\"OK\" action=\"bypass -h admin_fight_calculator lvl1 $lvl1 lvl2 $lvl2\"  width=100 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "</center>" + "</body></html>";
		}

		final NpcHtmlMessagePacket adminReply = new NpcHtmlMessagePacket(0, 1, false, replyMSG);
		activeChar.sendPacket(adminReply);
	}

	private void handleShow(Player activeChar, String[] params)
	{
		Creature npc1 = null;
		Creature npc2 = null;
		if (params.length == 0)
		{
			npc1 = activeChar;
			npc2 = (Creature) activeChar.getTarget();
			if (npc2 == null)
			{
				activeChar.sendPacket(SystemMsg.INVALID_TARGET);
				return;
			}
		}
		else
		{
			int mid1 = 0;
			int mid2 = 0;
			mid1 = Integer.parseInt(params[0]);
			mid2 = Integer.parseInt(params[1]);

			npc1 = new MonsterInstance(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(mid1), CreatureTemplate.getEmptyStatsSet());
			npc2 = new MonsterInstance(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(mid2), CreatureTemplate.getEmptyStatsSet());
		}

		int miss1 = 0;
		int miss2 = 0;
		int shld1 = 0;
		int shld2 = 0;
		int crit1 = 0;
		int crit2 = 0;
		double patk1 = 0;
		double patk2 = 0;
		double pdef1 = 0;
		double pdef2 = 0;
		double dmg1 = 0;
		double dmg2 = 0;

		// ATTACK speed in milliseconds
		int sAtk1 = Formulas.INSTANCE.calculateTimeBetweenAttacks(npc1.getPAtkSpd());
		int sAtk2 = Formulas.INSTANCE.calculateTimeBetweenAttacks(npc2.getPAtkSpd());
		// number of ATTACK per 100 seconds
		sAtk1 = 100000 / sAtk1;
		sAtk2 = 100000 / sAtk2;

		for (int i = 0; i < 10000; i++)
		{
			boolean _miss1 = Formulas.INSTANCE.calcHitMiss(npc1, npc2);
			if (_miss1)
			{
				miss1++;
			}
			byte _shld1 = Formulas.INSTANCE.calcShldUse(npc1, npc2, false);
			if (_shld1 > 0)
			{
				shld1++;
			}
			boolean _crit1 = Formulas.INSTANCE.calcCrit(npc1.getStat().getCriticalHit(), npc1, npc2, null);
			if (_crit1)
			{
				crit1++;
			}

			double _patk1 = npc1.getStat().getPAtk();
			_patk1 += npc1.getRandomDamageMultiplier();
			patk1 += _patk1;

			double _pdef1 = npc1.getStat().getPDef();
			pdef1 += _pdef1;

			if (!_miss1)
			{
				double _dmg1 = Formulas.INSTANCE.calcAutoAttackDamage(npc1, npc2, _shld1, _crit1, false);
				dmg1 += _dmg1;
				npc1.abortAttack(true, false);
			}
		}

		for (int i = 0; i < 10000; i++)
		{
			boolean _miss2 = Formulas.INSTANCE.calcHitMiss(npc2, npc1);
			if (_miss2)
			{
				miss2++;
			}
			byte _shld2 = Formulas.INSTANCE.calcShldUse(npc2, npc1, false);
			if (_shld2 > 0)
			{
				shld2++;
			}
			boolean _crit2 = Formulas.INSTANCE.calcCrit(npc2.getStat().getCriticalHit(), npc2, npc1, null);
			if (_crit2)
			{
				crit2++;
			}

			double _patk2 = npc2.getStat().getPAtk();
			_patk2 *= npc2.getRandomDamageMultiplier();
			patk2 += _patk2;

			double _pdef2 = npc2.getStat().getPDef();
			pdef2 += _pdef2;

			if (!_miss2)
			{
				double _dmg2 = Formulas.INSTANCE.calcAutoAttackDamage(npc2, npc1, _shld2, _crit2, false);
				dmg2 += _dmg2;
				npc2.abortAttack(true, false);
			}
		}

		miss1 /= 100;
		miss2 /= 100;
		shld1 /= 100;
		shld2 /= 100;
		crit1 /= 100;
		crit2 /= 100;
		patk1 /= 10000;
		patk2 /= 10000;
		pdef1 /= 10000;
		pdef2 /= 10000;
		dmg1 /= 10000;
		dmg2 /= 10000;

		// total damage per 100 seconds
		int tdmg1 = (int) (sAtk1 * dmg1);
		int tdmg2 = (int) (sAtk2 * dmg2);
		// HP restored per 100 seconds
		double maxHp1 = npc1.getMaxHp();
		int hp1 = (int) ((npc1.getStat().getValue(DoubleStat.HP_REGEN) * 100000) / Formulas.INSTANCE.getRegeneratePeriod(npc1));

		double maxHp2 = npc2.getMaxHp();
		int hp2 = (int) ((npc2.getStat().getValue(DoubleStat.HP_REGEN) * 100000) / Formulas.INSTANCE.getRegeneratePeriod(npc2));

		final StringBuilder replyMSG = new StringBuilder(1000);
		replyMSG.append("<html><title>Selected mobs to fight</title><body><table>");

		if (params.length == 0)
		{
			replyMSG.append("<tr><td width=140>Parameter</td><td width=70>me</td><td width=70>target</td></tr>");
		}
		else
		{
			replyMSG.append("<tr><td width=140>Parameter</td><td width=70>" + ((NpcTemplate) npc1.getTemplate()).getName() + "</td><td width=70>" + ((NpcTemplate) npc2.getTemplate()).getName() + "</td></tr>");
		}

		replyMSG.append("<tr><td>miss</td><td>" + miss1 + "%</td><td>" + miss2 + "%</td></tr><tr><td>shld</td><td>" + shld2 + "%</td><td>" + shld1 + "%</td></tr><tr><td>crit</td><td>" + crit1 + "%</td><td>" + crit2 + "%</td></tr><tr><td>pAtk / pDef</td><td>" + (int) patk1 + " / " + (int) pdef1 + "</td><td>" + (int) patk2 + " / " + (int) pdef2 + "</td></tr><tr><td>made hits</td><td>" + sAtk1 + "</td><td>" + sAtk2 + "</td></tr><tr><td>dmg per hit</td><td>" + (int) dmg1 + "</td><td>" + (int) dmg2 + "</td></tr><tr><td>got dmg</td><td>" + tdmg2 + "</td><td>" + tdmg1 + "</td></tr><tr><td>got regen</td><td>" + hp1 + "</td><td>" + hp2 + "</td></tr><tr><td>had HP</td><td>" + (int) maxHp1 + "</td><td>" + (int) maxHp2 + "</td></tr><tr><td>die</td>");

		if ((tdmg2 - hp1) > 1)
		{
			replyMSG.append("<td>" + ((int) ((100 * maxHp1) / (tdmg2 - hp1))) + " sec</td>");
		}
		else
		{
			replyMSG.append("<td>never</td>");
		}

		if ((tdmg1 - hp2) > 1)
		{
			replyMSG.append("<td>" + ((int) ((100 * maxHp2) / (tdmg1 - hp2))) + " sec</td>");
		}
		else
		{
			replyMSG.append("<td>never</td>");
		}

		replyMSG.append("</tr>" + "</table>" + "<center><br>");

		if (params.length == 0)
		{
			replyMSG.append("<button value=\"Retry\" action=\"bypass -h admin_fight_calculator_show\"  width=100 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		else
		{
			replyMSG.append("<button value=\"Retry\" action=\"bypass -h admin_fight_calculator_show " + npc1.getTemplate().getId() + " " + npc2.getTemplate().getId() + "\"  width=100 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}

		replyMSG.append("</center>" + "</body></html>");

		final NpcHtmlMessagePacket adminReply = new NpcHtmlMessagePacket(0, 1, false, replyMSG);
		activeChar.sendPacket(adminReply);

		if (params.length != 0)
		{
			npc1.deleteMe();
			npc2.deleteMe();
		}
	}

	private void handleCancel(Player activeChar, String targetName)
	{
		GameObject obj = activeChar.getTarget();
		if(targetName != null)
		{
			Player plyr = World.getPlayer(targetName);
			if(plyr != null)
				obj = plyr;
			else
				try
				{
					int radius = Math.max(Integer.parseInt(targetName), 100);
					for(Creature character : activeChar.getAroundCharacters(radius, 200))
					{
						character.getAbnormalList().stopAll();
						if(character.isPlayer())
							character.getPlayer().deleteCubics();
					}
					activeChar.sendMessage("Apply Cancel within " + radius + " unit radius.");
					return;
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage("Enter valid player name or radius");
					return;
				}
		}

		if(obj == null)
			obj = activeChar;
		if(obj.isCreature())
		{
			Creature creature = (Creature) obj;
			creature.getAbnormalList().stopAll();
			if(creature.isPlayer())
				creature.getPlayer().deleteCubics();
		}
		else
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
	}
}
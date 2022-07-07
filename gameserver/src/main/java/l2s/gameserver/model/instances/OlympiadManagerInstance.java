package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.olympiad.CompType;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.entity.olympiad.OlympiadDatabase;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.c2s.RequestBypassToServer;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExHeroListPacket;
import l2s.gameserver.network.l2.s2c.ExOlympiadMatchList;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.StringTokenizer;

public class OlympiadManagerInstance extends NpcInstance
{
	private static final Logger _log = LoggerFactory.getLogger(OlympiadManagerInstance.class);

	public OlympiadManagerInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
		if(Config.ENABLE_OLYMPIAD && getNpcId() == 31688)
			Olympiad.addOlympiadNpc(this);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(command.startsWith("_olympiad?"))
		{
			if(!Config.ENABLE_OLYMPIAD)
				return;

			String[] ar = command.split("&");

			if(ar.length < 2)
				return;

			if(!"_olympiad?command=move_op_field".equalsIgnoreCase(ar[0]))
				return;

			if(!Config.ENABLE_OLYMPIAD_SPECTATING)
				return;

			String[] command2 = ar[1].split("=");

			if(command2.length < 2)
				return;

			Olympiad.addObserver(Integer.parseInt(command2[1]) - 1, player);
			return;
		}

		if(!canBypassCheck(player))
			return;

		if(!Config.ENABLE_OLYMPIAD)
			return;

		StringTokenizer st = new StringTokenizer(command);
		String cmd = st.nextToken();

		if("noble_manage".equalsIgnoreCase(cmd))
		{
			if(!Config.ENABLE_OLYMPIAD)
				return;

			if(!st.hasMoreTokens())
				return;

			int val = Integer.parseInt(st.nextToken());
			switch(val)
			{
				case 1:
				{
					Olympiad.unregisterParticipant(player);
					showChatWindow(player, 0, false);
					return;
				}
				case 2:
				{
					if(Olympiad.isRegistered(player, false))
					{
						showChatWindow(player, "olympiad/manager_noregister.htm", false);
						return;
					}
					int count = Olympiad._nonClassBasedRegisters.size() + Olympiad._classBasedRegisters.size();
					showChatWindow(player, "olympiad/manager_register_no_classed.htm", false, "<?period?>", Olympiad.getCurrentCycle(), "<?week?>", Olympiad.getCompWeek(), "<?members_count?>", count);
					return;
				}
				case 4:
				{
					Olympiad.registerParticipant(player, CompType.NON_CLASSED);
					return;
				}
				case 6:
				{
					int passes = Olympiad.getParticipantRewardCount(player, true);
					if(passes > 0)
					{
						player.getInventory().addItem(45584, passes * 20);
						player.sendPacket(SystemMessagePacket.obtainItems(45584, passes * 20, 0));
						return;
					}
					showChatWindow(player, "olympiad/manager_nopoints.htm", false);
					return;
				}
				case 7:
				{
					MultiSellHolder.getInstance().SeparateAndSend(103, player, getObjectId(), 0.0);
					return;
				}
				case 9:
				{
//					MultiSellHolder.getInstance().SeparateAndSend(103, player, 0.0);
					return;
				}
				case 11:
				{
					if(player.isHero() || player.isCustomHero())
					{
						for(int itemId : ItemTemplate.HERO_WEAPON_IDS)
						{
							if(player.getInventory().getItemByItemId(itemId) == null)
								continue;

							showChatWindow(player, "olympiad/monument_weapon_have.htm", false);
							return;
						}
						showChatWindow(player, "olympiad/monument_weapon_list.htm", false);
						return;
					}
					showChatWindow(player, "olympiad/monument_weapon_no_hero.htm", false);
					return;
				}
				case 12:
				{
					if(player.isHero() || player.isCustomHero())
					{
						for(int itemId : ItemTemplate.HERO_WEAPON_IDS)
						{
							if(player.getInventory().getItemByItemId(itemId) == null)
								continue;

							showChatWindow(player, "olympiad/monument_weapon_have.htm", false);
							return;
						}
						if(!st.hasMoreTokens())
							return;

						int weaponId = Integer.parseInt(st.nextToken());

						if(!ArrayUtils.contains(ItemTemplate.HERO_WEAPON_IDS, weaponId))
							return;

						ItemFunctions.addItem(player, weaponId, 1, true);
						showChatWindow(player, "olympiad/monument_weapon_give.htm", false);
						return;
					}
					showChatWindow(player, "olympiad/monument_weapon_no_hero.htm", false);
					return;
				}
				case 13:
				{
					if(player.isHero() || player.isCustomHero())
					{
						if(player.getInventory().getItemByItemId(6842) != null)
						{
							showChatWindow(player, "olympiad/monument_circlet_have.htm", false);
							return;
						}

						ItemFunctions.addItem(player, 6842, 1, true);
						showChatWindow(player, "olympiad/monument_circlet_give.htm", false);
						return;
					}
					showChatWindow(player, "olympiad/monument_circlet_no_hero.htm", false);
					return;
				}
				case 14:
				{
					if(player.isHero() || player.isCustomHero())
					{
						if(player.getInventory().getItemByItemId(30372) != null)
						{
							showChatWindow(player, "olympiad/monument_cloak_have.htm", false);
							return;
						}

						ItemFunctions.addItem(player, 30372, 1, true);
						showChatWindow(player, "olympiad/monument_cloak_hero_give.htm", false);
						return;
					}

					showChatWindow(player, "olympiad/monument_cloak_no_hero.htm", false);
					return;
				}
				default:
				{
					_log.warn("Olympiad System: Couldnt send packet for request " + val);
				}
			}
			return;
		}
		else if("manage".equalsIgnoreCase(cmd))
		{
			if(!Config.ENABLE_OLYMPIAD)
				return;

			if(!st.hasMoreTokens())
				return;

			int val = Integer.parseInt(st.nextToken());
			HtmlMessage reply = new HtmlMessage(this);
			switch(val)
			{
				case 1:
				{
					if(!Olympiad.inCompPeriod() || Olympiad.isOlympiadEnd())
					{
						player.sendPacket(SystemMsg.THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS);
						return;
					}
					player.sendPacket(new ExOlympiadMatchList(player));
					return;
				}
				case 2:
				{
					if(!st.hasMoreTokens())
						return;

					int classId = Integer.parseInt(st.nextToken());
					if(classId < 88)
						return;

					reply.setFile("olympiad/manager_ranking.htm");
					List<String> names = OlympiadDatabase.getClassLeaderBoard(classId);
					int index = 1;
					for(String name : names)
					{
						reply.replace("%place" + index + "%", String.valueOf(index));
						reply.replace("%rank" + index + "%", name);
						if(++index <= 10)
							continue;
						break;
					}
					while(index <= 10)
					{
						reply.replace("%place" + index + "%", "");
						reply.replace("%rank" + index + "%", "");
						++index;
					}
					player.sendPacket(reply);
					return;
				}
				case 3:
				{
					if(!Config.ENABLE_OLYMPIAD_SPECTATING)
						return;

					Olympiad.addObserver(Integer.parseInt(command.substring(11)), player);
					return;
				}
				case 4:
				{
					player.sendPacket(new ExHeroListPacket());
					return;
				}
				case 5:
				{
					if(Hero.getInstance().isInactiveHero(player.getObjectId()))
					{
						Hero.getInstance().activateHero(player);
						reply.setFile("olympiad/monument_give_hero.htm");
					}
					else
					{
						reply.setFile("olympiad/monument_dont_hero.htm");
					}
					player.sendPacket(reply);
					return;
				}
				default:
				{
					_log.warn("Olympiad System: Couldnt send packet for request " + val);
				}
			}
			return;
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... arg)
	{
		String fileName = "olympiad/";
		int npcId = getNpcId();
		switch(npcId)
		{
			case 31688:
			{
				fileName = fileName + "manager";
				break;
			}
			default:
			{
				fileName = fileName + "monument";
			}
		}

		if(player.getLevel() >= Config.OLYMPIAD_MIN_LEVEL && player.getClassId().getClassLevel().ordinal() >= 2)
			fileName = fileName + "_n";

		if(val > 0)
			fileName = fileName + "-" + val;

		fileName = fileName + ".htm";
		player.sendPacket(new HtmlMessage(this, fileName).setPlayVoice(firstTalk));
	}

	@Override
	public boolean canPassPacket(Player player, Class<? extends L2GameClientPacket> packet, Object... arg)
	{
		return packet == RequestBypassToServer.class && arg.length == 1 && "_olympiad?command=move_op_field".equals(arg[0]);
	}
}

package l2s.gameserver.handler.admincommands.impl;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.ai.CharacterAI;
import l2s.gameserver.dao.SpawnsDAO;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.instancemanager.RaidBossSpawnManager;
import l2s.gameserver.model.*;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminSpawn implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().CanEditNPC)
			return false;
		switch(command)
		{
			case admin_show_spawns:
			{
				activeChar.sendPacket(new HtmlMessage(5).setFile("admin/spawns.htm"));
				break;
			}
			case admin_spawn_index:
			{
				try
				{
					String val = fullString.substring(18);
					activeChar.sendPacket(new HtmlMessage(5).setFile("admin/spawns/" + val + ".htm"));
				}
				catch(StringIndexOutOfBoundsException ex)
				{}
				break;
			}
			case admin_spawn1:
			{
				StringTokenizer st = new StringTokenizer(fullString, " ");
				try
				{
					st.nextToken();
					String id = st.nextToken();
					int mobCount = 1;
					if(st.hasMoreTokens())
						mobCount = Integer.parseInt(st.nextToken());
					spawnMonster(activeChar, id, 0, mobCount);
				}
				catch(Exception ex2)
				{}
				break;
			}
			case admin_spawn:
			case admin_spawn_monster:
			{
				StringTokenizer st = new StringTokenizer(fullString, " ");
				try
				{
					st.nextToken();
					String id = st.nextToken();
					int mobCount2 = 1;
					if(st.hasMoreTokens())
						mobCount2 = Integer.parseInt(st.nextToken());
					int respawnTime = 30;
					if(st.hasMoreTokens())
						respawnTime = Integer.parseInt(st.nextToken());
					spawnMonster(activeChar, id, respawnTime, mobCount2);
				}
				catch(Exception ex3)
				{}
				break;
			}
			case admin_setai:
			{
				if(activeChar.getTarget() == null || !activeChar.getTarget().isNpc())
				{
					activeChar.sendMessage("Please select target NPC or mob.");
					return false;
				}
				StringTokenizer st = new StringTokenizer(fullString, " ");
				st.nextToken();
				if(!st.hasMoreTokens())
				{
					activeChar.sendMessage("Please specify AI name.");
					return false;
				}
				String aiName = st.nextToken();
				NpcInstance target = (NpcInstance) activeChar.getTarget();
				Constructor<?> aiConstructor = null;
				try
				{
					if(!"npc".equalsIgnoreCase(aiName))
						aiConstructor = Class.forName("l2s.gameserver.ai." + aiName).getConstructors()[0];
				}
				catch(Exception e)
				{
					try
					{
						aiConstructor = Scripts.getInstance().getClasses().get("ai." + aiName).getConstructors()[0];
					}
					catch(Exception e2)
					{
						activeChar.sendMessage("This type AI not found.");
						return false;
					}
				}
				if(aiConstructor != null)
				{
					try
					{
						target.setAI((CharacterAI) aiConstructor.newInstance(target));
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					target.getAI().startAITask();
					break;
				}
				break;
			}
			case admin_setaiparam:
			{
				if(activeChar.getTarget() == null || !activeChar.getTarget().isNpc())
				{
					activeChar.sendMessage("Please select target NPC or mob.");
					return false;
				}
				StringTokenizer st = new StringTokenizer(fullString, " ");
				st.nextToken();
				if(!st.hasMoreTokens())
				{
					activeChar.sendMessage("Please specify AI parameter name.");
					activeChar.sendMessage("USAGE: //setaiparam <param> <value>");
					return false;
				}
				String paramName = st.nextToken();
				if(!st.hasMoreTokens())
				{
					activeChar.sendMessage("Please specify AI parameter value.");
					activeChar.sendMessage("USAGE: //setaiparam <param> <value>");
					return false;
				}
				String paramValue = st.nextToken();
				NpcInstance target = (NpcInstance) activeChar.getTarget();
				target.setParameter(paramName, paramValue);
				target.decayMe();
				target.spawnMe();
				activeChar.sendMessage("AI parameter " + paramName + " succesfully setted to " + paramValue);
				break;
			}
			case admin_dumpparams:
			{
				if(activeChar.getTarget() == null || !activeChar.getTarget().isNpc())
				{
					activeChar.sendMessage("Please select target NPC or mob.");
					return false;
				}
				NpcInstance target = (NpcInstance) activeChar.getTarget();
				MultiValueSet<String> set = target.getParameters();
				if(!set.isEmpty())
				{
					System.out.println("Dump of Parameters:\r\n" + set);
					break;
				}
				System.out.println("Parameters is empty.");
				break;
			}
			case admin_setheading:
			{
				GameObject obj = activeChar.getTarget();
				if(!obj.isNpc())
				{
					activeChar.sendMessage("Target is incorrect!");
					return false;
				}
				NpcInstance npc = (NpcInstance) obj;
				npc.setHeading(activeChar.getHeading());
				npc.decayMe();
				npc.spawnMe();
				activeChar.sendMessage("New heading : " + activeChar.getHeading());
				Spawner spawn = npc.getSpawn();
				if(spawn == null)
				{
					activeChar.sendMessage("Spawn for this npc == null!");
					return false;
				}
				break;
			}
			case admin_generate_loc:
			{
				if(wordList.length < 2)
				{
					activeChar.sendMessage("Incorrect argument count!");
					return false;
				}
				int id2 = Integer.parseInt(wordList[1]);
				int id3 = 0;
				if(wordList.length > 2)
					id3 = Integer.parseInt(wordList[2]);
				int min_x = Integer.MIN_VALUE;
				int min_y = Integer.MIN_VALUE;
				int min_z = Integer.MIN_VALUE;
				int max_x = Integer.MAX_VALUE;
				int max_y = Integer.MAX_VALUE;
				int max_z = Integer.MAX_VALUE;
				String name = "";
				for(NpcInstance _npc : World.getAroundNpc(activeChar))
					if(_npc.getNpcId() == id2 || _npc.getNpcId() == id3)
					{
						name = _npc.getName();
						min_x = Math.min(min_x, _npc.getX());
						min_y = Math.min(min_y, _npc.getY());
						min_z = Math.min(min_z, _npc.getZ());
						max_x = Math.max(max_x, _npc.getX());
						max_y = Math.max(max_y, _npc.getY());
						max_z = Math.max(max_z, _npc.getZ());
					}
				min_x -= 500;
				min_y -= 500;
				max_x += 500;
				max_y += 500;
				System.out.println("(0,'" + name + "'," + min_x + "," + min_y + "," + min_z + "," + max_z + ",0),");
				System.out.println("(0,'" + name + "'," + min_x + "," + max_y + "," + min_z + "," + max_z + ",0),");
				System.out.println("(0,'" + name + "'," + max_x + "," + max_y + "," + min_z + "," + max_z + ",0),");
				System.out.println("(0,'" + name + "'," + max_x + "," + min_y + "," + min_z + "," + max_z + ",0),");
				System.out.println("delete from spawnlist where npc_templateid in (" + id2 + ", " + id3 + ") and locx <= " + min_x + " and locy <= " + min_y + " and locz <= " + min_z + " and locx >= " + max_x + " and locy >= " + max_y + " and locz >= " + max_z + ";");
				break;
			}
			case admin_dumpspawn:
			{
				StringTokenizer st = new StringTokenizer(fullString, " ");
				try
				{
					st.nextToken();
					String id4 = st.nextToken();
					int respawnTime2 = 30;
					int mobCount3 = 1;
					spawnMonster(activeChar, id4, respawnTime2, mobCount3);
					try
					{
						new File("dumps").mkdir();
						File f = new File("dumps/spawndump.txt");
						if(!f.exists())
							f.createNewFile();
						FileWriter writer = new FileWriter(f, true);
						writer.write("<spawn count=\"1\" respawn=\"60\" respawn_random=\"0\" period_of_day=\"none\">\n\t<point x=\"" + activeChar.getLoc().x + "\" y=\"" + activeChar.getLoc().y + "\" z=\"" + activeChar.getLoc().z + "\" h=\"" + activeChar.getLoc().h + "\" />\n\t<npc id=\"" + Integer.parseInt(id4) + "\" /><!--" + NpcHolder.getInstance().getTemplate(Integer.parseInt(id4)).getName() + "-->\n</spawn>\n");
						writer.close();
					}
					catch(Exception ex4)
					{}
				}
				catch(Exception ex5)
				{}
				break;
			}
		}
		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private void spawnMonster(Player activeChar, String monsterId, int respawnTime, int mobCount)
	{
		GameObject target = activeChar.getTarget();
		if(target == null)
			target = activeChar;
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher regexp = pattern.matcher(monsterId);
		NpcTemplate template;
		if(regexp.matches())
		{
			int monsterTemplate = Integer.parseInt(monsterId);
			if(NpcHolder.getInstance().getTemplate(monsterTemplate) == null)
			{
				activeChar.sendMessage("Incorrect monster template.");
				return;
			}
			template = NpcHolder.getInstance().getTemplate(monsterTemplate);
		}
		else
		{
			monsterId = monsterId.replace('_', ' ');
			template = NpcHolder.getInstance().getTemplateByName(monsterId);
		}
		if(template == null)
		{
			activeChar.sendMessage("Incorrect monster template.");
			return;
		}
		if(RaidBossSpawnManager.getInstance().isDefined(template.getId()))
		{
			activeChar.sendMessage("Raid Boss " + template.name + " already spawned.");
			return;
		}
		int heading = activeChar.getHeading();
		Location loc = target.getLoc();
		try
		{
			SimpleSpawner spawn = new SimpleSpawner(template);
			spawn.setLoc(loc);
			spawn.setAmount(mobCount);
			spawn.setHeading(heading);
			spawn.setRespawnDelay(respawnTime);
			spawn.setReflection(activeChar.getReflection());
			spawn.init();
			if(respawnTime == 0)
				spawn.stopRespawn();
			activeChar.sendMessage("Created " + template.name + " on " + target.getObjectId() + ".");
		}
		catch(Exception e)
		{
			activeChar.sendMessage("Target is not ingame.");
			return;
		}
		SpawnsDAO.getInstance().insert(template.getId(), loc.x, loc.y, loc.z, heading, respawnTime, mobCount);
	}

	private enum Commands
	{
		admin_show_spawns,
		admin_spawn,
		admin_spawn_monster,
		admin_spawn_index,
		admin_spawn1,
		admin_setheading,
		admin_setai,
		admin_setaiparam,
		admin_dumpparams,
		admin_generate_loc,
		admin_dumpspawn
    }
}

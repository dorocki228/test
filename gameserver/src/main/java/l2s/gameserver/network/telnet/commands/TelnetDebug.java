package l2s.gameserver.network.telnet.commands;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.gameserver.GameServer;
import l2s.gameserver.config.xml.parser.HostsConfigParser;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.telnet.TelnetCommand;
import l2s.gameserver.network.telnet.TelnetCommandHolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TelnetDebug implements TelnetCommandHolder
{
	private final Set<TelnetCommand> _commands;

	public TelnetDebug()
	{
		(_commands = new LinkedHashSet<>()).add(new TelnetCommand("dumpnpc", "dnpc"){
			@Override
			public String getUsage()
			{
				return "dumpnpc";
			}

			@Override
			public String handle(String[] args)
			{
				StringBuilder sb = new StringBuilder();
				int total = 0;
				int maxId = 0;
				int maxCount = 0;
				TIntObjectHashMap<List<NpcInstance>> npcStats = new TIntObjectHashMap();
				for(NpcInstance npc : GameObjectsStorage.getNpcs())
				{
					int id = npc.getNpcId();
					List<NpcInstance> list;
					if((list = npcStats.get(id)) == null)
						npcStats.put(id, list = new ArrayList<>());
					list.add(npc);
					if(list.size() > maxCount)
					{
						maxId = id;
						maxCount = list.size();
					}
					++total;
				}
				sb.append("Total NPCs: ").append(total).append("\n");
				sb.append("Maximum NPC ID: ").append(maxId).append(" count : ").append(maxCount).append("\n");
				TIntObjectIterator<List<NpcInstance>> itr = npcStats.iterator();
				while(itr.hasNext())
				{
					itr.advance();
					int id2 = itr.key();
					List<NpcInstance> list = itr.value();
					sb.append("=== ID: ").append(id2).append(" ").append(" Count: ").append(list.size()).append(" ===").append("\n");
					for(NpcInstance npc2 : list)
						try
						{
							sb.append("AI: ");
							if(npc2.hasAI())
								sb.append(npc2.getAI().getClass().getName());
							else
								sb.append("none");
							sb.append(", ");
							if(npc2.getReflectionId() > 0)
							{
								sb.append("ref: ").append(npc2.getReflectionId());
								sb.append(" - ").append(npc2.getReflection().getName());
							}
							sb.append("loc: ").append(npc2.getLoc());
							sb.append(", ");
							sb.append("spawned: ");
							sb.append(npc2.isVisible());
							sb.append("\n");
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
				}
				try
				{
					new File("stats").mkdir();

					var format = new SimpleDateFormat("MMddHHmmss").format(System.currentTimeMillis());
					var file = Path.of("stats/NpcStats-" + format + ".txt");
					Files.writeString(file, sb.toString());
				}
				catch(IOException e2)
				{
					e2.printStackTrace();
				}
				return "NPC stats saved.\n";
			}
		});
		_commands.add(new TelnetCommand("asrestart"){
			@Override
			public String getUsage()
			{
				return "asrestart";
			}

			@Override
			public String handle(String[] args)
			{
				HostsConfigParser.getInstance().reload();
				GameServer.getInstance().getAuthServerCommunication().restart();
				return "Restarted.\n";
			}
		});
	}

	@Override
	public Set<TelnetCommand> getCommands()
	{
		return _commands;
	}
}

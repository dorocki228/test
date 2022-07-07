package l2s.gameserver.templates.spawn;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public class SpawnNpcInfo
{
	private final int _npcId;
	private final NpcTemplate _template;
	private Location _spawnLoc;
	private final int _max;
	private final MultiValueSet<String> _parameters;

	public SpawnNpcInfo(int npcId, int max, MultiValueSet<String> set)
	{
		_npcId = npcId;
		_template = NpcHolder.getInstance().getTemplate(npcId);
		_max = max;
		_parameters = set;
	}

	public int getNpcId()
	{
		return _npcId;
	}

	public Location getSpawnLoc()
	{
		return _spawnLoc;
	}

	public NpcTemplate getTemplate()
	{
		return _template;
	}

	public int getMax()
	{
		return _max;
	}

	public MultiValueSet<String> getParameters()
	{
		return _parameters;
	}
}

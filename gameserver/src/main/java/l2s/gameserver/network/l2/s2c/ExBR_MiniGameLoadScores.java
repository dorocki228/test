package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.instancemanager.games.MiniGameScoreManager;
import l2s.gameserver.model.Player;
import org.napile.pair.primitive.IntObjectPair;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExBR_MiniGameLoadScores extends L2GameServerPacket
{
	private int _place;
	private int _score;
	private int _lastScore;
	private final IntObjectMap<List<Map.Entry<String, Integer>>> _entries;

	public ExBR_MiniGameLoadScores(Player player)
	{
		_entries = new TreeIntObjectMap();
		int lastBig = 0;
		int i = 1;
		for(IntObjectPair<Set<String>> entry : MiniGameScoreManager.getInstance().getScores().entrySet())
			for(String name : entry.getValue())
			{
				List<Map.Entry<String, Integer>> set = _entries.get(i);
				if(set == null)
					_entries.put(i, set = new ArrayList<>());
				if(name.equalsIgnoreCase(player.getName()) && entry.getKey() > lastBig)
				{
					_place = i;
					lastBig = _score = entry.getKey();
				}
				set.add(Map.entry(name, entry.getKey()));
				++i;
				_lastScore = entry.getKey();
				if(i > 100)
					break;
			}
	}

	@Override
	protected void writeImpl()
	{
        writeD(_place);
        writeD(_score);
        writeD(0);
        writeD(_lastScore);
		for(IntObjectPair<List<Map.Entry<String, Integer>>> entry : _entries.entrySet())
			for(Map.Entry<String, Integer> scoreEntry : entry.getValue())
			{
                writeD(entry.getKey());
				writeS(scoreEntry.getKey());
                writeD(scoreEntry.getValue());
			}
	}
}

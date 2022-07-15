package l2s.gameserver.tables;

import static com.google.common.flogger.LazyArgs.lazy;

import com.google.common.flogger.FluentLogger;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.util.Arrays;
import java.util.Collection;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.SubClass;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.base.Sex;

/**
 * @author Bonux
 */
public final class SubClassTable
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	private static SubClassTable _instance;

	private TIntObjectMap<TIntSet> _subClasses;

	public SubClassTable()
	{
		init();
	}

	public static SubClassTable getInstance()
	{
		if(_instance == null)
			_instance = new SubClassTable();
		return _instance;
	}

	private void init()
	{
		_subClasses = new TIntObjectHashMap<TIntSet>();

		for(ClassId baseClassId : ClassId.values())
		{
			if(baseClassId.isDummy())
				continue;

			if(baseClassId.isOfLevel(ClassLevel.NONE))
				continue;

			if(baseClassId.isOfLevel(ClassLevel.FIRST))
				continue;

			TIntSet availSubs = new TIntHashSet();
			for(ClassId subClassId : ClassId.values())
			{
				if(subClassId.isDummy())
					continue;

				if(subClassId.isOfLevel(ClassLevel.NONE))
					continue;

				if(subClassId.isOfLevel(ClassLevel.FIRST))
					continue;

				if(!areClassesComportable(baseClassId, subClassId))
					continue;

				availSubs.add(subClassId.getId());
			}
			//availSubs.sort();
			_subClasses.put(baseClassId.getId(), availSubs);
		}
		_log.atInfo().log( "SubClassTable: Loaded %s sub-classes variations.", lazy(() -> _subClasses.size()) );
	}

	public int[] getAvailableSubClasses(Player player, int classId)
	{
		TIntSet subClassesList = _subClasses.get(classId);
		if(subClassesList == null || subClassesList.isEmpty())
			return new int[0];

		TIntSet tempSubClassesList = new TIntHashSet(subClassesList.size());
		tempSubClassesList.addAll(subClassesList);

		loop: for(int clsId : tempSubClassesList.toArray())
		{
			ClassId subClassId = ClassId.valueOf(clsId);
			if(subClassId.getClassLevel() != ClassLevel.SECOND)
			{
				tempSubClassesList.remove(clsId);
				continue;
			}

			if(player.getRace() == Race.ELF && subClassId.isOfRace(Race.DARKELF) || player.getRace() == Race.DARKELF && subClassId.isOfRace(Race.ELF)) // эльфы несовместимы с темными
			{
				tempSubClassesList.remove(clsId);
				continue;
			}

			Collection<SubClass> playerSubClasses = player.getSubClassList().values();
			for(SubClass playerSubClass : playerSubClasses)
			{
				ClassId playerSubClassId = ClassId.valueOf(playerSubClass.getClassId());
				if(!areClassesComportable(playerSubClassId, subClassId))
				{
					tempSubClassesList.remove(clsId);
					continue loop;
				}
			}
		}

		int[] result = tempSubClassesList.toArray();
		Arrays.sort(result);
		return result;
	}

	private static boolean areClassesComportable(ClassId baseClassId, ClassId subClassId)
	{
		if(baseClassId == subClassId)
			return false;
	
		if(ClassId.isKnight(baseClassId.getId()) && ClassId.isKnight(subClassId.getId()))
			return false;
	
		if(ClassId.isDagger(baseClassId.getId()) && ClassId.isDagger(subClassId.getId()))
			return false;
	
		if(ClassId.isBow(baseClassId.getId()) && ClassId.isBow(subClassId.getId()))
			return false;
	
		if(ClassId.isDance(baseClassId.getId()) && ClassId.isDance(subClassId.getId()))
			return false;
	
		if(ClassId.isWizard(baseClassId.getId()) && ClassId.isWizard(subClassId.getId()))
			return false;
	
		if(ClassId.isSummoner(baseClassId.getId()) && ClassId.isSummoner(subClassId.getId()))
			return false;
	
		if(ClassId.isHalfHealer(baseClassId.getId()) && ClassId.isHalfHealer(subClassId.getId()))
			return false;

		if(subClassId == ClassId.OVERLORD || subClassId == ClassId.WARSMITH)
			return false; // Данные классы запрещены к получению его как саб-класса.

		if(subClassId == ClassId.MAESTRO || subClassId == ClassId.DOMINATOR)
			return false; // Данные классы запрещены к получению его как саб-класса.

		return true;
	}
}
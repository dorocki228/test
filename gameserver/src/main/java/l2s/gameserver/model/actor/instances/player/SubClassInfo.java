package l2s.gameserver.model.actor.instances.player;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static l2s.gameserver.model.base.ClassId.*;

public final class SubClassInfo
{
	private static final Set<ClassId> MAIN_SUBCLASS_SET;
	private static final Set<ClassId> BANNED_SUBCLASSES = EnumSet.of(OVERLORD, WARSMITH);

	static
	{
		MAIN_SUBCLASS_SET = Arrays.stream(VALUES)
				.filter(classId -> classId.getClassLevel() == ClassLevel.SECOND)
				.collect(Collectors.toSet());
		MAIN_SUBCLASS_SET.removeAll(BANNED_SUBCLASSES);
	}

	/**
	 * Method getAvailableSubClasses.
	 * @param player Player
	 * @return Set<ClassId>
	 */
	public static Set<ClassId> getAvailableSubClasses(Player player)
	{
		List<ClassId> subClasses = player.getSubClassList().values().stream()
				.map(subClass -> VALUES[subClass.getClassId()])
				.collect(Collectors.toList());
		return MAIN_SUBCLASS_SET.stream()
				.filter(classId -> subClasses.stream().noneMatch(subClass -> equalsOrChildOf(classId, subClass)))
				.collect(Collectors.toSet());
	}

	private static boolean equalsOrChildOf(ClassId classId1, ClassId classId2) {
		return classId1.equalsOrChildOf(classId2) || classId2.equalsOrChildOf(classId1);
	}
}

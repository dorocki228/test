package l2s.gameserver.stats.conditions;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.CostumesHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.CostumeTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class ConditionPlayerCanGetCostume extends Condition {
	private final int[] values;
	private final boolean grades;

	public ConditionPlayerCanGetCostume(int[] values, boolean grades) {
		this.values = values;
		this.grades = grades;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value) {
		if (Config.EX_COSTUME_DISABLE)
			return false;

		if (target == null || !target.isPlayer())
			return false;

		Player player = target.getPlayer();
		int locationId = player.getLocationId();
		Set<CostumeTemplate> availableCostumes = new HashSet<>();
		if (grades) {
			for (int grade : values) {
				List<CostumeTemplate> costumeTemplates = CostumesHolder.getInstance().getCostumesByGrade(grade);
				for (CostumeTemplate costumeTemplate : costumeTemplates) {
					if (costumeTemplate.getLocationId() != -1 && costumeTemplate.getLocationId() != locationId)
						continue;

					availableCostumes.add(costumeTemplate);
				}
			}
		} else {
			for (int id : values) {
				CostumeTemplate costumeTemplate = CostumesHolder.getInstance().getCostume(id);
				if (costumeTemplate == null)
					continue;

				if(costumeTemplate.getLocationId() != -1 && costumeTemplate.getLocationId() != locationId)
					continue;

				availableCostumes.add(costumeTemplate);
			}
		}
		return !availableCostumes.isEmpty();
	}
}

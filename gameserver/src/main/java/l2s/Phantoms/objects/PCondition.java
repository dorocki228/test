package  l2s.Phantoms.objects;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import  l2s.Phantoms.enums.Condition;
import  l2s.gameserver.model.base.Element;
import  l2s.gameserver.templates.item.ArmorTemplate.ArmorType;
import  l2s.gameserver.templates.item.WeaponTemplate.WeaponType;
import  l2s.gameserver.utils.PositionUtils.TargetDirection;

public class PCondition
{
	protected final Logger _log = LoggerFactory.getLogger(PCondition.class);
	private Condition _skill_condition;
	private String _param;
	private List<Integer> list = new ArrayList<Integer>();
	//private List<NpcRace> npcRace = new ArrayList<NpcRace>();
	private List<WeaponType> weaponType = new ArrayList<WeaponType>();
	private List<ArmorType> armorType = new ArrayList<ArmorType>();
	private List<TargetDirection> targetDirection = new ArrayList<TargetDirection>();
	private int _IntParameter;
	
	private List<Element> element = new ArrayList<Element>();
	
	public PCondition(Condition skill_condition, String cond)
	{
		_skill_condition = skill_condition;
		switch (skill_condition)
		{
			case USE_OLYMPIAD:
			case NOT_USE_OLYMPIAD:
			case ONLY_MONSTER:
			case ONLY_PLAYER:
			case TARGET_FIGHTER:
			case TARGET_MAGE:
			case USE_IN_PARTY:
			case NOT_USE_IN_PARTY:
			case TARGET_SUMMON:
			case TARGET_NOT_RUNNING:
			case TARGET_RUNNING:
			case NO_TARGET:
			case CHARGING:
			case CUBIC:
			case IS_IN_BATTLE:
			case NOT_IN_BATTLE:
			case LIVE_SUMMON:
			case TARGET_IS_COMING:
			case TARGET_RUNS_AWAY:
			case SPOILED:
			case NOT_SPOILED:
			break;
			case SUMMON_NPC_ID:
			case TARGET_MAX_CP:
			case TARGET_MAX_HP:
			case TARGET_MAX_MP:
			case TARGET_MIN_CP:
			case TARGET_MIN_HP:
			case TARGET_MIN_MP:
			case TARGET_MAX_HP_COUNT:
			case TARGET_MIN_HP_COUNT:
			case MAX_CP:
			case MAX_HP:
			case MAX_MP:
			case MIN_CP:
			case MIN_HP:
			case MIN_MP:
			case MASS:
			case MIN_DISTANCE:
			case MAX_DISTANCE:
			case MASS_PLAYERS:
			case CHANCE_CAST:
			case MASS_MONSTER:
			case CANCEL_BUFF_BY_ID:
			case CONSUMED_SOULS:
			case DEBUFF_CHANCE:
			case SUMMON_MAX_HP:
			case SUMMON_MAX_MP:
			case SUMMON_MIN_HP:
			case SUMMON_MIN_MP:
			case SKILL_DISABLED:
			case MASS_MONSTER_SWEEPER:
			case MASS_MONSTER_SPOILED:
			{
				if (cond == null || cond.isEmpty())
					_log.info(skill_condition + " is emputy param");
				_IntParameter = Integer.parseInt(cond);
			}
			break;
			case SELF_NOT_SKILL_EFFECT:
			case TARGET_NOT_SKILL_EFFECT:
			case TARGET_SKILL_EFFECT:
			case SELF_SKILL_EFFECT:
			case MIN_PARTY_HP:
			case TARGET_CLASS_ID:
			case TARGET_NOT_USE_CLASS_ID:
			case TARGET_SKILL_DISABLED:
			case CANCELING_CAST:
			{
				String[] skill_list = cond.substring(1, cond.length() - 1).split(",");
				for (String id : skill_list)
					list.add(Integer.parseInt(id));
			}
			break;
			case TARGET_WEAPON_TYPE:
				if (cond == null || cond.isEmpty())
					_log.info(skill_condition + " is emputy param");
				String[] type_list = cond.substring(1, cond.length() - 1).split(",");
				for (String type : type_list)
					weaponType.add(WeaponType.valueOf(type.toUpperCase()));
			break;
			case TARGET_ARMOR_TYPE:
				if (cond == null || cond.isEmpty())
					_log.info(skill_condition + " is emputy param");
				String[] atype_list = cond.substring(1, cond.length() - 1).split(",");
				for (String type : atype_list)
					armorType.add(ArmorType.valueOf(type.toUpperCase()));
			break;
			case NPC_RACE:
			{
				/*if (cond == null || cond.isEmpty())
					_log.info(skill_condition + " is emputy param");
				String[] race_list = cond.substring(1, cond.length() - 1).split(",");
				for (String race : race_list)
					npcRace.add(NpcRace.valueOf(race.trim()));*/
			}
			break;
			case CHECK_WEAPON_ATTRIBUTE:
			case TARGET_CHECK_WEAPON_ATTRIBUTE:
				if (cond == null || cond.isEmpty())
					_log.info(skill_condition + " is emputy param");
				String[] element_list = cond.substring(1, cond.length() - 1).split(",");
				for (String type : element_list)
					element.add(Element.valueOf(type.trim()));
				break;
			case TARGET_DIRECTION:
				if (cond == null || cond.isEmpty())
					_log.info(skill_condition + " is emputy param");
				String[] td_list = cond.substring(1, cond.length() - 1).split(",");
				for (String type : td_list)
					targetDirection.add(TargetDirection.valueOf(type.trim()));
				break;
			case NEXT_ACTION:
				if (cond == null || cond.isEmpty())
					_log.info(skill_condition + " is emputy param");
				String[] param_list = cond.substring(1, cond.length() - 1).split(",");
				break;
			default:
				break;
		}
		_param = cond;
	}
	
/*	public List<NpcRace> getNpcRace()
	{
		return npcRace;
	}*/
	
	public String getParam()
	{
		return _param;
	}
	
	public int IntParameter()
	{
		return _IntParameter;
	}
	
	public List<Integer> getList()
	{
		return list;
	}
	
	public Condition getConditionType()
	{
		return _skill_condition;
	}
	
	@Override
	public String toString()
	{
		return _param;
	}
	
	public List<WeaponType> getWeaponTypeList()
	{
		return weaponType;
	}
	
	public List<ArmorType> getArmorTypeList()
	{
		return armorType;
	}

	public List<Element> getElement()
	{
		return element;
	}

	public List<TargetDirection> getTargetDirection()
	{
		return targetDirection;
	}
	
}
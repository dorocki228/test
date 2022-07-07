package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractHolder;
import l2s.commons.data.xml.AbstractParser;
import l2s.commons.reflect.FieldHelper;
import l2s.gameserver.model.base.PledgeRank;
import l2s.gameserver.model.base.Sex;
import l2s.gameserver.model.base.SubClassType;
import l2s.gameserver.model.entity.residence.ResidenceType;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.conditions.*;
import l2s.gameserver.stats.funcs.FuncTemplate;
import l2s.gameserver.stats.triggers.TriggerInfo;
import l2s.gameserver.stats.triggers.TriggerType;
import l2s.gameserver.templates.item.ArmorTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.utils.PositionUtils;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class StatParser<H extends AbstractHolder> extends AbstractParser<H>
{
	private static final Pattern TABLE_PATTERN = Pattern.compile("((?!;|:| |-).*?)((;|:| |-)|$)", 32);

	protected StatParser(H holder)
	{
		super(holder);
	}

	protected Condition parseFirstCond(Element sub, int... arg)
	{
		List<Element> e = sub.elements();
		if(e.isEmpty())
			return null;
		Element element = e.get(0);
		return parseCond(element, arg);
	}

	protected Condition parseCond(Element element, int... arg)
	{
		String name = element.getName();
		if("and".equalsIgnoreCase(name))
			return parseLogicAnd(element, arg);
		if("or".equalsIgnoreCase(name))
			return parseLogicOr(element, arg);
		if("not".equalsIgnoreCase(name))
			return parseLogicNot(element, arg);
		if("target".equalsIgnoreCase(name))
			return parseTargetCondition(element, arg);
		if("player".equalsIgnoreCase(name))
			return parsePlayerCondition(element, arg);
		if("using".equalsIgnoreCase(name))
			return parseUsingCondition(element, arg);
		if("zone".equalsIgnoreCase(name))
			return parseZoneCondition(element, arg);
		if("has".equalsIgnoreCase(name))
			return parseHasCondition(element, arg);
		if("game".equalsIgnoreCase(name))
			return parseGameCondition(element, arg);
		if("this".equalsIgnoreCase(name))
			return parseThisCondition(element, arg);
		else if("link".equalsIgnoreCase(name))
			return parseClassLinkCondition(element);
		return null;
	}

	protected Condition parseClassLinkCondition(final Element element) {
		final Condition cond;
		final String name = element.attributeValue("name");
		final String value = element.attributeValue("value");
		Object obj;
		try {
			final Class<?> clazz = Class.forName("l2s.gameserver.stats.conditions.link." + name);
			obj = clazz.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if(!(obj instanceof Condition))
			throw new RuntimeException(String.format("Not Inheritor from %s", "Condition class"));
		for (final String str : value.split(";")) {
			final String[] arr = str.split(":");
			final String fieldName = arr[0];
			String val = arr[1];
			try {
				FieldHelper.setObjectField(obj, fieldName, val, ",");
			} catch (NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		((Condition) obj).init();
		cond = joinAnd(null, (Condition) obj);
		return cond;
	}

	protected Condition parseLogicAnd(Element n, int... arg)
	{
		ConditionLogicAnd cond = new ConditionLogicAnd();
		Iterator<Element> iterator = n.elementIterator();
		while(iterator.hasNext())
		{
			Element condElement = iterator.next();
			cond.add(parseCond(condElement, arg));
		}
		if(cond._conditions == null || cond._conditions.length == 0)
            error("Empty <and> condition in " + getCurrentFileName());
		return cond;
	}

	protected Condition parseLogicOr(Element n, int... arg)
	{
		ConditionLogicOr cond = new ConditionLogicOr();
		Iterator<Element> iterator = n.elementIterator();
		while(iterator.hasNext())
		{
			Element condElement = iterator.next();
			cond.add(parseCond(condElement, arg));
		}
		if(cond._conditions == null || cond._conditions.length == 0)
            error("Empty <or> condition in " + getCurrentFileName());
		return cond;
	}

	protected Condition parseLogicNot(Element n, int... arg)
	{
		Iterator<Element> iterator = n.elements().iterator();
		if(iterator.hasNext())
		{
			Object element = iterator.next();
			return new ConditionLogicNot(parseCond((Element) element, arg));
		}
        error("Empty <not> condition in " + getCurrentFileName());
		return null;
	}

	protected Condition parseTargetCondition(Element element, int... arg)
	{
		Condition cond = null;
		Iterator<Attribute> iterator = element.attributeIterator();
		while(iterator.hasNext())
		{
			Attribute attribute = iterator.next();
			String name = attribute.getName();
			String value = parseString(attribute.getValue(), arg);
			if("is_pet_feed".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionTargetPetFeed(Integer.parseInt(value)));
			else if("type".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionTargetType(value));
			else if("aggro".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionTargetAggro(Boolean.valueOf(value)));
			else if("mobId".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionTargetMobId(Integer.parseInt(value)));
			else if("race".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionTargetRace(value));
			else if("npc_class".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionTargetNpcClass(value));
			else if("playerRace".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionTargetPlayerRace(value));
			else if("forbiddenClassIds".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionTargetForbiddenClassId(value.split(";")));
			else if("playerSameClan".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionTargetClan(value));
			else if("castledoor".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionTargetCastleDoor(Boolean.valueOf(value)));
			else if("direction".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionTargetDirection(PositionUtils.TargetDirection.valueOf(value.toUpperCase())));
			else if("percentHP".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionTargetPercentHp(Integer.parseInt(value)));
			else if("percentMP".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionTargetPercentMp(Integer.parseInt(value)));
			else if("percentCP".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionTargetPercentCp(Integer.parseInt(value)));
			else if("hasBuffId".equalsIgnoreCase(name))
			{
				StringTokenizer st = new StringTokenizer(value, ";");
				int id = Integer.parseInt(st.nextToken().trim());
				int level = -1;
				if(st.hasMoreTokens())
					level = parseNumber(st.nextToken().trim(), arg).intValue();
				cond = joinAnd(cond, new ConditionTargetHasBuffId(id, level));
			}
			else if("has_abnormal_type".equalsIgnoreCase(name))
			{
				StringTokenizer st = new StringTokenizer(value, ";");
				AbnormalType at = Enum.valueOf(AbnormalType.class, st.nextToken().trim());
				int level = -1;
				if(st.hasMoreTokens())
					level = parseNumber(st.nextToken().trim(), arg).intValue();
				cond = joinAnd(cond, new ConditionTargetHasBuff(at, level));
			}
			else if("has_abnormal_type_same_lvl".equalsIgnoreCase(name))
			{
				StringTokenizer st = new StringTokenizer(value, ";");
				AbnormalType at = Enum.valueOf(AbnormalType.class, st.nextToken().trim());
				int level = -1;
				if(st.hasMoreTokens())
					level = parseNumber(st.nextToken().trim(), arg).intValue();
				cond = joinAnd(cond, new ConditionTargetHasBuffSameLvl(at, level));
			}
			else if("hasForbiddenSkill".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionTargetHasForbiddenSkill(Integer.parseInt(value)));
			else
			{
				if(!"min_distance".equalsIgnoreCase(name))
					continue;
				cond = joinAnd(cond, new ConditionTargetMinDistance(Integer.parseInt(value)));
			}
		}
		return cond;
	}

	protected Condition parseZoneCondition(Element element, int... arg)
	{
		Condition cond = null;
		Iterator<Attribute> iterator = element.attributeIterator();
		while(iterator.hasNext())
		{
			Attribute attribute = iterator.next();
			String name = attribute.getName();
			String value = parseString(attribute.getValue(), arg);
			if("type".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionZoneType(value));
			else
			{
				if(!"name".equalsIgnoreCase(name))
					continue;
				cond = joinAnd(cond, new ConditionZoneName(value));
			}
		}
		return cond;
	}

	protected Condition parseHasCondition(Element element, int... arg)
	{
		Condition cond = null;
		Iterator<Attribute> iterator = element.attributeIterator();
		while(iterator.hasNext())
		{
			Attribute attribute = iterator.next();
			String name = attribute.getName();
			String value = parseString(attribute.getValue(), arg);
			if("skill".equalsIgnoreCase(name))
			{
				StringTokenizer st = new StringTokenizer(value, ";");
				int id = Integer.parseInt(st.nextToken().trim());
				int level = parseNumber(st.nextToken().trim(), arg).intValue();
				cond = joinAnd(cond, new ConditionHasSkill(id, level));
			}
			else
			{
				if(!"success".equalsIgnoreCase(name))
					continue;
				cond = joinAnd(cond, new ConditionFirstEffectSuccess(Boolean.valueOf(value)));
			}
		}
		return cond;
	}

	protected Condition parseThisCondition(Element element, int... arg)
	{
		Condition cond = null;
		Iterator<Attribute> iterator = element.attributeIterator();
		while(iterator.hasNext())
		{
			Attribute attribute = iterator.next();
			String name = attribute.getName();
			String value = parseString(attribute.getValue(), arg);
			if("level".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionThisLevel(Integer.parseInt(value)));
		}
		return cond;
	}

	protected Condition parseGameCondition(Element element, int... arg)
	{
		Condition cond = null;
		Iterator<Attribute> iterator = element.attributeIterator();
		while(iterator.hasNext())
		{
			Attribute attribute = iterator.next();
			String name = attribute.getName();
			String value = parseString(attribute.getValue(), arg);
			if("night".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionGameTime(ConditionGameTime.CheckGameTime.NIGHT, Boolean.valueOf(value)));
		}
		return cond;
	}

	protected Condition parsePlayerCondition(Element element, int... arg)
	{
		Condition cond = null;
		Iterator<Attribute> iterator = element.attributeIterator();
		while(iterator.hasNext())
		{
			Attribute attribute = iterator.next();
			String name = attribute.getName();
			String value = parseString(attribute.getValue(), arg);
			if("residence".equalsIgnoreCase(name))
			{
				String[] st = value.split(";");
				cond = joinAnd(cond, new ConditionPlayerResidence(Integer.parseInt(st[1]), ResidenceType.valueOf(st[0].toUpperCase())));
			}
			else if("classId".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerClassId(value.split(",")));
			else if("olympiad".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerOlympiad(Boolean.valueOf(value)));
			else if("instance_zone".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerInstanceZone(Integer.parseInt(value)));
			else if("is_clan_leader".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerIsClanLeader(Boolean.valueOf(value)));
			else if("is_hero".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerIsHero(Boolean.valueOf(value)));
			else if("is_chaotic".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerIsChaotic(Boolean.valueOf(value)));
			else if("race".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerRace(value));
			else if("sex".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerSex(Sex.valueOf(value.toUpperCase())));
			else if("castle_type".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerCastleType(Integer.parseInt(value)));
			else if("max_level".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerMaxLevel(Integer.parseInt(value)));
			else if("min_clan_level".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerMinClanLevel(Integer.parseInt(value)));
			else if("avail_max_sp".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerMaxSP(Integer.parseInt(value)));
			else if("minLevel".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerMinLevel(Integer.parseInt(value)));
			else if("class_type".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerClassType(SubClassType.valueOf(value.toUpperCase())));
			else if("isFlagged".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerFlagged(Boolean.valueOf(value)));
			else if("damage".equalsIgnoreCase(name))
			{
				StringTokenizer st2 = new StringTokenizer(value, ";");
				double min = Double.parseDouble(st2.nextToken().trim());
				double max = 2.147483647E9;
				if(st2.hasMoreTokens())
					max = parseNumber(st2.nextToken().trim(), arg).doubleValue();
				cond = joinAnd(cond, new ConditionPlayerMinMaxDamage(min, max));
			}
			else if("quest_state".equalsIgnoreCase(name))
			{
				StringTokenizer st2 = new StringTokenizer(value, ";");
				int questId = parseNumber(st2.nextToken().trim(), arg).intValue();
				int condId = parseNumber(st2.nextToken().trim(), arg).intValue();
				cond = joinAnd(cond, new ConditionPlayerQuestState(questId, condId));
			}
			else if("min_pledge_rank".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerMinPledgeRank(PledgeRank.valueOf(value.toUpperCase())));
			else if("summon_siege_golem".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerSummonSiegeGolem());
			else if("maxPK".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerMaxPK(Integer.parseInt(value)));
			else if("resting".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RESTING, Boolean.valueOf(value)));
			else if("moving".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.MOVING, Boolean.valueOf(value)));
			else if("running".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RUNNING, Boolean.valueOf(value)));
			else if("standing".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.STANDING, Boolean.valueOf(value)));
			else if("flying".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.FLYING, Boolean.valueOf(value)));
			else if("flyingTransform".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.FLYING_TRANSFORM, Boolean.valueOf(value)));
			else if("currentHP".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerCurrentHp(Integer.parseInt(value)));
			else if("percentHP".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerPercentHp(Integer.parseInt(value)));
			else if("percentMP".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerPercentMp(Integer.parseInt(value)));
			else if("percentCP".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerPercentCp(Integer.parseInt(value)));
			else if("clan_leader_online".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerClanLeaderOnline(Boolean.valueOf(value)));
			else if("riding".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerRiding(ConditionPlayerRiding.CheckPlayerRiding.valueOf(value.toUpperCase())));
			else if("hasBuffId".equalsIgnoreCase(name))
			{
				StringTokenizer st2 = new StringTokenizer(value, ";");
				int id = Integer.parseInt(st2.nextToken().trim());
				int level = -1;
				if(st2.hasMoreTokens())
					level = parseNumber(st2.nextToken().trim(), arg).intValue();
				cond = joinAnd(cond, new ConditionPlayerHasBuffId(id, level));
			}
			else if("has_abnormal_type".equalsIgnoreCase(name))
			{
				StringTokenizer st2 = new StringTokenizer(value, ";");
				AbnormalType at = Enum.valueOf(AbnormalType.class, st2.nextToken().trim());
				int level = -1;
				if(st2.hasMoreTokens())
					level = parseNumber(st2.nextToken().trim(), arg).intValue();
				cond = joinAnd(cond, new ConditionPlayerHasBuff(at, level));
			}
			else if("has_abnormal_type_same_lvl".equalsIgnoreCase(name))
			{
				StringTokenizer st2 = new StringTokenizer(value, ";");
				AbnormalType at = Enum.valueOf(AbnormalType.class, st2.nextToken().trim());
				int level = -1;
				if(st2.hasMoreTokens())
					level = parseNumber(st2.nextToken().trim(), arg).intValue();
				cond = joinAnd(cond, new ConditionPlayerHasBuffSameLvl(at, level));
			}
			else if("has_summon_id".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerHasSummonId(Integer.parseInt(value)));
			else if("can_transform".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerCanTransform(Integer.parseInt(value)));
			else if("can_untransform".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerCanUntransform(Boolean.valueOf(value)));
			else if("agathion".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerAgathion(Integer.parseInt(value)));
			else if("chargesMin".equalsIgnoreCase(name))
			{
				int val = parseNumber(value).intValue();
				cond = joinAnd(cond, new ConditionPlayerChargesMin(val));
			}
			else if("can_learn_skill".equalsIgnoreCase(name))
			{
				StringTokenizer st2 = new StringTokenizer(value, "-");
				int id = parseNumber(st2.nextToken().trim(), arg).intValue();
				int level = 1;
				if(st2.hasMoreTokens())
					level = parseNumber(st2.nextToken().trim(), arg).intValue();
				cond = joinAnd(cond, new ConditionPlayerCanLearnSkill(id, level));
			}
			else if("fraction".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionPlayerFraction(value.split(",")));
		}
		return cond;
	}

	protected Condition parseUsingCondition(Element element, int... arg)
	{
		Condition cond = null;
		Iterator<Attribute> iterator = element.attributeIterator();
		while(iterator.hasNext())
		{
			Attribute attribute = iterator.next();
			String name = attribute.getName();
			String value = parseString(attribute.getValue(), arg);
			if("slotitem".equalsIgnoreCase(name))
			{
				StringTokenizer st = new StringTokenizer(value, ";");
				int id = Integer.parseInt(st.nextToken().trim());
				int slot = Integer.parseInt(st.nextToken().trim());
				int enchant = 0;
				if(st.hasMoreTokens())
					enchant = parseNumber(st.nextToken().trim(), arg).intValue();
				cond = joinAnd(cond, new ConditionSlotItemId(slot, id, enchant));
			}
			else if("kind".equalsIgnoreCase(name) || "weapon".equalsIgnoreCase(name))
			{
				long mask = 0L;
				StringTokenizer st2 = new StringTokenizer(value, ",");
				tokens: while(st2.hasMoreTokens())
				{
					String item = st2.nextToken().trim();
					for(WeaponTemplate.WeaponType wt : WeaponTemplate.WeaponType.VALUES)
					{
						if(wt.toString().equalsIgnoreCase(item))
						{
							mask |= wt.mask();
							continue tokens;
						}
					}
					for(ArmorTemplate.ArmorType at : ArmorTemplate.ArmorType.VALUES)
					{
						if(at.toString().equalsIgnoreCase(item))
						{
							mask |= at.mask();
							continue tokens;
						}
					}
					error("Invalid item kind: \"" + item + "\" in " + getCurrentFileName());
				}
				if(mask == 0L)
					continue;
				cond = joinAnd(cond, new ConditionUsingItemType(mask));
			}
			else if("skill".equalsIgnoreCase(name))
				cond = joinAnd(cond, new ConditionUsingSkill(Integer.parseInt(value)));
			else
			{
				if(!"armor".equalsIgnoreCase(name))
					continue;
				cond = joinAnd(cond, new ConditionUsingArmor(ArmorTemplate.ArmorType.valueOf(value.toUpperCase())));
			}
		}
		return cond;
	}

	protected Condition joinAnd(Condition cond, Condition c)
	{
		if(cond == null)
			return c;
		if(cond instanceof ConditionLogicAnd)
		{
			((ConditionLogicAnd) cond).add(c);
			return cond;
		}
		ConditionLogicAnd and = new ConditionLogicAnd();
		and.add(cond);
		and.add(c);
		return and;
	}

	protected void parseFor(Element forElement, StatTemplate template, int... arg)
	{
		Iterator<Element> iterator = forElement.elementIterator();
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			String elementName = element.getName();
			if("add".equalsIgnoreCase(elementName))
                attachFunc(element, template, "Add", arg);
			else if("set".equalsIgnoreCase(elementName))
                attachFunc(element, template, "Set", arg);
			else if("sub".equalsIgnoreCase(elementName))
                attachFunc(element, template, "Sub", arg);
			else if("mul".equalsIgnoreCase(elementName))
                attachFunc(element, template, "Mul", arg);
			else if ("div".equalsIgnoreCase(elementName))
                attachFunc(element, template, "Div", arg);
		}
	}

	protected void parseTriggers(Element f, StatTemplate triggerable, int... arg)
	{
		Iterator<Element> iterator = f.elementIterator();
		while(iterator.hasNext())
		{
			Element element = iterator.next();
			int id = parseNumber(element.attributeValue("id"), arg).intValue();
			int level = parseNumber(element.attributeValue("level"), arg).intValue();
			if(id > 0)
			{
				if(level <= 0)
					continue;
				TriggerType t = TriggerType.valueOf(parseString(element.attributeValue("type"), arg));
				double chance = element.attributeValue("chance") == null ? 100.0 : parseNumber(element.attributeValue("chance"), arg).doubleValue();
				boolean increasing = element.attributeValue("increasing") != null && parseBoolean(element.attributeValue("increasing"));
				int delay = element.attributeValue("delay") != null ? parseNumber(element.attributeValue("delay"), arg).intValue() * 1000 : 0;
				boolean cancel = element.attributeValue("cancel_effects_on_remove") != null && parseBoolean(element.attributeValue("cancel_effects_on_remove"));
				String args = element.attributeValue("args") != null ? element.attributeValue("args") : "";
				TriggerInfo trigger = new TriggerInfo(id, level, t, chance, increasing, delay, cancel, args);
				Condition condition = parseFirstCond(element, arg);
				if(condition != null)
					trigger.addCondition(condition);
				triggerable.addTrigger(trigger);
			}
		}
	}

	protected void attachFunc(Element n, StatTemplate template, String name, int... arg)
	{
		Stats stat = Stats.valueOfXml(n.attributeValue("stat"));
		String order = n.attributeValue("order");
		int ord = parseNumber(order, arg).intValue();
		Condition applyCond = parseFirstCond(n, arg);
		double val = 0.0;
		if(n.attributeValue("value") != null)
			val = parseNumber(n.attributeValue("value"), arg).doubleValue();
		template.attachFunc(new FuncTemplate(applyCond, name, stat, ord, val));
	}

	protected final Object parseValue(Object object, int... arg)
	{
		if(object == null)
			return null;
		String value = String.valueOf(object);
		if(value.isEmpty())
			return object;
		if(value.contains("#"))
		{
			StringBuilder sb = new StringBuilder();
			Matcher m = TABLE_PATTERN.matcher(value);
			while(m.find())
			{
				String temp = m.group(1);
				if(temp != null)
				{
					if(temp.isEmpty())
						continue;
					if(temp.charAt(0) == '#')
						sb.append(getTableValue(temp, arg));
					else
						sb.append(temp);
					temp = m.group(2);
					if(temp == null)
						continue;
					if(temp.isEmpty())
						continue;
					sb.append(temp);
				}
			}
			return sb.toString();
		}
		return object;
	}

	protected final String parseString(Object object, int... arg)
	{
		object = parseValue(object, arg);
		return String.valueOf(object);
	}

	protected final boolean parseBoolean(Object object, int... arg)
	{
		return Boolean.parseBoolean(parseString(object, arg));
	}

	protected final Number parseNumber(String value, int... arg)
	{
		value = parseString(value, arg);
		try
		{
			if("max".equalsIgnoreCase(value))
				return Double.POSITIVE_INFINITY;
			if("min".equalsIgnoreCase(value))
				return Double.NEGATIVE_INFINITY;
			if(value.indexOf(46) == -1)
			{
				int radix = 10;
				if(value.length() > 2 && "0x".equalsIgnoreCase(value.substring(0, 2)))
				{
					value = value.substring(2);
					radix = 16;
				}
				return Integer.valueOf(value, radix);
			}
			return Double.valueOf(value);
		}
		catch(NumberFormatException e)
		{
            warn("Error while parsing number: " + value, e);
			return null;
		}
	}

	protected abstract Object getTableValue(String p0, int... p1);
}

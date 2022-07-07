package  l2s.Phantoms.objects;


import java.util.ArrayList;
import java.util.List;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import  l2s.Phantoms.Utils.PhantomUtils;
import  l2s.Phantoms.templates.ItemsGroup;
import  l2s.Phantoms.templates.SkillsGroup;
import  l2s.gameserver.model.Skill;
import  l2s.gameserver.model.base.Element;

@XStreamAlias("ClassObject")
public class PhantomClassAI
{
	// общие
	@XStreamAlias("self_buff_skills")
	@XStreamAsAttribute
	private SkillsGroup self_buff_skills;
	
	@XStreamAlias("debuff_skills")
	@XStreamAsAttribute
	private SkillsGroup debuff_skills;
	
	@XStreamAlias("passive_skills")
	@XStreamAsAttribute
	private SkillsGroup passive_skills;
	
	@XStreamAlias("use_items")
	@XStreamAsAttribute
	private ItemsGroup consumble_items;
	
	@XStreamAlias("res_items")
	@XStreamAsAttribute
	private ItemsGroup resurrect_items;
	
	@XStreamAlias("resurrect_skills")
	@XStreamAsAttribute
	private SkillsGroup resurrect_skills;
	
	@XStreamAlias("control_skills")
	@XStreamAsAttribute
	private SkillsGroup control_skills;
	
	@XStreamAlias("cleansing_skills")
	@XStreamAsAttribute
	private SkillsGroup cleansing_skills;
	
	// для хилеров
	@XStreamAlias("heal_skills")
	@XStreamAsAttribute
	private SkillsGroup heal_skills;
	
	// для саппортов
	
	@XStreamAlias("buffs_skills")
	@XStreamAsAttribute
	private SkillsGroup buffs_skills; // баффы
	
	@XStreamAlias("support_skills")
	@XStreamAsAttribute
	private SkillsGroup support_skills; // мр заливалка, хил, рес и тд
	
	// по ситуации
	@XStreamAlias("situation_skills")
	@XStreamAsAttribute
	private SkillsGroup situation_skills;
	
	@XStreamAlias("summon_skills")
	@XStreamAsAttribute
	private SkillsGroup summon_skills;
	
	// спойлер
	@XStreamAlias("sweeper_skills")
	@XStreamAsAttribute
	private SkillsGroup sweeper_skills;
	
	@XStreamAlias("spoil_skills")
	@XStreamAsAttribute
	private SkillsGroup spoil_skills;
	
	// для нюкеров
	@XStreamAlias("nuke_skills")
	@XStreamAsAttribute
	private SkillsGroup nuke_skills;
	
	@XStreamAlias("aoe_skills")
	@XStreamAsAttribute
	private SkillsGroup aoe_skills1;
	
	@XStreamAlias("detection_skills")
	@XStreamAsAttribute
	private SkillsGroup detection_skills;
	
	@XStreamAlias("party_heal_skills")
	@XStreamAsAttribute
	private SkillsGroup party_heal_skills;
	
	@XStreamAlias("summon_actions")
	@XStreamAsAttribute
	private SkillsGroup summon_actions;
	
	@XStreamAlias("add_castom_skills")
	@XStreamAsAttribute
	private SkillsGroup add_castom_skills;
	
	// класс
	@XStreamAlias("class_id")
	@XStreamAsAttribute
	private int class_id;
	
	@XStreamAlias("class_name")
	@XStreamAsAttribute
	private String class_name;
	
	private List<Element> preferredAttribute;
	
	private List<Skill> buff_list;
	private List<Skill> summon_buff_list;
	
	private List<BObjects> behavior_list;
	
	public List<BObjects> getBehaviorList()
	{
		return behavior_list;
	}
	
	public void putBehavior(BObjects b)
	{
		behavior_list.add(b);
	}
	
	public String getPName()
	{
		return class_name;
	}
	
	public PhantomClassAI(int id)
	{
		class_id = id;
		class_name = PhantomUtils.getFullClassName(id);
		situation_skills = new SkillsGroup();
		summon_skills = new SkillsGroup();
		self_buff_skills = new SkillsGroup();
		debuff_skills = new SkillsGroup();
		passive_skills = new SkillsGroup();
		consumble_items = new ItemsGroup();
		resurrect_items = new ItemsGroup();
		resurrect_skills = new SkillsGroup();
		heal_skills = new SkillsGroup();
		buffs_skills = new SkillsGroup();
		support_skills = new SkillsGroup();
		nuke_skills = new SkillsGroup();
		detection_skills = new SkillsGroup();
		sweeper_skills = new SkillsGroup();
		spoil_skills = new SkillsGroup();
		party_heal_skills = new SkillsGroup();
		control_skills = new SkillsGroup(); // контрол. умения рут,раш и тд (используем только в пвп)
		summon_actions = new SkillsGroup();
		buff_list = new ArrayList<Skill>();
		summon_buff_list = new ArrayList<Skill>();
		preferredAttribute = new ArrayList<Element>();
		add_castom_skills = new SkillsGroup();
		
		behavior_list = new ArrayList<BObjects>();
	}
	
	public int getClassId()
	{
		return class_id;
	}

	public void putCastomSkills(SkillsGroup bs)
	{
		add_castom_skills = bs;
	}
	
	public SkillsGroup getCastomSkills()
	{
		return add_castom_skills;
	}
	
	public void putPartyHealSkills(SkillsGroup bs)
	{
		party_heal_skills = bs;
	}
	
	public SkillsGroup getPartyHealSkills()
	{
		return party_heal_skills;
	}
	
	public SkillsGroup getSummonActions()
	{
		return summon_actions;
	}
	
	public void putSummonActions(SkillsGroup bs)
	{
		summon_actions = bs;
	}
	
	public void putSelfBuffs(SkillsGroup bs)
	{
		self_buff_skills = bs;
	}
	
	public void putDebuffs(SkillsGroup bs)
	{
		debuff_skills = bs;
	}
	
	public void putPassive(SkillsGroup bs)
	{
		passive_skills = bs;
	}
	
	public void putItem(ItemsGroup bs)
	{
		consumble_items = bs;
	}
	
	public void putResItem(ItemsGroup bs)
	{
		resurrect_items = bs;
	}
	
	public void putResurrectSkills(SkillsGroup bs)
	{
		resurrect_skills = bs;
	}
	
	public SkillsGroup getSelfBuffs()
	{
		return self_buff_skills;
	}
	
	public SkillsGroup getDebuffs()
	{
		return debuff_skills;
	}
	
	public void putCleansingSkills(SkillsGroup bs)
	{
		cleansing_skills = bs;
	}
	
	public SkillsGroup getCleansingSkills()
	{
		return cleansing_skills;
	}
	
	public void putControlSkills(SkillsGroup bs)
	{
		control_skills = bs;
	}
	
	public SkillsGroup getControlSkill()
	{
		return control_skills;
	}
	
	public SkillsGroup getPassive()
	{
		return passive_skills;
	}
	
	public ItemsGroup getItemUse()
	{
		return consumble_items;
	}
	
	public ItemsGroup getResItem()
	{
		return resurrect_items;
	}
	
	public SkillsGroup getResurrectSkills()
	{
		return resurrect_skills;
	}
	
	public void putHeals(SkillsGroup bs)
	{
		heal_skills = bs;
	}
	
	public SkillsGroup getHealSkills()
	{
		return heal_skills;
	}
	
	public void putBuffs(SkillsGroup bs)
	{
		buffs_skills = bs;
	}
	
	public void putSituationSkills(SkillsGroup bs)
	{
		situation_skills = bs;
	}
	
	public void putSupports(SkillsGroup bs)
	{
		support_skills = bs;
	}
	
	public void putSummons(SkillsGroup bs)
	{
		summon_skills = bs;
	}
	
	public SkillsGroup getBuffSkills()
	{
		return buffs_skills;
	}
	
	public SkillsGroup getSituationSkills()
	{
		return situation_skills;
	}
	
	public SkillsGroup getSpoilSkills()
	{
		return spoil_skills;
	}
	
	public SkillsGroup getSweperSkills()
	{
		return sweeper_skills;
	}
	
	public void putSpoil(SkillsGroup s)
	{
		spoil_skills = s;
	}
	
	public void putSweper(SkillsGroup s)
	{
		sweeper_skills = s;
	}
	
	public SkillsGroup getSupportSkills()
	{
		return support_skills;
	}
	
	public SkillsGroup getSummonSkills()
	{
		return summon_skills;
	}
	
	public void putNukes(SkillsGroup s)
	{
		nuke_skills = s;
	}
	
	
	public void putDetectionSkills(SkillsGroup s)
	{
		detection_skills = s;
	}

	public SkillsGroup getNukeSkills()
	{
		return nuke_skills;
	}

	public SkillsGroup getDetectionSkills()
	{
		return detection_skills;
	}
	
	
	public int size()
	{
		return situation_skills.getAllSkills().size()+party_heal_skills.getAllSkills().size()+self_buff_skills.getAllSkills().size()+debuff_skills.getAllSkills().size()+passive_skills.getAllSkills().size()+consumble_items.getAllItems().size()+resurrect_items.getAllItems().size()+resurrect_skills.getAllSkills().size()+heal_skills.getAllSkills().size()+buffs_skills.getAllSkills().size()
				+support_skills.getAllSkills().size()+nuke_skills.getAllSkills().size()+detection_skills.getAllSkills().size();
	}

	public void addPreferredAttribute(Element valueOf)
	{
		preferredAttribute.add(valueOf);
	}
	
	public List<Element> getPreferredAttribute()
	{
		return preferredAttribute;
	}
	
	public List<Skill> getBuffList()
	{
		return buff_list;
	}
	
	public void addBuff(Skill s)
	{
		buff_list.add(s);
	}
	
	public List<Skill> getSummonBuffList()
	{
		return summon_buff_list;
	}
	
	public void addSummonBuff(Skill s)
	{
		summon_buff_list.add(s);
	}
}

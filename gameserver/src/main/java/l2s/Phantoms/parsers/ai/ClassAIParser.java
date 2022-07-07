package  l2s.Phantoms.parsers.ai;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;

import  l2s.Phantoms.enums.Behavior;
import  l2s.Phantoms.objects.BObjects;
import  l2s.Phantoms.objects.PhantomClassAI;
import  l2s.Phantoms.objects.Ai.MapEntryConverter;
import  l2s.Phantoms.templates.PhantomSkill;
import  l2s.Phantoms.templates.SkillsGroup;
import  l2s.gameserver.Config;
import  l2s.gameserver.model.Skill;
import  l2s.gameserver.model.base.Element;
import  l2s.gameserver.data.xml.holder.SkillHolder;
import  l2s.gameserver.templates.StatsSet;

public class ClassAIParser extends PhantomAiParser
{
	private static final Logger _log = LoggerFactory.getLogger(ClassAIParser.class);
	
	private static ArrayList <PhantomClassAI> class_list;
	
	private static ClassAIParser _instance;
	
	public static ClassAIParser getInstance()
	{
		if (_instance == null)
			_instance = new ClassAIParser();
		return _instance;
	}
	
	public PhantomClassAI getClassAI(int class_id)
	{
		for(PhantomClassAI ai : class_list)
			if (ai.getClassId() == class_id)
				return ai;
			
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList <PhantomClassAI> allParse()
	{
		class_list = new ArrayList <PhantomClassAI>();
		
		File dir = new File(Config.DATAPACK_ROOT, "config/Phantom/ai");
		if (!dir.exists())
		{
			_log.info("Dir "+dir.getAbsolutePath()+" not exists");
			return class_list;
		}
		
		Collection <File> files = FileUtils.listFiles(dir, FileFilterUtils.suffixFileFilter(".xml"), FileFilterUtils.directoryFileFilter());
		
		for(File file : files)
		{
			ArrayList <PhantomClassAI> s = loadObjects(file, "ClassObject");
			if (s == null || s.isEmpty())
				continue;
			
			class_list.addAll(s);
		}
		
		_log.info("Loaded "+class_list.size()+" phantoms classes.");
		//SaveAi();
		return class_list;
	}
	
	public void SaveAi()
	{
		
		Map <String,List <PhantomClassAI>> result = class_list.stream().collect(Collectors.groupingBy(PhantomClassAI::getPName));
		for(Entry <String,List <PhantomClassAI>> tmp : result.entrySet())
		{
			saveTStoFile(tmp.getValue(), "config/ai2/", tmp.getKey()+".xml");
		}
	}
	
	private void saveTStoFile(List <PhantomClassAI> list, String folder, String file)
	{
		XStream xs = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("_-", "_")));
		
		xs.alias("list", List.class);
		xs.alias("ClassObject", PhantomClassAI.class);
		//xs.registerLocalConverter(PhantomSkill.class, "condition", new SkillConditionConverter());
		xs.registerLocalConverter(PhantomSkill.class, "condition", new MapEntryConverter());
		//xs.registerConverter(new MapEntryConverter());
		xs.autodetectAnnotations(true);

		File theDir = new File(folder);
		if (!theDir.exists())
			theDir.mkdirs();
		String spase= "\t";
		// JAVA OBJECT --> XML
		String xml = xs.toXML(list).replace(spase+"<self_buff_skills/>\n", "")
				.replace(spase+"<ultimate_self_buff_skills/>\n", "")
				.replace(spase+"<debuff_skills/>\n", "")
				.replace(spase+"<rare_debuff_skills/>\n", "")
				.replace(spase+"<passive_skills/>\n", "")
				.replace(spase+"<use_items/>\n", "")
				.replace(spase+"<res_items/>\n", "")
				.replace(spase+"<resurrect_skills/>\n", "")
				.replace(spase+"<control_skills/>\n", "")
				.replace(spase+"<counterattack_skills/>\n", "")
				.replace(spase+"<cleansing_skills/>\n", "")
				.replace(spase+"<heal_skills/>\n", "")
				.replace(spase+"<buffs_skills/>\n", "")
				.replace(spase+"<support_skills/>\n", "")
				.replace(spase+"<situation_skills/>\n", "")
				.replace(spase+"<summon_skills/>\n", "")
				.replace(spase+"<sweeper_skills/>\n", "")
				.replace(spase+"<spoil_skills/>\n", "")
				.replace(spase+"<nuke_skills/>\n", "")
				.replace(spase+"<aoe_skills/>\n", "")
				.replace(spase+"<detection_skills/>\n", "")
				.replace(spase+"<rare_nuke_skills/>\n", "")
				.replace(spase+"<charge_skill/>\n", "")
				.replace(spase+"<class_id/>\n", "")
				.replace(spase+"<class_name/>\n", "")
				.replace(" is_enchantable=\"false\" is_random=\"false\" ench_max_value=\"-1\" ench_route=\"-1\"", "")
				.replace(" target=\"TARGET_NONE\"", "");
		try
		{
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(folder+file)));
			bufferedWriter.write(xml);
			
			bufferedWriter.close();
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	protected PhantomClassAI parseObj(Node setObject)
	{
		NamedNodeMap attrs = setObject.getAttributes();
		int class_id = Integer.parseInt(attrs.getNamedItem("class_id").getNodeValue());
		
		try
		{
			Node first = setObject.getFirstChild();
			PhantomClassAI class_ai = new PhantomClassAI(class_id);
			for(setObject = first; setObject != null; setObject = setObject.getNextSibling())
			{
				if (setObject.getNodeName().equalsIgnoreCase("preferred_attribute"))
				{
					String[] s_att = setObject.getAttributes().getNamedItem("att").getNodeValue().split(",");
					for (String att : s_att)
						class_ai.addPreferredAttribute(Element.valueOf(att));
				}
				else if (setObject.getNodeName().equalsIgnoreCase("buff_list"))
				{
					String[] s_buffs = setObject.getAttributes().getNamedItem("buffs").getNodeValue().split(",");
					for (String s_skill : s_buffs)
					{
						Skill skill =SkillHolder.getInstance().getSkill(Integer.parseInt(s_skill), SkillHolder.getInstance().getSkill(Integer.parseInt(s_skill), 1).getMaxLevel());
						if (skill == null)
						{
							_log.info("Phantom class_id:" +class_id +" buff skill "  + s_skill + " = null");
							continue;
						}
						class_ai.addBuff(skill);
					}
				}
				else if (setObject.getNodeName().equalsIgnoreCase("behavior"))
				{
					for(Node n = setObject.getFirstChild(); n != null; n = n.getNextSibling())
					{
						if (n.getNodeName().equalsIgnoreCase("option"))
						{
							NamedNodeMap attrs1 = n.getAttributes();
							String target_condition = "";
							if (attrs1.getNamedItem("target_condition") != null)
								target_condition = attrs1.getNamedItem("target_condition").getNodeValue();
							String self_condition = "";
							if (attrs1.getNamedItem("self_condition") != null)
								self_condition = attrs1.getNamedItem("self_condition").getNodeValue();
							
							Behavior change_type = Behavior.STOP;
							if (attrs1.getNamedItem("change_type") != null)
								change_type = Behavior.valueOf(attrs1.getNamedItem("change_type").getNodeValue());
							
							class_ai.putBehavior(new BObjects(change_type,target_condition,self_condition));
						}
					}
				}
				else if (setObject.getNodeName().equalsIgnoreCase("summon_buff"))
				{
					String[] s_buffs = setObject.getAttributes().getNamedItem("buffs").getNodeValue().split(",");
					for (String s_skill : s_buffs)
					{
						Skill skill =SkillHolder.getInstance().getSkill(Integer.parseInt(s_skill), SkillHolder.getInstance().getSkill(Integer.parseInt(s_skill), 1).getMaxLevel());
						if (skill == null)
						{
							_log.info("Phantom class_id:" +class_id +" buff skill "  + s_skill + " = null");
							continue;
						}
						class_ai.addSummonBuff(skill);
					}
				}
				else if (setObject.getNodeName().equalsIgnoreCase("add_castom_skills"))
					class_ai.putCastomSkills(parseCastomSkills(setObject,class_ai));
				else if (setObject.getNodeName().equalsIgnoreCase("nuke_skills"))
					class_ai.putNukes(parseSkills(setObject,class_ai));
				else if (setObject.getNodeName().equalsIgnoreCase("sweeper_skills"))
					class_ai.putSweper(parseSkills(setObject,class_ai));
				else if (setObject.getNodeName().equalsIgnoreCase("spoil_skills"))
					class_ai.putSpoil(parseSkills(setObject,class_ai));
				else if (setObject.getNodeName().equalsIgnoreCase("detection_skills"))
					class_ai.putDetectionSkills(parseSkills(setObject,class_ai));
				else if (setObject.getNodeName().equalsIgnoreCase("buffs_skills"))
					class_ai.putBuffs(parseSkills(setObject,class_ai));
				else if (setObject.getNodeName().equalsIgnoreCase("situation_skills"))
					class_ai.putSituationSkills(parseSkills(setObject,class_ai));
				else if (setObject.getNodeName().equalsIgnoreCase("support_skills"))
					class_ai.putSupports(parseSkills(setObject,class_ai));
				else if (setObject.getNodeName().equalsIgnoreCase("summon_skills"))
					class_ai.putSummons(parseSkills(setObject,class_ai));
				else if (setObject.getNodeName().equalsIgnoreCase("summon_actions"))
					class_ai.putSummonActions(parseSkills(setObject,class_ai));
				else if (setObject.getNodeName().equalsIgnoreCase("heal_skills"))
					class_ai.putHeals(parseSkills(setObject,class_ai));
				else if (setObject.getNodeName().equalsIgnoreCase("self_buff_skills"))
					class_ai.putSelfBuffs(parseSkills(setObject,class_ai));
				else if (setObject.getNodeName().equalsIgnoreCase("debuff_skills"))
					class_ai.putDebuffs(parseSkills(setObject,class_ai));
				else if (setObject.getNodeName().equalsIgnoreCase("passive_skills"))
					class_ai.putPassive(parseSkills(setObject,class_ai));
				else if (setObject.getNodeName().equalsIgnoreCase("use_items"))
					class_ai.putItem(parseItem(setObject));
				else if (setObject.getNodeName().equalsIgnoreCase("res_items"))
					class_ai.putResItem(parseItem(setObject));
				else if (setObject.getNodeName().equalsIgnoreCase("resurrect_skills"))
					class_ai.putResurrectSkills(parseSkills(setObject,class_ai));
				else if (setObject.getNodeName().equalsIgnoreCase("control_skills"))
					class_ai.putControlSkills(parseSkills(setObject,class_ai));
				else if (setObject.getNodeName().equalsIgnoreCase("cleansing_skills"))
					class_ai.putCleansingSkills(parseSkills(setObject,class_ai));
				else if (setObject.getNodeName().equalsIgnoreCase("party_heal_skills"))
					class_ai.putPartyHealSkills(parseSkills(setObject,class_ai));
			}
			
			return class_ai;
		}catch(Exception e)
		{
			_log.error("Error loading set for class: "+class_id, e);
		}
		return null;
	}

}

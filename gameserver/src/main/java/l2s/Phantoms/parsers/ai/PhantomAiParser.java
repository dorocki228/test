package  l2s.Phantoms.parsers.ai;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import  l2s.Phantoms.objects.PhantomClassAI;
import  l2s.Phantoms.templates.ItemsGroup;
import  l2s.Phantoms.templates.PhantomItem;
import  l2s.Phantoms.templates.PhantomSkill;
import  l2s.Phantoms.templates.SkillsGroup;
import  l2s.gameserver.templates.StatsSet;

public abstract class PhantomAiParser
{
	@SuppressWarnings("unused")
	private static final Logger _log = LoggerFactory.getLogger(PhantomAiParser.class);
	
	@SuppressWarnings("rawtypes")
	public ArrayList loadObjects(File file, String name)
	{
		if (file == null)
			return null;
		
		return parse(file, name);
	}
	
	@SuppressWarnings("rawtypes")
	protected ArrayList parse(File file, String name)
	{
		Document doc;
		ArrayList collection;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(file);
		}catch(Exception e)
		{
			return null;
		}
		try
		{
			collection = parseDocument(doc, name);
		}catch(Exception e)
		{
			return null;
		}
		return collection;
	}
	
	@SuppressWarnings(
	{"rawtypes","unchecked"})
	protected ArrayList parseDocument(Document doc, String name)
	{
		ArrayList collection = new ArrayList();
		for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeName().equalsIgnoreCase("list"))
			{
				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if (d.getNodeName().equalsIgnoreCase(name))
					{
						Object obj = parseObj(d);
						if (obj != null)
							collection.add(obj);
					}
				}
			}
		}
		return collection;
	}
	
	protected ItemsGroup parseItem(Node n)
	{
		int item_id = -1;
		int _delay = -1;
		String _condition = "";
		
		n = n.getFirstChild();
		ItemsGroup group = new ItemsGroup();
		if (n == null)
			return group;
		
		for(; n != null; n = n.getNextSibling())
		{
			String nodeName = n.getNodeName();
			if (nodeName.equalsIgnoreCase("item"))
			{
				NamedNodeMap attrs = n.getAttributes();
				if (attrs.getNamedItem("id") != null)
					item_id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
				
				if (attrs.getNamedItem("delay") != null)
					_delay = Integer.parseInt(attrs.getNamedItem("delay").getNodeValue());
				
				if (attrs.getNamedItem("condition") != null)
					_condition = attrs.getNamedItem("condition").getNodeValue();
				
				StatsSet set = new StatsSet();
				set.set("id", item_id);
				set.set("delay", _delay);
				set.set("condition", _condition);
				
				group.addItem(new PhantomItem(set));
			}
			
		}
		return group;
	}
	
	protected SkillsGroup parseCastomSkills(Node n, PhantomClassAI class_ai)
	{
		n = n.getFirstChild();
		SkillsGroup group = new SkillsGroup();
		if (n == null)
			return group;
		int tmp_rnd = 0;
		for(; n != null; n = n.getNextSibling())
		{
			String nodeName = n.getNodeName();
			if (nodeName.equalsIgnoreCase("rnd"))
			{
				tmp_rnd++;
				List<PhantomSkill> rnd_skill = new ArrayList<>();
				for(Node secondNode = n.getFirstChild(); secondNode != null; secondNode = secondNode.getNextSibling())
				{
					if ("skill".equalsIgnoreCase(secondNode.getNodeName()))
					{
						int skill_id = -1;
						int skill_level = -1;

						NamedNodeMap attrs = n.getAttributes();
						if (attrs.getNamedItem("id") != null)
							skill_id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
						
						if (attrs.getNamedItem("lvl") != null)
							skill_level = Integer.parseInt(attrs.getNamedItem("lvl").getNodeValue());

						StatsSet set = new StatsSet();
						set.set("id", skill_id);
						set.set("lvl", skill_level);	
					 rnd_skill.add(new PhantomSkill(set));
					}
				}
					group.addRndSkill(tmp_rnd,rnd_skill);
			}
			if (nodeName.equalsIgnoreCase("skill"))
			{
				int skill_id = -1;
				int skill_level = -1;

				NamedNodeMap attrs = n.getAttributes();
				if (attrs.getNamedItem("id") != null)
					skill_id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
				
				if (attrs.getNamedItem("lvl") != null)
					skill_level = Integer.parseInt(attrs.getNamedItem("lvl").getNodeValue());

				StatsSet set = new StatsSet();
				set.set("id", skill_id);
				set.set("lvl", skill_level);

				group.addSkill(new PhantomSkill(set));
			}
			
		}
		return group;
	}
	
	protected SkillsGroup parseSkills(Node n, PhantomClassAI class_ai)
	{
		int chance_mob = 100;
		int chance_group = 100;
		
		String _chance_mob = null;
		Node tmp = n.getAttributes().getNamedItem("ChanceCastOnMonster");
		if (tmp!=null)
			_chance_mob = tmp.getNodeValue();
		
		if (_chance_mob!=null&& !_chance_mob.isEmpty())
			chance_mob = Integer.parseInt(_chance_mob);

		String _chance_group = null;
		Node tmp1 = n.getAttributes().getNamedItem("chance");
		if (tmp1!=null)
			_chance_group = tmp1.getNodeValue();
		
		if (_chance_group!=null&& !_chance_group.isEmpty())
			chance_group = Integer.parseInt(_chance_group);

		n = n.getFirstChild();
		SkillsGroup group = new SkillsGroup(chance_mob, chance_group);
		if (n == null)
			return group;
		
		for(; n != null; n = n.getNextSibling())
		{
			String nodeName = n.getNodeName();
			if (nodeName.equalsIgnoreCase("skill"))
			{
				int skill_id = -1;
				int ench_max_value = -1;
				boolean is_enchantable = false;
				boolean is_random = false;
				int ench_route = 0;
				String _target = "TARGET_NONE";
				String _condition = "";
				
				NamedNodeMap attrs = n.getAttributes();
				if (attrs.getNamedItem("id") != null)
					skill_id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
				
				if (attrs.getNamedItem("is_enchantable") != null)
					is_enchantable = Boolean.parseBoolean(attrs.getNamedItem("is_enchantable").getNodeValue());
				
				if (attrs.getNamedItem("is_random") != null)
					is_random = Boolean.parseBoolean(attrs.getNamedItem("is_random").getNodeValue());
				
				if (attrs.getNamedItem("target") != null)
					_target = attrs.getNamedItem("target").getNodeValue();
				
				if (attrs.getNamedItem("condition") != null)
					_condition = attrs.getNamedItem("condition").getNodeValue();
				
				if (attrs.getNamedItem("ench_max_value") != null)
					ench_max_value = Integer.parseInt(attrs.getNamedItem("ench_max_value").getNodeValue());
				
				if (attrs.getNamedItem("ench_route") != null)
					ench_route = Integer.parseInt(attrs.getNamedItem("ench_route").getNodeValue());
				
				StatsSet set = new StatsSet();
				set.set("id", skill_id);
				set.set("is_enchantable", is_enchantable);
				set.set("is_random", is_random);
				set.set("ench_max_value", ench_max_value);
				set.set("ench_route", ench_route);
				
				set.set("target", _target);
				set.set("condition", _condition);
				
				group.addSkill(new PhantomSkill(set));
			}
			
		}
		return group;
	}
	
	protected abstract Object parseObj(Node setObject);
	
	public Object loadObject(File file, String name)
	{
		if (file == null)
			return null;
		
		return parseObj(file, name);
	}
	
	protected Object parseObj(File file, String name)
	{
		Document doc;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(file);
		}catch(Exception e)
		{
			return null;
		}
		try
		{
			return parseDocumentObj(doc, name);
		}catch(Exception e)
		{
			return null;
		}
	}
	
	protected Object parseDocumentObj(Document doc, String name)
	{
		Object obj;
		for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeName().equalsIgnoreCase("list"))
			{
				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if (d.getNodeName().equalsIgnoreCase(name))
					{
						obj = parseObj(d);
						if (obj != null)
							return obj;
					}
				}
			}
		}
		return null;
	}
}

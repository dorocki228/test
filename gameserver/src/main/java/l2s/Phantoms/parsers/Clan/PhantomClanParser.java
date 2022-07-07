package  l2s.Phantoms.parsers.Clan;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import  l2s.Phantoms.enums.ClanType;
import  l2s.Phantoms.enums.PartyType;
import  l2s.Phantoms.objects.Clan.ConstantParty;
import  l2s.Phantoms.objects.Clan.MemberCP;
import  l2s.Phantoms.objects.Clan.PhantomClan;
import l2s.commons.data.xml.AbstractParser;
import  l2s.gameserver.Config;
import  l2s.gameserver.model.base.Sex;

public class PhantomClanParser extends AbstractParser <PhantomClanHolder>
{
	private static PhantomClanParser _instance = new PhantomClanParser();
	
	public static PhantomClanParser getInstance()
	{
		return _instance;
	}
	
	protected PhantomClanParser()
	{
		super(PhantomClanHolder.getInstance());
	}
	
	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "config/Phantom/Clan/PhantomClan.xml");
	}
	
	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Element surveyElement : rootElement.elements("clan"))
		{
			String clan_name = surveyElement.attributeValue("name");
			String cl_name = surveyElement.attributeValue("cl_name");
			int cl_class_id =  Integer.parseInt(surveyElement.attributeValue("cl_classId"));
			String crest = surveyElement.attributeValue("crest");
			String allyname = surveyElement.attributeValue("allyname");
			String allycrest = surveyElement.attributeValue("allycrest");
			ClanType clantype = ClanType.valueOf(surveyElement.attributeValue("clantype"));
			
			List <ConstantParty> cp_list = new ArrayList<ConstantParty>();
			
			String academy = surveyElement.element("academy").attributeValue("name");
			String RoyalGuard1st = surveyElement.element("RoyalGuard1st").attributeValue("name");
			String RoyalGuard2st = surveyElement.element("RoyalGuard2st").attributeValue("name");
			String KnightlyOrder1st = surveyElement.element("KnightlyOrder1st").attributeValue("name");
			String KnightlyOrder2st = surveyElement.element("KnightlyOrder2st").attributeValue("name");
			String KnightlyOrder3st = surveyElement.element("KnightlyOrder3st").attributeValue("name");
			String KnightlyOrder4st = surveyElement.element("KnightlyOrder4st").attributeValue("name");
			
			for(Element itemElement : surveyElement.elements("cp"))
			{
				int id = Integer.parseInt(itemElement.attributeValue("id"));
				String[] prime_time = itemElement.attributeValue("prime_time").split(",");
				int despawn = Integer.parseInt(itemElement.attributeValue("despawn"));
				PartyType party_type = PartyType.valueOf( itemElement.attributeValue("party_type"));
				
				ConstantParty cp = new ConstantParty(id, prime_time, despawn, party_type);
				for(Element member : itemElement.elements("member"))
				{ 
					String member_name = member.attributeValue("name");
					int member_class_id = Integer.parseInt(member.attributeValue("class_id"));
					Sex sex = Sex.valueOf(member.attributeValue("sex"));
					cp.addMember(new MemberCP(member_name, member_class_id,sex));
				}
				cp_list.add(cp);
			}
			
			getHolder().addItems(new PhantomClan(clan_name, cl_name, cl_class_id, crest, allyname, allycrest, clantype, cp_list,  new String[]{academy, RoyalGuard1st,RoyalGuard2st,KnightlyOrder1st,KnightlyOrder2st,KnightlyOrder3st,KnightlyOrder4st}));
		}
	}

	@Override
	public String getDTDFileName()
	{
		return "PhantomClan.dtd";
	}

}

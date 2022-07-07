package  l2s.Phantoms.objects.Clan;

import java.util.List;

import  l2s.Phantoms.enums.ClanType;

public class PhantomClan
{
	private String clan_name; 
	private String cl_name; 
	private int cl_class_id; 
	private String crest; 
	private String allyname; 
	private String allycrest; 
	private ClanType clantype; 
	private List <ConstantParty> cp_list; 
	private String[] clanStructure;
	
	public PhantomClan(String clan_name,String cl_name,int cl_class_id,String crest,String allyname,String allycrest,ClanType clantype,List <ConstantParty> cp_list,String[] strings)
	{
		this.clan_name = clan_name;
		this.cl_name = cl_name;
		this.cl_class_id =cl_class_id;
		this.crest = crest;
		this.allyname =allyname;
		this.allycrest = allycrest;
		this.clantype =clantype;
		this.cp_list =cp_list;
		this.clanStructure = strings;
	}

	public String getClanName()
	{
		return clan_name;
	}

	public String getClName()
	{
		return cl_name;
	}

	public int getClClassId()
	{
		return cl_class_id;
	}

	public String getCrest()
	{
		return crest;
	}

	public String getAllyName()
	{
		return allyname;
	}

	public String getAllyCrest()
	{
		return allycrest;
	}

	public ClanType getClanType()
	{
		return clantype;
	}

	public List <ConstantParty> getCpList()
	{
		return cp_list;
	}

	public String[] getClanStructure()
	{
		return clanStructure;
	}

	public ConstantParty getCpListById(int i)
	{
		for (ConstantParty cp : cp_list)
		{
			if (cp.getCpId() == 1)
				return cp;
		}
		return null;
	}
	
}
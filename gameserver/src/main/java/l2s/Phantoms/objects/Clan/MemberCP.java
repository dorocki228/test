package  l2s.Phantoms.objects.Clan;


import  l2s.gameserver.model.base.Sex;

public class MemberCP
{
	private String member_name;
	private int member_class_id;
	private Sex sex;
	
	public MemberCP(String _member_name,int _member_class_id,Sex _sex)
	{
		this.setMemberName(_member_name);
		this.setMemberClassId(_member_class_id);
		this.setSex(_sex);
	}
	
	public Sex getSex()
	{
		return sex;
	}
	
	public void setSex(Sex sex)
	{
		this.sex = sex;
	}
	
	public int getMemberClassId()
	{
		return member_class_id;
	}
	
	public void setMemberClassId(int member_class_id)
	{
		this.member_class_id = member_class_id;
	}
	
	public String getMemberName()
	{
		return member_name;
	}
	
	public void setMemberName(String member_name)
	{
		this.member_name = member_name;
	}
	
}

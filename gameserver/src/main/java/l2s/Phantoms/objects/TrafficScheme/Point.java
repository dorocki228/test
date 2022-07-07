package  l2s.Phantoms.objects.TrafficScheme;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import l2s.gameserver.model.base.Fraction;
import  l2s.gameserver.utils.Location;

@XStreamAlias("point")
public class Point
{
	@XStreamAlias("x")
	@XStreamAsAttribute
	private int x;
	@XStreamAlias("y")
	@XStreamAsAttribute
	private int y;
	@XStreamAlias("z")
	@XStreamAsAttribute
	private int z;
	
	@XStreamAlias("rnd")
	@XStreamAsAttribute
	private int rnd;
	
	
	@XStreamAlias("kill_npc_id")
	@XStreamImplicit 
	private List<KillNpcId> _kill_npc_id = new ArrayList<KillNpcId>();
	
	@XStreamAlias("select_random_task")
	private SelectRandomTask select_random_task;
	
	@XStreamAlias("pick_up_item")
	private PickUpItem _pick_up_item;

	@XStreamAlias("move_delay")
	private MoveDelay _delay;
	
	@XStreamAlias("mount_agation")
	@XStreamImplicit 
	private List<Agation> _mount_agation = new ArrayList<Agation>();
	
	@XStreamAlias("summon_pet")
	@XStreamImplicit 
	private List<Pet> _summon_pet = new ArrayList<Pet>();
	
	@XStreamAlias("target")
	private TargetNpc _target;
	
	@XStreamAlias("trader")
	private Trader _trader;
	
	@XStreamAlias("teleport")
	private Teleport teleport;
	
	@XStreamAlias("fraction")
	private Fraction fraction;
	
	@XStreamAlias("social_action")
	private PSocialAction _social;
	
	@XStreamAlias("equip_item")
	private EquipItem _equip_item;

	@XStreamAlias("use_item")
	@XStreamImplicit 
	private List<PUseItem> _use_item = new ArrayList<PUseItem>();
	
	@XStreamAlias("cast_skill")
	@XStreamImplicit 
	private List<PCastSkill> _cast_skill = new ArrayList<PCastSkill>();
	
	@XStreamOmitField
	private int rndDelay = 0;
	@XStreamOmitField
	private boolean leaveParty = false;
	
	@XStreamAlias("regroup_distance")
	@XStreamAsAttribute
	private int regroup_distance;
	
	public Point()
	{}
	
	public boolean getAttachments()
	{
		return teleport != null 
				|| (_use_item!=null&& !_use_item.isEmpty() )
				|| _trader !=null
				|| _target!=null 
				|| _pick_up_item!=null 
				|| _summon_pet !=null 
				|| select_random_task!=null
				|| _social!=null
				|| (_cast_skill!=null && !_cast_skill.isEmpty())
				|| (_mount_agation!=null && !_mount_agation.isEmpty()) 
				|| _delay!=null;
	}
	public Point(int x, int y,int z, int rnd, int regroup_distance)
	{
		this.setX(x);
		this.setY(y);
		this.setZ(z);
		this.setRnd(rnd);
		this.setRegroupDistance(regroup_distance);
	}

	public Point(Location loc, int rnd)
	{
		this.setX(loc.getX());
		this.setY(loc.getY());
		this.setZ(loc.getZ());
		this.setRnd(rnd);
	}
	
	public void setLoc(Location loc, int rnd)
	{
		this.x = loc.getX();
		this.y = loc.getY();
		this.z = loc.getZ();
		this.setRnd(rnd);
	}
	
	public EquipItem getEquipItem()
	{
		return _equip_item;
	}

	public void setEquipItem(EquipItem _equip_item)
	{
		this._equip_item = _equip_item;
	}
	
	public Location getLoc()
	{
		return new Location (x,y,z);
	}

	
	public int getX()
	{
		return x;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public int getY()
	{
		return y;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	public int getZ()
	{
		return z;
	}

	public void setZ(int z)
	{
		this.z = z;
	}
	
  @Override
  public String toString() 
  {
      return " x:" +x + " y:" + y + " z:"+ z;
  }

  public MoveDelay getDelay()
	{
		return _delay;
	}
	
	public void setDelay(MoveDelay _delay)
	{
		this._delay = _delay;
	}
	
	public void addPriorityTarget(KillNpcId kill_npc_id)
	{
		this._kill_npc_id.add(kill_npc_id);
	}
	public List<KillNpcId> getPriorityTarget()
	{
		return _kill_npc_id;
	}
	public boolean containsPriorityTarget(int id, Location target_loc)
	{
		boolean contains = false;
		for (KillNpcId npc_id : _kill_npc_id)
		{
			if (id==npc_id.getId() && target_loc.distance(x,y) <= npc_id.getRadius() /*&& Math.abs(target_loc.z-z)<= npc_id.getHeight()*/)
				contains = true;
		}
		return contains;
	}
	
	public List <Agation> getMount_agation()
	{
		return _mount_agation;
	}

	public void addMountAgation(Agation mountagation)
	{
		this._mount_agation.add(mountagation);
	}

	public List <Pet> getSummonPet()
	{
		return _summon_pet;
	}

	public void setSummonPet(List <Pet> _pet)
	{
		this._summon_pet = _pet;
	}

	public void addSummonPet(Pet _pet)
	{
		this._summon_pet.add(_pet);
	}
	
	public TargetNpc getTarget()
	{
		return _target;
	}

	public void setTarget(TargetNpc _target)
	{
		this._target = _target;
	}

	public Trader getTrader()
	{
		return _trader;
	}

	public void setTrader(Trader _trader)
	{
		this._trader = _trader;
	}

	public void setTeleport(Teleport _teleport)
	{
		this.teleport = _teleport;
	}

	public void setFraction(Fraction _fraction)
	{
		this.fraction = _fraction;
	}
	
	public Fraction getFraction()
	{
		return fraction;
	}
	
	public Teleport getTeleport()
	{
		return teleport;
	}
	
	public List <PUseItem> getUseitem()
	{
		return _use_item;
	}

	public void setLastItemDelay(int delay)
	{
		if (getUseitem() != null && !getUseitem().isEmpty())
			getUseitem().get(getUseitem().size()-1).setItemDelay((int) delay);
	}
	
	public void addUseitem(PUseItem _use_item)
	{
		this._use_item.add(_use_item);
	}

	public int getRnd()
	{
		return rnd;
	}

	public void setRnd(int rnd)
	{
		this.rnd = rnd;
	}

	public List<PCastSkill> getCastSkill()
	{
		return _cast_skill;
	}

	public void setLastSkillDelay(int delay)
	{
		if (getCastSkill() != null && getCastSkill().size() > 0)
			getCastSkill().get(getCastSkill().size()-1).setSkillDelay((int) delay);
	}
	
	public void addCastSkill(PCastSkill _skill)
	{
		this._cast_skill.add(_skill);
	}

	public PSocialAction getSocial()
	{
		return _social;
	}

	public void setSocial(PSocialAction _social)
	{
		this._social = _social;
	}

	public int getRndDelay()
	{
		return rndDelay;
	}

	public void setRndDelay(int rndDelay)
	{
		this.rndDelay = rndDelay;
	}

	public SelectRandomTask getSelectRandomTask()
	{
		return select_random_task;
	}

	public void setSelectRandomTask(SelectRandomTask select_random_task)
	{
		this.select_random_task = select_random_task;
	}

	public PickUpItem getPickUpItem()
	{
		return _pick_up_item;
	}

	public void setPickUpItem(PickUpItem pick_up_item)
	{
		this._pick_up_item = pick_up_item;
	}

	public void leaveParty()
	{
		leaveParty  = true;
	}

	public boolean getleaveParty()
	{
		return leaveParty;
	}

	public int getRegroupDistance()
	{
		return regroup_distance;
	}

	public void setRegroupDistance(int regroup_distance)
	{
		this.regroup_distance = regroup_distance;
	}
}
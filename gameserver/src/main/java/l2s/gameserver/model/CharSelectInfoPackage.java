package l2s.gameserver.model;

import l2s.gameserver.dao.ItemsDAO;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;

import java.util.Collection;

public class CharSelectInfoPackage
{
	private String _name;
	private int _objectId;
	private int _charId;
	private long _exp;
	private long _sp;
	private int _clanId;
	private int _race;
	private int _classId;
	private int _baseClassId;
	private int _deleteTimer;
	private long _lastAccess;
	private int _face;
	private int _hairStyle;
	private int _hairColor;
	private int _sex;
	private int _level;
	private int _karma;
	private int _pk;
	private int _pvp;
	private int _maxHp;
	private double _currentHp;
	private int _maxMp;
	private double _currentMp;
	private final CharSelectInfoPaperdollItem[] _paperdoll;
	private boolean available;
	private int _x;
	private int _y;
	private int _z;
	private boolean _hairAccessoryEnabled;

	public CharSelectInfoPackage(int objectId, String name)
	{
		_objectId = 0;
		_charId = 0x00030b7a;
		_exp = 0L;
		_sp = 0L;
		_clanId = 0;
		_race = 0;
		_classId = 0;
		_baseClassId = 0;
		_deleteTimer = 0;
		_lastAccess = 0L;
		_face = 0;
		_hairStyle = 0;
		_hairColor = 0;
		_sex = 0;
		_level = 1;
		_karma = 0;
		_pk = 0;
		_pvp = 0;
		_maxHp = 0;
		_currentHp = 0.0;
		_maxMp = 0;
		_currentMp = 0.0;
		_x = 0;
		_y = 0;
		_z = 0;
		_hairAccessoryEnabled = true;
		setObjectId(objectId);
		_name = name;
		Collection<ItemInstance> items = ItemsDAO.getInstance().getItemsByOwnerIdAndLoc(objectId, ItemInstance.ItemLocation.PAPERDOLL);
		_paperdoll = new CharSelectInfoPaperdollItem[Inventory.PAPERDOLL_MAX];
		for(ItemInstance item : items) //FIXME [G1ta0] временный фикс отображения одетых вещей при входе на персонажа в NO CARRIER
		{
			if(item.getEquipSlot() < Inventory.PAPERDOLL_MAX) {
				_paperdoll[item.getEquipSlot()] = new CharSelectInfoPaperdollItem(item);
			}
		}
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public void setObjectId(int objectId)
	{
		_objectId = objectId;
	}

	public int getCharId()
	{
		return _charId;
	}

	public void setCharId(int charId)
	{
		_charId = charId;
	}

	public int getClanId()
	{
		return _clanId;
	}

	public void setClanId(int clanId)
	{
		_clanId = clanId;
	}

	public int getClassId()
	{
		return _classId;
	}

	public int getBaseClassId()
	{
		return _baseClassId;
	}

	public void setBaseClassId(int baseClassId)
	{
		_baseClassId = baseClassId;
	}

	public void setClassId(int classId)
	{
		_classId = classId;
	}

	public double getCurrentHp()
	{
		return _currentHp;
	}

	public void setCurrentHp(double currentHp)
	{
		_currentHp = currentHp;
	}

	public double getCurrentMp()
	{
		return _currentMp;
	}

	public void setCurrentMp(double currentMp)
	{
		_currentMp = currentMp;
	}

	public int getDeleteTimer()
	{
		return _deleteTimer;
	}

	public void setDeleteTimer(int deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}

	public long getLastAccess()
	{
		return _lastAccess;
	}

	public void setLastAccess(long lastAccess)
	{
		_lastAccess = lastAccess;
	}

	public long getExp()
	{
		return _exp;
	}

	public void setExp(long exp)
	{
		_exp = exp;
	}

	public int getFace()
	{
		return _face;
	}

	public void setFace(int face)
	{
		_face = face;
	}

	public int getHairColor()
	{
		return _hairColor;
	}

	public void setHairColor(int hairColor)
	{
		_hairColor = hairColor;
	}

	public int getHairStyle()
	{
		return _hairStyle;
	}

	public void setHairStyle(int hairStyle)
	{
		_hairStyle = hairStyle;
	}

	public int getPaperdollItemId(int slot)
	{
		CharSelectInfoPaperdollItem item = _paperdoll[slot];
		if(item != null)
			return item.getItemId();
		return 0;
	}

	public int[] getPaperdollAugmentationId(int slot)
	{
		CharSelectInfoPaperdollItem item = _paperdoll[slot];
		if(item != null)
			return item.getAugmentations();
		return ItemInstance.EMPTY_AUGMENTATIONS;
	}

	public int getPaperdollEnchantEffect(int slot)
	{
		CharSelectInfoPaperdollItem item = _paperdoll[slot];
		if(item != null)
			return item.getEnchantLevel();
		return 0;
	}

	public int getPaperdollVisualId(int slot)
	{
		CharSelectInfoPaperdollItem item = _paperdoll[slot];
		if(item != null)
			return item.getVisualId();
		return 0;
	}

	public int getLevel()
	{
		return _level;
	}

	public void setLevel(int level)
	{
		_level = level;
	}

	public int getMaxHp()
	{
		return _maxHp;
	}

	public void setMaxHp(int maxHp)
	{
		_maxHp = maxHp;
	}

	public int getMaxMp()
	{
		return _maxMp;
	}

	public void setMaxMp(int maxMp)
	{
		_maxMp = maxMp;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public int getRace()
	{
		return _race;
	}

	public void setRace(int race)
	{
		_race = race;
	}

	public int getSex()
	{
		return _sex;
	}

	public void setSex(int sex)
	{
		_sex = sex;
	}

	public long getSp()
	{
		return _sp;
	}

	public void setSp(long sp)
	{
		_sp = sp;
	}

	public int getKarma()
	{
		return _karma;
	}

	public void setKarma(int karma)
	{
		_karma = karma;
	}

	public boolean isAvailable()
	{
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public int getX()
	{
		return _x;
	}

	public void setX(int x)
	{
		_x = x;
	}

	public int getY()
	{
		return _y;
	}

	public void setY(int y)
	{
		_y = y;
	}

	public int getZ()
	{
		return _z;
	}

	public void setZ(int z)
	{
		_z = z;
	}

	public int getPk()
	{
		return _pk;
	}

	public void setPk(int pk)
	{
		_pk = pk;
	}

	public int getPvP()
	{
		return _pvp;
	}

	public void setPvP(int pvp)
	{
		_pvp = pvp;
	}

	public boolean isHairAccessoryEnabled()
	{
		return _hairAccessoryEnabled;
	}

	public void setHairAccessoryEnabled(boolean value)
	{
		_hairAccessoryEnabled = value;
	}
}

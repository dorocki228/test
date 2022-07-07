package l2s.gameserver.data.xml.holder;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.pet.PetData;

public final class PetDataHolder extends AbstractHolder
{
	public static final int PET_WOLF_ID = 12077;
	public static final int HATCHLING_WIND_ID = 12311;
	public static final int HATCHLING_STAR_ID = 12312;
	public static final int HATCHLING_TWILIGHT_ID = 12313;
	public static final int STRIDER_WIND_ID = 12526;
	public static final int STRIDER_STAR_ID = 12527;
	public static final int STRIDER_TWILIGHT_ID = 12528;
	public static final int RED_STRIDER_WIND_ID = 16038;
	public static final int RED_STRIDER_STAR_ID = 16039;
	public static final int RED_STRIDER_TWILIGHT_ID = 16040;
	public static final int WYVERN_ID = 12621;
	public static final int BABY_BUFFALO_ID = 12780;
	public static final int BABY_KOOKABURRA_ID = 12781;
	public static final int BABY_COUGAR_ID = 12782;
	public static final int IMPROVED_BABY_BUFFALO_ID = 16034;
	public static final int IMPROVED_BABY_KOOKABURRA_ID = 16035;
	public static final int IMPROVED_BABY_COUGAR_ID = 16036;
	public static final int SIN_EATER_ID = 12564;
	public static final int GREAT_WOLF_ID = 16025;
	public static final int WGREAT_WOLF_ID = 16037;
	public static final int FENRIR_WOLF_ID = 16041;
	public static final int WFENRIR_WOLF_ID = 16042;
	public static final int FOX_SHAMAN_ID = 16043;
	public static final int WILD_BEAST_FIGHTER_ID = 16044;
	public static final int WHITE_WEASEL_ID = 16045;
	public static final int FAIRY_PRINCESS_ID = 16046;
	public static final int OWL_MONK_ID = 16050;
	public static final int SPIRIT_SHAMAN_ID = 16051;
	public static final int TOY_KNIGHT_ID = 16052;
	public static final int TURTLE_ASCETIC_ID = 16053;
	public static final int DEINONYCHUS_ID = 16067;
	public static final int GUARDIANS_STRIDER_ID = 16068;
	public static final int ROSE_DESELOPH_ID = 1562;
	public static final int ROSE_HYUM_ID = 1563;
	public static final int ROSE_REKANG_ID = 1564;
	public static final int ROSE_LILIAS_ID = 1565;
	public static final int ROSE_LAPHAM_ID = 1566;
	public static final int ROSE_MAPHUM_ID = 1567;
	public static final int IMPROVED_ROSE_DESELOPH_ID = 1568;
	public static final int IMPROVED_ROSE_HYUM_ID = 1569;
	public static final int IMPROVED_ROSE_REKANG_ID = 1570;
	public static final int IMPROVED_ROSE_LILIAS_ID = 1571;
	public static final int IMPROVED_ROSE_LAPHAM_ID = 1572;
	public static final int IMPROVED_ROSE_MAPHUM_ID = 1573;
	private static final PetDataHolder _instance;
	private static final TIntObjectMap<PetData> _templatesByNpcId;
	private static final TIntObjectMap<PetData> _templatesByItemId;

	public static PetDataHolder getInstance()
	{
		return _instance;
	}

	public void addTemplate(PetData template)
	{
		_templatesByNpcId.put(template.getNpcId(), template);
		_templatesByItemId.put(template.getControlItemId(), template);
	}

	public PetData getTemplateByNpcId(int npcId)
	{
		return _templatesByNpcId.get(npcId);
	}

	public PetData getTemplateByItemId(int itemId)
	{
		return _templatesByItemId.get(itemId);
	}

	public boolean isControlItem(int itemId)
	{
		return _templatesByItemId.containsKey(itemId);
	}

	@Override
	public int size()
	{
		return _templatesByNpcId.size();
	}

	@Override
	public void clear()
	{
		_templatesByNpcId.clear();
		_templatesByItemId.clear();
	}

	public static boolean isWolf(int id)
	{
		return id == 12077;
	}

	public static boolean isGreatWolf(int id)
	{
		switch(id)
		{
			case 16025:
			case 16037:
			case 16041:
			case 16042:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public static boolean isHatchling(int id)
	{
		switch(id)
		{
			case 12311:
			case 12312:
			case 12313:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public static boolean isStrider(int id)
	{
		switch(id)
		{
			case 12526:
			case 12527:
			case 12528:
			case 16038:
			case 16039:
			case 16040:
			case 16068:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public static boolean isBabyPet(int id)
	{
		switch(id)
		{
			case 12780:
			case 12781:
			case 12782:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public static boolean isImprovedBabyPet(int id)
	{
		switch(id)
		{
			case 16034:
			case 16035:
			case 16036:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public static boolean isSpecialPet(int id)
	{
		switch(id)
		{
			case 1562:
			case 1563:
			case 1564:
			case 1565:
			case 1566:
			case 1567:
			case 1568:
			case 1569:
			case 1570:
			case 1571:
			case 1572:
			case 1573:
			case 16043:
			case 16044:
			case 16045:
			case 16046:
			case 16050:
			case 16051:
			case 16052:
			case 16053:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	static
	{
		_instance = new PetDataHolder();
		_templatesByNpcId = new TIntObjectHashMap();
		_templatesByItemId = new TIntObjectHashMap();
	}
}

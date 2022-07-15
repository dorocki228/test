package l2s.gameserver.network.l2.s2c.updatetype;

import l2s.gameserver.model.items.Inventory;

/**
 * @author UnAfraid
 */
public enum InventorySlot implements IUpdateTypeComponent
{
	PENDANT(Inventory.PAPERDOLL_PENDANT),
	REAR(Inventory.PAPERDOLL_REAR),
	LEAR(Inventory.PAPERDOLL_LEAR),
	NECK(Inventory.PAPERDOLL_NECK),
	RFINGER(Inventory.PAPERDOLL_RFINGER),
	LFINGER(Inventory.PAPERDOLL_LFINGER),
	HEAD(Inventory.PAPERDOLL_HEAD),
	RHAND(Inventory.PAPERDOLL_RHAND),
	LHAND(Inventory.PAPERDOLL_LHAND),
	GLOVES(Inventory.PAPERDOLL_GLOVES),
	CHEST(Inventory.PAPERDOLL_CHEST),
	LEGS(Inventory.PAPERDOLL_LEGS),
	FEET(Inventory.PAPERDOLL_FEET),
	CLOAK(Inventory.PAPERDOLL_BACK),
	LRHAND(Inventory.PAPERDOLL_LRHAND),
	HAIR(Inventory.PAPERDOLL_HAIR),
	HAIR2(Inventory.PAPERDOLL_DHAIR),
	RBRACELET(Inventory.PAPERDOLL_RBRACELET),
	LBRACELET(Inventory.PAPERDOLL_LBRACELET),
	AGATHION_MAIN(Inventory.PAPERDOLL_AGATHION_MAIN),
	AGATHION_1(Inventory.PAPERDOLL_AGATHION_1),
	AGATHION_2(Inventory.PAPERDOLL_AGATHION_2),
	AGATHION_3(Inventory.PAPERDOLL_AGATHION_3),
	AGATHION_4(Inventory.PAPERDOLL_AGATHION_4),
	DECO1(Inventory.PAPERDOLL_DECO1),
	DECO2(Inventory.PAPERDOLL_DECO2),
	DECO3(Inventory.PAPERDOLL_DECO3),
	DECO4(Inventory.PAPERDOLL_DECO4),
	DECO5(Inventory.PAPERDOLL_DECO5),
	DECO6(Inventory.PAPERDOLL_DECO6),
	BELT(Inventory.PAPERDOLL_BELT),
	BROOCH(Inventory.PAPERDOLL_BROOCH),
	BROOCH_JEWEL(Inventory.PAPERDOLL_JEWEL1),
	BROOCH_JEWEL2(Inventory.PAPERDOLL_JEWEL2),
	BROOCH_JEWEL3(Inventory.PAPERDOLL_JEWEL3),
	BROOCH_JEWEL4(Inventory.PAPERDOLL_JEWEL4),
	BROOCH_JEWEL5(Inventory.PAPERDOLL_JEWEL5),
	BROOCH_JEWEL6(Inventory.PAPERDOLL_JEWEL6),
	ARTIFACT_BOOK(Inventory.PAPERDOLL_ARTIFACT_BOOK),
	ARTIFACT_1_1(Inventory.PAPERDOLL_ARTIFACT_1_1),
	ARTIFACT_1_2(Inventory.PAPERDOLL_ARTIFACT_1_2),
	ARTIFACT_1_3(Inventory.PAPERDOLL_ARTIFACT_1_3),
	ARTIFACT_1_4(Inventory.PAPERDOLL_ARTIFACT_1_4),
	ARTIFACT_2_1(Inventory.PAPERDOLL_ARTIFACT_2_1),
	ARTIFACT_2_2(Inventory.PAPERDOLL_ARTIFACT_2_2),
	ARTIFACT_2_3(Inventory.PAPERDOLL_ARTIFACT_2_3),
	ARTIFACT_2_4(Inventory.PAPERDOLL_ARTIFACT_2_4),
	ARTIFACT_3_1(Inventory.PAPERDOLL_ARTIFACT_3_1),
	ARTIFACT_3_2(Inventory.PAPERDOLL_ARTIFACT_3_2),
	ARTIFACT_3_3(Inventory.PAPERDOLL_ARTIFACT_3_3),
	ARTIFACT_3_4(Inventory.PAPERDOLL_ARTIFACT_3_4),
	ARTIFACT_EFFECT_1_1(Inventory.PAPERDOLL_ARTIFACT_EFFECT_1_1),
	ARTIFACT_EFFECT_1_2(Inventory.PAPERDOLL_ARTIFACT_EFFECT_1_2),
	ARTIFACT_EFFECT_1_3(Inventory.PAPERDOLL_ARTIFACT_EFFECT_1_3),
	ARTIFACT_EFFECT_2_1(Inventory.PAPERDOLL_ARTIFACT_EFFECT_2_1),
	ARTIFACT_EFFECT_2_2(Inventory.PAPERDOLL_ARTIFACT_EFFECT_2_2),
	ARTIFACT_EFFECT_2_3(Inventory.PAPERDOLL_ARTIFACT_EFFECT_2_3),
	ARTIFACT_EFFECT_3_1(Inventory.PAPERDOLL_ARTIFACT_EFFECT_3_1),
	ARTIFACT_EFFECT_3_2(Inventory.PAPERDOLL_ARTIFACT_EFFECT_3_2),
	ARTIFACT_EFFECT_3_3(Inventory.PAPERDOLL_ARTIFACT_EFFECT_3_3);

	public static final InventorySlot[] VALUES = values();

	public static InventorySlot valueOf(int slot)
	{
		for(InventorySlot s : VALUES)
		{
			if(s.getSlot() == slot)
				return s;
		}
		return null;
	}
	
	private final int _paperdollSlot;
	
	private InventorySlot(int paperdollSlot)
	{
		_paperdollSlot = paperdollSlot;
	}
	
	public int getSlot()
	{
		return _paperdollSlot;
	}
	
	@Override
	public int getMask()
	{
		return ordinal();
	}
}

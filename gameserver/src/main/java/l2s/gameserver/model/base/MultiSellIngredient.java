package l2s.gameserver.model.base;

import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.items.ItemAttributes;
import l2s.gameserver.model.items.ItemInstance;

public class MultiSellIngredient implements Cloneable
{
	private int _itemId;
	private long _itemCount;
	private int _itemEnchant;
	private int[] _itemAugmentations;
	private ItemAttributes _itemAttributes;
	private boolean _mantainIngredient;
	private int activeStage;

	private int _chance;

	public MultiSellIngredient(int itemId, long itemCount)
	{
		this(itemId, itemCount, 0);
	}

	public MultiSellIngredient(int itemId, long itemCount, int chance)
	{
		this(itemId, itemCount, ItemInstance.EMPTY_AUGMENTATIONS, chance);
	}

	public MultiSellIngredient(int itemId, long itemCount, int[] augmentations, int chance)
	{
		this(itemId, itemCount, augmentations, chance, 0);
	}

	public MultiSellIngredient(int itemId, long itemCount, int[] augmentations, int chance, int activeStage) {
		_itemId = itemId;
		_itemCount = itemCount;
		_itemAugmentations = augmentations.clone();
		_chance = chance;
		_itemEnchant = 0;
		_mantainIngredient = false;
		_itemAttributes = new ItemAttributes();
		this.activeStage = activeStage;
	}

	@Override
	public MultiSellIngredient clone()
	{
		MultiSellIngredient mi = new MultiSellIngredient(_itemId, _itemCount, _itemAugmentations, _chance, activeStage);
		mi.setItemEnchant(_itemEnchant);
		mi.setMantainIngredient(_mantainIngredient);
		mi.setItemAttributes(_itemAttributes.clone());
		return mi;
	}

	public void setItemId(int itemId)
	{
		_itemId = itemId;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public void setItemCount(long itemCount)
	{
		_itemCount = itemCount;
	}

	public long getItemCount()
	{
		return _itemCount;
	}

	public boolean isStackable()
	{
		return _itemId <= 0 || ItemHolder.getInstance().getTemplate(_itemId).isStackable();
	}

	public void setItemEnchant(int itemEnchant)
	{
		_itemEnchant = itemEnchant;
	}

	public int getItemEnchant()
	{
		return _itemEnchant;
	}

	public int[] getItemAugmentations() {
		return _itemAugmentations;
	}

	public void setItemAugmentations(int[] _itemAugmentations) {
		this._itemAugmentations = _itemAugmentations;
	}

	public ItemAttributes getItemAttributes()
	{
		return _itemAttributes;
	}

	public void setItemAttributes(ItemAttributes attr)
	{
		_itemAttributes = attr;
	}

	public void setChance(int val)
	{
		_chance = val;
	}

	public int getChance()
	{
		return _chance;
	}

	public int getActiveStage() {
		return activeStage;
	}

	public void setActiveStage(int activeStage) {
		this.activeStage = activeStage;
	}

	@Override
	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = 31 * result + (int) (_itemCount ^ _itemCount >>> 32);
		for(Element e : Element.VALUES)
			result = 31 * result + _itemAttributes.getValue(e);
		result = 31 * result + _itemEnchant;
		result = 31 * result + _itemId;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		MultiSellIngredient other = (MultiSellIngredient) obj;
		if(_itemId != other._itemId)
			return false;
		if(_itemCount != other._itemCount)
			return false;
		if(_itemEnchant != other._itemEnchant)
			return false;
		for(Element e : Element.VALUES)
			if(_itemAttributes.getValue(e) != other._itemAttributes.getValue(e))
				return false;
		return true;
	}

	public boolean getMantainIngredient()
	{
		return _mantainIngredient;
	}

	public void setMantainIngredient(boolean mantainIngredient)
	{
		_mantainIngredient = mantainIngredient;
	}
}

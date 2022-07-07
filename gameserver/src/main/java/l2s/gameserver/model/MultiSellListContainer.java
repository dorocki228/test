package l2s.gameserver.model;

import l2s.gameserver.model.base.MultiSellEntry;

import java.util.ArrayList;
import java.util.List;

public class MultiSellListContainer
{
	private int _listId;
	private boolean _showall = true;
    private boolean keep_enchanted = false;
	private boolean is_dutyfree = false;
	private boolean nokey = false;
	private boolean _allowBBS = false;
	private boolean _disabled = false;
	private final List<MultiSellEntry> entries = new ArrayList<>();
	private int _npcObjectId = -1;
	private double priceMultiplier = 1.0D;

	private MultisellType _type = MultisellType.NORMAL;
	private boolean discount;
	private volatile boolean invalidate;

	public void setListId(int listId)
	{
		_listId = listId;
	}

	public int getListId()
	{
		return _listId;
	}

	public void setShowAll(boolean bool)
	{
		_showall = bool;
	}

	public boolean isShowAll()
	{
		return _showall;
	}

	public void setNoTax(boolean bool)
	{
		is_dutyfree = bool;
	}

	public boolean isNoTax()
	{
		return is_dutyfree;
	}

	public void setNoKey(boolean bool)
	{
		nokey = bool;
	}

	public boolean isNoKey()
	{
		return nokey;
	}

	public void setKeepEnchant(boolean bool)
	{
		keep_enchanted = bool;
	}

	public boolean isKeepEnchant()
	{
		return keep_enchanted;
	}

	public boolean isBBSAllowed()
	{
		return _allowBBS;
	}

	public void setBBSAllowed(boolean bool)
	{
		_allowBBS = bool;
	}

	public boolean isDisabled()
	{
		return _disabled;
	}

	public void setDisabled(boolean b)
	{
		_disabled = b;
	}

	public void setType(MultisellType val)
	{
		_type = val;
	}

	public MultisellType getType()
	{
		return _type;
	}

	public void addEntry(MultiSellEntry e)
	{
		entries.add(e);
	}

	public List<MultiSellEntry> getEntries()
	{
		return entries;
	}

	public boolean isEmpty()
	{
		return entries.isEmpty();
	}

	public int getNpcObjectId()
	{
		return _npcObjectId;
	}

	public void setNpcObjectId(int id)
	{
		_npcObjectId = id;
	}

	public double getPriceMultiplier()
	{
		return priceMultiplier;
	}

	public void setPriceMultiplier(double priceMultiplier)
	{
		this.priceMultiplier = priceMultiplier;
	}

	public void setDiscount(boolean discount) {
		this.discount = discount;
	}

	public boolean isDiscount() {
		return discount;
	}

	public void invalidate() {
		this.invalidate = true;
	}

	public boolean isInvalidate() {
		return invalidate;
	}

	public enum MultisellType
	{
		NORMAL,
		CHANCED
	}
}

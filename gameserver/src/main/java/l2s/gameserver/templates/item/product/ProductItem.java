package l2s.gameserver.templates.item.product;

import l2s.commons.util.Rnd;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ProductItem implements Comparable<ProductItem>
{
	public static final long NOT_LIMITED_START_TIME = 315547200000L;
	public static final long NOT_LIMITED_END_TIME = 2127445200000L;
	public static final int NOT_LIMITED_START_HOUR = 0;
	public static final int NOT_LIMITED_END_HOUR = 23;
	public static final int NOT_LIMITED_START_MIN = 0;
	public static final int NOT_LIMITED_END_MIN = 59;
	private final int _id;
	private final int _category;
	private final int _points;
	private final int _tabId;
	private final int _locationId;
	private final long _startTimeSale;
	private final long _endTimeSale;
	private final int _startHour;
	private final int _endHour;
	private final int _startMin;
	private final int _endMin;
	private final int _discount;
	private final int _mainPageCategory;
	private int _boughtCount;
	private final boolean _onSale;
	private final List<ProductItemComponent> _components;

	public ProductItem(int id, int category, int points, int tabId, long startTimeSale, long endTimeSale, boolean onSale, int discount, int locationId)
	{
		_boughtCount = 0;
		_components = new ArrayList<>();
		_id = id;
		_category = category;
		_points = points;
		_tabId = tabId;
		_onSale = onSale;
		_discount = discount;
		_locationId = locationId;
		_mainPageCategory = Rnd.get(new int[] { 0, 1, 2, 4 });
		if(startTimeSale > 0L)
		{
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(startTimeSale);
			_startTimeSale = startTimeSale;
			_startHour = calendar.get(11);
			_startMin = calendar.get(12);
		}
		else
		{
			_startTimeSale = 315547200000L;
			_startHour = 0;
			_startMin = 0;
		}
		if(endTimeSale > 0L)
		{
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(endTimeSale);
			_endTimeSale = endTimeSale;
			_endHour = calendar.get(11);
			_endMin = calendar.get(12);
		}
		else
		{
			_endTimeSale = 2127445200000L;
			_endHour = 23;
			_endMin = 59;
		}
	}

	public void addComponent(ProductItemComponent component)
	{
		_components.add(component);
	}

	public List<ProductItemComponent> getComponents()
	{
		return _components;
	}

	public int getId()
	{
		return _id;
	}

	public int getCategory()
	{
		return _category;
	}

	public int getPoints(boolean withDiscount)
	{
		if(withDiscount)
			return (int) (_points * ((100 - _discount) * 0.01));
		return _points;
	}

	public int getTabId()
	{
		return _tabId;
	}

	public int getLocationId()
	{
		return _locationId;
	}

	public long getStartTimeSale()
	{
		return _startTimeSale;
	}

	public int getStartHour()
	{
		return _startHour;
	}

	public int getStartMin()
	{
		return _startMin;
	}

	public long getEndTimeSale()
	{
		return _endTimeSale;
	}

	public int getEndHour()
	{
		return _endHour;
	}

	public int getEndMin()
	{
		return _endMin;
	}

	public boolean isOnSale()
	{
		return _onSale;
	}

	public int getDiscount()
	{
		return _discount;
	}

	public int getMainPageCategory()
	{
		return _mainPageCategory;
	}

	public void setBoughtCount(int val)
	{
		_boughtCount = val;
	}

	public int getBoughtCount()
	{
		return _boughtCount;
	}

	@Override
	public int compareTo(ProductItem o)
	{
		return o.getId() - getId();
	}
}

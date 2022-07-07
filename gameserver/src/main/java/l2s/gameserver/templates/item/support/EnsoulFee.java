package l2s.gameserver.templates.item.support;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import l2s.gameserver.templates.item.data.ItemData;

import java.util.Collections;
import java.util.List;

public class EnsoulFee
{
	private Table<Integer, Integer, EnsoulFeeInfo> _ensoulsFee;

	public void addFeeInfo(int type, int id, EnsoulFeeInfo feeInfo)
	{
		if(_ensoulsFee == null)
			_ensoulsFee = HashBasedTable.create();

		_ensoulsFee.put(type, id, feeInfo);
	}

	public EnsoulFeeInfo getFeeInfo(int type, int id)
	{
		if(_ensoulsFee == null)
			return null;

		return _ensoulsFee.get(type, id);
	}

	public static class EnsoulFeeInfo
	{
		private List<ItemData> _insertFee = Collections.emptyList();
		private List<ItemData> _changeFee = Collections.emptyList();
		private List<ItemData> _removeFee = Collections.emptyList();

		public void setInsertFee(List<ItemData> value)
		{
			_insertFee = value;
		}

		public List<ItemData> getInsertFee()
		{
			return _insertFee;
		}

		public void setChangeFee(List<ItemData> value)
		{
			_changeFee = value;
		}

		public List<ItemData> getChangeFee()
		{
			return _changeFee;
		}

		public void setRemoveFee(List<ItemData> value)
		{
			_removeFee = value;
		}

		public List<ItemData> getRemoveFee()
		{
			return _removeFee;
		}
	}
}
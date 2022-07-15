package l2s.gameserver.templates.player;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.gameserver.network.l2.s2c.updatetype.InventorySlot;
import l2s.gameserver.templates.CreatureTemplate;
import l2s.gameserver.templates.StatsSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Bonux
**/
public abstract class PCTemplate extends CreatureTemplate
{
	private Map<InventorySlot, Optional<Double>> baseDefBySlot;

	private final double _baseRideRunSpd;
	private final double _baseRideWalkSpd;

	private final TIntObjectMap<HpMpCpData> _regenData = new TIntObjectHashMap<HpMpCpData>();

	public PCTemplate(StatsSet set)
	{
		super(set);

		baseDefBySlot = new HashMap<>(12);
		baseDefBySlot.put(InventorySlot.CHEST, set.getOptionalDouble("baseChestDef"));
		baseDefBySlot.put(InventorySlot.LEGS, set.getOptionalDouble("baseLegsDef"));
		baseDefBySlot.put(InventorySlot.HEAD, set.getOptionalDouble("baseHelmetDef"));
		baseDefBySlot.put(InventorySlot.FEET, set.getOptionalDouble("baseBootsDef"));
		baseDefBySlot.put(InventorySlot.GLOVES, set.getOptionalDouble("baseGlovesDef"));
		baseDefBySlot.put(InventorySlot.PENDANT, set.getOptionalDouble("basePendantDef"));
		baseDefBySlot.put(InventorySlot.CLOAK, set.getOptionalDouble("baseCloakDef"));

		baseDefBySlot.put(InventorySlot.REAR, set.getOptionalDouble("baseREarDef"));
		baseDefBySlot.put(InventorySlot.LEAR, set.getOptionalDouble("baseLEarDef"));
		baseDefBySlot.put(InventorySlot.RFINGER, set.getOptionalDouble("baseRRingDef"));
		baseDefBySlot.put(InventorySlot.LFINGER, set.getOptionalDouble("baseLRingDef"));
		baseDefBySlot.put(InventorySlot.NECK, set.getOptionalDouble("baseNecklaceDef"));

		_baseRideRunSpd = set.getDouble("baseRideRunSpd");
		_baseRideWalkSpd = set.getDouble("baseRideWalkSpd");
	}

	/**
	 * @param slotId id of inventory slot to return value
	 * @return defence value of charactert for EMPTY given slot
	 */
	public Optional<Double> getBaseDefBySlot(InventorySlot slotId)
	{
		return baseDefBySlot.get(slotId);
	}

	public double getBaseRideRunSpd()
	{
		return _baseRideRunSpd;
	}

	public double getBaseRideWalkSpd()
	{
		return _baseRideWalkSpd;
	}

	public void addRegenData(int level, HpMpCpData data)
	{
		_regenData.put(level, data);
	}

	@Override
	public Optional<Double> getBaseHpReg(int level)
	{
		HpMpCpData data = _regenData.get(level);
		if(data == null)
			return Optional.empty();
		return Optional.of(data.getHP());
	}

	@Override
	public Optional<Double> getBaseMpReg(int level)
	{
		HpMpCpData data = _regenData.get(level);
		if(data == null)
			return Optional.empty();
		return Optional.of(data.getMP());
	}

	@Override
	public Optional<Double> getBaseCpReg(int level)
	{
		HpMpCpData data = _regenData.get(level);
		if(data == null)
			return Optional.empty();
		return Optional.of(data.getCP());
	}
}
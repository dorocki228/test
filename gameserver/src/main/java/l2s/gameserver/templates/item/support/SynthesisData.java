package l2s.gameserver.templates.item.support;

public class SynthesisData
{
	private final int slotone;
	private final int slottwo;
	private final double chance;
	private final int successId;
	private final int failId;
	private final int failCount;

	public SynthesisData(int slotone, int slottwo, double chance, int successId, int failId, int failCount)
	{
		this.slotone = slotone;
		this.slottwo = slottwo;
		this.chance = chance;
		this.successId = successId;
		this.failId = failId;
		this.failCount = failCount;
	}

	public int getSlotone()
	{
		return slotone;
	}

	public int getSlottwo()
	{
		return slottwo;
	}

	public double getChance()
	{
		return chance;
	}

	public int getSuccessId()
	{
		return successId;
	}

	public int getFailId()
	{
		return failId;
	}

	public int getFailCount()
	{
		return failCount;
	}
}

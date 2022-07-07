package l2s.gameserver.templates.premiumaccount;

public class PremiumAccountModifiers
{
	private final double _dropChance;
	private final double _spoilChance;
	private final double enchant;

	public PremiumAccountModifiers(double dropChance, double spoilChance, double enchant)
	{
		_dropChance = dropChance;
		_spoilChance = spoilChance;
		this.enchant = enchant;
	}

	public double getDropChance()
	{
		return _dropChance;
	}

	public double getSpoilChance()
	{
		return _spoilChance;
	}

	public double getEnchant()
	{
		return enchant;
	}
}

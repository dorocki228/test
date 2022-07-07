package l2s.gameserver.data.xml.parser;

import com.thoughtworks.xstream.XStream;
import l2s.commons.data.xml.AbstractXStreamParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.EnchantBonusHolder;
import l2s.gameserver.model.items.enchant.EnchantBonus;
import l2s.gameserver.model.items.enchant.EnchantBonusStat;
import l2s.gameserver.model.items.enchant.EnchantBonuses;

import java.io.File;

public final class EnchantBonusParser extends AbstractXStreamParser<EnchantBonusHolder, EnchantBonuses>
{
	private static final EnchantBonusParser _instance = new EnchantBonusParser();

	public static EnchantBonusParser getInstance()
	{
		return _instance;
	}

	private EnchantBonusParser()
	{
		super(EnchantBonusHolder.getInstance());
	}

	protected void initializeXStream(XStream xstream) {
		xstream.alias("enchant_bonuses", EnchantBonuses.class);

		xstream.addImplicitCollection(EnchantBonuses.class, "bonuses");
		xstream.alias("bonus", EnchantBonus.class);
		xstream.useAttributeFor(EnchantBonus.class, "enchant");
		xstream.useAttributeFor(EnchantBonus.class, "itemType");
		xstream.useAttributeFor(EnchantBonus.class, "grade");
		xstream.addImplicitCollection(EnchantBonus.class, "stats");

		xstream.alias("stat", EnchantBonusStat.class);
		xstream.useAttributeFor(EnchantBonusStat.class, "stat");
		xstream.useAttributeFor(EnchantBonusStat.class, "func");
		xstream.useAttributeFor(EnchantBonusStat.class, "value");
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/enchant_bonuses.xml");
	}

	@Override
	protected void readData(EnchantBonuses data) {
		getHolder().addBonuses(data.getBonuses());
	}
}

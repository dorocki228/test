package handler.voicecommands;

import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.AttributeType;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.stats.DoubleStat;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2s.gameserver.utils.HtmlUtils;
import org.apache.commons.text.TextStringBuilder;

import java.text.NumberFormat;
import java.util.Locale;

public class WhoAmI extends ScriptVoiceCommandHandler
{
	private final String[] COMMANDS = new String[] { "whoami", "whoiam" };

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		Creature target = null;

		//TODO [G1ta0] добавить рефлекты
		//TODO [G1ta0] возможно стоит показывать статы в зависимости от цели
		double hpRegen = player.getHpRegen();
		double cpRegen = player.getCpRegen();
		double mpRegen = player.getMpRegen();
		/*double hpDrain = player.getStat().getValue(DoubleStat.ABSORB_DAMAGE_PERCENT, 0., target, null);
		double mpDrain = player.getStat().getValue(DoubleStat.ABSORB_DAMAGEMP_PERCENT, 0., target, null);
		double hpGain = player.getStat().getMul(DoubleStat.HEAL_EFFECT, target, null) * 100;
		double mpGain = player.getStat().getValue(DoubleStat.MANAHEAL_EFFECTIVNESS, 100., target, null);
		double pCritPerc = player.getStat().getMul(DoubleStat.CRITICAL_DAMAGE, target, null) * 100;
		double pCritStatic = player.getStat().getAdd(DoubleStat.CRITICAL_DAMAGE, target, null);
		double mCritPerc = player.getStat().getMul(DoubleStat.MAGIC_CRITICAL_DMG, target, null) * 100;
		double mCritStatic = player.getStat().getAdd(DoubleStat.MAGIC_CRITICAL_DMG, target, null);
		double blowRate = player.getStat().getValue(DoubleStat.FATALBLOW_RATE, target, null) * 100. - 100.;*/

		ItemInstance shld = player.getSecondaryWeaponInstance();
		boolean shield = shld != null && shld.getItemType() == WeaponType.NONE;

		double shieldDef = shield ? player.getStat().getValue(DoubleStat.SHIELD_DEFENCE) : 0.;
		double shieldRate = shield ? player.getStat().getValue(DoubleStat.SHIELD_DEFENCE_RATE) : 0.;

		double xpRate = player.getRateExp();
		double spRate = player.getRateSp();
		double dropRate = player.getRateItems();
		double adenaRate = player.getRateAdena();
		double spoilRate = player.getRateSpoil();

		double fireResist = player.getStat().getValue(AttributeType.FIRE.getDefence(), 0.);
		double windResist = player.getStat().getValue(AttributeType.WIND.getDefence(), 0.);
		double waterResist = player.getStat().getValue(AttributeType.WATER.getDefence(), 0.);
		double earthResist = player.getStat().getValue(AttributeType.EARTH.getDefence(), 0.);
		double holyResist = player.getStat().getValue(AttributeType.HOLY.getDefence(), 0.);
		double unholyResist = player.getStat().getValue(AttributeType.UNHOLY.getDefence(), 0.);

		/*double bleedPower = player.getStat().getValue(DoubleStat.ATTACK_TRAIT_BLEED);
		double bleedResist = player.getStat().getValue(DoubleStat.DEFENCE_TRAIT_BLEED);
		double poisonPower = player.getStat().getValue(DoubleStat.ATTACK_TRAIT_POISON);
		double poisonResist = player.getStat().getValue(DoubleStat.DEFENCE_TRAIT_POISON);
		double stunPower = player.getStat().getValue(DoubleStat.ATTACK_TRAIT_SHOCK);
		double stunResist = player.getStat().getValue(DoubleStat.DEFENCE_TRAIT_SHOCK);
		double rootPower = player.getStat().getValue(DoubleStat.ATTACK_TRAIT_HOLD);
		double rootResist = player.getStat().getValue(DoubleStat.DEFENCE_TRAIT_HOLD);
		double sleepPower = player.getStat().getValue(DoubleStat.ATTACK_TRAIT_SLEEP);
		double sleepResist = player.getStat().getValue(DoubleStat.DEFENCE_TRAIT_SLEEP);
		double paralyzePower = player.getStat().getValue(DoubleStat.ATTACK_TRAIT_PARALYZE);
		double paralyzeResist = player.getStat().getValue(DoubleStat.DEFENCE_TRAIT_PARALYZE);
		double mentalPower = player.getStat().getValue(DoubleStat.ATTACK_TRAIT_DERANGEMENT);
		double mentalResist = player.getStat().getValue(DoubleStat.DEFENCE_TRAIT_DERANGEMENT);
		double debuffResist = player.getStat().getValue(DoubleStat.RESIST_ABNORMAL_DEBUFF, target, null);
		double cancelPower = player.getStat().getValue(DoubleStat.CANCEL_POWER, target, null);
		double cancelResist = player.getStat().getValue(DoubleStat.CANCEL_RESIST, target, null);

		double swordResist = player.getStat().getValue(DoubleStat.DEFENCE_TRAIT_SWORD);
		double dualResist = player.getStat().getValue(DoubleStat.DEFENCE_TRAIT_DUAL);
		double bluntResist = player.getStat().getValue(DoubleStat.DEFENCE_TRAIT_BLUNT);
		double daggerResist = player.getStat().getValue(DoubleStat.DEFENCE_TRAIT_DAGGER);
		double bowResist = player.getStat().getValue(DoubleStat.DEFENCE_TRAIT_BOW);
		double crossbowResist = player.getStat().getValue(DoubleStat.DEFENCE_TRAIT_CROSSBOW);
		double twoHandCrossbowResist = player.getStat().getValue(DoubleStat.DEFENCE_TRAIT_TWOHANDCROSSBOW);
		double poleResist = player.getStat().getValue(DoubleStat.DEFENCE_TRAIT_POLE);
		double fistResist = player.getStat().getValue(DoubleStat.DEFENCE_TRAIT_FIST);

		double critChanceResist = 100. - player.getStat().getValue(DoubleStat.PHYSICAL_CRIT_CHANCE_RECEPTIVE, target, null);
		double critDamResist = 100. - player.getStat().getValue(DoubleStat.PHYSICAL_CRIT_DAMAGE_RECEPTIVE, target, null);*/

		String dialog = HtmCache.getInstance().getHtml("command/whoami.htm", player);

		NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
		df.setMaximumFractionDigits(1);
		df.setMinimumFractionDigits(0);

		TextStringBuilder sb = new TextStringBuilder(dialog);
		sb.replaceFirst("%hpRegen%", df.format(hpRegen));
		sb.replaceFirst("%cpRegen%", df.format(cpRegen));
		sb.replaceFirst("%mpRegen%", df.format(mpRegen));
		/*sb.replaceFirst("%hpDrain%", df.format(hpDrain));
		sb.replaceFirst("%mpDrain%", df.format(mpDrain));
		sb.replaceFirst("%hpGain%", df.format(hpGain));
		sb.replaceFirst("%mpGain%", df.format(mpGain));
		sb.replaceFirst("%pCritPerc%", df.format(pCritPerc));
		sb.replaceFirst("%pCritStatic%", df.format(pCritStatic));
		sb.replaceFirst("%mCritPerc%", df.format(mCritPerc));
		sb.replaceFirst("%mCritStatic%", df.format(mCritStatic));
		sb.replaceFirst("%blowRate%", df.format(blowRate));*/
		sb.replaceFirst("%shieldDef%", df.format(shieldDef));
		sb.replaceFirst("%shieldRate%", df.format(shieldRate));
		sb.replaceFirst("%xpRate%", df.format(xpRate));
		sb.replaceFirst("%spRate%", df.format(spRate));
		sb.replaceFirst("%dropRate%", df.format(dropRate));
		sb.replaceFirst("%adenaRate%", df.format(adenaRate));
		sb.replaceFirst("%spoilRate%", df.format(spoilRate));
		sb.replaceFirst("%fireResist%", df.format(fireResist));
		sb.replaceFirst("%windResist%", df.format(windResist));
		sb.replaceFirst("%waterResist%", df.format(waterResist));
		sb.replaceFirst("%earthResist%", df.format(earthResist));
		sb.replaceFirst("%holyResist%", df.format(holyResist));
		sb.replaceFirst("%darkResist%", df.format(unholyResist));
		/*sb.replaceFirst("%bleedPower%", bleedPower == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(bleedPower));
		sb.replaceFirst("%bleedResist%", bleedResist == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(bleedResist));
		sb.replaceFirst("%poisonPower%", poisonPower == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(poisonPower));
		sb.replaceFirst("%poisonResist%", poisonResist == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(poisonResist));
		sb.replaceFirst("%stunPower%", stunPower == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(stunPower));
		sb.replaceFirst("%stunResist%", stunResist == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(stunResist));
		sb.replaceFirst("%rootPower%", rootPower == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(rootPower));
		sb.replaceFirst("%rootResist%", rootResist == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(rootResist));
		sb.replaceFirst("%sleepPower%", sleepPower == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(sleepPower));
		sb.replaceFirst("%sleepResist%", sleepResist == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(sleepResist));
		sb.replaceFirst("%paralyzePower%", paralyzePower == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(paralyzePower));
		sb.replaceFirst("%paralyzeResist%", paralyzeResist == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(paralyzeResist));
		sb.replaceFirst("%mentalPower%", mentalPower == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(mentalPower));
		sb.replaceFirst("%mentalResist%", mentalResist == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(mentalResist));
		sb.replaceFirst("%debuffResist%", df.format(debuffResist));
		sb.replaceFirst("%cancelPower%", df.format(cancelPower));
		sb.replaceFirst("%cancelResist%", df.format(cancelResist));
		sb.replaceFirst("%swordResist%", swordResist == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(swordResist));
		sb.replaceFirst("%dualResist%", dualResist == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(dualResist));
		sb.replaceFirst("%bluntResist%", bluntResist == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(bluntResist));
		sb.replaceFirst("%daggerResist%", daggerResist == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(daggerResist));
		sb.replaceFirst("%bowResist%", bowResist == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(bowResist));
		sb.replaceFirst("%crossbowResist%", crossbowResist == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(crossbowResist));
		sb.replaceFirst("%twoHandCrossbowResist%", twoHandCrossbowResist == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(twoHandCrossbowResist));
		sb.replaceFirst("%fistResist%", fistResist == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(fistResist));
		sb.replaceFirst("%poleResist%", poleResist == Double.POSITIVE_INFINITY ? (player.isLangRus() ? "Неуяз." : "Invul.") : df.format(poleResist));
		sb.replaceFirst("%critChanceResist%", df.format(critChanceResist));
		sb.replaceFirst("%critDamResist%", df.format(critDamResist));*/

		HtmlMessage msg = new HtmlMessage(0);
		msg.setHtml(HtmlUtils.bbParse(sb.toString()));
		player.sendPacket(msg);

		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}

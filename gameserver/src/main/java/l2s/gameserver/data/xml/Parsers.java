package l2s.gameserver.data.xml;

import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.string.ItemNameHolder;
import l2s.gameserver.data.string.SkillNameHolder;
import l2s.gameserver.data.string.StringsHolder;
import l2s.gameserver.data.xml.parser.*;
import l2s.gameserver.instancemanager.ReflectionManager;

public abstract class Parsers {
    public static void parseAll() {
        HtmCache.getInstance();
        StringsHolder.getInstance().load();
        ItemNameHolder.getInstance().load();
        SkillNameHolder.getInstance().load();
        SkillParser.getInstance().load();
        OptionDataParser.getInstance().load();
        EnchantBonusParser.getInstance().load();
        ItemParser.getInstance().load();
        RecipeParser.getInstance().load();
        BaseStatsBonusParser.getInstance().load();
        LevelBonusParser.getInstance().load();
        KarmaIncreaseDataParser.getInstance().load();
        HitCondBonusParser.getInstance().load();
        PlayerTemplateParser.getInstance().load();
        ClassDataParser.getInstance().load();
        TransformTemplateParser.getInstance().load();
        NpcParser.getInstance().load();
        PetDataParser.getInstance().load();
        DomainParser.getInstance().load();
        RestartPointParser.getInstance().load();
        StaticObjectParser.getInstance().load();
        DoorParser.getInstance().load();
        ZoneParser.getInstance().load();
        ShapeParser.getInstance().load();
        SpawnParser.getInstance().load();
        InstantZoneParser.getInstance().load();
        ReflectionManager.getInstance().init();
        SkillAcquireParser.getInstance().load();
        FortressUpgradeParser.getInstance().load();
        ResidenceFunctionsParser.getInstance().load();
        ResidenceParser.getInstance().load();
        ShuttleTemplateParser.getInstance().load();
        EventParser.getInstance().load();
        CubicParser.getInstance().load();
        BuyListParser.getInstance().load();
        MultiSellParser.getInstance().load();
        ProductDataParser.getInstance().load();
        AttendanceRewardParser.getInstance().load();

        AugmentationDataParser.INSTANCE.load();
        HennaParser.getInstance().load();
        EnchantItemParser.getInstance().load();
        SoulCrystalParser.getInstance().load();
        ArmorSetsParser.getInstance().load();
        LevelUpRewardParser.getInstance().load();
        PremiumAccountParser.getInstance().load();
        PetitionGroupParser.getInstance().load();
        BotReportPropertiesParser.getInstance().load();
        DailyMissionsParser.getInstance().load();
        FishDataParser.getInstance().load();
        SynthesisDataParser.getInstance().load();
        EnsoulParser.getInstance().load();
        ArtifactParser.getInstance().load();
        FactionWarSkillParser.getInstance().load();
        FactionLeaderCommandParser.getInstance().load();
        if (Config.GVE_FARM_ENABLED) {
            SteadDataParser.getInstance().load();
        }
    }
}

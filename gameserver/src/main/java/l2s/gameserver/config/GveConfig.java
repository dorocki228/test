package l2s.gameserver.config;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.templates.item.data.RewardItemData;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

import java.util.List;

/**
 * @author Java-man
 * @since 27.12.2018
 */
@Sources("file:config/gve_config.properties")
public interface GveConfig extends Reloadable
{
    @Separator(";")
    List<RewardItemData> killRewards();

    @Separator(";")
    List<RewardItemData> killRewardsSiege();

    @Key("KillDailyRewardsLimit")
    int killDailyRewardsLimit();

    double rewardPercentMercenaries();

    boolean statisticsScheduleEnabled();

    SchedulingPattern statisticsSchedule();
}

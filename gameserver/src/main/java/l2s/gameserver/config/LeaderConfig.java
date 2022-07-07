package l2s.gameserver.config;

import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.templates.skill.data.SkillData;
import l2s.gameserver.time.Interval;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

import java.util.List;

/**
 * @author Java-man
 * @since 25.01.2019
 */
@Sources("file:config/leader.properties")
public interface LeaderConfig extends Reloadable {
    @Key("Enabled")
    boolean enabled();

    @Key("MinPersonalFactionEfficiency")
    int minPersonalFactionEfficiency();

    @Key("LeaderSkillSet")
    @Separator(";")
    List<SkillData> leaderSkillSet();

    @Key("LeaderItemSet")
    @Separator(";")
    List<ItemData> leaderItemSet();

    @Key("BroadcastCommandRadius")
    int broadcastCommandRadius();

    @Key("SelfVoteAvailable")
    boolean selfVoteAvailable();

    @Key("MinLevelForVoting")
    int minLevelForVoting();

    @Key("CheckHWID")
    boolean checkHWID();

    @Key("AvailableIntervalsEnabled")
    boolean availableIntervalsEnabled();

    @Key("AvailableIntervals")
    @Separator(";")
    List<Interval> availableIntervals();
}

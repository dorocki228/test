package services;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import l2s.gameserver.Config;
import l2s.gameserver.component.player.ConfrontationComponent;
import l2s.gameserver.dao.ConfrontationDAO;
import l2s.gameserver.data.xml.holder.FactionWarSkillHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.FactionWarSkill;
import l2s.gameserver.templates.SkillName;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.Pagination;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.ParametersAreNonnullByDefault;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class Confrontation {
    public static class TopCache {
        private static final TopCache INSTANCE = new TopCache();
        private LoadingCache<Fraction, List<Integer>> cache =
                CacheBuilder.newBuilder().refreshAfterWrite(Duration.ofMinutes(2)).build(new CacheLoader<>() {
                    @Override
                    @ParametersAreNonnullByDefault
                    public List<Integer> load(Fraction key) {
                        return ConfrontationDAO.getInstance().selectConfrontationPlayer(key).stream().map(Triple::getLeft).collect(Collectors.toList());
                    }
                });

        public static TopCache getInstance() {
            return INSTANCE;
        }

        public List<Integer> get(Fraction fraction) {
            return cache.getUnchecked(fraction);
        }
    }

    @Bypass(value = "services.Confrontation:main", bbsInvoke = true)
    public void main(Player player, NpcInstance npc, String[] param) {
        if(!Config.FACTION_WAR_ENABLED) {
            Functions.show("scripts/services/service_disabled.htm", player, npc);
            return;
        }
        HtmlMessage htmlMessage = new HtmlMessage(0);
        htmlMessage.setFile("community/fw_info.htm");
        htmlMessage.addVar("availablePoints", player.getConfrontationComponent().getAvailablePoints());
        htmlMessage.addVar("currentPeriodPoints", player.getConfrontationComponent().getCurrentPeriodPoints());
        htmlMessage.addVar("trustLevel", player.getConfrontationComponent().getTrustLevel());
        List<Integer> top = TopCache.getInstance().get(player.getFraction());
        final int index = top.indexOf(player.getObjectId()) + 1;
        int level = 0;
        if(index > 300)
            level = 1;
        else if(index > 100)
            level = 2;
        else if(index > 50)
            level = 3;
        else if(index > 10)
            level = 4;
        else if(index > 1)
            level = 5;
        else if(index == 1)
            level = 6;
        htmlMessage.addVar("rewardLevel", level);
        player.sendPacket(htmlMessage);
    }

    @Bypass("services.Confrontation:skillList")
    public void skillList(Player player, NpcInstance npc, String[] param) {
        if(!Config.FACTION_WAR_ENABLED) {
            Functions.show("scripts/services/service_disabled.htm", player, npc);
            return;
        }
        if(param.length != 1)
            return;
        HtmlMessage htmlMessage = new HtmlMessage(0);
        htmlMessage.setFile("community/fw_skill_list.htm");
        final ConfrontationComponent component = player.getConfrontationComponent();
        final int trustLevel = component.getTrustLevel();
        final List<FactionWarSkill> skillList = FactionWarSkillHolder.getInstance().getSkillListFromTrustLevel(trustLevel);
        List<SkillInfo> list = skillList.stream().
                map(s -> {
                    int playerSkillLevel = component.getSkillLevel(s.getSkillId());
                    if(playerSkillLevel > s.getSkillLevel())
                        return null;
                    int maxLevel = FactionWarSkillHolder.getInstance().getMaxLevel(s.getSkillId(), trustLevel);
                    if(playerSkillLevel == 0 || playerSkillLevel < s.getSkillLevel() || playerSkillLevel >= maxLevel) {
                        final SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(s.getSkillId(), s.getSkillLevel());
                        if(skillEntry == null)
                            return null;
                        final SkillInfo skillInfo = new SkillInfo(skillEntry, skillEntry.getTemplate().getIcon());
                        skillInfo.setMax(playerSkillLevel >= maxLevel);
                        return skillInfo;
                    }
                    return null;
                }).
                filter(Objects::nonNull).
                collect(Collectors.groupingBy(s-> s.getSkill().getId(), Collectors.minBy(Comparator.comparingInt(s-> s.getSkill().getId())))).
                values().
                stream().
                map(Optional::get).
                sorted(Comparator.comparingInt(s-> s.getSkill().getId())).
                collect(Collectors.toList());
        final Pagination pagination = new Pagination<>(list, 8);
        pagination.setPage(Integer.parseInt(param[0]));
        htmlMessage.addVar("pagination", pagination);
        htmlMessage.addVar("player", player);
        player.sendPacket(htmlMessage);
    }

    @Bypass("services.Confrontation:skillView")
    public void skillView(Player player, NpcInstance npc, String[] param) {
        if(!Config.FACTION_WAR_ENABLED) {
            Functions.show("scripts/services/service_disabled.htm", player, npc);
            return;
        }
        if(param.length != 2)
            return;
        int skillId = Integer.parseInt(param[0]);
        int skillLevel = Integer.parseInt(param[1]);
        final FactionWarSkill factionWarSkill = FactionWarSkillHolder.getInstance().getSkill(skillId, skillLevel);
        if(factionWarSkill == null)
            return;
        final SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(skillId, skillLevel);
        if(skillEntry == null)
            return;
        final SkillName skillName = skillEntry.getTemplate().getSkillName(player);
        if(skillName == null)
            return;
        HtmlMessage htmlMessage = new HtmlMessage(0);
        htmlMessage.setFile("community/fw_skill_learn.htm");
        htmlMessage.addVar("availablePoints", player.getConfrontationComponent().getAvailablePoints());
        htmlMessage.addVar("cost", factionWarSkill.getCost());
        htmlMessage.addVar("skillId", skillId);
        htmlMessage.addVar("skillLevel", skillLevel);
        htmlMessage.addVar("skillName", skillName.getName());
        htmlMessage.addVar("icon", skillEntry.getTemplate().getIcon());
        htmlMessage.addVar("desc", skillName.getDesc());
        player.sendPacket(htmlMessage);
    }

    @Bypass("services.Confrontation:skillLearn")
    public void skillLearn(Player player, NpcInstance npc, String[] param) {
        if(!Config.FACTION_WAR_ENABLED) {
            Functions.show("scripts/services/service_disabled.htm", player, npc);
            return;
        }
        if(param.length != 2)
            return;
        HtmlMessage htmlMessage = new HtmlMessage(0);
        htmlMessage.setFile("community/fw_skill_learn.htm");
        int skillId = Integer.parseInt(param[0]);
        int skillLevel = Integer.parseInt(param[1]);
        final FactionWarSkill factionWarSkill = FactionWarSkillHolder.getInstance().getSkill(skillId, skillLevel);
        if(factionWarSkill == null || factionWarSkill.getMinTrust() > player.getConfrontationComponent().getTotalPoints())
            return;
        final int currentSkillLevel = player.getConfrontationComponent().getSkillLevel(skillId);
        if(skillLevel < currentSkillLevel || skillLevel != currentSkillLevel + 1)
            return;
        SkillEntry entry = SkillHolder.getInstance().getSkillEntry(skillId, skillLevel);
        if(entry == null)
            return;
        if(player.getConfrontationComponent().reduceFixedCount(1, factionWarSkill.getCost(), true)) {
            player.getConfrontationComponent().addSkill(entry);
            player.sendMessage(new CustomMessage("faction.war.s4").addString(entry.getName(player)).addNumber(entry.getLevel()));
        } else
            player.sendMessage(new CustomMessage("faction.war.s3"));
        skillList(player, npc, new String[]{String.valueOf(0)});
    }

    public static class SkillInfo {
        private final SkillEntry skillEntry;
        private final String icon;
        private boolean max;

        public SkillInfo(SkillEntry skillEntry, String icon) {
            this.skillEntry = skillEntry;
            this.icon = icon;
        }

        public String getIcon() {
            return icon;
        }

        public SkillEntry getSkill() {
            return skillEntry;
        }

        public boolean isMax() {
            return max;
        }

        public void setMax(boolean max) {
            this.max = max;
        }
    }
}

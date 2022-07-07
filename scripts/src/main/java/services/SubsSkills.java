package services;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import l2s.gameserver.Config;
import l2s.gameserver.data.string.SkillNameHolder;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Request;
import l2s.gameserver.model.Request.L2RequestType;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ConfirmDlgPacket;
import l2s.gameserver.service.SubsSkillsService;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.SkillName;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.Pagination;

/**
 * @author KRonst
 */
public class SubsSkills {

    @Bypass("services.SubsSkills:list")
    public void skillsList(Player player, NpcInstance npc, String[] param) {
        if (player == null) {
            return;
        }
        int page = 0;
        if(param.length > 0) {
            page = Integer.parseInt(param[0]);
        }

        List<Integer> learnedSkills = SubsSkillsService.getInstance().getLearnedSkills(player);
        List<SkillInfo> availableSkills = Config.SUBS_SKILLS_LIST
            .stream()
            .filter(s -> !learnedSkills.contains(s))
            .map(s -> {
                SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(s, 1);
                if (skillEntry == null) {
                    return null;
                }
                String icon = skillEntry.getTemplate().getIcon();
                SkillName skillName = SkillNameHolder.getInstance().getSkillName(player, s, 1);
                if (skillName == null) {
                    return new SkillInfo(s, "NoName (" + s + ")", "NoDescription", icon);
                } else {
                    return new SkillInfo(s, skillName.getName(), skillName.getDesc(), icon);
                }

            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        Pagination<SkillInfo> skills = new Pagination<>(availableSkills, 5);
        skills.setPage(page);
        HtmlMessage message = new HtmlMessage(0).setFile("gve/subskills/subskill_list.htm");
        message.addVar("skills", skills);
        player.sendPacket(message);
    }

    @Bypass("services.SubsSkills:add")
    public void addSkill(Player player, NpcInstance npc, String[] param) {
        if (player == null) {
            return;
        }
        if (SubsSkillsService.getInstance().getLearnedSkillsCount(player) >= Config.SUBS_SKILLS_REWARD_MAX_COUNT) {
            return;
        }
        if (param.length < 1) {
            return;
        }
        int skillId = Integer.parseInt(param[0]);
        SkillEntry skill = SkillHolder.getInstance().getSkillEntry(skillId, 1);
        if (skill == null) {
            return;
        }
        ItemTemplate template = ItemHolder.getInstance().getTemplate(Config.SUBS_SKILLS_CURRENCY);
        if (template == null) {
            return;
        }

        if (player.consumeItem(Config.SUBS_SKILLS_CURRENCY, Config.SUBS_SKILLS_PRICE, true)) {
            SubsSkillsService.getInstance().addSkill(player, skill);
            SkillName skillName = SkillNameHolder.getInstance().getSkillName(player, skillId, 1);
            if (skillName != null) {
                CustomMessage message = new CustomMessage("services.subsskills.skill.add").addString(skillName.getName());
                player.sendMessage(message);
            }
            skillsList(player, npc, new String[0]);
        } else {
            CustomMessage message = new CustomMessage("services.subsskills.error.item")
                .addNumber(Config.SUBS_SKILLS_PRICE)
                .addString(template.getName());
            player.sendMessage(message);
        }
    }

    @Bypass("services.SubsSkills:removeAll")
    public void removeSkills(Player player, NpcInstance npc, String[] param) {
        Request request = new Request(L2RequestType.CUSTOM, player, player);
        SubSkillsRemoveRequest removeRequest = new SubSkillsRemoveRequest(request);
        String message = new CustomMessage("services.subsskills.reset.request")
            .addNumber(Config.SUBS_SKILLS_RESET_PRICE)
            .toString(player);
        ConfirmDlgPacket packet = new ConfirmDlgPacket(SystemMsg.S1, 10000).addString(message);
        player.ask(packet, removeRequest);
    }

    public static class SkillInfo {
        private final int id;
        private final String name;
        private final String desc;
        private final String icon;

        public SkillInfo(int id, String name, String desc, String icon) {
            this.id = id;
            this.name = name;
            this.desc = desc;
            this.icon = icon;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        public String getIcon() {
            return icon;
        }
    }

    static class SubSkillsRemoveRequest implements OnAnswerListener {

        private final Request request;

        public SubSkillsRemoveRequest(Request request) {
            this.request = request;
        }

        @Override
        public void sayYes() {
            Player player = request.getRequestor();
            if (player != null) {
                SubsSkillsService.getInstance().removeAllSkills(player);
            }
        }

        @Override
        public void sayNo() {

        }
    }
}

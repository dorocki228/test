package l2s.gameserver.model.actor.instances.player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import l2s.gameserver.Config;
import l2s.gameserver.data.string.StringsHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.utils.Pagination;
import org.apache.commons.lang3.ArrayUtils;

/**
 * @author KRonst
 */
public class PrivateBuffer {
    private final Player player;
    private final AtomicBoolean enabled = new AtomicBoolean(false);
    private final List<Integer> targetPet = new ArrayList<>();
    private long price;

    public static final String VAR_PBUFFER = "pbuffer";
    public static final String VAR_PBUFFER_PRICE = "pbuffer_price";
    public static final String VAR_PBUFFER_SAVE_TITLE = "pbuffer_save_title";
    public static final int TITLE_COLOR;

    static {
        String rgb = Config.PRIVATE_BUFFER.titleColor();
        String brg = "0x" + rgb.substring(4, 6) + rgb.substring(2, 4) + rgb.substring(0, 2);
        TITLE_COLOR = Integer.decode(brg);
    }

    public PrivateBuffer(Player player) {
        this.player = player;
    }

    public void start(long price, String title) {
        if (!enabled.compareAndSet(false, true)) {
            return;
        }
        price = Math.max(price, Config.PRIVATE_BUFFER.minPrice());
        price = Math.min(price, Config.PRIVATE_BUFFER.maxPrice());
        // устанавливаем переменную, чтобы при подгрузке оффлайн-трейдера посадить на продажу бафа.
        player.setVar(VAR_PBUFFER, "true", -1);
        // так же для оффлайн-трейдера.
        player.setVar(VAR_PBUFFER_PRICE, price, -1);
        // сохраняем текущий титул, чтобы заменить после завершения продажи.
        if (!player.getTitle().isEmpty()) {
            player.setVar(VAR_PBUFFER_SAVE_TITLE, player.getTitle(), -1);
        }

        player.setTitle(title);
        // устанавливаем цену
        this.price = price;

        // и сажаем персонажа
        player.sitDown(null);
        player.startAbnormalEffect(AbnormalEffect.D_NOCHAT);
        player.broadcastCharInfo();
    }

    public void cancel() {
        if (!enabled.compareAndSet(true, false)) {
            return;
        }

        // убираем все переменные.
        player.unsetVar(VAR_PBUFFER);
        player.unsetVar(VAR_PBUFFER_PRICE);
        // заменим титул, на тот, что был изначально.
        String title = player.getVar(VAR_PBUFFER_SAVE_TITLE, "");
        player.unsetVar(VAR_PBUFFER_SAVE_TITLE);
        player.setTitle(title);

        // поднимаем персонажа.
        player.standUp();
        player.stopAbnormalEffect(AbnormalEffect.D_NOCHAT);
        player.broadcastCharInfo();
    }

    public void restore() {
        enabled.set(true);
        price = player.getVarLong(VAR_PBUFFER_PRICE);
        price = Math.max(price, Config.PRIVATE_BUFFER.minPrice());
        price = Math.min(price, Config.PRIVATE_BUFFER.maxPrice());
        player.setSitting(true);
        player.startAbnormalEffect(AbnormalEffect.D_NOCHAT);
    }

    public void setTarget(Player actor, String target, int page) {
        if (target.equalsIgnoreCase("player")) {
            targetPet.remove((Integer) actor.getObjectId());
        }
        if (target.equalsIgnoreCase("servitor")) {
            if (!targetPet.contains(actor.getObjectId()) && actor.getServitor(actor.getObjectId()) != null) {
                targetPet.add(actor.getObjectId());
            }
        }
        sendList(actor, page);
    }

    public void sendList(Player actor, int page) {
        List<SkillEntry> skillEntries = new ArrayList<>();

        for (SkillEntry skill : player.getAllSkills()) {
            Skill.SkillTargetType targetType = skill.getTemplate().getTargetType();
            if (targetType != Skill.SkillTargetType.TARGET_SELF && skill.getSkillType() == Skill.SkillType.BUFF && !skill.getTemplate().isSpecial()) {
                if (available(skill) && notRestricted(skill)) {
                    skillEntries.add(skill);
                }
            }
        }

        skillEntries.sort((e1, e2) -> {
            if (e1.getDisplayLevel() > 100 && e2.getDisplayLevel() < 100) {
                return -1;
            }
            if (e1.getDisplayLevel() < 100 && e2.getDisplayLevel() > 100) {
                return 1;
            }
            return e1.getId() > e2.getId() ? 1 : 0;
        });

        Pagination<SkillEntry> skills = new Pagination<>(skillEntries, 7);
        skills.setPage(page);

        String targetServitor = StringsHolder.getInstance().getString(actor, "services.privatebuffer.servitor");
        String targetPlayer = StringsHolder.getInstance().getString(actor, "services.privatebuffer.player");
        String target = targetPet.contains(actor.getObjectId()) && actor.getServitor(actor.getObjectId()) != null ? targetServitor : targetPlayer;
        HtmlMessage message = new HtmlMessage(0).setFile("buffer/private/list.htm");
        message.addVar("skills", skills);
        message.addVar("bufferName", player.getName());
        message.addVar("classId", player.getClassId().getId());
        message.addVar("level", player.getLevel());
        message.addVar("price", price);
        message.addVar("target", target);
        actor.sendPacket(message);
    }

    public void buff(Player actor, int buff, int page) {
        // дополнительная проверка, мало ли подделают байпас :)
        if (ArrayUtils.contains(Config.PRIVATE_BUFFER.restrictedSkills(), buff)) {
            return;
        }

        if (actor.getAdena() < price) {
            actor.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            return;
        }

        actor.reduceAdena(price, true);
        // добавляем адену с процентами.
        player.addAdena(price - (price * Config.PRIVATE_BUFFER.taxPercent() / 100), true);
        Creature target = targetPet.contains(actor.getObjectId()) && actor.getServitor(actor.getObjectId()) != null ? actor.getServitor(actor.getObjectId()) : actor;

        SkillEntry skill = player.getKnownSkill(buff);
        if (skill != null) {
            skill.getEffects(player, target);
        }
        sendList(actor, page);
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public long getPrice() {
        return price;
    }

    private boolean available(SkillEntry skill) {
        return ArrayUtils.contains(Config.PRIVATE_BUFFER.availableSkills(), skill.getId());
    }

    private boolean notRestricted(SkillEntry skill) {
        return !ArrayUtils.contains(Config.PRIVATE_BUFFER.restrictedSkills(), skill.getId());
    }
}

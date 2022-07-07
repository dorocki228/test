package l2s.gameserver.skills;

import l2s.gameserver.listener.actor.OnChangeLocationListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Location;

import java.util.List;
import java.util.Objects;

/**
 * @author Mangol
 */
public class OnChangeLocationListenerImpl implements OnChangeLocationListener {
    private final Player player;
    private final List<SkillEntry> skills;
    private final int distance;

    public OnChangeLocationListenerImpl(Player player, List<SkillEntry> skills, int distance) {
        this.player = player;
        this.skills = skills;
        this.distance = distance;
    }

    @Override
    public void location(Creature actor, Location location, int reflectionId) {
        if(player == null || player.isLogoutStarted())
            remove(actor);
        else if(player.getReflectionId() != reflectionId)
            remove(actor);
        else if(player.getLoc().distance(location) >= distance)
            remove(actor);
    }

    private void remove(Creature actor) {
        actor.removeListener(this);
        skills.forEach(s -> actor.getPlayer().removeSkill(s, false));
    }
}

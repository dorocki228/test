package l2s.gameserver.service;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.Fortress;
import l2s.gameserver.model.instances.ArtifactInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.OutpostInstance;
import l2s.gameserver.skills.SkillEntry;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MoraleBoostService {
    private final List<SkillEntry> buffs;

    private static MoraleBoostService INSTANCE = new MoraleBoostService();

    public MoraleBoostService()
    {
        buffs = Arrays.stream(Config.MORALE_EFFECT)
                .map(s -> SkillHolder.getInstance().getSkillEntry(s[0], s[1]))
                .collect(Collectors.toList());
    }

    public static MoraleBoostService getInstance() {
        return INSTANCE;
    }

    public void castleSuccessAttack(Castle castle) {
        if(!Config.BOOST_MORALE_ENABLED || !Config.CASTLE_SUCCESS_ATTACK_BOOST)
            return;
        buffMorale(castle.getZone().getInsidePlayers().stream()
                .filter(p -> p.getFraction() == castle.getFraction())
                .collect(Collectors.toList()));
    }

    public void castleSuccessDefense(Castle castle) {
        if(!Config.BOOST_MORALE_ENABLED || !Config.CASTLE_SUCCESS_DEFENSE_BOOST)
            return;
        buffMorale(castle.getZone().getInsidePlayers().stream()
                .filter(p -> p.getFraction() == castle.getFraction())
                .collect(Collectors.toList()));
    }

    public void fortressSuccessAttack(Fortress fortress) {
        if(!Config.BOOST_MORALE_ENABLED || !Config.FORTRESS_SUCCESS_ATTACK_BOOST || fortress == null || fortress.getOwner() == null || fortress.getOwner().getLeader().getFraction() == Fraction.NONE)
            return;
        final Fraction fraction = fortress.getOwner().getLeader().getFraction();
        buffMorale(fortress.getZone().getInsidePlayers().stream().filter(p -> p.getFraction() == fraction).collect(Collectors.toList()));
    }

    public void fortressSuccessDefense(Fortress fortress, Fraction fraction) {
        if(!Config.BOOST_MORALE_ENABLED || !Config.FORTRESS_SUCCESS_DEFENSE_BOOST)
            return;
        buffMorale(fortress.getZone().getInsidePlayers().stream().filter(p -> p.getFraction() == fraction).collect(Collectors.toList()));
    }

    public void outpostDestroy(OutpostInstance outpostInstance, Playable playable) {
        if(!Config.BOOST_MORALE_ENABLED || !Config.OUTPOST_DESTROY_BOOST || outpostInstance == null || playable == null)
            return;
        final List<Player> playerList = World.getAroundPlayers(outpostInstance, 3000, 3000).
                stream().
                filter(p -> p.getFraction() == playable.getFraction()).
                collect(Collectors.toList());
        buffMorale(playerList);
    }

    public void artifactCapture(ArtifactInstance artifact, Player caster) {
        if(!Config.BOOST_MORALE_ENABLED || !Config.ARTIFACT_CAPTURE_BOOST || artifact == null || caster == null)
            return;
        final List<Player> playerList = World.getAroundPlayers(artifact, 3000, 3000).stream().filter(p -> p.getFraction() == caster.getFraction()).collect(Collectors.toList());
        buffMorale(playerList);
    }

    public void bossKill(NpcInstance npc, Playable killer) {
        if(!Config.BOOST_MORALE_ENABLED || !Config.BOSS_KILL_BOOST || npc == null || killer == null)
            return;
        final List<Player> playerList = World.getAroundPlayers(npc, 3000, 3000).stream().filter(p -> p.getFraction() == killer.getFraction()).collect(Collectors.toList());
        buffMorale(playerList);
    }

    private void buffMorale(List<Player> playerList) {
        playerList.forEach(p -> buffs.forEach(s -> s.getEffects(p, p)));
    }
}

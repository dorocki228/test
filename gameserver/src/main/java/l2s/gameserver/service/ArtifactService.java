package l2s.gameserver.service;

import l2s.gameserver.Config;
import l2s.gameserver.dao.ArtifactDAO;
import l2s.gameserver.data.xml.holder.ArtifactHolder;
import l2s.gameserver.entity.ArtifactEntity;
import l2s.gameserver.listener.actor.player.OnActiveClassListener;
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.listener.PlayerListenerList;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.utils.Language;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArtifactService {
    private Map<Integer, ArtifactEntity> artifactEntityMap = Collections.emptyMap();
    private static ArtifactService INSTANCE = new ArtifactService();

    public static ArtifactService getInstance() {
        return INSTANCE;
    }

    public void restore() {
        if(!Config.ARTIFACT_ENABLED)
            return;
        final Map<Integer, ArtifactEntity> restore = ArtifactDAO.getInstance().restore();
        this.artifactEntityMap = ArtifactHolder.getInstance().getTemplateMap().values().stream().map(t -> {
            final ArtifactEntity artifactEntity = restore.get(t.getId());
            if(artifactEntity != null)
                return artifactEntity;
            return new ArtifactEntity(t, Fraction.NONE);
        }).collect(Collectors.toUnmodifiableMap(t -> t.getTemplate().getId(), Function.identity()));
        artifactEntityMap.values().forEach(ArtifactEntity::spawn);
        PlayerListenerList.addGlobal((OnPlayerEnterListener) this::playerGiveSkill);
        PlayerListenerList.addGlobal((OnActiveClassListener) (p0, p1, p2, onRestore) -> {
            if(onRestore)
                return;
            ArtifactService.getInstance().playerGiveSkill(p0);
        });
    }

    private void playerGiveSkill(Player player) {
        if(player.getFraction() == Fraction.NONE)
            return;
        artifactEntityMap.values().stream().
                filter(a -> a.getFraction() == player.getFraction()).
                flatMap(a -> a.getTemplate().getSkillEntryList().stream()).
                forEach(s -> player.addSkill(s, false));
    }

    public ArtifactEntity getEntity(int id) {
        return artifactEntityMap.get(id);
    }

    public int getArtifactSizeFromFaction(Fraction fraction) {
        return (int) artifactEntityMap.values().stream().filter(a -> a.getFraction() == fraction).count();
    }

    public Map<Fraction, Map<Language, String>> getBroadcastDefense() {
        return Arrays.stream(Fraction.VALUES).map(f -> Pair.of(f, getBroadcastDefense(f))).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    public Map<Language, String> getBroadcastDefense(Fraction fraction) {
        final List<ArtifactEntity> entities = artifactEntityMap.values().stream().
                filter(t -> t.getFraction() == fraction).
                filter(t -> (t.getLastCastFromFaction(fraction.revert()) + Duration.ofMinutes(1).toMillis()) > System.currentTimeMillis()).collect(Collectors.toList());
        if(entities.isEmpty())
            return Collections.emptyMap();
        return Stream.of(Language.RUSSIAN, Language.ENGLISH).map(l -> {
            final String text = entities.stream().map(a -> new CustomMessage(a.getTemplate().getStringName()).toString(l)).collect(Collectors.joining("\n"));
            return Pair.of(l, text);
        }).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

/*
    public Map<Fraction, Map<Language, List<RegisterStringPacket>>> getBroadcastDefense() {
        return Arrays.stream(Fraction.VALUES).map(f -> Pair.of(f, getBroadcastDefense(f))).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    public Map<Language, List<RegisterStringPacket>> getBroadcastDefense(Fraction fraction) {
        final List<ArtifactEntity> entities = artifactEntityMap.values().stream().
                filter(t -> t.getFraction() == fraction).
                filter(t -> (t.getLastCastFromFaction(fraction.revert()) + Duration.ofMinutes(5).toMillis()) > System.currentTimeMillis()).collect(Collectors.toList());
        if(entities.isEmpty())
            return Collections.emptyMap();
        return Stream.of(Language.RUSSIAN, Language.ENGLISH).map(l -> {
            final var artifactHeader = AAScreenStringPacketPresets.ARTIFACT_DEFENSE_HEADER.addOrUpdate(new CustomMessage("need.defense").toString(l));
            final String text = entities.stream().map(a -> new CustomMessage(a.getTemplate().getStringName()).toString(l)).collect(Collectors.joining("\n"));
            final var artifactDefense = AAScreenStringPacketPresets.ARTIFACT_DEFENSE.addOrUpdate(text);
            return Pair.of(l, List.of(artifactHeader, artifactDefense));
        }).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }
*/

    public Map<Fraction, Map<Language, String>> getBroadcastAttackMap() {
        return Arrays.stream(Fraction.VALUES).map(f -> Pair.of(f, getBroadcastAttack(f))).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    public Map<Language, String> getBroadcastAttack(Fraction fraction) {
        final List<ArtifactEntity> entities = artifactEntityMap.values().stream().
                filter(t -> t.getFraction() != fraction).
                filter(t -> (t.getLastCastFromFaction(fraction) + Duration.ofMinutes(5).toMillis()) > System.currentTimeMillis()).collect(Collectors.toList());
        if(entities.isEmpty())
            return Collections.emptyMap();
        return Stream.of(Language.RUSSIAN, Language.ENGLISH).map(l -> {
            final String text = entities.stream().map(a -> new CustomMessage(a.getTemplate().getStringName()).toString(l)).collect(Collectors.joining("\n"));
            return Pair.of(l, text);
        }).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    public Map<Integer, ArtifactEntity> getArtifactEntityMap() {
        return artifactEntityMap;
    }

    public void changeFraction(Player player, Fraction oldFraction, Fraction newFraction) {
        if(player == null)
            return;
        if(oldFraction == Fraction.NONE)
            playerGiveSkill(player);
    }
}

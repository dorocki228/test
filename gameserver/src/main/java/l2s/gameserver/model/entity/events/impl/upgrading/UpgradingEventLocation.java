package l2s.gameserver.model.entity.events.impl.upgrading;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * @author KRonst
 */
public enum UpgradingEventLocation {
    ALLIGATOR_ISLAND(
        "alligator_event_zone",
        "alligator-event",
        new Location(114344, 175960, -3712),
        Map.of(
            Fraction.FIRE, Arrays.asList(
                new Location(116085, 166158, -2456),
                new Location(115435, 166453, -2552),
                new Location(114731, 166839, -2656),
                new Location(114304, 166252, -2816),
                new Location(113774, 165967, -2936),
                new Location(112857, 165660, -3096),
                new Location(112151, 165700, -3152),
                new Location(111480, 166055, -3224),
                new Location(111148, 166902, -3320),
                new Location(110349, 166988, -3328),
                new Location(109386, 167363, -3320),
                new Location(108599, 167064, -3208),
                new Location(108628, 168128, -3328),
                new Location(107671, 168372, -3408),
                new Location(109358, 169221, -3312),
                new Location(108440, 169653, -3400),
                new Location(107994, 170696, -3440),
                new Location(108976, 171246, -3712),
                new Location(110011, 170755, -3712),
                new Location(110473, 169990, -3352),
                new Location(110352, 172399, -3736),
                new Location(111964, 172443, -3768)
            ),
            Fraction.WATER, Arrays.asList(
                new Location(117576, 165576, -2480),
                new Location(118536, 165672, -2616),
                new Location(119592, 166184, -2896),
                new Location(120344, 166760, -3104),
                new Location(121272, 167016, -3200),
                new Location(122168, 166904, -3120),
                new Location(121224, 167944, -3312),
                new Location(121816, 168488, -3312),
                new Location(120440, 168776, -3248),
                new Location(122968, 169576, -3320),
                new Location(123752, 169256, -3312),
                new Location(123848, 167976, -3312),
                new Location(122719, 166792, -3016),
                new Location(123992, 167016, -2976),
                new Location(125336, 167896, -3016),
                new Location(123016, 170728, -3496),
                new Location(123656, 171208, -3312),
                new Location(122312, 171544, -3456),
                new Location(121704, 170072, -3728),
                new Location(120408, 169896, -3688),
                new Location(119368, 169720, -3504),
                new Location(119208, 170408, -3728),
                new Location(120552, 171880, -3744),
                new Location(121416, 172920, -3440),
                new Location(121864, 173688, -3432),
                new Location(120232, 175000, -3616),
                new Location(119672, 175400, -3472)
            )
        )
    ),
    RUINS_OF_DESPAIR(
        "ruins_despair_event_zone",
        "ruins-despair-event",
        new Location(-18056, 142520, -3912),
        Map.of(
            Fraction.FIRE, Arrays.asList(
                new Location(-16264, 138696, -3792),
                new Location(-15592, 138536, -3792),
                new Location(-15528, 139224, -3792),
                new Location(-14616, 139112, -3792),
                new Location(-14536, 139688, -3792),
                new Location(-14008, 139720, -3792),
                new Location(-14024, 140376, -3792),
                new Location(-13320, 140456, -3744),
                new Location(-13672, 141112, -3824),
                new Location(-13128, 141224, -3744),
                new Location(-13464, 141656, -3768),
                new Location(-13096, 141800, -3704),
                new Location(-13448, 142360, -3696),
                new Location(-12696, 142552, -3696),
                new Location(-12456, 143320, -3696),
                new Location(-12360, 143688, -3616),
                new Location(-12776, 143832, -3600),
                new Location(-12648, 144424, -3600),
                new Location(-12088, 144696, -3592),
                new Location(-12504, 145112, -3600),
                new Location(-12168, 145544, -3560),
                new Location(-12536, 145912, -3552),
                new Location(-13000, 145896, -3592),
                new Location(-13320, 146344, -3560),
                new Location(-13768, 146040, -3600),
                new Location(-14360, 146472, -3584),
                new Location(-14728, 146008, -3648)
            ),
            Fraction.WATER, Arrays.asList(
                new Location(-18296, 145864, -3760),
                new Location(-18200, 146328, -3720),
                new Location(-18936, 146440, -3720),
                new Location(-19016, 145960, -3760),
                new Location(-19784, 146200, -3760),
                new Location(-19848, 146728, -3720),
                new Location(-20664, 146584, -3728),
                new Location(-20696, 146072, -3760),
                new Location(-21176, 145688, -3760),
                new Location(-21656, 145832, -3752),
                new Location(-21912, 145240, -3808),
                new Location(-21592, 144952, -3784),
                new Location(-21640, 144344, -3824),
                new Location(-22104, 144376, -3824),
                new Location(-22552, 144056, -3824),
                new Location(-22184, 143560, -3848),
                new Location(-22680, 143272, -3904),
                new Location(-22232, 142760, -3864),
                new Location(-22024, 142408, -3840),
                new Location(-22456, 142232, -3840),
                new Location(-22712, 141864, -3840),
                new Location(-22232, 141656, -3840),
                new Location(-21992, 141352, -3840),
                new Location(-22424, 141096, -3840),
                new Location(-22488, 140632, -3840),
                new Location(-22296, 140280, -3840),
                new Location(-21832, 140440, -3840),
                new Location(-21704, 139864, -3840),
                new Location(-21928, 139304, -3840),
                new Location(-21224, 139272, -3840),
                new Location(-21048, 138760, -3840)
            )
        )
    );

    private final Zone zone;
    private final String npcGroup;
    private final Location artifactLocation;
    private final Map<Fraction, List<Location>> respawnPoints;

    UpgradingEventLocation(String zoneName, String npcGroup, Location artifactLocation, Map<Fraction, List<Location>> respawnPoints) {
        this.zone = ReflectionUtils.getZone(zoneName);
        this.artifactLocation = artifactLocation;
        this.respawnPoints = respawnPoints;
        this.npcGroup = npcGroup;
    }

    public Zone getZone() {
        return zone;
    }

    public String getNpcGroup() {
        return npcGroup;
    }

    public Location getArtifactLocation() {
        return artifactLocation;
    }

    public Map<Fraction, List<Location>> getRespawnPoints() {
        return respawnPoints;
    }

    public static UpgradingEventLocation nextRandom(UpgradingEventLocation current) {
        UpgradingEventLocation[] locations = UpgradingEventLocation.values();
        if (locations.length == 0) {
            return null;
        } else if (locations.length == 1) {
            return locations[0];
        } else {
            List<UpgradingEventLocation> available = Arrays.stream(locations)
                .filter(loc -> loc != current)
                .collect(Collectors.toList());
            return Rnd.get(available);
        }
    }
}

package l2s.gameserver.config;

import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

/**
 * @author KRonst
 */
@Sources("file:config/private_buffer.properties")
public interface PrivateBufferConfig extends Reloadable {

    @Key("PrivateBufferEnabled")
    boolean enabled();

    @Key("PrivateBufferMinPrice")
    long minPrice();

    @Key("PrivateBufferMaxPrice")
    long maxPrice();

    @Key("PrivateBufferOnlyInSpecialZone")
    boolean onlyInSpecialZone();

    @Key("PrivateBufferTaxPercent")
    int taxPercent();

    @Key("PrivateBufferAvailableClass")
    int[] availableClass();

    @Key("PrivateBufferAvailableSkills")
    int[] availableSkills();

    @Key("PrivateBufferRestrictSkills")
    int[] restrictedSkills();

    @Key("PrivateBufferTitleColor")
    String titleColor();
}

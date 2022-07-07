package l2s.gameserver.config;

import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

/**
 * @author Java-man
 * @since 27.12.2018
 */
@Sources("file:config/active_anticheat.properties")
public interface ActiveAnticheatConfig extends Reloadable
{
    boolean enableScreenString();
}

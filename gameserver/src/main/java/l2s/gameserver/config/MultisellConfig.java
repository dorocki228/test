package l2s.gameserver.config;

import java.util.List;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

/**
 * @author KRonst
 */
@Sources("file:config/multisell.properties")
public interface MultisellConfig extends Reloadable {

    @Key("LoggableItems")
    @Separator(",")
    List<Integer> loggableItems();
}

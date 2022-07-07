package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author KRonst
 */
public class UpgradingArtifactInstance extends NpcInstance {

    public UpgradingArtifactInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
    }
}

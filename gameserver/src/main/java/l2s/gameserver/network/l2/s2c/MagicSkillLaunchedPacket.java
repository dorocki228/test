package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import org.apache.commons.lang3.ArrayUtils;

public class MagicSkillLaunchedPacket extends L2GameServerPacket {
    private final int _casterId;
    private final int _skillId;
    private final int _skillLevel;
    private final int[] _targets;

    public MagicSkillLaunchedPacket(int casterId, int skillId, int skillLevel, int target) {
        _casterId = casterId;
        _skillId = skillId;
        _skillLevel = skillLevel;
        _targets = new int[]{target};
    }

    public MagicSkillLaunchedPacket(int casterId, int skillId, int skillLevel, int[] targets) {
        _casterId = casterId;
        _skillId = skillId;
        _skillLevel = skillLevel;
        _targets = targets;
    }

    @Override
    protected final void writeImpl() {
        writeD(0);
        writeD(_casterId);
        writeD(_skillId);
        writeD(_skillLevel);
        writeD(_targets.length);
        for (final int i : _targets) {
            writeD(i);
        }
    }

    @Override
    public L2GameServerPacket packet(Player player) {
        if (player != null && player.isNotShowBuffAnim())
            return _casterId == player.getObjectId() ? super.packet(player) : null;
        return super.packet(player);
    }

    @Override
    public boolean isInPacketRange(final Creature sender, final Player recipient) {
        if (ArrayUtils.contains(_targets, recipient.getObjectId()))
            return true;
        int limit = recipient.getPacketThrottler().getPacketRange().range();
        if (limit == 0)
            return false;
        return limit * limit > sender.getXYDeltaSq(recipient.getX(), recipient.getY());
    }

    @Override
    public void onSendPacket(Player player) {
        player.getPacketThrottler().onSendPacket();
    }
}

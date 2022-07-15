package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;

public abstract class ExBlockUpSetState implements IClientOutgoingPacket {
    public static class ChangePoints extends ExBlockUpSetState {
        private final int _timeLeft;
        private final int _bluePoints;
        private final int _redPoints;
        private final boolean _isRedTeam;
        private final int _objectId;
        private final int _playerPoints;

        public ChangePoints(int timeLeft, int bluePoints, int redPoints, boolean isRedTeam, Player player, int playerPoints) {
            _timeLeft = timeLeft;
            _bluePoints = bluePoints;
            _redPoints = redPoints;
            _isRedTeam = isRedTeam;
            _objectId = player.getObjectId();
            _playerPoints = playerPoints;
        }

        @Override
        public boolean write(l2s.commons.network.PacketWriter packetWriter) {
            OutgoingExPackets.EX_BLOCK_UPSET_STATE.writeId(packetWriter);
            packetWriter.writeD(0x00);

            packetWriter.writeD(_timeLeft);
            packetWriter.writeD(_bluePoints);
            packetWriter.writeD(_redPoints);

            packetWriter.writeD(_isRedTeam ? 0x01 : 0x00);
            packetWriter.writeD(_objectId);
            packetWriter.writeD(_playerPoints);

			return true;
        }
    }

    public static class GameEnd extends ExBlockUpSetState {
        public static final GameEnd REMOVE_COUNTER = new GameEnd();

        private final int _winner;

        public GameEnd(boolean isRedTeamWin) {
            _winner = isRedTeamWin ? 0x01 : 0x00;
        }

        private GameEnd() {
            _winner = -1;
        }

        @Override
        public boolean write(l2s.commons.network.PacketWriter packetWriter) {
			OutgoingExPackets.EX_BLOCK_UPSET_STATE.writeId(packetWriter);
            packetWriter.writeD(0x01);
            packetWriter.writeD(_winner);
            packetWriter.writeD(0x00); // TODO

			return true;
        }
    }

    public static class PointsInfo extends ExBlockUpSetState {
        private final int _timeLeft;
        private final int _bluePoints;
        private final int _redPoints;

        public PointsInfo(int timeLeft, int bluePoints, int redPoints) {
            _timeLeft = timeLeft;
            _bluePoints = bluePoints;
            _redPoints = redPoints;
        }

        @Override
        public boolean write(l2s.commons.network.PacketWriter packetWriter) {
			OutgoingExPackets.EX_BLOCK_UPSET_STATE.writeId(packetWriter);
            packetWriter.writeD(0x02);
            packetWriter.writeD(_timeLeft);
            packetWriter.writeD(_bluePoints);
            packetWriter.writeD(_redPoints);

			return true;
        }
    }
}
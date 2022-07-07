package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.model.ObservePoint;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.utils.Location;
import org.apache.logging.log4j.message.ParameterizedMessage;

public class MoveBackwardToLocation extends L2GameClientPacket
{
	private final Location _targetLoc;
	private final Location _originLoc;
	private int _moveMovement;

	public MoveBackwardToLocation()
	{
		_targetLoc = new Location();
		_originLoc = new Location();
	}

	@Override
	protected void readImpl()
	{
		_targetLoc.x = readD();
		_targetLoc.y = readD();
		_targetLoc.z = readD();
		_originLoc.x = readD();
		_originLoc.y = readD();
		_originLoc.z = readD();
		if(_buf.hasRemaining())
			_moveMovement = readD();
	}

	@Override
	protected void runImpl()
	{
        Player activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;
        final boolean keyboardMove = _moveMovement == 0;
        if (keyboardMove && !Config.ALLOW_KEYBOARD_MOVE) {
            activeChar.sendActionFailed();
            return;
        }
        activeChar.setActive();
        final long delta = System.currentTimeMillis() - activeChar.getLastMovePacket();
        if (delta < Config.MOVE_PACKET_DELAY) {
            activeChar.sendActionFailed();
            return;
        }
        if (!keyboardMove) {
            // Как щелкните мышью , вы получаете около на 27 ниже позиции , чем когда двигаетесь с помощью клавиатуры
            _targetLoc.z += 27;
        }
        final double diff = _targetLoc.distance3D(activeChar.getLoc());

        if (!keyboardMove || diff > 100)
            activeChar.setLastMovePacket();

        if (diff > 11000.0D) {
            activeChar.sendActionFailed();

            String messagePattern = "Player {} trying to use MoveBackwardToLocation exploit, ban this player!";
            ParameterizedMessage message = new ParameterizedMessage(messagePattern, activeChar);
            LogService.getInstance().log(LoggerType.ILLEGAL_ACTIONS, message);
            return;
        }

        if (activeChar.isTeleporting()) {
            activeChar.sendActionFailed();
            return;
        }

        if (activeChar.isFrozen()) {
            activeChar.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFailPacket.STATIC);
            return;
        }
        if (activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			return;
		}
		if(activeChar.getNpcDialogEndTime() > System.currentTimeMillis() / 1000L)
		{
            activeChar.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_SPEAKING_TO_AN_NPC, ActionFailPacket.STATIC);
            return;
        }
        if (activeChar.isInObserverMode()) {
            ObservePoint observer = activeChar.getObservePoint();
            if (observer != null)
                observer.moveToLocation(_targetLoc, 0, false);
            return;
        }
        if (activeChar.isOutOfControl()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.getTeleMode() > 0) {
            if (activeChar.getTeleMode() == 1)
                activeChar.setTeleMode(0);
            activeChar.sendActionFailed();
            activeChar.teleToLocation(_targetLoc);
            return;
        }
        if (activeChar.isInFlyingTransform()) {
            _targetLoc.z = Math.min(5950, Math.max(50, _targetLoc.z));
            _targetLoc.x = Math.min(-166168, _targetLoc.x);
        }

       //activeChar.moveToLocation(_targetLoc.x, _targetLoc.y, _targetLoc.z, 0, !keyboardMove, keyboardMove);
		activeChar.moveBackwardToLocationForPacket(_targetLoc, _moveMovement !=0);
    }
}

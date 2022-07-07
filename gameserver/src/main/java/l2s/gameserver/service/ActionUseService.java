package l2s.gameserver.service;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.ResourceNpcInstance;
import l2s.gameserver.permission.ActionPermissionComponent;
import l2s.gameserver.permission.EActionPermissionLevel;
import l2s.gameserver.permission.interfaces.IAttackPermission;
import l2s.gameserver.permission.interfaces.IIncomingAttackPermission;

/**
 * @author mangol
 */
public class ActionUseService {
	private static final ActionUseService instance = new ActionUseService();

	public static ActionUseService getInstance() {
		return instance;
	}

	public boolean isNextTargetAttackable(Playable actor, Playable attacked) {
		Player actorPlayer = actor.getPlayer();
		Player attackedPlayer = attacked.getPlayer();
		if(attackedPlayer.isInOlympiadMode() && attackedPlayer.isOlympiadCompStart() && attackedPlayer.getOlympiadSide() != actorPlayer.getOlympiadSide()) {
			return true;
		}
		for(Event e : attacked.getEvents()) {
			if(e.checkForAttack(attacked, actor, null, false) != null) {
				return false;
			}
		}
		for(Event e : attacked.getEvents()) {
			if(e.canAttack(attacked, actor, null, false, false)) {
				return true;
			}
		}
		ActionPermissionComponent actionPermissionComponent = actor.getActionPermissionComponent();
		if(actionPermissionComponent.anyFailure(EActionPermissionLevel.None, IAttackPermission.class, actor, attacked)) {
			return false;
		}
		if(actionPermissionComponent.anySuccess(EActionPermissionLevel.None, IAttackPermission.class, actor, attacked)) {
			ActionPermissionComponent selfActionPermission = attacked.getActionPermissionComponent();
			if(selfActionPermission.anyFailure(EActionPermissionLevel.None, IIncomingAttackPermission.class, actor, attacked)) {
				return false;
			}
			if(selfActionPermission.anySuccess(EActionPermissionLevel.None, IIncomingAttackPermission.class, actor, attacked)) {
				return true;
			}
			return true;
		}
		return actor.getFraction().canAttack(attacked.getFraction());
	}

	public boolean isNextTargetNpc(Playable actor, Creature attacked) {
		if(attacked == null) {
			return false;
		}
		if(!attacked.isNpc()) {
			return false;
		}
		for(Event e : attacked.getEvents()) {
			if(e.checkForAttack(attacked, actor, null, false) != null) {
				return false;
			}
		}
		for(Event e : attacked.getEvents()) {
			if(e.canAttack(attacked, actor, null, false, false)) {
				return true;
			}
		}
		ActionPermissionComponent actionPermissionComponent = attacked.getActionPermissionComponent();
		if(actionPermissionComponent.anyFailure(EActionPermissionLevel.None, IAttackPermission.class, actor, attacked)) {
			return false;
		}
		if(actionPermissionComponent.anySuccess(EActionPermissionLevel.None, IAttackPermission.class, actor, attacked)) {
			ActionPermissionComponent selfActionPermission = actor.getActionPermissionComponent();
			if(selfActionPermission.anyFailure(EActionPermissionLevel.None, IIncomingAttackPermission.class, actor, attacked)) {
				return false;
			}
			if(selfActionPermission.anySuccess(EActionPermissionLevel.None, IIncomingAttackPermission.class, actor, attacked)) {
				return true;
			}
			return true;
		}
		if(attacked instanceof ResourceNpcInstance) {
			return false;
		}
		return !attacked.isSummon() && !attacked.isPet() && (attacked.isMonster() || attacked.isGuard() || attacked.isSiegeGuard()) && actor.getFraction().canAttack(attacked);
	}
}

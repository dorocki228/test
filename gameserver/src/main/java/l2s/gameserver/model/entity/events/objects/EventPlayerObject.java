package l2s.gameserver.model.entity.events.objects;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import l2s.gameserver.GameServer;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.network.l2.s2c.RevivePacket;

public class EventPlayerObject extends DuelSnapshotObject {
	private Multiset<String> points = HashMultiset.create();

	public EventPlayerObject(Player player, TeamType team, boolean store, boolean storeShortCuts) {
		super(player, team, store, storeShortCuts);
	}

	public int getPoints(String id) {
		return points.count(id);
	}

	public void increasePoints(String id, int add) {
		Player player = getPlayer();
		if(player != null && GameServer.DEVELOP) {
			player.sendMessage("You received " + add + ' ' + id + " event points.");
		}
		points.add(id, add);
	}

	public void reducePoints(String id, int reduce) {
		Player player = getPlayer();
		if(player != null && GameServer.DEVELOP) {
			player.sendMessage("You wasted " + reduce + ' ' + id + " event points.");
		}
		points.remove(id, reduce);
	}

	@Override
	public void restore() {
		Player _player = getPlayer();
		if(_player == null) {
			return;
		}
		for(Abnormal e : _player.getAbnormalList().getEffects()) {
			if(!e.getSkill().isToggle()) {
				e.exit();
			}
		}
		if(_classIndex == _player.getActiveSubClass().getIndex()) {
			for(Abnormal e : _effects) {
				e.schedule();
			}
			boolean isDead = _player.isDead();
			_player.setCurrentCp(_currentCp);
			_player.setCurrentHpMp(_currentHp, _currentMp, true);
			if(isDead) {
				_player.broadcastPacket(new RevivePacket(_player));
			}

			if(shortCuts != null) {
				_player.restoreShortCuts(shortCuts);
			}
		}
		else {
			boolean isDead = _player.isDead();
			_player.setCurrentCp(_player.getMaxCp());
			_player.setCurrentHpMp(_player.getMaxHp(), _player.getMaxMp(), true);
			if(isDead) {
				_player.broadcastPacket(new RevivePacket(_player));
			}
		}
	}
}

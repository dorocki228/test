package l2s.gameserver.model.entity.events.impl.arena;

import l2s.gameserver.model.entity.events.impl.arena.enums.EArenaType;

/**
 * @author mangol
 */
public class ArenaRequest {
	private final int requestId;
	private final long createTimeStamp;
	private final EArenaType type;
	private final ArenaTeam arenaParty;

	public ArenaRequest(int requestId, EArenaType type, ArenaTeam arenaParty) {
		this.requestId = requestId;
		this.createTimeStamp = System.currentTimeMillis();
		this.type = type;
		this.arenaParty = arenaParty;
	}

	public int getRequestId() {
		return requestId;
	}

	public long getCreateTimeStamp() {
		return createTimeStamp;
	}

	public EArenaType getType() {
		return type;
	}

	public ArenaTeam getArenaParty() {
		return arenaParty;
	}
}

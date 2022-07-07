package l2s.gameserver.model.entity.events.impl.casino;

/**
 * @author KRonst
 */
public class CasinoHistory {
    private final int objId1;
    private final int objId2;
    private final int bet;
    private final long date;
    private final int winnerId;

    public CasinoHistory(CasinoRoom room) {
        this.objId1 = room.getCreatorId();
        this.objId2 = room.getParticipant() == null ? 0 : room.getParticipant().getObjectId();
        this.bet = room.getBet();
        this.date = System.currentTimeMillis() / 1000L;
        this.winnerId = room.getWinner() == null ? 0 : room.getWinner().getObjectId();
    }

    public int getObjId1() {
        return objId1;
    }

    public int getObjId2() {
        return objId2;
    }

    public int getBet() {
        return bet;
    }

    public long getDate() {
        return date;
    }

    public int getWinnerId() {
        return winnerId;
    }
}

package l2s.gameserver.model.entity.events.impl.casino;

import l2s.gameserver.model.Player;

/**
 * @author KRonst
 */
public class CasinoRoom {

    private final int id;
    private final int creatorId;
    private final String name;
    private final int bet;
    private Player participant;
    private Player winner;

    public CasinoRoom(int creatorId, String name, int bet) {
        this(-1, creatorId, name, bet);
    }

    public CasinoRoom(int id, int creatorId, String name, int bet) {
        this.id = id;
        this.creatorId = creatorId;
        this.bet = bet;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public String getName() {
        return name;
    }

    public int getBet() {
        return bet;
    }

    public Player getParticipant() {
        return participant;
    }

    public void setParticipant(Player participant) {
        this.participant = participant;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    public boolean hasWinner() {
        return winner != null;
    }
}

package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.handler.bbs.BbsHandlerHolder;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.utils.Pagination;

/**
 * @author KRonst
 */
public class RatingService {

    private static final long UPDATE_RATING_DELAY = 60;
    private static final int PVP_RATING_LIMIT = 20;
    private static final String PVP_RATING_QUERY = "SELECT char_name, pvpkills, pkkills FROM characters ORDER BY pvpkills DESC LIMIT " + PVP_RATING_LIMIT;

    private final List<PlayerPvpRatingInfo> topPVP = new ArrayList<>();

    public RatingService() {
        ThreadPoolManager.getInstance()
            .scheduleAtFixedDelay(new UpdateRatingTask(), 0, UPDATE_RATING_DELAY, TimeUnit.SECONDS);
    }

    @Bypass("services.Rating:pvp")
    public void pvpList(Player player, NpcInstance npc, String[] param) {
        refreshCommunityBoard(player);
        int page = param.length == 0 ? 0 : Integer.parseInt(param[0]);
        Pagination<PlayerPvpRatingInfo> players = new Pagination<>(topPVP, PVP_RATING_LIMIT);
        players.setPage(page);
        HtmlMessage htmlMessage = new HtmlMessage(0).setFile("gve/rating/pvp-rating.htm");
        htmlMessage.addVar("players", players);
        player.sendPacket(htmlMessage);
    }

    /**
     * Костыль, чтобы борда не переставала быть активной после открытия HTML из нее
     * @param player игрок
     */
    private void refreshCommunityBoard(Player player) {
        BbsHandlerHolder.getInstance()
            .getCommunityHandler("_bbshome")
            .onBypassCommand(player, "_bbshome");
    }

    private void cleanup() {
        topPVP.clear();
    }

    private void loadTopPvp() {
        try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(PVP_RATING_QUERY);
            ResultSet rs = statement.executeQuery()
        ) {
            while (rs.next()) {
                String name = rs.getString("char_name");
                int pvp = rs.getInt("pvpkills");
                int deaths = rs.getInt("pkkills");
                topPVP.add(new PlayerPvpRatingInfo(name, pvp, deaths));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private class UpdateRatingTask implements Runnable {

        @Override
        public void run() {
            cleanup();
            loadTopPvp();
        }
    }

    public static class PlayerPvpRatingInfo {
        private final String name;
        private final int pvp;
        private final int deaths;

        public PlayerPvpRatingInfo(String name, int pvp, int deaths) {
            this.name = name;
            this.pvp = pvp;
            this.deaths = deaths;
        }

        public String getName() {
            return name;
        }

        public int getPvp() {
            return pvp;
        }

        public int getDeaths() {
            return deaths;
        }
    }
}

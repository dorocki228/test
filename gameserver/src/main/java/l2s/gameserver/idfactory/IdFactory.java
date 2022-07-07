package l2s.gameserver.idfactory;

import gnu.trove.set.hash.TIntHashSet;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Arrays;

public abstract class IdFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdFactory.class);

    public static final String[][] EXTRACT_OBJ_ID_TABLES = {
            {"characters", "obj_id"},
            {"items", "object_id"},
            {"clan_data", "clan_id"},
            {"ally_data", "ally_id"},
            {"pets", "objId"},
            {"couples", "id"}
    };

    public static final int FIRST_OID = 0x00000001; // 0x10000000 - для тестов, двинем генерацию на еденичку.
    public static final int LAST_OID = 0x7FFFFFFF;
    public static final int FREE_OBJECT_ID_SIZE = LAST_OID - FIRST_OID;

    protected static final IdFactory INSTANCE = new SparseBitSetIdFactory();

    protected volatile boolean initialized;
    protected long releasedCount;

    protected IdFactory() {
        resetOnlineStatus();
        globalRemoveItems();
        cleanUpDB();
    }

    public static IdFactory getInstance() {
        return INSTANCE;
    }

    private void resetOnlineStatus() {
        Connection con = null;
        Statement st = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            st = con.createStatement();
            st.executeUpdate("UPDATE characters SET online = 0");
            LOGGER.info("IdFactory: Clear characters online status.");
        } catch (SQLException e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, st);
        }
    }

    private void globalRemoveItems() {
        int itemToDeleteCount = 0;
        StringBuilder itemsToDelete = new StringBuilder();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT item_id FROM items_to_delete");
            ResultSet rset = statement.executeQuery();
            while (rset.next()) {
                if (itemsToDelete.length() > 0)
                    itemsToDelete.append(",");
                itemsToDelete.append(rset.getInt("item_id"));
                ++itemToDeleteCount;
            }
            DbUtils.closeQuietly(statement, rset);
            statement = con.prepareStatement("DELETE FROM items_to_delete");
            statement.execute();
        } catch (SQLException e) {
            LOGGER.error("Error while select items for global remove:", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        if (itemsToDelete.length() > 0) {
            try {
                con = DatabaseFactory.getInstance().getConnection();
                statement = con.prepareStatement("DELETE FROM items WHERE item_id IN (?)");
                statement.setString(1, itemsToDelete.toString());
                statement.execute();
                DbUtils.closeQuietly(statement);
                statement = con.prepareStatement("DELETE FROM items_delayed WHERE item_id IN (?)");
                statement.setString(1, itemsToDelete.toString());
                statement.execute();
            } catch (SQLException e) {
                LOGGER.error("Error while global remove items:", e);
            } finally {
                DbUtils.closeQuietly(con, statement);
            }
            LOGGER.info("IdFactory: Global removed {} items.", itemToDeleteCount);
        }
    }

    private void cleanUpDB() {
        Connection con = null;
        Statement st = null;
        try {
            long cleanupStart = System.currentTimeMillis();
            con = DatabaseFactory.getInstance().getConnection();
            st = con.createStatement();
            int cleanCount = 0;
            cleanCount += st.executeUpdate("DELETE FROM premium_accounts WHERE premium_accounts.account NOT IN (SELECT account_name FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM account_variables WHERE account_variables.account_name NOT IN (SELECT account_name FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM bbs_memo WHERE bbs_memo.account_name NOT IN (SELECT account_name FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_blocklist WHERE character_blocklist.obj_Id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_blocklist WHERE character_blocklist.target_Id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_bookmarks WHERE character_bookmarks.char_Id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_effects_save WHERE character_effects_save.object_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_friends WHERE character_friends.char_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_friends WHERE character_friends.friend_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_group_reuse WHERE character_group_reuse.object_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_hennas WHERE character_hennas.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_instances WHERE character_instances.obj_Id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_macroses WHERE character_macroses.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_minigame_score WHERE character_minigame_score.object_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_post_friends WHERE character_post_friends.object_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_post_friends WHERE character_post_friends.post_friend NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_product_history WHERE character_product_history.char_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_quests WHERE character_quests.char_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_recipebook WHERE character_recipebook.char_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_shortcuts WHERE character_shortcuts.object_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_skills WHERE character_skills.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_skills_save WHERE character_skills_save.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_subclasses WHERE character_subclasses.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_variables WHERE character_variables.obj_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM siege_players WHERE siege_players.object_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM punishments WHERE punishments.target NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM bbs_favorites WHERE bbs_favorites.object_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM bbs_mail WHERE bbs_mail.to_object_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM couples WHERE couples.player1Id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM couples WHERE couples.player2Id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM cursed_weapons WHERE cursed_weapons.player_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM items WHERE items.loc != 'MAIL' AND items.owner_id NOT IN (SELECT obj_Id FROM characters) AND items.owner_id NOT IN (SELECT clan_id FROM clan_data);");
            cleanCount += st.executeUpdate("DELETE FROM items_delayed WHERE items_delayed.owner_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM pets WHERE pets.item_obj_id NOT IN (SELECT object_id FROM items);");
            cleanCount += st.executeUpdate("DELETE FROM clan_data WHERE clan_data.clan_id NOT IN (SELECT clanid FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM clan_subpledges WHERE clan_subpledges.type = 0 AND clan_subpledges.leader_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM clan_data WHERE clan_data.clan_id NOT IN (SELECT clan_id FROM clan_subpledges WHERE clan_subpledges.type = 0);");
            cleanCount += st.executeUpdate("DELETE FROM clan_subpledges WHERE clan_subpledges.clan_id NOT IN (SELECT clan_id FROM clan_data);");
            cleanCount += st.executeUpdate("DELETE FROM clan_subpledges_skills WHERE clan_subpledges_skills.clan_id NOT IN (SELECT clan_id FROM clan_subpledges);");
            cleanCount += st.executeUpdate("DELETE FROM ally_data WHERE ally_data.leader_id NOT IN (SELECT clan_id FROM clan_data);");
            cleanCount += st.executeUpdate("DELETE FROM ally_data WHERE ally_data.ally_id NOT IN (SELECT ally_id FROM clan_data);");
            cleanCount += st.executeUpdate("DELETE FROM bbs_clannotice WHERE bbs_clannotice.clan_id NOT IN (SELECT clan_id FROM clan_data);");
            cleanCount += st.executeUpdate("DELETE FROM clan_privs WHERE clan_privs.clan_id NOT IN (SELECT clan_id FROM clan_data);");
            cleanCount += st.executeUpdate("DELETE FROM clan_skills WHERE clan_skills.clan_id NOT IN (SELECT clan_id FROM clan_data);");
            cleanCount += st.executeUpdate("DELETE FROM clan_subpledges WHERE clan_subpledges.clan_id NOT IN (SELECT clan_id FROM clan_data);");
            cleanCount += st.executeUpdate("DELETE FROM clan_wars WHERE clan_wars.attacker_clan NOT IN (SELECT clan_id FROM clan_data);");
            cleanCount += st.executeUpdate("DELETE FROM clan_wars WHERE clan_wars.opposing_clan NOT IN (SELECT clan_id FROM clan_data);");
            cleanCount += st.executeUpdate("DELETE FROM siege_players WHERE siege_players.clan_id NOT IN (SELECT clan_id FROM clan_data);");
            cleanCount += st.executeUpdate("DELETE FROM siege_clans WHERE siege_clans.clan_id NOT IN (SELECT clan_id FROM clan_data);");
            cleanCount += st.executeUpdate("DELETE FROM clan_largecrests WHERE clan_largecrests.clan_id NOT IN (SELECT clan_id FROM clan_data);");
            cleanCount += st.executeUpdate("DELETE FROM character_mail WHERE character_mail.char_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += st.executeUpdate("DELETE FROM character_mail WHERE character_mail.message_id NOT IN (SELECT message_id FROM mail);");
            cleanCount += st.executeUpdate("DELETE FROM mail WHERE mail.message_id NOT IN (SELECT message_id FROM character_mail);");
            cleanCount += st.executeUpdate("DELETE FROM mail_attachments WHERE mail_attachments.message_id NOT IN (SELECT message_id FROM mail);");
            st.executeUpdate("UPDATE clan_data SET ally_id = '0' WHERE clan_data.ally_id NOT IN (SELECT ally_id FROM ally_data);");
            st.executeUpdate("UPDATE clan_subpledges SET leader_id=0 WHERE leader_id > 0 AND clan_subpledges.leader_id NOT IN (SELECT obj_Id FROM characters);");
            st.executeUpdate("UPDATE characters SET clanid = '0', title = '', pledge_type = '0', pledge_rank = '0', lvl_joined_academy = '0', apprentice = '0' WHERE characters.clanid > 0 AND characters.clanid NOT IN (SELECT clan_id FROM clan_data);");
            st.executeUpdate("UPDATE items SET loc = 'WAREHOUSE' WHERE loc = 'MAIL' AND items.object_id NOT IN (SELECT item_id FROM mail_attachments);");

            LOGGER.info("IdFactory: Cleaned {} elements from database in {}sec.", cleanCount, (System.currentTimeMillis() - cleanupStart) / 1000L);
        } catch (SQLException e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, st);
        }
    }

    protected int[] extractUsedObjectIDTable() throws SQLException {
        TIntHashSet objectIds = new TIntHashSet();

        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            st = con.createStatement();
            for (String[] table : EXTRACT_OBJ_ID_TABLES) {
                rs = st.executeQuery("SELECT " + table[1] + " FROM " + table[0]);
                int size = objectIds.size();
                while (rs.next()) {
                    objectIds.add(rs.getInt(1));
                }

                DbUtils.close(rs);

                size = objectIds.size() - size;
                if (size > 0) {
                    LOGGER.info("IdFactory: Extracted {} used id's from {}", size, table[0]);
                }
            }
        } finally {
            DbUtils.closeQuietly(con, st, rs);
        }

        int[] extracted = objectIds.toArray();

        Arrays.sort(extracted);

        LOGGER.info("IdFactory: Extracted total {} used id's.", extracted.length);

        return extracted;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public abstract int getNextId();

    /**
     * return a used Object ID back to the pool
     *
     * @param id ID
     */
    public void releaseId(int id) {
        releasedCount++;
    }

    public long getReleasedCount() {
        return releasedCount;
    }

    public abstract int size();
}
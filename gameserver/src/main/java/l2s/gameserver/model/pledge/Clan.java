package l2s.gameserver.model.pledge;

import com.google.common.collect.Iterators;
import l2s.commons.dbutils.DbUtils;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Config;
import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.mysql;
import l2s.gameserver.instancemanager.clansearch.ClanSearchManager;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ResidenceType;
import l2s.gameserver.model.entity.residence.clanhall.AuctionClanHall;
import l2s.gameserver.model.items.ClanWarehouse;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.utils.PlayerUtils;
import l2s.gameserver.utils.SiegeUtils;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CTreeIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

public class Clan implements Iterable<UnitMember> {
    private static final Logger _log = LoggerFactory.getLogger(Clan.class);
    private final int _clanId;
    private int _allyId;
    private int _level;
    private int _hasCastle;
    private int _castleDefendCount;
    private int _hasHideout;
    private int _crestId;
    private int _crestLargeId;
    private long _expelledMemberTime;
    private long _leavedAllyTime;
    private long _dissolvedAllyTime;
    private long _disbandEndTime;
    private long _disbandPenaltyTime;
    private int _academyGraduatesCount;
    public static long EXPELLED_MEMBER_PENALTY = Config.ALT_EXPELLED_MEMBER_PENALTY_TIME * 60 * 60 * 1000L;
    public static long LEAVED_ALLY_PENALTY = Config.ALT_LEAVED_ALLY_PENALTY_TIME * 60 * 60 * 1000L;
    public static long DISSOLVED_ALLY_PENALTY = Config.ALT_DISSOLVED_ALLY_PENALTY_TIME * 60 * 60 * 1000L;
    public static long DISBAND_PENALTY = 604800000L;
    public static SchedulingPattern DISBAND_TIME_PATTERN = new SchedulingPattern(Config.CLAN_DELETE_TIME);
    public static SchedulingPattern CHANGE_LEADER_TIME_PATTERN = new SchedulingPattern(Config.CLAN_CHANGE_LEADER_TIME);
    public static long JOIN_PLEDGE_PENALTY = 86400000L;
    public static long CREATE_PLEDGE_PENALTY = 864000000L;
    private final ClanWarehouse _warehouse;
    private int _whBonus;
    private String _notice;
    private final List<ClanWar> _clanWars;
    private final Set<Clan> _atWarWith;
    protected IntObjectMap<SkillEntry> _skills;
    protected IntObjectMap<RankPrivs> _privs;
    protected IntObjectMap<SubUnit> _subUnits;
    private int _reputation;
    public static final int CP_NOTHING = 0;
    public static final int CP_CL_INVITE_CLAN = 2;
    public static final int CP_CL_MANAGE_TITLES = 4;
    public static final int CP_CL_WAREHOUSE_SEARCH = 8;
    public static final int CP_CL_MANAGE_RANKS = 16;
    public static final int CP_CL_CLAN_WAR = 32;
    public static final int CP_CL_DISMISS = 64;
    public static final int CP_CL_EDIT_CREST = 128;
    public static final int CP_CL_APPRENTICE = 256;
    public static final int CP_CL_TROOPS_FAME = 512;
    public static final int CP_CH_ENTRY_EXIT = 2048;
    public static final int CP_CH_USE_FUNCTIONS = 4096;
    public static final int CP_CH_AUCTION = 8192;
    public static final int CP_CH_DISMISS = 16384;
    public static final int CP_CH_SET_FUNCTIONS = 32768;
    public static final int CP_CS_ENTRY_EXIT = 65536;
    public static final int CP_CS_MANOR_ADMIN = 131072;
    public static final int CP_CS_MANAGE_SIEGE = 262144;
    public static final int CP_CS_USE_FUNCTIONS = 524288;
    public static final int CP_CS_DISMISS = 1048576;
    public static final int CP_CS_TAXES = 2097152;
    public static final int CP_CS_MERCENARIES = 4194304;
    public static final int CP_CS_SET_FUNCTIONS = 8388606;
    public static final int CP_ALL = 16777214;
    public static final int RANK_FIRST = 1;
    public static final int RANK_LAST = 9;
    public static final int SUBUNIT_NONE = -128;
    public static final int SUBUNIT_ACADEMY = -1;
    public static final int SUBUNIT_MAIN_CLAN = 0;
    public static final int SUBUNIT_ROYAL1 = 100;
    public static final int SUBUNIT_ROYAL2 = 200;
    public static final int SUBUNIT_KNIGHT1 = 1001;
    public static final int SUBUNIT_KNIGHT2 = 1002;
    public static final int SUBUNIT_KNIGHT3 = 2001;
    public static final int SUBUNIT_KNIGHT4 = 2002;
    private static final ClanReputationComparator REPUTATION_COMPARATOR = new ClanReputationComparator();
    private static final int REPUTATION_PLACES = 100;
    private static final Skill CLAN_REBIRTH_SKILL = SkillHolder.getInstance().getSkill(19009, 1);
    private String _desc;
    private String _title;
    private int _hasFortres;

    public Clan(int clanId) {
        _level = Config.START_CLAN_LEVEL;
        _whBonus = -1;
        _notice = null;
        _clanWars = new ArrayList<>();
        _atWarWith = new HashSet<>();
        _skills = new CTreeIntObjectMap<>();
        _privs = new CTreeIntObjectMap<>();
        _subUnits = new CTreeIntObjectMap<>();
        _reputation = 0;
        _clanId = clanId;
        initializePrivs();
        (_warehouse = new ClanWarehouse(this)).restore();
    }

    public int getClanId() {
        return _clanId;
    }

    public int getLeaderId() {
        return getLeaderId(0);
    }

    public UnitMember getLeader() {
        return getLeader(0);
    }

    public String getLeaderName() {
        return getLeaderName(0);
    }

    public String getName() {
        return getUnitName(0);
    }

    public UnitMember getAnyMember(int id) {
        for (SubUnit unit : getAllSubUnits()) {
            UnitMember m = unit.getUnitMember(id);
            if (m != null)
                return m;
        }
        return null;
    }

    public UnitMember getAnyMember(String name) {
        for (SubUnit unit : getAllSubUnits()) {
            UnitMember m = unit.getUnitMember(name);
            if (m != null)
                return m;
        }
        return null;
    }

    public int getAllSize() {
        int size = 0;
        for (SubUnit unit : getAllSubUnits())
            size += unit.size();
        return size;
    }

    public String getUnitName(int unitType) {
        if (unitType == -128 || !_subUnits.containsKey(unitType))
            return "";
        return getSubUnit(unitType).getName();
    }

    public String getLeaderName(int unitType) {
        if (unitType == -128 || !_subUnits.containsKey(unitType))
            return "";
        return getSubUnit(unitType).getLeaderName();
    }

    public int getLeaderId(int unitType) {
        if (unitType == -128 || !_subUnits.containsKey(unitType))
            return 0;
        return getSubUnit(unitType).getLeaderObjectId();
    }

    public UnitMember getLeader(int unitType) {
        if (unitType == -128 || !_subUnits.containsKey(unitType))
            return null;
        return getSubUnit(unitType).getLeader();
    }

    public void flush() {
        for (UnitMember member : this)
            removeClanMember(member.getObjectId());
        _warehouse.writeLock();
        try {
            for (ItemInstance item : _warehouse.getItems())
                _warehouse.destroyItem(item);
        } finally {
            _warehouse.writeUnlock();
        }
        if (_hasCastle != 0)
            ResidenceHolder.getInstance().getResidence(Castle.class, _hasCastle).changeOwner(null);
    }

    public void removeClanMember(int id) {
        if (id == getLeaderId(0))
            return;
        ClanChangeLeaderRequest changeLeaderRequest = ClanTable.getInstance().getRequest(getClanId());
        if (changeLeaderRequest != null && changeLeaderRequest.getNewLeaderId() == id)
            ClanTable.getInstance().cancelRequest(changeLeaderRequest, false);
        for (SubUnit unit : getAllSubUnits()) {
            UnitMember member = unit.getUnitMember(id);
            if (member != null) {
                onLeaveClan(member.getPlayer());
                removeClanMember(unit.getType(), id);
                break;
            }
        }
    }

    public void removeClanMember(int subUnitId, int objectId) {
        SubUnit subUnit = getSubUnit(subUnitId);
        if (subUnit == null)
            return;
        subUnit.removeUnitMember(objectId);
    }

    public List<UnitMember> getAllMembers() {
        Collection<SubUnit> units = getAllSubUnits();
        int size = 0;
        for (SubUnit unit : units)
            size += unit.size();
        List<UnitMember> members = new ArrayList<>(size);
        for (SubUnit unit2 : units)
            members.addAll(unit2.getUnitMembers());
        return members;
    }

    public List<Player> getOnlineMembers(int exclude) {
        List<Player> result = new ArrayList<>(getAllSize() - 1);
        for (UnitMember temp : this)
            if (temp != null && temp.isOnline() && temp.getObjectId() != exclude)
                result.add(temp.getPlayer());
        return result;
    }

    public int getOnlineMembersCount(int exclude) {
        int result = 0;
        for (UnitMember temp : this)
            if (temp != null && temp.isOnline() && temp.getObjectId() != exclude)
                ++result;
        return result;
    }

    public int getAllyId() {
        return _allyId;
    }

    public int getLevel() {
        return _level;
    }

    public int getCastle() {
        return _hasCastle;
    }

    public int getHasHideout() {
        return _hasHideout;
    }

    public int getResidenceId(ResidenceType r) {
        switch (r) {
            case CASTLE: {
                return _hasCastle;
            }
            case CLANHALL: {
                return _hasHideout;
            }
            default: {
                return 0;
            }
        }
    }

    public void setAllyId(int allyId) {
        _allyId = allyId;
    }

    public void setHasCastle(int castle) {
        _hasCastle = castle;
    }

    public void setHasHideout(int hasHideout) {
        _hasHideout = hasHideout;
    }

    public void setLevel(int level) {
        _level = level;
    }

    public boolean isAnyMember(int id) {
        for (SubUnit unit : getAllSubUnits())
            if (unit.isUnitMember(id))
                return true;
        return false;
    }

    private void updateClanScoreInDB() {
        if (getClanId() == 0) {
            _log.warn("updateClanScoreInDB with empty ClanId");
            Thread.dumpStack();
            return;
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE clan_data SET reputation_score=? WHERE clan_id=?");
            statement.setInt(1, getReputationScore());
            statement.setInt(2, getClanId());
            statement.execute();
        } catch (Exception e) {
            _log.warn("error while updating clan reputation score '" + getClanId() + "' data in db");
            _log.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void updateClanInDB() {
        if (getLeaderId() == 0) {
            _log.warn("updateClanInDB with empty LeaderId");
            Thread.dumpStack();
            return;
        }
        if (getClanId() == 0) {
            _log.warn("updateClanInDB with empty ClanId");
            Thread.dumpStack();
            return;
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE clan_data SET ally_id=?,reputation_score=?,expelled_member=?,leaved_ally=?,dissolved_ally=?,clan_level=?,warehouse=?,academy_graduates=?,castle_defend_count=?,disband_end=?,disband_penalty=? WHERE clan_id=?");
            statement.setInt(1, getAllyId());
            statement.setInt(2, getReputationScore());
            statement.setLong(3, getExpelledMemberTime() / 1000L);
            statement.setLong(4, getLeavedAllyTime() / 1000L);
            statement.setLong(5, getDissolvedAllyTime() / 1000L);
            statement.setInt(6, _level);
            statement.setInt(7, getWhBonus());
            statement.setInt(8, getAcademyGraduatesCount());
            statement.setInt(9, getCastleDefendCount());
            statement.setInt(10, (int) (getDisbandEndTime() / 1000L));
            statement.setInt(11, (int) (getDisbandPenaltyTime() / 1000L));
            statement.setInt(12, getClanId());
            statement.execute();
        } catch (Exception e) {
            _log.warn("error while updating clan '" + getClanId() + "' data in db");
            _log.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void store() {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("INSERT INTO clan_data (clan_id,clan_level,ally_id,expelled_member,leaved_ally,dissolved_ally,academy_graduates) values (?,?,?,?,?,?,?)");
            statement.setInt(1, _clanId);
            statement.setInt(2, _level);
            statement.setInt(3, _allyId);
            statement.setLong(4, getExpelledMemberTime() / 1000L);
            statement.setLong(5, getLeavedAllyTime() / 1000L);
            statement.setLong(6, getDissolvedAllyTime() / 1000L);
            statement.setInt(7, getAcademyGraduatesCount());
            statement.execute();
            DbUtils.close(statement);
            SubUnit mainSubUnit = _subUnits.get(0);
            statement = con.prepareStatement("INSERT INTO clan_subpledges (clan_id, type, leader_id, name) VALUES (?,?,?,?)");
            statement.setInt(1, _clanId);
            statement.setInt(2, mainSubUnit.getType());
            statement.setInt(3, mainSubUnit.getLeaderObjectId());
            statement.setString(4, mainSubUnit.getName());
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("UPDATE characters SET clanid=?,pledge_type=? WHERE obj_Id=?");
            statement.setInt(1, getClanId());
            statement.setInt(2, mainSubUnit.getType());
            statement.setInt(3, getLeaderId());
            statement.execute();
        } catch (Exception e) {
            _log.warn("Exception: " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public static Clan restore(int clanId) {
        if (clanId == 0)
            return null;
        Clan clan = null;
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT clan_level,ally_id,reputation_score,expelled_member,leaved_ally,dissolved_ally,warehouse,academy_graduates,castle_defend_count,disband_end,disband_penalty FROM clan_data WHERE clan_id=?");
            statement.setInt(1, clanId);
            rset = statement.executeQuery();
            if (!rset.next()) {
                _log.warn("Clan " + clanId + " doesnt exists!");
                return null;
            }
            clan = new Clan(clanId);
            clan.setLevel(rset.getInt("clan_level"));
            clan.setAllyId(rset.getInt("ally_id"));
            clan._reputation = rset.getInt("reputation_score");
            clan.setExpelledMemberTime(rset.getLong("expelled_member") * 1000L);
            clan.setLeavedAllyTime(rset.getLong("leaved_ally") * 1000L);
            clan.setDissolvedAllyTime(rset.getLong("dissolved_ally") * 1000L);
            clan.setDisbandEndTime(rset.getLong("disband_end") * 1000L);
            clan.setDisbandPenaltyTime(rset.getLong("disband_penalty") * 1000L);
            clan.setWhBonus(rset.getInt("warehouse"));
            clan.setCastleDefendCount(rset.getInt("castle_defend_count"));
            clan.setAcademyGraduatesCount(rset.getInt("academy_graduates"));
            DbUtils.close(statement, rset);
            statement = con.prepareStatement("SELECT id FROM castle WHERE owner_id=?");
            statement.setInt(1, clanId);
            rset = statement.executeQuery();
            if (rset.next())
                clan.setHasCastle(rset.getInt("id"));
            DbUtils.close(statement, rset);
            statement = con.prepareStatement("SELECT id FROM fortress WHERE owner_id=?");
            statement.setInt(1, clanId);
            rset = statement.executeQuery();
            if (rset.next())
                clan.setHasFortress(rset.getInt("id"));
            DbUtils.close(statement, rset);
            statement = con.prepareStatement("SELECT id FROM clanhall WHERE owner_id=?");
            statement.setInt(1, clanId);
            rset = statement.executeQuery();
            if (rset.next())
                clan.setHasHideout(rset.getInt("id"));
            if (clan.getHasHideout() == 0) {
                DbUtils.close(statement, rset);
                statement = con.prepareStatement("SELECT id FROM instant_clanhall_owners WHERE owner_id=?");
                statement.setInt(1, clanId);
                rset = statement.executeQuery();
                if (rset.next())
                    clan.setHasHideout(rset.getInt("id"));
            }
        } catch (Exception e) {
            _log.error("Error while restoring clan!", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        if (clan == null) {
            _log.warn("Clan " + clanId + " does't exist");
            return null;
        }
        clan.restoreSkills();
        clan.restoreSubPledges();
        for (SubUnit unit : clan.getAllSubUnits()) {
            unit.restore();
            unit.restoreSkills();
        }
        clan.restoreRankPrivs();
        clan.setCrestId(CrestCache.getInstance().getPledgeCrestId(clanId));
        clan.setCrestLargeId(CrestCache.getInstance().getPledgeCrestLargeId(clanId));
        return clan;
    }

    public void broadcastToOnlineMembers(IBroadcastPacket... packets) {
        for (UnitMember member : this)
            if (member.isOnline())
                member.getPlayer().sendPacket(packets);
    }

    public void broadcastToOtherOnlineMembers(IBroadcastPacket packet, Player player) {
        for (UnitMember member : this)
            if (member.isOnline() && member.getPlayer() != player)
                member.getPlayer().sendPacket(packet);
    }

    @Override
    public String toString() {
        return getName() + "[" + getClanId() + "]";
    }

    public void setCrestId(int newcrest) {
        _crestId = newcrest;
    }

    public int getCrestId() {
        return _crestId;
    }

    public boolean hasCrest() {
        return _crestId > 0;
    }

    public int getCrestLargeId() {
        return _crestLargeId;
    }

    public void setCrestLargeId(int newcrest) {
        _crestLargeId = newcrest;
    }

    public boolean hasCrestLarge() {
        return _crestLargeId > 0;
    }

    public long getAdenaCount() {
        return _warehouse.getCountOfAdena();
    }

    public ClanWarehouse getWarehouse() {
        return _warehouse;
    }

    public boolean isAtWar() {
        return !_atWarWith.isEmpty();
    }

    public Set<Clan> getWarList() {
        return _atWarWith;
    }

    public boolean isAtWarWith(Clan clan) {
        return clan != null && _atWarWith.contains(clan);
    }

    public List<ClanWar> getClanWars() {
        return _clanWars;
    }

    public ClanWar getClanWar(Clan clan) {
        for (ClanWar war : _clanWars)
            if (war.getAttackerClan() == clan || war.getOpposingClan() == clan)
                return war;
        return null;
    }

    public void addClanWar(ClanWar war) {
        if (_clanWars.contains(war))
            return;
        if (war.getAttackerClan() != this && war.getOpposingClan() != this)
            return;
        updateClanWarStatus(war, this);
        _clanWars.add(war);
    }

    public void removeClanWar(ClanWar war) {
        if (!_clanWars.contains(war))
            return;
        if (war.getAttackerClan() != this && war.getOpposingClan() != this)
            return;
        _clanWars.remove(war);
        updateClanWarStatus(war, this);
    }

    public void updateClanWarStatus(ClanWar war, Clan requester) {
        if (war.getPeriod() == ClanWar.ClanWarPeriod.PREPARATION) {
            if (!war.getOpposingClan().equals(this)) {
                _atWarWith.remove(war.getOpposingClan());
            } else if (!war.getAttackerClan().equals(this)) {
                _atWarWith.add(war.getAttackerClan());
            }
        } else if (war.getPeriod() == ClanWar.ClanWarPeriod.MUTUAL) {
            if (this != war.getOpposingClan()) {
                _atWarWith.add(war.getOpposingClan());
            } else if (this != war.getAttackerClan()) {
                _atWarWith.add(war.getAttackerClan());
            }
        } else if (war.getPeriod() == ClanWar.ClanWarPeriod.PEACE)
            if (this != war.getOpposingClan()) {
                _atWarWith.remove(war.getOpposingClan());
            } else if (this != war.getAttackerClan()) {
                _atWarWith.remove(war.getAttackerClan());
            }

        broadcastClanStatus(true, true, true);
    }

    public void broadcastClanStatus(boolean updateList, boolean needUserInfo, boolean relation) {
        List<IBroadcastPacket> listAll = updateList ? listAll() : null;
        PledgeShowInfoUpdatePacket update = new PledgeShowInfoUpdatePacket(this);
        for (UnitMember member : this)
            if (member.isOnline()) {
                if (updateList) {
                    member.getPlayer().sendPacket(PledgeShowMemberListDeleteAllPacket.STATIC);
                    member.getPlayer().sendPacket(listAll);
                }
                member.getPlayer().sendPacket(update);
                if (needUserInfo)
                    member.getPlayer().broadcastCharInfo();
                if (!relation)
                    continue;
                PlayerUtils.updateAttackableFlags(member.getPlayer());
            }
    }

    public Alliance getAlliance() {
        return _allyId == 0 ? null : ClanTable.getInstance().getAlliance(_allyId);
    }

    public void setExpelledMemberTime(long time) {
        _expelledMemberTime = time;
    }

    public long getExpelledMemberTime() {
        return _expelledMemberTime;
    }

    public void setExpelledMember() {
        _expelledMemberTime = System.currentTimeMillis();
        updateClanInDB();
    }

    public void setLeavedAllyTime(long time) {
        _leavedAllyTime = time;
    }

    public long getLeavedAllyTime() {
        return _leavedAllyTime;
    }

    public void setLeavedAlly() {
        _leavedAllyTime = System.currentTimeMillis();
        updateClanInDB();
    }

    public void setDissolvedAllyTime(long time) {
        _dissolvedAllyTime = time;
    }

    public long getDissolvedAllyTime() {
        return _dissolvedAllyTime;
    }

    public void setDissolvedAlly() {
        _dissolvedAllyTime = System.currentTimeMillis();
        updateClanInDB();
    }

    public boolean canInvite() {
        return System.currentTimeMillis() - _expelledMemberTime >= EXPELLED_MEMBER_PENALTY;
    }

    public boolean canJoinAlly() {
        return System.currentTimeMillis() - _leavedAllyTime >= LEAVED_ALLY_PENALTY;
    }

    public boolean canCreateAlly() {
        return System.currentTimeMillis() - _dissolvedAllyTime >= DISSOLVED_ALLY_PENALTY;
    }

    public boolean canDisband() {
        return System.currentTimeMillis() > _disbandPenaltyTime;
    }

    public int getRank() {
        Clan[] clans = ClanTable.getInstance().getClans();
        Arrays.sort(clans, REPUTATION_COMPARATOR);
        int place = 1;
        for (int i = 0; i < clans.length; ++i) {
            if (i == REPUTATION_PLACES)
                return 0;
            Clan clan = clans[i];
            if (clan == this)
                return place + i;
        }
        return 0;
    }

    public int getReputationScore() {
        return _reputation;
    }

    public void setReputationScore(int rep) {
        if (_reputation >= 0 && rep < 0) {
            broadcastToOnlineMembers(SystemMsg.SINCE_THE_CLAN_REPUTATION_SCORE_HAS_DROPPED_TO_0_OR_LOWER_YOUR_CLAN_SKILLS_WILL_BE_DEACTIVATED);
            for (UnitMember member : this)
                if (member.isOnline() && member.getPlayer() != null)
                    disableSkills(member.getPlayer());
        } else if (_reputation < 0 && rep >= 0) {
            broadcastToOnlineMembers(SystemMsg.CLAN_SKILLS_WILL_NOW_BE_ACTIVATED_SINCE_THE_CLANS_REPUTATION_SCORE_IS_0_OR_HIGHER);
            for (UnitMember member : this)
                if (member.isOnline() && member.getPlayer() != null)
                    enableSkills(member.getPlayer());
        }
        if (_reputation != rep) {
            _reputation = rep;
            broadcastToOnlineMembers(new PledgeShowInfoUpdatePacket(this));
        }
        updateClanScoreInDB();
    }

    public int incReputation(int inc, boolean rate, String source) {
        if (rate && Math.abs(inc) <= Config.RATE_CLAN_REP_SCORE_MAX_AFFECTED)
            inc = (int) Math.round(inc * Config.RATE_CLAN_REP_SCORE);
        setReputationScore(_reputation + inc);

        String messagePattern = "{}|{}|{}|{}";
        ParameterizedMessage message = new ParameterizedMessage(messagePattern, this, inc, _reputation, source);
        LogService.getInstance().log(LoggerType.CLAN, message);

        return inc;
    }

    private void restoreSkills() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT skill_id,skill_level FROM clan_skills WHERE clan_id=?");
            statement.setInt(1, getClanId());
            rset = statement.executeQuery();
            while (rset.next()) {
                int id = rset.getInt("skill_id");
                int level = rset.getInt("skill_level");
                SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(id, level);
                if (skillEntry != null)
                    _skills.put(skillEntry.getId(), skillEntry);
            }
        } catch (Exception e) {
            _log.warn("Could not restore clan skills: " + e);
            _log.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public Collection<SkillEntry> getSkills() {
        return _skills.values();
    }

    public SkillEntry[] getAllSkills() {
        if (_reputation < 0)
            return SkillEntry.EMPTY_ARRAY;
        Collection<SkillEntry> values = _skills.values();
        return values.toArray(new SkillEntry[0]);
    }

    public SkillEntry addSkill(SkillEntry newSkillEntry, boolean store) {
        SkillEntry oldSkillEntry = null;
        if (newSkillEntry != null) {
            oldSkillEntry = _skills.put(newSkillEntry.getId(), newSkillEntry);
            if (store) {
                Connection con = null;
                PreparedStatement statement = null;
                try {
                    con = DatabaseFactory.getInstance().getConnection();
                    if (oldSkillEntry != null) {
                        statement = con.prepareStatement("UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?");
                        statement.setInt(1, newSkillEntry.getLevel());
                        statement.setInt(2, oldSkillEntry.getId());
                        statement.setInt(3, getClanId());
                        statement.execute();
                    } else {
                        statement = con.prepareStatement("INSERT INTO clan_skills (clan_id,skill_id,skill_level) VALUES (?,?,?)");
                        statement.setInt(1, getClanId());
                        statement.setInt(2, newSkillEntry.getId());
                        statement.setInt(3, newSkillEntry.getLevel());
                        statement.execute();
                    }
                } catch (Exception e) {
                    _log.warn("Error could not store char skills: " + e);
                    _log.error("", e);
                } finally {
                    DbUtils.closeQuietly(con, statement);
                }
            }
            PledgeSkillListAddPacket p = new PledgeSkillListAddPacket(newSkillEntry.getId(), newSkillEntry.getLevel());
            PledgeSkillListPacket p2 = new PledgeSkillListPacket(this);
            for (UnitMember temp : this)
                if (temp.isOnline()) {
                    Player player = temp.getPlayer();
                    if (player == null)
                        continue;
                    addSkill(player, newSkillEntry);
                    player.sendPacket(p, p2);
                    player.sendSkillList();
                }
        }
        return oldSkillEntry;
    }

    public void addSkillsQuietly(Player player) {
        for (SkillEntry skillEntry : _skills.values())
            addSkill(player, skillEntry);
        SubUnit subUnit = getSubUnit(player.getPledgeType());
        if (subUnit != null)
            subUnit.addSkillsQuietly(player);
        if (player.isClanLeader() && getLevel() >= 3)
            SiegeUtils.addSiegeSkills(player);
    }

    public void enableSkills(Player player) {
        if (player.isInOlympiadMode())
            return;

        for (SkillEntry skillEntry : _skills.values()) {
            Skill skill = skillEntry.getTemplate();
            if (skill.getMinPledgeRank().ordinal() <= player.getPledgeRank().ordinal() && (!skill.clanLeaderOnly() || skill.clanLeaderOnly() && player.isClanLeader()))
                player.removeUnActiveSkill(skill);
        }
        SubUnit subUnit = getSubUnit(player.getPledgeType());
        if (subUnit != null)
            subUnit.enableSkills(player);
    }

    public void disableSkills(Player player) {
        for (SkillEntry skillEntry : _skills.values())
            player.addUnActiveSkill(skillEntry.getTemplate());
        SubUnit subUnit = getSubUnit(player.getPledgeType());
        if (subUnit != null)
            subUnit.disableSkills(player);
    }

    private void addSkill(Player player, SkillEntry skillEntry) {
        Skill skill = skillEntry.getTemplate();
        if (skill.getMinPledgeRank().ordinal() <= player.getPledgeRank().ordinal() && (!skill.clanLeaderOnly() || skill.clanLeaderOnly() && player.isClanLeader())) {
            player.addSkill(skillEntry, false);
            if (_reputation < 0 || player.isInOlympiadMode())
                player.addUnActiveSkill(skill);
        }
    }

    public void removeSkill(int skill) {
        _skills.remove(skill);
        PledgeSkillListAddPacket p = new PledgeSkillListAddPacket(skill, 0);
        for (UnitMember temp : this) {
            Player player = temp.getPlayer();
            if (player != null && player.isOnline()) {
                player.removeSkillById(skill);
                player.sendPacket(p);
                player.sendSkillList();
            }
        }
    }

    public void broadcastSkillListToOnlineMembers() {
        for (UnitMember temp : this) {
            Player player = temp.getPlayer();
            if (player != null && player.isOnline()) {
                player.sendPacket(new PledgeSkillListPacket(this));
                player.sendSkillList();
            }
        }
    }

    public static boolean isAcademy(int pledgeType) {
        return pledgeType == -1;
    }

    public static boolean isRoyalGuard(int pledgeType) {
        return pledgeType == 100 || pledgeType == 200;
    }

    public static boolean isOrderOfKnights(int pledgeType) {
        return pledgeType == 1001 || pledgeType == 1002 || pledgeType == 2001 || pledgeType == 2002;
    }

    public int getAffiliationRank(int pledgeType) {
        if (isAcademy(pledgeType))
            return 9;
        if (isOrderOfKnights(pledgeType))
            return 8;
        if (isRoyalGuard(pledgeType))
            return 7;
        return 6;
    }

    public SubUnit getSubUnit(int pledgeType) {
        return _subUnits.get(pledgeType);
    }

    public void addSubUnit(SubUnit sp, boolean updateDb) {
        _subUnits.put(sp.getType(), sp);
        if (updateDb) {
            broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(sp));
            Connection con = null;
            PreparedStatement statement = null;
            try {
                con = DatabaseFactory.getInstance().getConnection();
                statement = con.prepareStatement("INSERT INTO `clan_subpledges` (clan_id,type,leader_id,name) VALUES (?,?,?,?)");
                statement.setInt(1, getClanId());
                statement.setInt(2, sp.getType());
                statement.setInt(3, sp.getLeaderObjectId());
                statement.setString(4, sp.getName());
                statement.execute();
            } catch (Exception e) {
                _log.warn("Could not store clan Sub pledges: " + e);
                _log.error("", e);
            } finally {
                DbUtils.closeQuietly(con, statement);
            }
        }
    }

    public int createSubPledge(Player player, int pledgeType, UnitMember leader, String name) {
        pledgeType = getAvailablePledgeTypes(pledgeType);
        if (pledgeType == -128)
            return -128;
        switch (pledgeType) {
            case 100:
            case 200: {
                if (getReputationScore() < 5000) {
                    player.sendPacket(SystemMsg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
                    return -128;
                }
                incReputation(-5000, false, "SubunitCreate");
                break;
            }
            case 1001:
            case 1002:
            case 2001:
            case 2002: {
                if (getReputationScore() < 10000) {
                    player.sendPacket(SystemMsg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
                    return -128;
                }
                incReputation(-10000, false, "SubunitCreate");
                break;
            }
        }
        addSubUnit(new SubUnit(this, pledgeType, leader, name, false), true);
        return pledgeType;
    }

    public int getAvailablePledgeTypes(int pledgeType) {
        if (pledgeType == 0)
            return -128;
        if (_subUnits.get(pledgeType) != null)
            switch (pledgeType) {
                case -1: {
                    return -128;
                }
                case 100: {
                    pledgeType = getAvailablePledgeTypes(200);
                    break;
                }
                case 200: {
                    return -128;
                }
                case 1001: {
                    pledgeType = getAvailablePledgeTypes(1002);
                    break;
                }
                case 1002: {
                    pledgeType = getAvailablePledgeTypes(2001);
                    break;
                }
                case 2001: {
                    pledgeType = getAvailablePledgeTypes(2002);
                    break;
                }
                case 2002: {
                    return -128;
                }
            }
        return pledgeType;
    }

    private void restoreSubPledges() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM clan_subpledges WHERE clan_id=?");
            statement.setInt(1, getClanId());
            rset = statement.executeQuery();
            while (rset.next()) {
                int type = rset.getInt("type");
                int leaderId = rset.getInt("leader_id");
                String name = rset.getString("name");
                SubUnit pledge = new SubUnit(this, type, leaderId, name);
                pledge.setUpgraded(rset.getBoolean("upgraded"), false);
                addSubUnit(pledge, false);
            }
        } catch (Exception e) {
            _log.warn("Could not restore clan SubPledges: " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public int getClanMembersLimit() {
        int limit = 0;
        for (SubUnit su : getAllSubUnits())
            limit += getSubPledgeLimit(su.getType());
        return limit;
    }

    public int getSubPledgeLimit(int pledgeType) {
        int limit = 0;
        SubUnit subUnit = _subUnits.get(pledgeType);
        Label_0230:
        {
            switch (pledgeType) {
                case 0: {
                    switch (_level) {
                        case 0: {
                            limit = 10;
                            break Label_0230;
                        }
                        case 1: {
                            limit = 15;
                            break Label_0230;
                        }
                        case 2: {
                            limit = 20;
                            break Label_0230;
                        }
                        case 3: {
                            limit = 30;
                            break Label_0230;
                        }
                        default: {
                            limit = 40;
                            break Label_0230;
                        }
                    }
                }
                case -1: {
                    limit = 20;
                    break;
                }
                case 100:
                case 200: {
                    if (subUnit != null && subUnit.isUpgraded()) {
                        limit = 30;
                        break;
                    }
                    limit = 20;
                    break;
                }
                case 1001:
                case 1002: {
                    if (subUnit != null && subUnit.isUpgraded()) {
                        limit = 25;
                        break;
                    }
                    limit = 10;
                    break;
                }
                case 2001:
                case 2002: {
                    if (subUnit != null && subUnit.isUpgraded()) {
                        limit = 25;
                        break;
                    }
                    limit = 10;
                    break;
                }
            }
        }
        return limit;
    }

    public int getUnitMembersSize(int pledgeType) {
        if (pledgeType == -128 || !_subUnits.containsKey(pledgeType))
            return 0;
        return getSubUnit(pledgeType).size();
    }

    private void restoreRankPrivs() {
        if (_privs == null)
            initializePrivs();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM clan_privs WHERE clan_id=?");
            statement.setInt(1, getClanId());
            rset = statement.executeQuery();
            while (rset.next()) {
                int rank = rset.getInt("rank");
                int privileges = rset.getInt("privilleges");
                RankPrivs p = _privs.get(rank);
                if (p != null)
                    p.setPrivs(privileges);
                else
                    _log.warn("Invalid rank value (" + rank + "), please check clan_privs table");
            }
        } catch (Exception e) {
            _log.warn("Could not restore clan privs by rank: " + e);
            _log.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public void initializePrivs() {
        for (int i = 1; i <= 9; ++i)
            _privs.put(i, new RankPrivs(i, 0, 0));
    }

    public void updatePrivsForRank(int rank) {
        for (UnitMember member : this)
            if (member.isOnline() && member.getPlayer() != null && member.getPlayer().getPowerGrade() == rank) {
                if (member.getPlayer().isClanLeader())
                    continue;
                member.getPlayer().sendUserInfo();
            }
    }

    public RankPrivs getRankPrivs(int rank) {
        if (rank < 1 || rank > 9) {
            _log.warn("Requested invalid rank value: " + rank);
            Thread.dumpStack();
            return null;
        }
        if (_privs.get(rank) == null) {
            _log.warn("Request of rank before init: " + rank);
            Thread.dumpStack();
            setRankPrivs(rank, 0);
        }
        return _privs.get(rank);
    }

    public int countMembersByRank(int rank) {
        int ret = 0;
        for (UnitMember m : this)
            if (m.getPowerGrade() == rank)
                ++ret;
        return ret;
    }

    public void setRankPrivs(int rank, int privs) {
        if (rank < 1 || rank > 9) {
            _log.warn("Requested set of invalid rank value: " + rank);
            Thread.dumpStack();
            return;
        }
        if (_privs.get(rank) != null)
            _privs.get(rank).setPrivs(privs);
        else
            _privs.put(rank, new RankPrivs(rank, countMembersByRank(rank), privs));
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("REPLACE INTO clan_privs (clan_id,rank,privilleges) VALUES (?,?,?)");
            statement.setInt(1, getClanId());
            statement.setInt(2, rank);
            statement.setInt(3, privs);
            statement.execute();
        } catch (Exception e) {
            _log.warn("Could not store clan privs for rank: " + e);
            _log.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public RankPrivs[] getAllRankPrivs() {
        if (_privs == null)
            return new RankPrivs[0];
        return _privs.values().toArray(new RankPrivs[0]);
    }

    public int getWhBonus() {
        return _whBonus;
    }

    public void setWhBonus(int i) {
        if (_whBonus != -1)
            mysql.set("UPDATE `clan_data` SET `warehouse`=? WHERE `clan_id`=?", i, getClanId());
        _whBonus = i;
    }

    public Collection<SubUnit> getAllSubUnits() {
        return _subUnits.values();
    }

    public List<IBroadcastPacket> listAll() {
        List<IBroadcastPacket> p = new ArrayList<>(_subUnits.size());
        for (SubUnit unit : getAllSubUnits())
            p.add(new PledgeShowMemberListAllPacket(this, unit));
        return p;
    }

    public String getNotice() {
        return _notice;
    }

    public void setNotice(String notice) {
        _notice = notice;
    }

    public int getSkillLevel(int id, int def) {
        SkillEntry skillEntry = _skills.get(id);
        return skillEntry == null ? def : skillEntry.getLevel();
    }

    public int getSkillLevel(int id) {
        return getSkillLevel(id, -1);
    }

    public String getDesc() {
        return _desc;
    }

    public void setDesc(String desc) {
        _desc = desc;
    }

    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        _title = title;
    }

    @Override
    public Iterator<UnitMember> iterator() {
        List<Iterator<UnitMember>> iterators = _subUnits.values().stream()
                .map(subUnit -> subUnit.getUnitMembers().iterator())
                .collect(Collectors.toCollection(() -> new ArrayList<>(_subUnits.size())));
        return Iterators.concat(iterators.iterator());
    }

    public int getAcademyGraduatesCount() {
        return _academyGraduatesCount;
    }

    public void setAcademyGraduatesCount(int val) {
        _academyGraduatesCount = val;
    }

    public void loginClanCond(Player player, boolean login) {
        if (login) {
            SubUnit subUnit = player.getSubUnit();
            if (subUnit == null)
                return;
            UnitMember member = subUnit.getUnitMember(player.getObjectId());
            if (member == null)
                return;
            member.setPlayerInstance(player, false);
            int sponsor = player.getSponsor();
            int apprentice = player.getApprentice();
            L2GameServerPacket msg = new SystemMessagePacket(SystemMsg.CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME).addName(player);
            PledgeShowMemberListUpdatePacket memberUpdate = new PledgeShowMemberListUpdatePacket(player);
            for (Player clanMember : getOnlineMembers(player.getObjectId())) {
                clanMember.sendPacket(memberUpdate);
                if (clanMember.getObjectId() == sponsor)
                    clanMember.sendPacket(new SystemMessagePacket(SystemMsg.YOUR_APPRENTICE_C1_HAS_LOGGED_OUT).addName(player));
                else if (clanMember.getObjectId() == apprentice)
                    clanMember.sendPacket(new SystemMessagePacket(SystemMsg.YOUR_SPONSOR_C1_HAS_LOGGED_IN).addName(player));
                else
                    clanMember.sendPacket(msg);
            }
            if (player.isClanLeader()) {
                if (getLevel() >= 5)
                    for (Player clanMember : getOnlineMembers(0))
                        CLAN_REBIRTH_SKILL.getEffects(player, clanMember);
                AuctionClanHall clanHall = getHasHideout() != 0 ? ResidenceHolder.getInstance().getResidence(AuctionClanHall.class, getHasHideout()) : null;
                if (clanHall == null || clanHall.getAuctionLength() != 0)
                    return;
                if (clanHall.getSiegeEvent().getClass() != ClanHallAuctionEvent.class)
                    return;
                if (getWarehouse().getCountOf(clanHall.getFeeItemId()) < clanHall.getRentalFee())
                    player.sendPacket(new SystemMessagePacket(SystemMsg.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_ME_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW).addNumber(clanHall.getRentalFee()));
            } else if (getLevel() >= 5 && getLeader().isOnline())
                CLAN_REBIRTH_SKILL.getEffects(getLeader().getPlayer(), player);
        } else if (player.isClanLeader())
            for (Player clanMember2 : getOnlineMembers(player.getObjectId()))
                clanMember2.getAbnormalList().stopEffects(CLAN_REBIRTH_SKILL);
        ExPledgeCount pledgeCount = new ExPledgeCount(getOnlineMembersCount(login ? 0 : player.getObjectId()));
        for (Player clanMember3 : getOnlineMembers(login ? 0 : player.getObjectId()))
            clanMember3.sendPacket(pledgeCount);
    }

    public void onLevelChange(int oldLevel, int newLevel) {
        if (getLeader().isOnline()) {
            Player clanLeader = getLeader().getPlayer();
            if (oldLevel < 3 && newLevel >= 3)
                SiegeUtils.addSiegeSkills(clanLeader);
            if (newLevel == 3)
                clanLeader.sendPacket(SystemMsg.NOW_THAT_YOUR_CLAN_LEVEL_IS_ABOVE_LEVEL_5_IT_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS);
            if (newLevel > oldLevel && oldLevel < 5 && newLevel >= 5)
                for (Player clanMember : getOnlineMembers(0))
                    CLAN_REBIRTH_SKILL.getEffects(clanLeader, clanMember);
            else if (newLevel < 5 && oldLevel >= 5)
                for (Player member : getOnlineMembers(0))
                    member.getAbnormalList().stopEffects(CLAN_REBIRTH_SKILL);
        }
        PledgeShowInfoUpdatePacket pu = new PledgeShowInfoUpdatePacket(this);
        PledgeStatusChangedPacket ps = new PledgeStatusChangedPacket(this);
        for (Player member2 : getOnlineMembers(0)) {
            member2.updatePledgeRank();
            member2.sendPacket(SystemMsg.YOUR_CLANS_LEVEL_HAS_INCREASED, pu, ps);
            member2.broadcastUserInfo(true);
        }
    }

    public void onEnterClan(Player player) {
        if (getLevel() >= 5 && getLeader().isOnline())
            CLAN_REBIRTH_SKILL.getEffects(getLeader().getPlayer(), player);
        ExPledgeCount pledgeCount = new ExPledgeCount(getOnlineMembersCount(0));
        for (Player clanMember : getOnlineMembers(0))
            clanMember.sendPacket(pledgeCount);
        ClanSearchManager.getInstance().removeApplicant(getClanId(), player.getObjectId());
    }

    public void onLeaveClan(Player player) {
        int playerId = 0;
        if (player != null) {
            playerId = player.getObjectId();
            player.sendPacket(new ExPledgeCount(0));
            player.getAbnormalList().stopEffects(CLAN_REBIRTH_SKILL);
        }
        ExPledgeCount pledgeCount = new ExPledgeCount(getOnlineMembersCount(playerId));
        for (Player clanMember : getOnlineMembers(playerId))
            clanMember.sendPacket(pledgeCount);
    }

    public boolean isSpecialEffect(Skill skill) {
        return getLevel() >= 5 && getLeader().isOnline() && CLAN_REBIRTH_SKILL.getId() == skill.getId();
    }

    public boolean checkJoinPledgeCondition(Player player, int pledgeType) {
        if (pledgeType == -1) {
            if (player.isAcademyGraduated())
                return false;
            if (player.getLevel() >= 85)
                return false;
        }

        if (getLeader().getFraction().canAttack(player.getFraction()) || player.isMercenary())
            return false;

        return true;
    }

    public boolean joinInPledge(Player player, int pledgeType) {
        player.sendPacket(new JoinPledgePacket(getClanId()));
        SubUnit subUnit = getSubUnit(pledgeType);
        if (subUnit == null)
            return false;
        UnitMember member = new UnitMember(this, player.getName(), player.getTitle(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), pledgeType, player.getPowerGrade(), player.getApprentice(), player.getSex().ordinal(), -128, 2, player.getFraction());
        subUnit.addUnitMember(member);
        player.setPledgeType(pledgeType);
        player.setClan(this);
        player.setVar("isNewClanMember", true);
        member.setPlayerInstance(player, false);
        if (pledgeType == -1)
            player.setLvlJoinedAcademy(player.getLevel());
        member.setPowerGrade(getAffiliationRank(player.getPledgeType()));
        broadcastToOtherOnlineMembers(new PledgeShowMemberListAddPacket(member), player);
        broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.S1_HAS_JOINED_THE_CLAN).addString(player.getName()), new PledgeShowInfoUpdatePacket(this));
        player.sendPacket(SystemMsg.ENTERED_THE_CLAN);
        player.sendPacket(player.getClan().listAll());
        player.setLeaveClanTime(0L);
        player.updatePledgeRank();
        addSkillsQuietly(player);
        player.sendPacket(new PledgeSkillListPacket(this));
        player.sendSkillList();
        EventHolder.getInstance().findEvent(player);
        player.broadcastCharInfo();
        onEnterClan(player);
        player.store(false);
        return true;
    }

    public int getCastleDefendCount() {
        return _castleDefendCount;
    }

    public void setCastleDefendCount(int castleDefendCount) {
        _castleDefendCount = castleDefendCount;
    }

    public boolean isPlacedForDisband() {
        return _disbandEndTime != 0L;
    }

    public void placeForDisband() {
        _disbandEndTime = DISBAND_TIME_PATTERN.next(System.currentTimeMillis());
        updateClanInDB();
    }

    public void unPlaceDisband() {
        _disbandEndTime = 0L;
        _disbandPenaltyTime = System.currentTimeMillis() + DISBAND_PENALTY;
        updateClanInDB();
    }

    public long getDisbandEndTime() {
        return _disbandEndTime;
    }

    public void setDisbandEndTime(long disbandEndTime) {
        _disbandEndTime = disbandEndTime;
    }

    public long getDisbandPenaltyTime() {
        return _disbandPenaltyTime;
    }

    public void setDisbandPenaltyTime(long disbandPenaltyTime) {
        _disbandPenaltyTime = disbandPenaltyTime;
    }

    private static class ClanReputationComparator implements Comparator<Clan> {
        @Override
        public int compare(Clan o1, Clan o2) {
            if (o1 == null || o2 == null)
                return 0;
            return o2.getReputationScore() - o1.getReputationScore();
        }
    }

    public Collection<ClanWar> getOpposingWarList() {
        List<ClanWar> cw = new ArrayList<>();
        for (ClanWar w : getClanWars())
            if (w.isAttacker(this))
                cw.add(w);

        return cw;
    }

    public int getHasFortress() {
        return _hasFortres;
    }

    public void setHasFortress(int id) {
        _hasFortres = id;
    }

    public Fraction getFraction() {
        return getLeader().getFraction();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Clan))
            return false;
        Clan that = (Clan) o;
        return _clanId == that._clanId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_clanId);
    }
}

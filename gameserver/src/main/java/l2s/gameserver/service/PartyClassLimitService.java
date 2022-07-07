package l2s.gameserver.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import l2s.gameserver.Config;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.network.l2.components.CustomMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KRonst
 */
public class PartyClassLimitService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyClassLimitService.class);
    private static final PartyClassLimitService INSTANCE = new PartyClassLimitService();

    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, List<ClassId>> groups = new HashMap<>();
    private final Map<String, Integer> limits = new HashMap<>();

    private PartyClassLimitService() {

    }

    public static PartyClassLimitService getInstance() {
        return INSTANCE;
    }

    public void init() {
        Arrays.stream(Config.GVE_PARTY_CLASS_LIMITS_GROUPS).forEach(group -> {
            String[] groupInfo = group.split(":");
            if (groupInfo.length == 3) {
                try {
                    String groupName = groupInfo[0];
                    List<ClassId> classes = Arrays.stream(groupInfo[1].split(","))
                        .map(id -> ClassId.valueOf(Integer.parseInt(id)).orElse(null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                    Integer limit = Integer.parseInt(groupInfo[2]);
                    groups.put(groupName, classes);
                    limits.put(groupName, limit);
                } catch (NumberFormatException e) {
                    LOGGER.error("Can't parse party limit group for group=" + groupInfo[0] + ", classes=" + groupInfo[1] + ", limit=" + groupInfo[2], e);
                }

            } else {
                LOGGER.error("Error party limit group initialization. Limit info length=" + groupInfo.length + ", expected=3");
            }
        });
    }

    public void checkKickFromParty(Player player, int id) {
        Party party = player.getParty();
        if (party == null) {
            return;
        }
        ClassId classId = ClassId.valueOf(id).orElse(null);
        if (classId == null) {
            return;
        }
        String group = findGroup(classId);
        if (group == null) {
            return;
        }

        lock.lock();
        try {
            if (limitReached(party, classId)) {
                Integer limit = limits.get(group);
                party.removePartyMember(player, true);
                CustomMessage messageToPlayer = new CustomMessage("services.party.limit.kick.player").addNumber(limit);
                player.sendMessage(messageToPlayer);
                party.broadcastCustomMessageToPartyMembers("services.party.limit.kick.party", player.getName(), String.valueOf(limit));
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean canJoin(Player requester, Player player) {
        if (requester == null || player == null) {
            return false;
        }
        if (sameClassWithLimit(requester, player)) {
            return false;
        }
        Party party = requester.getParty();
        if (party == null) {
            return true;
        }
        lock.lock();
        try {
            return !limitReached(party, player.getClassId());
        } finally {
            lock.unlock();
        }
    }

    public int getLimit(ClassId classId) {
        String group = findGroup(classId);
        if (group == null) {
            return -1;
        }
        Integer limit = limits.get(group);
        return limit == null ? -1 : limit;
    }

    private boolean sameClassWithLimit(Player requester, Player player) {
        final String requesterGroup = findGroup(requester.getClassId());
        final String playerGroup = findGroup(player.getClassId());
        if (requesterGroup == null || playerGroup == null) {
            return false;
        }
        return requesterGroup.equalsIgnoreCase(playerGroup) && getLimit(requester.getClassId()) == 1;
    }

    private String findGroup(ClassId classId) {
        Entry<String, List<ClassId>> groupEntry = groups.entrySet()
            .stream()
            .filter(e -> e.getValue().contains(classId))
            .findFirst()
            .orElse(null);
        return groupEntry == null ? null : groupEntry.getKey();
    }

    private boolean limitReached(Party party, ClassId classId) {
        int limit = getLimit(classId);
        if (limit == -1) {
            return false;
        }
        long members = getMembersWithGroupByClass(party, classId);
        if (members == 0) {
            return false;
        }
        return members >= limit;
    }

    private long getMembersWithGroupByClass(Party party, ClassId classId) {
        String group = findGroup(classId);
        if (group == null) {
            return 0;
        }
        List<ClassId> classes = groups.get(group);

        return party.getPartyMembers().stream().filter(m -> classes.contains(m.getClassId())).count();
    }
}

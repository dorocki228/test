package l2s.gameserver.service;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Party;
import l2s.gameserver.skills.SkillEntry;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

public class PartyService {
    private static final PartyService INSTANCE = new PartyService();

    private final SkillEntry buffPartySkill1;
    private final SkillEntry buffPartySkill2;
    private final SkillEntry buffPartySkill3;

    private final Collection<Party> parties = new CopyOnWriteArraySet<>();

    private PartyService() {
        buffPartySkill1 = SkillHolder.getInstance().getSkillEntry(56057, 1);
        buffPartySkill2 = SkillHolder.getInstance().getSkillEntry(56057, 2);
        buffPartySkill3 = SkillHolder.getInstance().getSkillEntry(56057, 3);
    }

    public static PartyService getInstance() {
        return INSTANCE;
    }

    public void init() {
        // TODO: 17.07.2018 Нужно ли будет отключать систему?
        startBuffTask();
    }

    private void startBuffTask() {
        ThreadPoolManager.getInstance().scheduleAtFixedDelay(() -> {
            final long currentTime = System.currentTimeMillis();
            final long delayMillis = Duration.ofMinutes(5).toMillis();
            getParties().stream().
                    filter(p -> (p.getLastBuff() + delayMillis) <= currentTime).
                    distinct().
                    forEach(p -> {
                        final int memberCount = p.getMemberCount();
                        if(memberCount == 2)
                            partyBuff(p, buffPartySkill1);
                        else if(memberCount == 3)
                            partyBuff(p, buffPartySkill2);
                        else if(memberCount >= 4 && memberCount <= 5)
                            partyBuff(p, buffPartySkill3);
                    });
        }, 60, 60, TimeUnit.SECONDS);
    }

    private void partyBuff(Party party, SkillEntry entry) {
        if(entry == null)
            return;
        party.setLastBuff(System.currentTimeMillis());
        party.getPartyMembers().forEach(p -> entry.getEffects(p, p));
    }

    public void addParty(Party party)
    {
        parties.add(party);
    }

    public void removeParty(Party party)
    {
        parties.remove(party);
    }

    public Collection<Party> getParties()
    {
        return Collections.unmodifiableCollection(parties);
    }
}

package l2s.gameserver.model.instances.residences.farm;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.component.farm.GatheringTemplate;
import l2s.gameserver.component.farm.Harvest;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.SteadDataHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.farm.Stead;
import l2s.gameserver.model.farm.SteadBarnManager;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

public class SeedInstance extends NpcInstance {
    private static final Logger _log = LoggerFactory.getLogger(SeedInstance.class);
    static final String SCARECROW_VAR = "scarecrow_upgrade";
    static final String HAVE_SLAVE = "have_slave";
    static final String HAVE_BARN = "have_barn";
    private int owner;
    private int state;
    private long startTime;
    private long finishTime;
    private boolean slave;
    private boolean barn;
    private boolean water;
    private Stead stead;
    private GatheringTemplate gathering;
    private AtomicBoolean pest = new AtomicBoolean(false);
    private AtomicBoolean defective = new AtomicBoolean(false);

    public SeedInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
    }

    @Override
    public void onBypassFeedback(Player player, String command) {
        if (player.getObjectId() != owner) {
            _log.warn("Player talk with not owned seed " + player);
            return;
        }

        StringTokenizer tokenizer = new StringTokenizer(command, " ");
        String comm = tokenizer.nextToken();

        if ("remove".equalsIgnoreCase(comm)) {
            stead.getSeeds(player).remove(this);
            deleteMe();
        } else if ("take".equalsIgnoreCase(comm)) {
            final float progression = calcProgression();
            if (progression < 100) {
                _log.warn("Player " + player + " try get reward from unripe seed");
                return;
            } else if (defective.get()) // С гнилого семячка лута нету
                return;

            final String type = tokenizer.nextToken();
            if ("crops".equalsIgnoreCase(type)) {
                gathering.getCrops().forEach(crop -> {
                    int calc = (int) (crop.getMax() * (Math.min(progression, 150) / 100));
                    if (calc >= 1 && Rnd.chance(crop.getChance())) {
                        ItemFunctions.addItem(player, crop.getId(), Rnd.get(crop.getMin(), calc));
                        stead.getSeeds(player).remove(this);
                        deleteMe();
                    }
                });
            } else if ("seeds".equalsIgnoreCase(type)) {
                gathering.getSeeds().forEach(seed -> {
                    int calc = (int) (seed.getMax() * (Math.min(calcProgression(), 150) / 100));
                    if (calc >= 1 && Rnd.chance(seed.getChance())) {
                        ItemFunctions.addItem(player, seed.getId(), Rnd.get(seed.getMin(), calc));
                        stead.getSeeds(player).remove(this);
                        deleteMe();
                    }
                });
            } else
                _log.warn("Unknown command type " + command + ", from player " + player);
        } else if ("pest".equalsIgnoreCase(comm))
            removePest();
        else
            super.onBypassFeedback(player, command);
    }

    @Override
    public int getDisplayId() {
        final float progress = calcProgression();
        if (progress < 100)
            return gathering.getModel(0);
        else if (progress < 150 && !defective.get())
            return gathering.getModel(1);
        else if (defective.get())
            return gathering.getModel(2);
        else if (progress >= 150)
            return gathering.getModel(3);
        else
            return super.getDisplayId();
    }

    @Override
    public void showChatWindow(Player player, int val, boolean firstTalk, Object... arg) {
        HtmlMessage html = new HtmlMessage(this, "stead/seed.htm");
        html.addVar("time", TimeUtils.dateTimeFormat(Instant.ofEpochSecond(finishTime)));
        html.addVar("progress", ((int) (calcProgression() * Math.pow(10, 2))) / Math.pow(10, 2));
        html.addVar("slave", slave);
        html.addVar("slave_expire", TimeUtils.dateTimeFormat(Instant.ofEpochMilli(player.getVarBoolean(HAVE_SLAVE) ? player.getVarExpireTime(HAVE_SLAVE) : System.currentTimeMillis()))); // Если переменная истекла выводим текущее время для избежания вывода 70го года....
        html.addVar("barn", barn);
        html.addVar("gathering", gathering);
        html.addVar("pest", pest);
        html.addVar("finishTime", finishTime);
        html.addVar("defective", defective.get());
        html.addVar("water", water);
        html.addVar("seed", ItemHolder.getInstance().getTemplate(gathering.getId()));

        player.sendPacket(html.setPlayVoice(firstTalk));
    }

    public void removeMe(final boolean shutdown) {
        if (barn || shutdown) {
            final float progression = Math.min(calcProgression(), 150);
            gathering.getCrops().forEach(crop -> {
                int calc = (int) (crop.getMax() * (progression / 100));
                if (calc >= 1 && Rnd.chance(crop.getChance())) {
                    int count = Rnd.get((int) (crop.getMin() * (progression / 100)), calc);
                    Harvest harvest = new Harvest(crop.getId(), count, owner, true);
                    SteadBarnManager.getInstance().addHarvest(harvest);
                }
            });
        }

        Player player = GameObjectsStorage.getPlayer(owner);
        if (player != null) {
            stead.getSeeds(player).remove(this);
        }
        deleteMe();
    }

    public void setStead(Stead stead) {
        this.stead = stead;
    }

    public Stead getStead() {
        return stead;
    }

    public float calcProgression() {
        return (float) (((System.currentTimeMillis() / 1000) - startTime) * 100F / (finishTime - startTime));
    }

    public void init(Player player, GatheringTemplate template) {
        gathering = template;
        owner = player.getObjectId();
        slave = player.getVarBoolean(HAVE_SLAVE);
        barn = player.getVarBoolean(HAVE_BARN);

        if (player.getVarBoolean(SCARECROW_VAR))
            useScarecrow();
        startTime = System.currentTimeMillis() / 1000L;
        finishTime = startTime + gathering.getMaturationTime();
    }

    public boolean isDried() {
        return !water;
    }

    public void pourWater() {
        if (!water) {
            water = true;
            recalcTime(SteadDataHolder.getInstance().getConfiguration().getDouble("water_time_mul"));
        }
    }

    public void useScarecrow() {
        recalcTime(SteadDataHolder.getInstance().getConfiguration().getDouble("scarecrow_time_mul"));
    }

    public void updateSlave(boolean slave) {
        this.slave = slave;
    }

    public void updateBarn(boolean barn) {
        this.barn = barn;
    }

    public void tick() {
        if (slave && Rnd.chance(SteadDataHolder.getInstance().getConfiguration().getDouble("slave_remove_pest_chance_by_tick")) && pest.get())  // Рабочий убирает насеомых с шансом
            removePest();

        final float progress = calcProgression();
        if (Rnd.chance(SteadDataHolder.getInstance().getConfiguration().getDouble("pest_chance_by_tick")) && progress < 100) {
            if (pest.compareAndSet(false, true)) {
                recalcTime(SteadDataHolder.getInstance().getConfiguration().getDouble("pest_time_increase_by_tick"));
                startAbnormalEffect(AbnormalEffect.DOT_POISON);
            }
        }

        if (progress <= 150 && progress >= 100 && Rnd.chance(SteadDataHolder.getInstance().getConfiguration().getDouble("defective_chance_by_tick")) && defective.compareAndSet(false, true)) {
            stopAbnormalEffect(AbnormalEffect.AURA_BUFF);
            startAbnormalEffect(AbnormalEffect.AURA_DEBUFF);
        }

        if (progress >= 100 && !getAbnormalEffects().contains(AbnormalEffect.AURA_BUFF))
            startAbnormalEffect(AbnormalEffect.AURA_BUFF);

        if (progress >= 100 && state == 0 && !defective.get()) // Плод вырос
            changeState(1);
        else if (progress >= 100 && state == 1 && defective.get()) // Плод сгнил
            changeState(2);
        else if (progress >= 150 && state == 1 && !defective.get()) // Плод достиг высшого сорта
        {
            removePest();
            changeState(3);
        }
    }

    private void changeState(final int state) {
        this.state = state;
        decayMe();
        spawnMe();
    }

    private void removePest() {
        if (pest.compareAndSet(true, false)) {
            recalcTime(SteadDataHolder.getInstance().getConfiguration().getDouble("pest_time_decrease"));
            stopAbnormalEffect(AbnormalEffect.DOT_POISON);
        }
    }

    private void recalcTime(double mul) {
        final long now = System.currentTimeMillis() / 1000L;
        finishTime = (long) (now + (finishTime - now) * mul);
    }

    // TODO: подключить к ПРЕМИУМ-УСЛУГЕ оповещение о росте урожая
    private void notifyOwner(String message) {
        Player player = GameObjectsStorage.getPlayer(owner);
        if (player != null) {
            player.sendPacket(new ExShowScreenMessage(message, 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false));
            player.sendMessage(message);
        }
    }

    @Override
    public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper) {
        if (owner != forPlayer.getObjectId())
            return Collections.emptyList();
        return super.addPacketList(forPlayer, dropper);
    }
}
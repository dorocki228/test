package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.RadarControlPacket;
import l2s.gameserver.service.FractionService;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public class FractionManagerInstance extends NpcInstance
{
    private final Location FIRE_START_LOC = new Location(82488, 53688, -1488);
    private final Location WATER_START_LOC = new Location(45512, 48504, -3064);

    public FractionManagerInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
    {
        super(objectId, template, set);
    }

    @Override
    public void onBypassFeedback(Player player, String command)
    {
        if(command.startsWith("info"))
        {
            Location loc = null;
            if(command.endsWith("water"))
                loc = new Location(-53863, 181603, -4552);
            else if(command.endsWith("fire"))
                loc = new Location(-56170, 179324, -4808);

            player.sendPacket(new RadarControlPacket(0, 2, loc));
        }
        else if(command.startsWith("choose"))
        {
            Fraction fraction;
            if(command.endsWith("water"))
            {
                fraction = Fraction.WATER;
            }
            else if(command.endsWith("fire"))
            {
                fraction = Fraction.FIRE;
            }
            else
            {
                showChatWindow(player, "gve/start/start_stone.htm", false);
                return;
            }

            int percentage = FractionService.getInstance().getFractionPlayersCountPercentage(fraction);
            if(percentage >= 55)
            {
                int firePer = fraction == Fraction.FIRE ? percentage : 100 - percentage;
                int waterPer = fraction == Fraction.WATER ? percentage : 100 - percentage;
                showChatWindow(player, "gve/start/need_balance.htm", false,
                        "%fire%", String.valueOf(firePer), "%water%", String.valueOf(waterPer));
                return;
            }

            if(fraction == Fraction.WATER)
            {
                Location loc = Location.findAroundPosition(WATER_START_LOC, 100, player.getGeoIndex());
                player.changeFraction(Fraction.WATER);

                player.teleToLocation(loc);
                
            		if (!player.isPhantom() && player.tScheme_record.isLogging())
            		{
            			player.tScheme_record.setTeleport(WATER_START_LOC);
            			player.tScheme_record.setFraction(Fraction.WATER);
            		}
            }
            else if(fraction == Fraction.FIRE)
            {
                Location loc = Location.findAroundPosition(FIRE_START_LOC, 100, player.getGeoIndex());
                player.changeFraction(Fraction.FIRE);

                player.teleToLocation(loc);
                
            		if (!player.isPhantom() && player.tScheme_record.isLogging())
            		{
            			player.tScheme_record.setTeleport(FIRE_START_LOC);
            			player.tScheme_record.setFraction(Fraction.FIRE);
            		}
            }

            Quest t = QuestHolder.getInstance().getQuest(999);
            QuestState st = player.getQuestState(t);
            if(st == null)
            {
                t.newQuestState(player);
                st = player.getQuestState(t);
            }

            String res;
            try
            {
                res = t.onTutorialEvent("EW", "", st);
            }
            catch(Exception e)
            {
                t.showError(st.getPlayer(), e);
                return;
            }

            t.showTutorialResult(st.getPlayer(), res);
        }
        else
            super.onBypassFeedback(player, command);
    }

    @Override
    public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
    {
        String html = "start_stone.htm";
        switch(getNpcId())
        {
            case 40002:
                html = "start_fire.htm";
                break;
            case 40003:
                html = "start_water.htm";
                break;
        }

        int firePer = FractionService.getInstance().getFractionPlayersCountPercentage(Fraction.FIRE);
        int waterPer = FractionService.getInstance().getFractionPlayersCountPercentage(Fraction.WATER);

        showChatWindow(player, "gve/start/" + html, firstTalk,
                "%fire%", String.valueOf(firePer), "%water%", String.valueOf(waterPer));
    }
}

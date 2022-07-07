package npc.model.residences.castle;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.dao.CastleDamageZoneDAO;
import l2s.gameserver.dao.CastleDoorUpgradeDAO;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.CastleDamageZoneObject;
import l2s.gameserver.model.entity.events.objects.DoorObject;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.Privilege;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.c2s.RequestSetCrop;
import l2s.gameserver.network.l2.c2s.RequestSetSeed;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.CastleSiegeInfoPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.ReflectionUtils;
import l2s.gameserver.utils.Util;
import npc.model.residences.ResidenceManager;
import org.apache.logging.log4j.message.ParameterizedMessage;

import java.util.List;
import java.util.StringTokenizer;

public class ChamberlainInstance extends ResidenceManager {
    private static final long serialVersionUID = 1L;

    public ChamberlainInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
    }

    @Override
    protected void setDialogs() {
        _mainDialog = "castle/chamberlain/chamberlain.htm";
        _failDialog = "castle/chamberlain/chamberlain-notlord.htm";
        _siegeDialog = "castle/chamberlain/chamberlain-busy.htm";
    }

    @Override
    public void onBypassFeedback(Player player, String command) {
        int condition = getCond(player);

        if (condition != COND_OWNER)
            return;

        StringTokenizer st = new StringTokenizer(command, " ");
        String actualCommand = st.nextToken();
        String val = "";
        if (st.countTokens() >= 1)
            val = st.nextToken();

        Castle castle = getCastle();

        boolean isInSiege = castle.getSiegeEvent().isInProgress();

        if (isInSiege && (!"functions".equals(actualCommand) && !actualCommand.startsWith("teleport") && !"support".equals(actualCommand) || !castle.getSiegeEvent().hasState(4))) {
            HtmlMessage html = new HtmlMessage(this);
            html.setFile(_siegeDialog);
            player.sendPacket(html);
            return;
        }

        if ("viewSiegeInfo".equalsIgnoreCase(actualCommand)) {
            if (!isHaveRigths(player, Clan.CP_CS_MANAGE_SIEGE)) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            player.sendPacket(new CastleSiegeInfoPacket(castle, player));
        } else if ("ManageTreasure".equalsIgnoreCase(actualCommand)) {
            if (!player.isClanLeader()) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            HtmlMessage html = new HtmlMessage(this);
            html.setFile("castle/chamberlain/chamberlain-castlevault.htm");
            html.replace("%Treasure%", String.valueOf(castle.getTreasury()));
            html.replace("%CollectedShops%", String.valueOf(castle.getCollectedShops()));
            html.replace("%CollectedSeed%", String.valueOf(castle.getCollectedSeed()));
            player.sendPacket(html);
        } else if ("TakeTreasure".equalsIgnoreCase(actualCommand)) {
            if (!player.isClanLeader()) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
//			if(!val.isEmpty())
//			{
//				long treasure = Long.parseLong(val);
//				if(castle.getTreasury() < treasure)
//				{
//					HtmlMessage html = new HtmlMessage(this);
//					html.setFile("castle/chamberlain/chamberlain-havenottreasure.htm");
//					html.replace("%Treasure%", String.valueOf(castle.getTreasury()));
//					html.replace("%Requested%", String.valueOf(treasure));
//					player.sendPacket(html);
//					return;
//				}
//				if(treasure > 0)
//				{
//					castle.addToTreasuryNoTax(-treasure, false, false);
//
//					String messagePattern = "{}|{}|CastleChamberlain";
//					ParameterizedMessage message = new ParameterizedMessage(messagePattern, castle, -treasure);
//					LogService.getInstance().log(LoggerType.TREASURY, message);
//
//					player.addAdena(treasure);
//				}
//			}

            HtmlMessage html = new HtmlMessage(this);
            html.setFile("castle/chamberlain/chamberlain-castlevault.htm");
            html.replace("%Treasure%", String.valueOf(castle.getTreasury()));
            html.replace("%CollectedShops%", String.valueOf(castle.getCollectedShops()));
            html.replace("%CollectedSeed%", String.valueOf(castle.getCollectedSeed()));
            player.sendPacket(html);
        } else if ("PutTreasure".equalsIgnoreCase(actualCommand)) {
            if (!val.isEmpty()) {
                long treasure = Long.parseLong(val);
                if (treasure > player.getAdena()) {
                    player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                    return;
                }
                if (treasure > 0) {
                    castle.addToTreasuryNoTax(treasure, false, false);

                    String messagePattern = "{}|{}|CastleChamberlain";
                    ParameterizedMessage message = new ParameterizedMessage(messagePattern, castle, treasure);
                    LogService.getInstance().log(LoggerType.TREASURY, message);

                    player.reduceAdena(treasure, true);
                }
            }

            HtmlMessage html = new HtmlMessage(this);
            html.setFile("castle/chamberlain/chamberlain-castlevault.htm");
            html.replace("%Treasure%", String.valueOf(castle.getTreasury()));
            html.replace("%CollectedShops%", String.valueOf(castle.getCollectedShops()));
            html.replace("%CollectedSeed%", String.valueOf(castle.getCollectedSeed()));
            player.sendPacket(html);
        } else if ("operate_door".equalsIgnoreCase(actualCommand)) // door control
        {
            if (!isHaveRigths(player, Clan.CP_CS_ENTRY_EXIT)) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            if (castle.getSiegeEvent().isInProgress()) {
                showChatWindow(player, "residence2/castle/chamberlain_saius021.htm", false);
                return;
            }
            if (!val.isEmpty()) {
                boolean open = Integer.parseInt(val) == 1;
                while (st.hasMoreTokens()) {
                    DoorInstance door = ReflectionUtils.getDoor(Integer.parseInt(st.nextToken()));
                    if (open)
                        door.openMe(player, true);
                    else
                        door.closeMe(player, true);
                }
            }

            HtmlMessage html = new HtmlMessage(this);
            html.setFile("castle/chamberlain/" + getNpcId() + "-d.htm");
            player.sendPacket(html);
        } else if ("upgrade_castle".equalsIgnoreCase(actualCommand)) {
            if (!checkSiegeFunctions(player))
                return;

            showChatWindow(player, "castle/chamberlain/chamberlain-upgrades.htm", false);
        } else if ("reinforce".equalsIgnoreCase(actualCommand)) {
            if (!checkSiegeFunctions(player))
                return;

            HtmlMessage html = new HtmlMessage(this);
            html.setFile("castle/chamberlain/doorStrengthen-" + castle.getName() + ".htm");
            player.sendPacket(html);
        } else if ("trap_select".equalsIgnoreCase(actualCommand)) {
            if (!checkSiegeFunctions(player))
                return;

            HtmlMessage html = new HtmlMessage(this);
            if (val.isEmpty())
                html.setFile("castle/chamberlain/trap_select.htm");
            else {
                int stage = 1;
                List<String> boughtZones = castle.getSiegeEvent().getObjects(CastleSiegeEvent.BOUGHT_ZONES);

                for (String zoneName : boughtZones)
                    if (zoneName.startsWith(val))
                        stage = Integer.parseInt(zoneName.substring(val.length())) + 1;

                if (stage > 4) {
                    html.setFile("castle/chamberlain/trapAlready.htm");
                    player.sendPacket(html);
                    return;
                }

                List<CastleDamageZoneObject> objects = castle.getSiegeEvent().getObjects(val + stage);
                long price = 0;
                for (CastleDamageZoneObject o : objects)
                    price += o.getPrice();

                html.setFile("castle/chamberlain/trap_select_" + val + stage + ".htm");
                html.replace("%price%", "<font color=LEVEL>" + Util.formatAdena(price) + "</font> adena");
            }
            player.sendPacket(html);
        } else if ("trap_buy".equalsIgnoreCase(actualCommand)) {
            if (!checkSiegeFunctions(player))
                return;

            int stage = 1;
            List<String> boughtZones = castle.getSiegeEvent().getObjects(CastleSiegeEvent.BOUGHT_ZONES);

            for (String zoneName : boughtZones)
                if (zoneName.startsWith(val))
                    stage = Integer.parseInt(zoneName.substring(val.length())) + 1;

            if (stage > 4) {
                HtmlMessage html = new HtmlMessage(this);
                html.setFile("castle/chamberlain/trapAlready.htm");
                player.sendPacket(html);
                return;
            }

            List<CastleDamageZoneObject> objects = castle.getSiegeEvent().getObjects(val + stage);
            long price = 0;
            for (CastleDamageZoneObject o : objects)
                price += o.getPrice();

            if (player.getClan().getAdenaCount() < price) {
                player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                return;
            }

            player.getClan().getWarehouse().destroyItemByItemId(ItemTemplate.ITEM_ID_ADENA, price);

            castle.getSiegeEvent().removeObject(CastleSiegeEvent.BOUGHT_ZONES, val + (stage - 1));
            CastleDamageZoneDAO.getInstance().delete(castle, val + (stage - 1));

            castle.getSiegeEvent().addObject(CastleSiegeEvent.BOUGHT_ZONES, val + stage);
            CastleDamageZoneDAO.getInstance().insert(castle, val + stage);

            HtmlMessage html = new HtmlMessage(this);
            html.setFile("castle/chamberlain/trapSuccess.htm");
            player.sendPacket(html);
        } else if ("door_manage".equalsIgnoreCase(actualCommand)) {
            if (!isHaveRigths(player, Clan.CP_CS_ENTRY_EXIT)) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            if (castle.getSiegeEvent().isInProgress()) {
                showChatWindow(player, "residence2/castle/chamberlain_saius021.htm", false);
                return;
            }

            HtmlMessage html = new HtmlMessage(this);
            html.setFile("castle/chamberlain/doorManage.htm");
            html.replace("%id%", val);
            html.replace("%type%", st.nextToken());
            player.sendPacket(html);
        } else if ("upgrade_door_confirm".equalsIgnoreCase(actualCommand)) {
            if (!isHaveRigths(player, Clan.CP_CS_MANAGE_SIEGE)) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            int id = Integer.parseInt(val);
            int type = Integer.parseInt(st.nextToken());
            int level = Integer.parseInt(st.nextToken());
            long price = getDoorCost(type, level);

            HtmlMessage html = new HtmlMessage(this);
            html.setFile("castle/chamberlain/doorConfirm.htm");
            html.replace("%id%", String.valueOf(id));
            html.replace("%level%", String.valueOf(level));
            html.replace("%type%", String.valueOf(type));
            html.replace("%price%", String.valueOf(price));
            player.sendPacket(html);
        } else if ("upgrade_door".equalsIgnoreCase(actualCommand)) {
            if (checkSiegeFunctions(player))
                return;

            int id = Integer.parseInt(val);
            int type = Integer.parseInt(st.nextToken());
            int level = Integer.parseInt(st.nextToken());
            long price = getDoorCost(type, level);

            List<DoorObject> doorObjects = castle.getSiegeEvent().getObjects(SiegeEvent.DOORS);
            DoorObject targetDoorObject = null;
            for (DoorObject o : doorObjects)
                if (o.getUId() == id) {
                    targetDoorObject = o;
                    break;
                }

            DoorInstance door = targetDoorObject.getDoor();
            int upgradeHp = (door.getMaxHp() - door.getUpgradeHp()) * level - door.getMaxHp();

            if (price == 0 || upgradeHp < 0) {
                player.sendMessage(new CustomMessage("common.Error"));
                return;
            }

            if (door.getUpgradeHp() >= upgradeHp) {
                int oldLevel = door.getUpgradeHp() / (door.getMaxHp() - door.getUpgradeHp()) + 1;
                HtmlMessage html = new HtmlMessage(this);
                html.setFile("castle/chamberlain/doorAlready.htm");
                html.replace("%level%", String.valueOf(oldLevel));
                player.sendPacket(html);
                return;
            }

            if (player.getClan().getAdenaCount() < price) {
                player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                return;
            }

            player.getClan().getWarehouse().destroyItemByItemId(ItemTemplate.ITEM_ID_ADENA, price);

            targetDoorObject.setUpgradeValue(castle.<SiegeEvent>getSiegeEvent(), upgradeHp);
            CastleDoorUpgradeDAO.getInstance().insert(door.getDoorId(), upgradeHp);
        } else if ("report".equalsIgnoreCase(actualCommand)) // Report page
        {
            if (!isHaveRigths(player, Clan.CP_CS_USE_FUNCTIONS)) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }

            HtmlMessage html = new HtmlMessage(this);
            html.setFile("castle/chamberlain/chamberlain-report.htm");
            html.replace("%FeudName%", castle.getNpcStringName());
            html.replace("%CharClan%", player.getClan().getName());
            html.replace("%CharName%", player.getName());
            NpcString ssq_period = NpcString.PREPARATION;
            html.replace("%SSPeriod%", ssq_period);
            html.replace("%Avarice%", getSealOwner(1));
            html.replace("%Revelation%", getSealOwner(2));
            html.replace("%Strife%", getSealOwner(3));
            player.sendPacket(html);
        } else if ("Crown".equalsIgnoreCase(actualCommand)) // Give Crown to Castle Owner
        {
            if (!player.isClanLeader()) {
                player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            if (player.getInventory().getItemByItemId(6841) == null) {
                ItemFunctions.addItem(player, 6841, 1, true);

                HtmlMessage html = new HtmlMessage(this);
                html.setFile("castle/chamberlain/chamberlain-givecrown.htm");
                html.replace("%CharName%", player.getName());
                html.replace("%FeudName%", castle.getNpcStringName());
                player.sendPacket(html);
            } else {
                HtmlMessage html = new HtmlMessage(this);
                html.setFile("castle/chamberlain/alreadyhavecrown.htm");
                player.sendPacket(html);
            }
        } else if ("manageFunctions".equalsIgnoreCase(actualCommand)) {
            if (!player.hasPrivilege(Privilege.CS_FS_SET_FUNCTIONS))
                showChatWindow(player, "residence2/castle/chamberlain_saius063.htm", false);
            else
                showChatWindow(player, "residence2/castle/chamberlain_saius065.htm", false);
        } else if ("manageSiegeFunctions".equalsIgnoreCase(actualCommand)) {
            if (!player.hasPrivilege(Privilege.CS_FS_SET_FUNCTIONS))
                showChatWindow(player, "residence2/castle/chamberlain_saius063.htm", false);
            else
                showChatWindow(player, "residence2/castle/chamberlain_saius052.htm", false);
        } else if ("items".equalsIgnoreCase(actualCommand)) {
            HtmlMessage html = new HtmlMessage(this);
            html.setFile("residence2/castle/chamberlain_saius064.htm");
            html.replace("%npcId%", String.valueOf(getNpcId()));
            player.sendPacket(html);
        } else if ("default".equalsIgnoreCase(actualCommand)) {
            HtmlMessage html = new HtmlMessage(this);
            html.setFile("castle/chamberlain/chamberlain.htm");
            player.sendPacket(html);
        } else
            super.onBypassFeedback(player, command);
    }

    @Override
    protected boolean isCheckBuyFunction() {
        return false;
    }

    @Override
    protected int getCond(Player player) {
        Residence castle = getCastle();
        if (castle != null && castle.getId() != 0)
            if (player.getClan() != null)
                if (castle.getSiegeEvent().isInProgress() && !castle.getSiegeEvent().hasState(4))
                    return COND_SIEGE; // Busy because of siege
                else if (castle.getOwnerId() == player.getClanId()) {
                    if (player.isClanLeader()) // Leader of clan
                        return COND_OWNER;
                    if (isHaveRigths(player, Clan.CP_CS_ENTRY_EXIT) || // doors
                            isHaveRigths(player, Clan.CP_CS_MANOR_ADMIN) || // manor
                            isHaveRigths(player, Clan.CP_CS_MANAGE_SIEGE) || // siege
                            isHaveRigths(player, Clan.CP_CS_USE_FUNCTIONS) || // funcs
                            isHaveRigths(player, Clan.CP_CS_DISMISS) || // banish
                            isHaveRigths(player, Clan.CP_CS_TAXES) || // tax
                            isHaveRigths(player, Clan.CP_CS_MERCENARIES) || // merc
                            isHaveRigths(player, Clan.CP_CS_SET_FUNCTIONS) //funcs
                    )
                        return COND_OWNER; // Есть какие либо замковые привилегии
                }

        return COND_FAIL;
    }

    private NpcString getSealOwner(int seal) {
        //TODO: [Bonux] пересмотреть
        return NpcString.NO_OWNER;
    }

    private long getDoorCost(int type, int level) {
        int price = 0;

        switch (type) {
            case 1: // Главные ворота
                switch (level) {
                    case 2:
                        price = 100;
                        break;
                    case 3:
                        price = 200;
                        break;
                    case 5:
                        price = 300;
                        break;
                }
                break;
            case 2: // Внутренние ворота
                switch (level) {
                    case 2:
                        price = 100;
                        break;
                    case 3:
                        price = 200;
                        break;
                    case 5:
                        price = 300;
                        break;
                }
                break;
            case 3: // Стены
                switch (level) {
                    case 2:
                        price = 100;
                        break;
                    case 3:
                        price = 200;
                        break;
                    case 5:
                        price = 300;
                        break;
                }
                break;
        }

        return price;
    }

    @Override
    protected Residence getResidence() {
        return getCastle();
    }

    @Override
    public L2GameServerPacket decoPacket() {
        return null;
    }

    @Override
    protected int getPrivUseFunctions() {
        return Clan.CP_CS_USE_FUNCTIONS;
    }

    @Override
    protected int getPrivSetFunctions() {
        return Clan.CP_CS_SET_FUNCTIONS;
    }

    @Override
    protected int getPrivDismiss() {
        return Clan.CP_CS_DISMISS;
    }

    @Override
    protected int getPrivDoors() {
        return Clan.CP_CS_ENTRY_EXIT;
    }

    private boolean checkSiegeFunctions(Player player) {
        Castle castle = getCastle();
        if (!player.hasPrivilege(Privilege.CS_FS_SIEGE_WAR)) {
            player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return false;
        }

        if (castle.getSiegeEvent().isInProgress()) {
            showChatWindow(player, "residence2/castle/chamberlain_saius021.htm", false);
            return false;
        }
        return true;
    }

    @Override
    public boolean canPassPacket(Player player, Class<? extends L2GameClientPacket> packet, Object... arg) {
        return packet == RequestSetSeed.class || packet == RequestSetCrop.class || super.canPassPacket(player, packet, arg);
    }
}
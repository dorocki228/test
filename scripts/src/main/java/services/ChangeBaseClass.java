package services;

import l2s.gameserver.Config;
import l2s.gameserver.data.string.ItemNameHolder;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.SubClass;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.other.ChangeBaseClassDto;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChangeBaseClass {
    @Bypass("services.ChangeBaseClass:list")
    public void list(Player player, NpcInstance npc, String[] param) {
        if (!canInteract(player)) {
            return;
        }
        Set<ClassId> classIds = getClassIds(player);
        List<ChangeBaseClassDto> list = Arrays.stream(ClassId.values())
                .filter(c -> c.isOfLevel(ClassLevel.SECOND))
                .filter(c -> classIds.stream().noneMatch(p -> p.equalsOrChildOf(c) || c.equalsOrChildOf(p)))
                .map(i -> new ChangeBaseClassDto(i.getId(), Util.className(player, i.getId())))
                .collect(Collectors.toList());

        HtmlMessage htmlMessage = new HtmlMessage(0).setFile("gve/change/change_base_class_list.htm");
        htmlMessage.addVar("list", list);
        htmlMessage.addVar("player", player);
        htmlMessage.addVar("util", Util.class);
        player.sendPacket(htmlMessage);
    }

    private Set<ClassId> getClassIds(Player player) {
        if (player == null) {
            return Collections.emptySet();
        }
        return player.getSubClassList()
                .getListByClassId()
                .values()
                .stream()
                .filter(Objects::nonNull)
                .map(SubClass::getClassId)
                .map(ClassId::valueOf)
                .map(c -> c.orElse(null))
                .filter(Objects::nonNull)
                .filter(c-> c.getClassLevel() != ClassLevel.NONE)
                .collect(Collectors.toSet());
    }

    @Bypass("services.ChangeBaseClass:select")
    public void select(Player player, NpcInstance npc, String[] param) {
        if (!canInteract(player)) {
            return;
        }
        if (param.length != 1) {
            return;
        }
        int classId = Integer.parseInt(param[0]);
        Optional<ClassId> optionalClassId = ClassId.valueOf(classId);
        if (optionalClassId.isEmpty()) {
            return;
        }
        Set<ClassId> classIds = getClassIds(player);
        boolean canChange = classIds.stream().noneMatch(c -> c.equalsOrChildOf(optionalClassId.get()) || optionalClassId.get().equalsOrChildOf(c));
        if (!canChange) {
            return;
        }
        HtmlMessage htmlMessage = new HtmlMessage(0).setFile("gve/change/change_base_class_select.htm");
        htmlMessage.addVar("classId", classId);
        htmlMessage.addVar("className", Util.className(player, optionalClassId.get().getId()));
        htmlMessage.addVar("player", player);
        htmlMessage.addVar("util", Util.class);
        String item1 = ItemNameHolder.getInstance().getItemName(player, Config.CHANGE_BASE_CLASS_FIRST_PRICE[0]);
        String item2 = ItemNameHolder.getInstance().getItemName(player, Config.CHANGE_BASE_CLASS_SECOND_PRICE[0]);
        String itemName1 = item1 == null ? "Unknown" : item1;
        String itemName2 = item2 == null ? "Unknown" : item2;
        htmlMessage.addVar("itemName1", itemName1);
        htmlMessage.addVar("itemCount1", Config.CHANGE_BASE_CLASS_FIRST_PRICE[1]);
        htmlMessage.addVar("itemName2", itemName2);
        htmlMessage.addVar("itemCount2", Config.CHANGE_BASE_CLASS_SECOND_PRICE[1]);
        player.sendPacket(htmlMessage);
    }

    @Bypass("services.ChangeBaseClass:change")
    public void change(Player player, NpcInstance npc, String[] param) {
        if (!canInteract(player)) {
            return;
        }
        if (param.length != 2) {
            return;
        }
        if (player.isHero()) {
            player.sendMessage(new CustomMessage("services.change.base.class.s6"));
            return;
        }
        if (player.isInOlympiadMode()
                || player.isRegisteredInEvent()
                || player.getReflectionId() != ReflectionManager.MAIN.getId()
                || player.isInObserverMode()
                || player.isInCombat()
        ) {
            player.sendMessage(new CustomMessage("services.change.base.class.s5"));
            return;
        }
        int type = Integer.parseInt(param[0]);
        int classId = Integer.parseInt(param[1]);
        Map<Integer, ClassId> classIdMap = Arrays.stream(ClassId.values())
                .filter(c -> c.isOfLevel(ClassLevel.SECOND))
                .collect(Collectors.toMap(ClassId::getId, Function.identity()));
        if (!classIdMap.containsKey(classId)) {
            return;
        }
        ClassId _classId = classIdMap.get(classId);
        Set<ClassId> classIds = getClassIds(player);
        boolean canChange = classIds.stream().noneMatch(c -> c.equalsOrChildOf(_classId) || _classId.equalsOrChildOf(c));
        if (!canChange) {
            return;
        }
        int[] cost = type == 0 ? Config.CHANGE_BASE_CLASS_FIRST_PRICE : Config.CHANGE_BASE_CLASS_SECOND_PRICE;
        if (!ItemFunctions.deleteItem(player, cost[0], cost[1], true)) {
            String item = ItemNameHolder.getInstance().getItemName(player, cost[0]);
            String itemName = item == null ? "Unknown" : item;
            player.sendMessage(new CustomMessage("services.change.base.class.s3").addString(itemName));
            return;
        }
        Olympiad.removeParticipant(player);
        player.setClassId(classId, true);
        player.checkSkills();
        player.sendMessage(new CustomMessage("services.change.base.class.s2"));
        player.logout();
    }

    private boolean canInteract(Player player) {
        if (player == null) {
            return false;
        }
        if (!Config.CHANGE_BASE_CLASS_ENABLE) {
            player.sendMessage(new CustomMessage("NOT_AVAILABLE"));
            return false;
        }
        if (player.getActiveClassId() != player.getBaseClassId()) {
            player.sendMessage(new CustomMessage("services.change.base.class.s1"));
            return false;
        }
        int baseClassId = player.getBaseClassId();
        Optional<ClassId> optionalClassId = ClassId.valueOf(baseClassId);
        if (!optionalClassId.isPresent()) {
            return false;
        }
        ClassId baseClass = optionalClassId.get();
        if (baseClass.getClassLevel() != ClassLevel.THIRD) {
            player.sendMessage(new CustomMessage("services.change.base.class.s4"));
            return false;
        }
        return true;
    }
}

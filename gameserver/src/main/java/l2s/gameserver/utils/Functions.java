package l2s.gameserver.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExNoticePostArrived;
import l2s.gameserver.network.l2.s2c.ExUnReadMailCount;
import l2s.gameserver.network.l2.s2c.NSPacket;
import l2s.gameserver.templates.item.data.ItemData;

public class Functions
{
	public static void show(String text, Player player)
	{
		show(text, player, 0);
	}

	public static void show(CustomMessage message, Player player)
	{
		show(message.toString(player), player, 0);
	}

	public static void show(String text, Player player, int itemId, Object... arg)
	{
		if(text == null || player == null)
			return;
		HtmlMessage msg = new HtmlMessage(5);
		if(text.endsWith(".html") || text.endsWith(".htm"))
			msg.setFile(text);
		else
			msg.setHtml(HtmlUtils.bbParse(text));
		if(arg != null && arg.length % 2 == 0)
			for(int i = 0; i < arg.length; i += 2)
				msg.replace(String.valueOf(arg[i]), String.valueOf(arg[i + 1]));
		msg.setItemId(itemId);
		player.sendPacket(msg);
	}

	public static void show(String text, Player player, NpcInstance npc, Object... arg)
	{
		if(text == null || player == null)
			return;
		HtmlMessage msg = new HtmlMessage(npc);
		if(text.endsWith(".html") || text.endsWith(".htm"))
			msg.setFile(text);
		else
			msg.setHtml(HtmlUtils.bbParse(text));
		if(arg != null && arg.length % 2 == 0)
			for(int i = 0; i < arg.length; i += 2)
				msg.replace(String.valueOf(arg[i]), String.valueOf(arg[i + 1]));
		player.sendPacket(msg);
	}

	@Deprecated
	public static void npcSayInRange(NpcInstance npc, String text, int range)
	{
		npcSayInRange(npc, range, NpcString.NONE, text);
	}

	@Deprecated
	public static void npcSayInRange(NpcInstance npc, int range, NpcString fStringId, String... params)
	{
		ChatUtils.say(npc, range, new NSPacket(npc, ChatType.NPC_ALL, fStringId, params));
	}

	@Deprecated
	public static void npcSay(NpcInstance npc, String text)
	{
		npcSayInRange(npc, text, 1500);
	}

	@Deprecated
	public static void npcSay(NpcInstance npc, NpcString npcString, String... params)
	{
		npcSayInRange(npc, 1500, npcString, params);
	}

	@Deprecated
	public static void npcSayToPlayer(NpcInstance npc, Player player, NpcString npcString, String... params)
	{
		player.sendPacket(new NSPacket(npc, ChatType.TELL, npcString, params));
	}

	@Deprecated
	public static void npcShout(NpcInstance npc, NpcString npcString, String... params)
	{
		ChatUtils.shout(npc, npcString, params);
	}

	public static void npcSayCustomInRange(NpcInstance npc, CustomMessage message, int range) {
		World.getAroundObservers(npc).forEach(player -> {
			if(range <= 0 || player.isInRange(npc, range)) {
				String text = message.toString(player.getLanguage());
				NSPacket packet = new NSPacket(npc, ChatType.NPC_ALL, NpcString.NONE, text);
				player.sendPacket(packet);
			}
		});
	}

	public static boolean ride(Player player, int npcId)
	{
		if(player.hasServitor())
		{
			player.sendPacket(SystemMsg.YOU_ALREADY_HAVE_A_PET);
			return false;
		}
		player.setMount(0, npcId, player.getLevel(), -1);
		return true;
	}

	public static void unRide(Player player)
	{
		if(player.isMounted())
			player.setMount(null);
	}

	public static void unSummonPet(Player player, boolean onlyPets)
	{
		for(Servitor servitor : player.getServitors())
			if(!onlyPets || servitor.isPet())
				servitor.unSummon(false);
	}

	public static boolean IsActive(String name)
	{
		return "on".equalsIgnoreCase(ServerVariables.getString(name, "off"));
	}

	public static boolean SetActive(String name, boolean active)
	{
		if(active == IsActive(name))
			return false;
		if(active)
			ServerVariables.set(name, "on");
		else
			ServerVariables.unset(name);
		return true;
	}

	public static boolean SimpleCheckDrop(Creature mob, Creature killer)
	{
		return mob != null && mob.isMonster() && !mob.isRaid() && killer != null && killer.getPlayer() != null && killer.getLevel() - mob.getLevel() < 9;
	}

	public static void sendDebugMessage(Player player, String message)
	{
		if(!player.isGM())
			return;
		player.sendMessage(message);
	}

	public static void sendSystemMail(Player receiver, String title, String body, Map<Integer, Long> items)
	{
		if(receiver == null || !receiver.isOnline())
			return;
		if(title == null)
			return;
		if(items.keySet().size() > 8)
			return;
		Mail mail = new Mail();
		mail.setSenderId(1);
		mail.setSenderName("Admin");
		mail.setReceiverId(receiver.getObjectId());
		mail.setReceiverName(receiver.getName());
		mail.setTopic(title);
		mail.setBody(body);
		for(Map.Entry<Integer, Long> itm : items.entrySet())
		{
			ItemInstance item = ItemFunctions.createItem(itm.getKey());
			item.setLocation(ItemInstance.ItemLocation.MAIL);
			item.setCount(itm.getValue());
			item.save();
			mail.addAttachment(item);
		}
		mail.setType(Mail.SenderType.NEWS_INFORMER);
		mail.setUnread(true);
		mail.setExpireTime(2592000 + (int) (System.currentTimeMillis() / 1000L));
		mail.save();
		receiver.sendPacket(ExNoticePostArrived.STATIC_TRUE);
		receiver.sendPacket(new ExUnReadMailCount(receiver));
		receiver.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
	}

	public static void sendSystemMail(int objectId, String nickName, String title, String body, long[][] items) {
		Map<Integer, Long> mapItems = new HashMap<>();

		for (long[] item : items)
			mapItems.put((int) item[0], item[1]);

		sendSystemMail(objectId, nickName, title, body, mapItems);
	}

	public static void sendSystemMail(int objectId, String nickName, String title, String body, Map<Integer, Long> items) {
		if(title == null)
			return;

		if(items.keySet().size() > 8)
			return;

		Mail mail = new Mail();
		mail.setSenderId(1);
		mail.setSenderName("Admin");
		mail.setReceiverId(objectId);
		mail.setReceiverName(nickName);
		mail.setTopic(title);
		mail.setBody(body);
		for (Map.Entry<Integer, Long> itm : items.entrySet()) {
			ItemInstance item = ItemFunctions.createItem(itm.getKey());
			item.setLocation(ItemInstance.ItemLocation.MAIL);
			item.setCount(itm.getValue());
			item.save();
			mail.addAttachment(item);
		}
		mail.setType(Mail.SenderType.NEWS_INFORMER);
		mail.setUnread(true);
		mail.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
		mail.save();

		Player receiver = GameObjectsStorage.getPlayer(objectId);
		if(receiver != null) {
			receiver.sendPacket(ExNoticePostArrived.STATIC_TRUE);
			receiver.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
		}
	}

	public static void sendSystemMail(int objectId, String nickName, String title, String body, Collection<ItemData> items) {
		if(title == null)
			return;

		if(items.size() > 8)
			return;

		Mail mail = new Mail();
		mail.setSenderId(1);
		mail.setSenderName("Admin");
		mail.setReceiverId(objectId);
		mail.setReceiverName(nickName);
		mail.setTopic(title);
		mail.setBody(body);
		for (ItemData itm : items) {
			ItemInstance item = ItemFunctions.createItem(itm.getId());
			item.setLocation(ItemInstance.ItemLocation.MAIL);
			item.setCount(itm.getCount());
			item.save();
			mail.addAttachment(item);
		}
		mail.setType(Mail.SenderType.NEWS_INFORMER);
		mail.setUnread(true);
		mail.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
		mail.save();

		Player receiver = GameObjectsStorage.getPlayer(objectId);
		if(receiver != null) {
			receiver.sendPacket(ExNoticePostArrived.STATIC_TRUE);
			receiver.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
		}
	}
}

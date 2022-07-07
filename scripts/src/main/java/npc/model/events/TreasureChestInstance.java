package npc.model.events;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.reward.*;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KanuToIIIKa
 */

public class TreasureChestInstance extends NpcInstance
{
	public static RewardList dion_reward;
	public static RewardList giran_reward;
	public static RewardList aden_reward;
	private final int EMPTY_CHANCE = 20;

	public TreasureChestInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);

		dion_reward = new RewardList(RewardType.NOT_RATED_GROUPED, true);

		RewardGroup rgd = new RewardGroup((100 - EMPTY_CHANCE) * 10000);
		rgd.addData(new RewardData(57, 15000, 45000, 350000));
		rgd.addData(new RewardData(29011, 1, 1, 300000));
		rgd.addData(new RewardData(21468, 1, 1, 50000));
		rgd.addData(new RewardData(29565, 1, 1, 100000));
		dion_reward.add(rgd);

		giran_reward = new RewardList(RewardType.NOT_RATED_GROUPED, true);

		RewardGroup rgg = new RewardGroup((100 - EMPTY_CHANCE) * 10000);
		rgg.addData(new RewardData(57, 70000, 150000, 350000));
		rgg.addData(new RewardData(29012, 1, 1, 300000));
		rgg.addData(new RewardData(29037, 1, 1, 50000));
		rgg.addData(new RewardData(29565, 1, 1, 100000));
		giran_reward.add(rgg);

		aden_reward = new RewardList(RewardType.NOT_RATED_GROUPED, true);

		RewardGroup rga = new RewardGroup((100 - EMPTY_CHANCE) * 10000);
		rga.addData(new RewardData(57, 100000, 190000, 200000));
		rga.addData(new RewardData(29012, 1, 1, 300000));
		rga.addData(new RewardData(3936, 1, 1, 150000));
		rga.addData(new RewardData(37726, 1, 1, 50000));
		rga.addData(new RewardData(29565, 1, 1, 100000));
		aden_reward.add(rga);
	}

	public void showChatWindow(Player player, int val, boolean firstTalk, Object... arg)
	{
		HtmlMessage html = new HtmlMessage(getObjectId());

		String sb = "<html><head><body>" +
				"If you have Chest Key, you can try open it! Who know what reward you can get!<br1>" +
				"<Button ALIGN=LEFT ICON=\"QUEST\" action=\"bypass -h npc_%objectId%_tryLuck\">Open Chest</Button></body></html>";
		html.setHtml(sb);

		player.sendPacket(html);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if("tryLuck".equals(command))
		{
			int key = getParameter("key", -1);

			if(key != -1 && ItemFunctions.deleteItem(player, key, 1))
			{
				List<RewardItem> reward = new ArrayList<>();

				switch(key)
				{
					case 5202:
						reward.addAll(dion_reward.roll(player, 1.0, this));
						break;
					case 5203:
						reward.addAll(giran_reward.roll(player, 1.0, this));
						break;
					case 5204:
						reward.addAll(aden_reward.roll(player, 1.0, this));
						break;
				}
				if(!reward.isEmpty())
				{
					for(RewardItem r : reward)
						ItemFunctions.addItem(player, r.itemId, r.count);
				}

				doDie(player);
			}
			else
			{
				HtmlMessage html = new HtmlMessage(getObjectId());

				StringBuilder sb = new StringBuilder();

				sb.append("<html><head><body>");
				sb.append("All your attempts to open this chest without key did not succeed.");
				sb.append("</body></html>");

				html.setHtml(sb.toString());

				player.sendPacket(html);
			}
		}

	}
}

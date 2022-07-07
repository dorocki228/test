package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.util.StringTokenizer;

public final class MercManagerInstance extends MerchantInstance
{
	private static final long serialVersionUID = 1L;
	private static final int COND_ALL_FALSE;
	private static final int COND_BUSY_BECAUSE_OF_SIEGE;
	private static final int COND_OWNER;

	public MercManagerInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		int condition = validateCondition(player);
		if(condition <= COND_ALL_FALSE || condition == COND_BUSY_BECAUSE_OF_SIEGE)
			return;
		if(condition == COND_OWNER)
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String actualCommand = st.nextToken();
			String val = "";
			if(st.countTokens() >= 1)
				val = st.nextToken();
			if("hire".equalsIgnoreCase(actualCommand))
			{
				if(val.isEmpty())
					return;
                showShopWindow(player, Integer.parseInt(val), false);
			}
			else
				super.onBypassFeedback(player, command);
		}
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... arg)
	{
		String filename = "castle/mercmanager/mercmanager-no.htm";
		int condition = validateCondition(player);
		if(condition == COND_BUSY_BECAUSE_OF_SIEGE)
			filename = "castle/mercmanager/mercmanager-busy.htm";
		else if(condition == COND_OWNER)
			filename = "castle/mercmanager/mercmanager.htm";
		player.sendPacket(new HtmlMessage(this, filename).setPlayVoice(firstTalk));
	}

	private int validateCondition(Player player)
	{
		if(player.isGM())
			return COND_OWNER;
		if(getCastle() != null && getCastle().getId() != 0 && player.getClan() != null)
		{
			if(getCastle().getSiegeEvent().isInProgress())
				return COND_BUSY_BECAUSE_OF_SIEGE;
			if(getCastle().getOwnerId() == player.getClanId() && (player.getClanPrivileges() & 0x400000) == 0x400000)
				return COND_OWNER;
		}
		return COND_ALL_FALSE;
	}

	static
	{
		COND_ALL_FALSE = 0;
		COND_BUSY_BECAUSE_OF_SIEGE = 1;
		COND_OWNER = 2;
	}
}

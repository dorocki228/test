package l2s.gameserver.model.instances;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.BuyListHolder;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.instancemanager.MapRegionManager;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.network.l2.c2s.*;
import l2s.gameserver.network.l2.s2c.ExBuySellListPacket;
import l2s.gameserver.network.l2.s2c.ShopPreviewListPacket;
import l2s.gameserver.templates.mapregion.DomainArea;
import l2s.gameserver.templates.npc.BuyListTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.StringTokenizer;

public class MerchantInstance extends NpcInstance
{
	private static final Logger _log;
	private final TIntObjectMap<BuyListTemplate> _buyLists;

	public MerchantInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
		Collection<BuyListTemplate> buyLists = BuyListHolder.getInstance().getBuyLists(getNpcId());
		_buyLists = new TIntObjectHashMap<>(buyLists.size());
		for(BuyListTemplate b : buyLists)
		{
			BuyListTemplate buyList = b.clone();
			_buyLists.put(buyList.getId(), buyList);
			_buyLists.put(buyList.getListId(), buyList);
		}
	}

	private void showWearWindow(Player player, int val)
	{
		if(!player.getPlayerAccess().UseShop)
			return;
		BuyListTemplate list = getBuyList(val);
		if(list != null)
		{
			ShopPreviewListPacket bl = new ShopPreviewListPacket(list, player);
			player.sendPacket(bl);
		}
		else
		{
			_log.warn("no buylist with id:" + val);
			player.sendActionFailed();
		}
	}

	protected void showShopWindow(Player player, int listId, boolean tax)
	{
		if(!player.getPlayerAccess().UseShop)
			return;
		double sellTaxRate = 0.0;
		double buyTaxRate = 0.0;
		if(tax)
		{
			Castle castle = getCastle(player);
			if(castle != null)
			{
				sellTaxRate = castle.getSellTaxRate(player);
				buyTaxRate = castle.getBuyTaxRate(player);
			}
		}
		BuyListTemplate list = getBuyList(listId);
		if(list == null || list.getNpcId() == getNpcId())
			player.sendPacket(new ExBuySellListPacket.BuyList(list, player, sellTaxRate), new ExBuySellListPacket.SellRefundList(player, false, buyTaxRate));
		else
		{
			_log.warn("[L2MerchantInstance] possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
			_log.warn("buylist id:" + listId + " / list_npc = " + list.getNpcId() + " / npc = " + getNpcId());
		}
	}

	protected void showShopWindow(Player player)
	{
        showShopWindow(player, 0, false);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		if("Buy".equalsIgnoreCase(actualCommand) || "Sell".equalsIgnoreCase(actualCommand))
		{
			int val = 0;
			if(st.countTokens() > 0)
				val = Integer.parseInt(st.nextToken());
            showShopWindow(player, val, true);
		}
		else if("Wear".equalsIgnoreCase(actualCommand))
		{
			if(st.countTokens() < 1)
				return;
			int val = Integer.parseInt(st.nextToken());
			showWearWindow(player, val);
		}
		else if("Multisell".equalsIgnoreCase(actualCommand))
		{
			if(st.countTokens() < 1)
				return;
			int val = Integer.parseInt(st.nextToken());
			Castle castle = getCastle(player);
			MultiSellHolder.getInstance().SeparateAndSend(val, player, getObjectId(),
					castle != null ? castle.getSellTaxRate(player) : 0.0);
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public Castle getCastle(Player player)
	{
		if(Config.SERVICES_OFFSHORE_NO_CASTLE_TAX || getReflection() == ReflectionManager.PARNASSUS && Config.SERVICES_PARNASSUS_NOTAX)
			return null;
		if(getReflection() == ReflectionManager.GIRAN_HARBOR || getReflection() == ReflectionManager.PARNASSUS)
		{
			String var = player.getVar("backCoords");
			if(var != null && !var.isEmpty())
			{
				Location loc = Location.parseLoc(var);
				DomainArea domain = MapRegionManager.getInstance().getRegionData(DomainArea.class, loc);
				if(domain != null)
					return ResidenceHolder.getInstance().getResidence(Castle.class, domain.getId());
			}
			return getCastle();
		}
		return super.getCastle(player);
	}

	@Override
	public boolean canPassPacket(Player player, Class<? extends L2GameClientPacket> packet, Object... arg)
	{
		return packet == RequestBuyItem.class || packet == RequestSellItem.class || packet == RequestExRefundItem.class || packet == RequestPreviewItem.class || super.canPassPacket(player, packet, arg);
	}

	@Override
	public BuyListTemplate getBuyList(int listId)
	{
		return _buyLists.get(listId);
	}

	@Override
	protected void onSpawn()
	{
		for(BuyListTemplate buyList : _buyLists.valueCollection())
			buyList.refresh();
		super.onSpawn();
	}

	static
	{
		_log = LoggerFactory.getLogger(MerchantInstance.class);
	}
}

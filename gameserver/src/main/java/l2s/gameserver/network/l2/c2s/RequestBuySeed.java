package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.item.data.ItemData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author l3x
 */
public class RequestBuySeed implements IClientIncomingPacket
{
	private static final int BATCH_LENGTH = 12; // length of the one item
	private int _manorId;
	private List<ItemData> _items = null;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_manorId = packet.readD();
		final int count = packet.readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET
				|| count * BATCH_LENGTH != packet.getReadableBytes()) {
			return false;
		}

		_items = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			final int itemId = packet.readD();
			final long cnt = packet.readQ();
			if (cnt < 1 || itemId < 1) {
				_items = null;
				return false;
			}
			_items.add(new ItemData(itemId, cnt));
		}

		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if (activeChar == null) {
			return;
		} else if (_items == null) {
			activeChar.sendActionFailed();
			return;
		}

		/*
		TODO manor
		final CastleManorManager manor = CastleManorManager.getInstance();
		if (manor.isUnderMaintenance()) {
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		final Castle castle = CastleManager.getInstance().getCastleById(_manorId);
		if (castle == null) {
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		final L2Npc manager = player.getLastFolkNPC();
		if (!(manager instanceof L2MerchantInstance) || !manager.canInteract(player) || (manager.getParameters().getInt("manor_id", -1) != _manorId)) {
			client.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		long totalPrice = 0;
		int slots = 0;
		int totalWeight = 0;

		final Map<Integer, SeedProduction> _productInfo = new HashMap<>();
		for (ItemHolder ih : _items) {
			final SeedProduction sp = manor.getSeedProduct(_manorId, ih.getId(), false);
			if ((sp == null) || (sp.getPrice() <= 0) || (sp.getAmount() < ih.getCount()) || ((Inventory.MAX_ADENA / ih.getCount()) < sp.getPrice())) {
				client.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			// Calculate price
			totalPrice += (sp.getPrice() * ih.getCount());
			if (totalPrice > Inventory.MAX_ADENA) {
				GameUtils.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Inventory.MAX_ADENA + " adena worth of goods.", Config.DEFAULT_PUNISH);
				client.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			// Calculate weight
			final L2Item template = ItemTable.getInstance().getTemplate(ih.getId());
			totalWeight += ih.getCount() * template.getWeight();

			// Calculate slots
			if (!template.isStackable()) {
				slots += ih.getCount();
			} else if (player.getInventory().getItemByItemId(ih.getId()) == null) {
				slots++;
			}
			_productInfo.put(ih.getId(), sp);
		}

		if (!player.getInventory().validateWeight(totalWeight)) {
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			return;
		} else if (!player.getInventory().validateCapacity(slots)) {
			player.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
			return;
		} else if ((totalPrice < 0) || (player.getAdena() < totalPrice)) {
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		// Proceed the purchase
		for (ItemHolder i : _items) {
			final SeedProduction sp = _productInfo.get(i.getId());
			final long price = sp.getPrice() * i.getCount();

			// Take Adena and decrease seed amount
			if (!sp.decreaseAmount(i.getCount()) || !player.reduceAdena("Buy", price, player, false)) {
				// failed buy, reduce total price
				totalPrice -= price;
				continue;
			}

			// Add item to player's inventory
			player.addItem("Buy", i.getId(), i.getCount(), manager, true);
		}

		// Adding to treasury for Manor Castle
		if (totalPrice > 0) {
			castle.addToTreasuryNoTax(totalPrice);

			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ADENA_DISAPPEARED);
			sm.addLong(totalPrice);
			player.sendPacket(sm);

			if (Config.ALT_MANOR_SAVE_ALL_ACTIONS) {
				manor.updateCurrentProduction(_manorId, _productInfo.values());
			}
		}*/
	}
}
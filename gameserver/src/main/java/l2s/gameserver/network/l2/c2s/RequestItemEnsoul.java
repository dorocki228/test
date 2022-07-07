package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.EnsoulHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExEnsoulResult;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.templates.item.support.Ensoul;
import l2s.gameserver.templates.item.support.EnsoulFee;
import l2s.gameserver.templates.item.support.EnsoulFee.EnsoulFeeInfo;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.NpcUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class RequestItemEnsoul extends L2GameClientPacket
{
	private static final Logger LOGGER = LogManager.getLogger(RequestItemEnsoul.class);

	private int _itemObjectId;
	private List<EnsoulInfo> _ensoulsInfo;

	@Override
	protected void readImpl()
	{
		_itemObjectId = readD();
		int changesCount = readC();
		_ensoulsInfo = new ArrayList<>(changesCount);
		for(int i = 0; i < changesCount; ++i)
		{
			EnsoulInfo info = new EnsoulInfo();
			info.type = readC();
			info.id = readC();
			info.itemObjectId = readD();
			info.ensoulId = readD();
			_ensoulsInfo.add(info);
		}
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(NpcUtils.canPassPacket(activeChar, this) == null)
		{
			activeChar.sendPacket(ExEnsoulResult.FAIL);
			return;
		}

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendPacket(ExEnsoulResult.FAIL);
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(ExEnsoulResult.FAIL);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendPacket(ExEnsoulResult.FAIL);
			return;
		}

		ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_itemObjectId);
		if(targetItem == null)
		{
			activeChar.sendPacket(ExEnsoulResult.FAIL);
			return;
		}

		if(!targetItem.isWeapon())
		{
			activeChar.sendPacket(ExEnsoulResult.FAIL);
			return;
		}

		EnsoulFee ensoulFee = EnsoulHolder.getInstance().getEnsoulFee(targetItem.getGrade());
		if(ensoulFee == null)
		{
			LOGGER.warn("Can't find ensoul for grade {}", targetItem.getGrade());

			activeChar.sendPacket(ExEnsoulResult.FAIL);
			return;
		}
        boolean equipped = targetItem.isEquipped();

        if(equipped)
			activeChar.getInventory().unEquipItem(targetItem);

		boolean success = false;
		for(EnsoulInfo info : _ensoulsInfo)
        {
            Ensoul ensoul = EnsoulHolder.getInstance().getEnsoul(info.ensoulId);
            ItemInstance ensoulItem = activeChar.getInventory().getItemByObjectId(info.itemObjectId);
            if(ensoulItem == null || ensoul == null || ensoul.getItemId() != ensoulItem.getItemId() || !targetItem.canBeEnsoul(ensoul.getItemId()))
                continue;

            EnsoulFeeInfo feeInfo = ensoulFee.getFeeInfo(info.type, info.id);
            if(feeInfo == null)
            {
                targetItem.addEnsoul(info.type, info.id, ensoul, true);
                success = true;
            }
            else
            {
                activeChar.getInventory().writeLock();

                List<ItemData> feeItems = targetItem.containsEnsoul(info.type, info.id)
                        ? feeInfo.getChangeFee()
                        : feeInfo.getInsertFee();

                var haveItems = feeItems.stream()
                        .allMatch(item -> ItemFunctions.haveItem(activeChar, item.getId(), item.getCount()));
                if(!haveItems)
                {
                    activeChar.getInventory().writeUnlock();
                    continue;
                }

                var itemsDestroyed = feeItems.stream()
                        .allMatch(item ->
                                activeChar.getInventory().destroyItemByItemId(item.getId(), item.getCount()));
                if(!itemsDestroyed)
                {
                    activeChar.getInventory().writeUnlock();
                    continue;
                }

                if(!activeChar.getInventory().destroyItem(ensoulItem, 1))
                {
                    activeChar.getInventory().writeUnlock();
                    continue;
                }

                targetItem.addEnsoul(info.type, info.id, ensoul, true);

                activeChar.getInventory().writeUnlock();

                success = true;
            }
        }

		if(equipped)
		{
			activeChar.getInventory().equipItem(targetItem);
		}

		if(success)
		{
			activeChar.sendPacket(new ExEnsoulResult(targetItem.getNormalEnsouls(), targetItem.getSpecialEnsouls()));
		}
		else
		{
			activeChar.sendPacket(ExEnsoulResult.FAIL);
		}
	}

    private static class EnsoulInfo
	{
		public int type;
		public int id;
		public int itemObjectId;
		public int ensoulId;

		private EnsoulInfo()
		{}
	}
}

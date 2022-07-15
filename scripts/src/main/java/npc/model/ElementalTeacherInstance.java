package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Elemental;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.ElementalElement;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author Bonux
**/
public class ElementalTeacherInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;

	public ElementalTeacherInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void showMainChatWindow(Player player, boolean firstTalk, Object... replace)
	{
		showChatWindow(player, "default/ws_human001.htm", firstTalk);
	}

	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state)
	{
		if(ask == -20170726)
		{
			if(!Config.ELEMENTAL_SYSTEM_ENABLED)
				return;

			if(reply == 2) // Улучшить Ожерелье Духов
			{
				MultiSellHolder.getInstance().SeparateAndSend(606, player, 0);
			}
			else if(reply == 3) // Узнать о Духах стихий
			{
				if(player.getClassLevel().ordinal() < ClassLevel.THIRD.ordinal())
				{
					showChatWindow(player, "default/ws_human002.htm", false);
				}
				else if(player.getActiveElement() == ElementalElement.NONE)
				{
					player.setActiveElement(ElementalElement.FIRE);
					for(ElementalElement element : ElementalElement.VALUES)
					{
						Elemental elemental = player.getElementalList().get(element);
						if(elemental != null)
							elemental.setEvolutionLevel(1);
					}
					player.sendElementalInfo();
					player.sendPacket(SystemMsg.YOU_HAVE_OBTAINED_AN_ATTRIBUTE_OPEN_YOUR_CHARACTER_INFORMATION_SCREEN_TO_CHECK_);

					ItemFunctions.addItem(player, 91197, 1);
					ItemFunctions.addItem(player, 91198, 1);
					ItemFunctions.addItem(player, 91199, 1);
					ItemFunctions.addItem(player, 91200, 1);

					showChatWindow(player, "default/ws_human003.htm", false);
				}
				else
				{
					showChatWindow(player, "default/ws_human004.htm", false);
				}
			}
			else if(reply == 5) // Получить Агатиона Духа
			{
				MultiSellHolder.getInstance().SeparateAndSend(607, player, 0);
			}
			else if(reply == 6) // Использовать Фрагмент Стихии
			{
				MultiSellHolder.getInstance().SeparateAndSend(608, player, 0);
			}
		}
		else
			super.onMenuSelect(player, ask, reply, state);
	}
}
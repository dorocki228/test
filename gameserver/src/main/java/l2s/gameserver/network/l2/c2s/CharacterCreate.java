package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Macro;
import l2s.gameserver.model.actor.instances.player.ShortCut;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.CharacterCreateFail;
import l2s.gameserver.network.l2.s2c.CharacterCreateSuccessPacket;
import l2s.gameserver.network.l2.s2c.CharacterSelectionInfoPacket;
import l2s.gameserver.templates.item.StartItem;
import l2s.gameserver.templates.player.PlayerTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Util;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

public class CharacterCreate extends L2GameClientPacket
{
	private static final Logger LOGGER = LoggerFactory.getLogger(CharacterCreate.class);

	private String _name;
	private int _sex;
	private int _classId;
	private int _hairStyle;
	private int _hairColor;
	private int _face;

	@Override
	protected void readImpl()
	{
		_name = readS();
		readD();
		_sex = readD();
		_classId = readD();
		readD();
		readD();
		readD();
		readD();
		readD();
		readD();
		_hairStyle = readD();
		_hairColor = readD();
		_face = readD();
	}

	@Override
	protected void runImpl()
	{
		Optional<ClassId> cid = ClassId.valueOf(_classId);
		if(!cid.isPresent() || !cid.get().isOfLevel(ClassLevel.NONE))
			return;

		GameClient client = getClient();

		if(CharacterDAO.getInstance().accountCharNumber(client.getLogin()) >= 8)
			return;
		if(!Util.isMatchingRegexp(_name, Config.CNAME_TEMPLATE))
			return;
		if(Config.RESTRICTED_CHAR_CLAN_NAME_ENABLE && Config.RESTRICTED_CHAR_CLAN_NAME.matcher(_name).find())
			return;
		if(CharacterDAO.getInstance().getObjectIdByName(_name) > 0)
			return;
		if(!validateParams())
			return;

		Player newChar = Player.create(client.getHwidHolder(), _classId, _sex, client.getLogin(), _name,
				_hairStyle, _hairColor, _face);
		if(newChar == null)
			return;
		sendPacket(CharacterCreateSuccessPacket.STATIC);
		initNewChar(client, newChar);
	}

	private void initNewChar(GameClient client, Player newChar)
	{
		PlayerTemplate template = newChar.getTemplate();
		newChar.getSubClassList().restore();
		newChar.setLoc(template.getStartLocation());
		if(Config.CHAR_TITLE)
			newChar.setTitle(Config.ADD_CHAR_TITLE);
		else
			newChar.setTitle("");
		newChar.setCurrentHpMp(newChar.getMaxHp(), newChar.getMaxMp());
		newChar.setCurrentCp(0.0);
		for(StartItem i : template.getStartItems())
		{
			ItemInstance item = ItemFunctions.createItem(i.getId());
			if(i.getEnchantLevel() > 0)
				item.setEnchantLevel(i.getEnchantLevel());
			long count = i.getCount();
			if(item.isStackable())
			{
				item.setCount(count);
				newChar.getInventory().addItem(item);
			}
			else
			{
				for(long n = 0L; n < count; ++n)
				{
					item = ItemFunctions.createItem(i.getId());
					if(i.getEnchantLevel() > 0)
						item.setEnchantLevel(i.getEnchantLevel());
					newChar.getInventory().addItem(item);
				}
				if(item.isEquipable() && i.isEquiped())
					newChar.getInventory().equipItem(item);
			}
		}

		if (!Arrays.toString(Config.STARTING_ITEM).equals("[0,0]")) {
			int[][] rewards = new int[Config.STARTING_ITEM.length][2];
			int start_i = 0;
			for (String reward : Config.STARTING_ITEM) {
				String[] splitReward = reward.split(",");
				rewards[start_i][0] = Integer.parseInt(splitReward[0]);
				rewards[start_i][1] = Integer.parseInt(splitReward[1]);
				start_i++;
			}

			for (int[] reward : rewards) {
				ItemInstance itemstarting = ItemFunctions.createItem(reward[0]);
				itemstarting.setCount(reward[1]);
				newChar.getInventory().addItem(itemstarting);
			}
		}

		newChar.registerMacro(new Macro(1000, 3, "Menu", "",  "menu", new Macro.L2MacroCmd[]{new Macro.L2MacroCmd(1, 3, 0, 0, ".menu")}));
		newChar.registerMacro(new Macro(1001, 3, "ACP", "",  "acp", new Macro.L2MacroCmd[]{new Macro.L2MacroCmd(1, 3, 0, 0, ".acp")}));
		newChar.rewardSkills(false, false, false);
		if(newChar.getSkillLevel(1001) > 0)
			newChar.registerShortCut(new ShortCut(1, 0, 2, 1001, 1, 1));
		if(newChar.getSkillLevel(1177) > 0)
			newChar.registerShortCut(new ShortCut(1, 0, 2, 1177, 1, 1));
		if(newChar.getSkillLevel(1216) > 0)
			newChar.registerShortCut(new ShortCut(6, 0, 2, 1216, 1, 1));
		newChar.registerShortCut(new ShortCut(0, 0, 3, 2, -1, 1));
		newChar.registerShortCut(new ShortCut(3, 0, 3, 5, -1, 1));
		newChar.registerShortCut(new ShortCut(4, 0, 3, 999, -1, 1));
		newChar.registerShortCut(new ShortCut(7, 0, 2, 6088, 1, 1));
		newChar.registerShortCut(new ShortCut(8, 0, 4, 1000, -1, 1));
		newChar.registerShortCut(new ShortCut(9, 0, 2, 2099, 1, 1));
		newChar.registerShortCut(new ShortCut(10, 0, 3, 0, -1, 1));
		newChar.registerShortCut(new ShortCut(11, 0, 2, 246, 1, 1));
		newChar.registerShortCut(new ShortCut(4, 1, 3, 3, -1, 1));
		newChar.registerShortCut(new ShortCut(5, 1, 3, 7, -1, 1));
		newChar.registerShortCut(new ShortCut(11, 1, 4, 1001, -1, 1));
		newChar.registerShortCut(new ShortCut(6, 1, 3, 10000, -1, 1));
		newChar.checkLevelUpReward(true);
        newChar.setFraction(Fraction.NONE);
        newChar.setVar("fraction", Fraction.NONE.ordinal());
		newChar.setVar("noShift", "1");
		newChar.setOnlineStatus(false);
		newChar.store(false);
		newChar.getInventory().store();
		newChar.deleteMe();
		client.setCharSelection(CharacterSelectionInfoPacket.loadCharacterSelectInfo(client.getLogin()));
	}

	/**
	 * L2JServer hints
	 *
	 * @return bool
	 */
	private boolean validateParams()
	{
		if(_face > 2 || _face < 0)
		{
			String messagePattern = "Character Creation Failure: Character face={} is invalid. Possible client hack={}";
			Message message = new ParameterizedMessage(messagePattern, _face, getClient().getLogin());
			LogService.getInstance().log(LoggerType.ILLEGAL_ACTIONS, message);

            sendPacket(CharacterCreateFail.REASON_CREATION_FAILED);

			return false;
		}
		if(_hairStyle < 0 || _sex == 0 && _hairStyle > 4 || _sex != 0 && _hairStyle > 6)
		{
            String messagePattern = "Character Creation Failure: Character hair style={} is invalid. Possible client hack={}";
            Message message = new ParameterizedMessage(messagePattern, _hairStyle, getClient().getLogin());
            LogService.getInstance().log(LoggerType.ILLEGAL_ACTIONS, message);

            sendPacket(CharacterCreateFail.REASON_CREATION_FAILED);

			return false;
		}
		if(_hairColor > 3 || _hairColor < 0)
		{
		    String messagePattern = "Character Creation Failure: Character hair color={} is invalid. Possible client hack={}";
            Message message = new ParameterizedMessage(messagePattern, _hairColor, getClient().getLogin());
            LogService.getInstance().log(LoggerType.ILLEGAL_ACTIONS, message);

            sendPacket(CharacterCreateFail.REASON_CREATION_FAILED);

			return false;
		}
		return true;
	}
}

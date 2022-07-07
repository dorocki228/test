package l2s.gameserver.network.l2.components;

import l2s.gameserver.Config;
import l2s.gameserver.cache.ImagesCache;
import l2s.gameserver.config.GveTimeDiscountConfig;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.network.l2.s2c.PledgeCrestPacket;
import l2s.gameserver.utils.BypassStorage;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.velocity.VelocityUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class HtmlMessage implements IBroadcastPacket
{
	private static final Logger _log;
	public static final String OBJECT_ID_VAR = "OBJECT_ID";
	private String _filename;
	private String _html;
	private Map<String, Object> _variables;
	private Map<String, String> _replaces;
	private final NpcInstance _npc;
	private final int _npcObjId;
	private int _itemId;
	private int _questId;
	private boolean _playVoice;

	public HtmlMessage(NpcInstance npc, String filename)
	{
		_npc = npc;
		_npcObjId = npc.getObjectId();
		_filename = filename;
	}

	public HtmlMessage(NpcInstance npc)
	{
		this(npc, null);
	}

	public HtmlMessage(int npcObjId)
	{
		_npc = GameObjectsStorage.getNpc(npcObjId);
		_npcObjId = npcObjId;
	}

	public HtmlMessage setHtml(String text)
	{
		_html = text;
		return this;
	}

	public final HtmlMessage setFile(String file)
	{
		_filename = file;
		return this;
	}

	public final HtmlMessage setItemId(int itemId)
	{
		_itemId = itemId;
		return this;
	}

	public final HtmlMessage setQuestId(int questId)
	{
		_questId = questId;
		return this;
	}

	public final HtmlMessage setPlayVoice(boolean playVoice)
	{
		_playVoice = playVoice;
		return this;
	}

	public HtmlMessage addVar(String name, Object value)
	{
		if(name == null)
			throw new IllegalArgumentException("Name can't be null!");
		if(value == null)
			throw new IllegalArgumentException("Value can't be null!");
		if(name.startsWith("${"))
			throw new IllegalArgumentException("Incorrect name: " + name);
		if(_variables == null)
			_variables = new HashMap<>(2);
		_variables.put(name, value);
		return this;
	}

	public HtmlMessage replace(String name, String value)
	{
		if(name == null)
			throw new IllegalArgumentException("Name can't be null!");
		if(value == null)
			throw new IllegalArgumentException("Value can't be null!");
		if((!name.startsWith("%") || !name.endsWith("%")) && (!name.startsWith("<?") || !name.endsWith("?>")))
			throw new IllegalArgumentException("Incorrect name: " + name);
		if(_replaces == null)
			_replaces = new LinkedHashMap<>(2);
		_replaces.put(name, value);
		return this;
	}

	public HtmlMessage replace(String name, NpcString npcString)
	{
		return replace(name, HtmlUtils.htmlNpcString(npcString, ArrayUtils.EMPTY_OBJECT_ARRAY));
	}

	public HtmlMessage replace(String name, NpcString npcString, Object... arg)
	{
		if(npcString == null)
			throw new IllegalArgumentException("NpcString can't be null!");
		return replace(name, HtmlUtils.htmlNpcString(npcString, arg));
	}

	@Override
	public L2GameServerPacket packet(Player player)
	{
		CharSequence content = null;
		if(!StringUtils.isEmpty(_html))
			content = make(player, _html);
		else if(!StringUtils.isEmpty(_filename))
		{
			if(player.isGM())
				player.sendMessage("HTML: " + _filename);
			String htmCache = HtmCache.getInstance().getHtml(_filename, player);
			
			content = make(player, htmCache);
		}
		else
			_log.warn("HtmlMessage: empty dialog" + (_npc == null ? "!" : " npc id : " + _npc.getNpcId() + "!"), new Exception());
		if(_itemId == 0)
		{
			if(_npc != null)
				player.setLastNpc(_npc);
			player.getBypassStorage().parseHtml(content, BypassStorage.BypassType.DEFAULT);
		}
		else
			player.getBypassStorage().parseHtml(content, BypassStorage.BypassType.ITEM);
		if(StringUtils.isEmpty(content))
			return ActionFailPacket.STATIC;
		return new NpcHtmlMessagePacket(_npcObjId, _itemId, _playVoice, content);
	}

	public Map<String, Object> getVariables()
	{
		return _variables;
	}

	private CharSequence make(Player player, String content)
	{
		if(content == null)
			return "";
		StrBuilder sb = new StrBuilder(content);
		if(_replaces != null)
			for(Map.Entry<String, String> e : _replaces.entrySet())
				sb.replaceAll(e.getKey(), e.getValue());
		Matcher m = ImagesCache.HTML_PATTERN.matcher(content);
		while(m.find())
		{
			String imageName = m.group(1);
			int imageId = ImagesCache.getInstance().getImageId(imageName);
			sb.replaceAll("%image:" + imageName + "%", "Crest.crest_" + Config.REQUEST_ID + "_" + imageId);
			byte[] image = ImagesCache.getInstance().getImage(imageId);
			if(image != null)
				player.sendPacket(new PledgeCrestPacket(imageId, image));
		}
		sb.replaceAll("%playername%", player.getName());
		if(_npcObjId != 0)
		{
			sb.replaceAll("%objectId%", String.valueOf(_npcObjId));
			if(_npc != null)
				sb.replaceAll("%npcId%", String.valueOf(_npc.getNpcId()));
		}

		addGlobalVariables();

		content = VelocityUtils.evaluate(sb.toString(), _variables);
		sb.clear();
		if(!content.startsWith("<html>"))
		{
			sb.append("<html><body>");
			sb.append(content);
			sb.append("</body></html>");
			return sb;
		}
		return content;
	}

	static
	{
		_log = LoggerFactory.getLogger(HtmlMessage.class);
	}

	private void addGlobalVariables() {
		addVar("gveDiscounts", GveTimeDiscountConfig.class);
	}
}

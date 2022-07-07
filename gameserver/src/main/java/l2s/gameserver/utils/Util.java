package l2s.gameserver.utils;

import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.handler.bbs.BbsHandlerHolder;
import l2s.gameserver.handler.bbs.IBbsHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;
import l2s.gameserver.templates.item.ItemTemplate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class Util {
    private static final double SCALE = Math.pow(10, 5);
    static final String PATTERN = "0.0000000000E00";
    static final DecimalFormat df;
    private static final Logger _log = LoggerFactory.getLogger(Util.class);
    /**
     * Форматтер для адены.<br>
     * Locale.KOREA заставляет его фортматировать через ",".<br>
     * Locale.FRANCE форматирует через " "<br>
     * Для форматирования через "." убрать с аргументов Locale.FRANCE
     */
    private static final NumberFormat adenaFormatter;

	static
	{
		adenaFormatter = NumberFormat.getIntegerInstance(Locale.FRANCE);
		df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
		df.applyPattern(PATTERN);
		df.setPositivePrefix("+");
	}

	/**
	 * Проверяет строку на соответсвие регулярному выражению
	 * @param text Строка-источник
	 * @param template Шаблон для поиска
	 * @return true в случае соответвия строки шаблону
	 */
	public static boolean isMatchingRegexp(String text, String template)
	{
		Pattern pattern = null;
		try
		{
			pattern = Pattern.compile(template);
		}
		catch(PatternSyntaxException e) // invalid template
		{
			e.printStackTrace();
		}
		if(pattern == null)
			return false;
		Matcher regexp = pattern.matcher(text);
		return regexp.matches();
	}

	public static String formatDouble(double x, String nanString, boolean forceExponents)
	{
		if(Double.isNaN(x))
			return nanString;
		if(forceExponents)
			return df.format(x);
		if((long) x == x)
			return String.valueOf((long) x);
		return String.valueOf(x);
	}

	/**
	 * Return amount of adena formatted with " " delimiter
	 * @param amount
	 * @return String formatted adena amount
	 */
	public static String formatAdena(long amount)
	{
		return adenaFormatter.format(amount);
	}

	/**
	 * форматирует время в секундах в дни/часы/минуты/секунды
	 */
	public static String formatTime(int time)
	{
		if(time == 0)
			return "now";
		time = Math.abs(time);
        long numDays = time / 86400;
		time -= numDays * 86400;
		long numHours = time / 3600;
		time -= numHours * 3600;
		long numMins = time / 60;
		time -= numMins * 60;
		long numSeconds = time;
        String ret = "";
        if(numDays > 0)
			ret += numDays + "d ";
		if(numHours > 0)
			ret += numHours + "h ";
		if(numMins > 0)
			ret += numMins + "m ";
		if(numSeconds > 0)
			ret += numSeconds + "s";
		return ret.trim();
	}

	/**
	 * форматирует время в секундах в дни/часы/минуты/секунды
	 */
	public static String formatTimeDot(int time)
	{
		if(time == 0)
			return "---";
		time = Math.abs(time);
        long numDays = time / 86400;
		time -= numDays * 86400;
		long numHours = time / 3600;
		time -= numHours * 3600;
		long numMins = time / 60;
		time -= numMins * 60;
		long numSeconds = time;
        String ret = "";
        if(numDays > 0)
			ret += numDays + ":";
		if(numHours > 0)
			ret += numHours + ":";
		if(numMins > 0)
			ret += format(numMins);
		if(numSeconds >= 0)
			ret += ":" + format(numSeconds);
		return ret.trim();
	}

	private static String format(long number)
	{
		if(number < 10)
			return "0" + number;
		else
			return String.valueOf(number);
	}

	public static int packInt(int[] a, int bits) throws Exception
	{
		int m = 32 / bits;
		if(a.length > m)
			throw new Exception("Overflow");

		int result = 0;
        int mval = (int) Math.pow(2, bits);
		for(int i = 0; i < m; i++)
		{
			result <<= bits;
            int next;
            if(a.length > i)
			{
				next = a[i];
				if(next >= mval || next < 0)
					throw new Exception("Overload, value is out of range");
			}
			else
				next = 0;
			result += next;
		}
		return result;
	}

	public static int[] unpackInt(int a, int bits)
	{
		int m = 32 / bits;
		int mval = (int) Math.pow(2, bits);
		int[] result = new int[m];
        for(int i = m; i > 0; i--)
		{
            int next = a;
            a = a >> bits;
			result[i - 1] = next - a * mval;
		}
		return result;
	}

	/** Just alias */
	public static String joinStrings(String glueStr, String[] strings, int startIdx, int maxCount)
	{
		return Strings.joinStrings(glueStr, strings, startIdx, maxCount);
	}

	/** Just alias */
	public static String joinStrings(String glueStr, String[] strings, int startIdx)
	{
		return Strings.joinStrings(glueStr, strings, startIdx, -1);
	}

	public static boolean isNumber(String s)
	{
		return NumberUtils.isNumber(s);
	}

	public static String dumpObject(Object o, boolean simpleTypes, boolean parentFields, boolean ignoreStatics)
	{
		Class<?> cls = o.getClass();
		String result = "[" + (simpleTypes ? cls.getSimpleName() : cls.getName()) + "\n";
        List<Field> fields = new ArrayList<>();
		while(cls != null)
		{
			for(Field fld : cls.getDeclaredFields())
				if(!fields.contains(fld))
				{
					if(ignoreStatics && Modifier.isStatic(fld.getModifiers()))
						continue;
					fields.add(fld);
				}
			cls = cls.getSuperclass();
			if(!parentFields)
				break;
		}

		for(Field fld : fields)
		{
			fld.setAccessible(true);
            String val;
            try
			{
                Object fldObj = fld.get(o);
                if(fldObj == null)
					val = "NULL";
				else
					val = fldObj.toString();
			}
			catch(Throwable e)
			{
				e.printStackTrace();
				val = "<ERROR>";
			}
            String type = simpleTypes ? fld.getType().getSimpleName() : fld.getType().toString();

            result += String.format("\t%s [%s] = %s;\n", fld.getName(), type, val);
		}

		result += "]\n";
		return result;
	}

	private static final Pattern _pattern = Pattern.compile("<!--(TEMPLATE|TEMPLET)(\\d+)(.*?)(TEMPLATE|TEMPLET)-->", Pattern.DOTALL);

	public static HashMap<Integer, String> parseTemplate(String html)
	{
		Matcher m = _pattern.matcher(html);
		HashMap<Integer, String> tpls = new HashMap<>();
		while(m.find())
		{
			tpls.put(Integer.parseInt(m.group(2)), m.group(3));
			html = html.replace(m.group(0), "");
		}

		tpls.put(0, html);
		return tpls;
	}

	public static String formatPay(Player player, long count, int item)
	{
		return formatPay(player.getLanguage(), count, item);
	}

	public static String formatPay(Language lang, long count, int item)
	{
		if(count > 0)
			return formatAdena(count) + " " + getItemName(item);
		else
			return new CustomMessage("price.free").toString(lang);
	}

	public static String getItemIcon(int itemId)
	{
		return ItemHolder.getInstance().getTemplate(itemId).getIcon();
	}

	public static String getItemName(int itemId)
	{
		if(itemId == ItemTemplate.ITEM_ID_FAME)
			return "Fame";
		else if(itemId == ItemTemplate.ITEM_ID_PC_BANG_POINTS)
			return "PC bang Point";
		else if(itemId == ItemTemplate.ITEM_ID_CLAN_REPUTATION_SCORE)
			return "Clan Reputation score";
		else
			return ItemHolder.getInstance().getTemplate(itemId).getName();
	}

	public static boolean getPay(Player player, int itemid, long count, boolean sendMessage)
	{
		if(count == 0)
			return true;
		else if(player.isGM())
		{
			player.sendMessage("Cost " + formatPay(player, count, itemid) + " but for gm is free.");
			return true;
		}

		boolean check = false;
		switch(itemid)
		{
			case ItemTemplate.ITEM_ID_CLAN_REPUTATION_SCORE:
				if(player.getClan() != null && player.getClan().getLevel() >= 5 && player.getClan().getLeader().isClanLeader() && player.getClan().getReputationScore() >= count)
				{
					player.getClan().incReputation((int) -count, false, "Util.GetPay");
					check = true;
				}
				break;
			case ItemTemplate.ITEM_ID_PC_BANG_POINTS:
				if(player.getPcBangPoints() >= count)
				{
					if(player.reducePcBangPoints((int) count))
						check = true;
				}
				break;
			default:
				if(player.getInventory().getCountOf(itemid) >= count)
				{
					if(player.getInventory().destroyItemByItemId(itemid, count))
						check = true;
				}
				break;
		}

		if(!check)
		{
			if(sendMessage)
				enoughtItem(player, itemid, count);
			return false;
		}
		else
		{
			if(sendMessage)
				player.sendMessage(new CustomMessage("util.getpay").addString(formatPay(player, count, itemid)));
			return true;
		}
	}

	private static void enoughtItem(Player player, int itemid, long count)
	{
		player.sendPacket(new ExShowScreenMessage(new CustomMessage("util.enoughItemCount").addString(formatPay(player, count, itemid)).toString(player), 5000, ScreenMessageAlign.TOP_CENTER, true, 1, -1, false));
		player.sendMessage(new CustomMessage("util.enoughItemCount").addString(formatPay(player, count, itemid)));
	}

	public static String declension(Language lang, long count, DeclensionKey word)
	{
		String one = "", two = "", five = "";

		switch(word)
		{
			case DAYS:
				one = new CustomMessage("common.declension.day.1").toString(lang);
				two = new CustomMessage("common.declension.day.2").toString(lang);
				five = new CustomMessage("common.declension.day.5").toString(lang);
				break;
			case HOUR:
				one = new CustomMessage("common.declension.hour.1").toString(lang);
				two = new CustomMessage("common.declension.hour.2").toString(lang);
				five = new CustomMessage("common.declension.hour.5").toString(lang);
				break;
			case MINUTES:
				one = new CustomMessage("common.declension.minutes.1").toString(lang);
				two = new CustomMessage("common.declension.minutes.2").toString(lang);
				five = new CustomMessage("common.declension.minutes.5").toString(lang);
				break;
			case SECONDS:
				one = new CustomMessage("common.declension.seconds.1").toString(lang);
				two = new CustomMessage("common.declension.seconds.2").toString(lang);
				five = new CustomMessage("common.declension.seconds.5").toString(lang);
				break;
			case PIECE:
				one = new CustomMessage("common.declension.piece.1").toString(lang);
				two = new CustomMessage("common.declension.piece.2").toString(lang);
				five = new CustomMessage("common.declension.piece.5").toString(lang);
				break;
			case POINT:
				one = new CustomMessage("common.declension.point.1").toString(lang);
				two = new CustomMessage("common.declension.point.2").toString(lang);
				five = new CustomMessage("common.declension.point.5").toString(lang);
				break;

		}

		if(count > 100)
			count %= 100;

		if(count > 20)
			count %= 10;

		if(count == 1)
			return one;
		else if(count == 2 || count == 3 || count == 4)
			return two;
		else
			return five;
	}

	public static long addDay(long count)
	{
		long DAY = count * 1000 * 60 * 60 * 24;
		return DAY;
	}

	public static String getRaceIcon(ClassId Class)
	{
		if(Class.isOfRace(Race.HUMAN))
			return "icon.skillhuman";
		else if(Class.isOfRace(Race.DARKELF))
			return "icon.skilldarkelf";
		else if(Class.isOfRace(Race.DWARF))
			return "icon.skilldwarf";
		else if(Class.isOfRace(Race.ELF))
			return "icon.skillelf";
		else if(Class.isOfRace(Race.ORC))
			return "icon.skillorc";
		else
			return "icon.NOIMAGE";
	}

	public static String getRaceName(Player player, ClassId Class)
	{
		if(Class.isOfRace(Race.HUMAN))
			return new CustomMessage("utils.race.human").toString(player);
		else if(Class.isOfRace(Race.DARKELF))
			return new CustomMessage("utils.race.darkelf").toString(player);
		else if(Class.isOfRace(Race.DWARF))
			return new CustomMessage("utils.race.dwarf").toString(player);
		else if(Class.isOfRace(Race.ELF))
			return new CustomMessage("utils.race.elf").toString(player);
		else if(Class.isOfRace(Race.ORC))
			return new CustomMessage("utils.race.orc").toString(player);
		else
			return new CustomMessage("utils.classId.name.default").toString(player);
	}

	// Список профессии для вывода на всех языках методам посылки пакета CustomMessage.
	public static String className(Player player, int id)
	{
		return className(player.getLanguage(), id);
	}

	public static String className(Language lang, int id)
	{
		if(id < 0 || id > 136 || (id > 118 && id < 123) || (id > 57 && id < 88))
			return new CustomMessage("utils.classId.name.default").toString(lang);
		else
			return new CustomMessage("utils.classId.name." + id).toString(lang);
	}

	public static String getFortName(Player player, int id)
	{
		return getFortName(player.getLanguage(), id);
	}

	public static String getFortName(Language lang, int id)
	{
		return new CustomMessage("common.fort." + id).toString(lang);
	}

	public static void communityNextPage(Player player, String link)
	{
		IBbsHandler handler = BbsHandlerHolder.getInstance().getCommunityHandler(link);
		if(handler != null)
			handler.onBypassCommand(player, link);
	}

	public static String ArrayToString(String[] array, int start)
	{
		String text = "";

		if(array.length > 1)
		{
			int count = 1;
			for(int i = start; i < array.length; i++)
			{
				text += (count > 1 ? " " : "") + array[i];
				count++;
			}
		}
		else
			text = array[start];

		return text;
	}

	public static boolean getClanPay(Player player, int itemid, long price, boolean b)
	{
		Clan clan;

		if((clan = player.getClan()) == null)
			return false;

		long wh = clan.getWarehouse().getCountOf(itemid);
		if(clan.getWarehouse().getCountOf(itemid) >= price)
		{
			clan.getWarehouse().destroyItemByItemId(itemid, price);
			return true;
		}
		else
		{
			long enought = price - wh;
			enoughtItem(player, itemid, enought);
			return false;
		}
	}

	public static int parseColor(String color)
	{
		color = color.substring(4, 6) + color.substring(2, 4) + color.substring(0, 2);
		return Integer.decode("0x" + color);
	}

	public static int calcLineSize(int curr, int max, int line)
	{
		int result = line + (line * (curr * 100 / max)) / 100 - line;
		return result < 1 ? 1 : result;
	}

	public static double cutOff(double num, int pow)
	{
		return ((int) (num * Math.pow(10, pow))) / Math.pow(10, pow);
	}

	public static final String asHex(byte[] raw)
	{
		StringBuilder strbuf = new StringBuilder(raw.length * 2);
		for(int i = 0; i < raw.length; i++)
		{
			if((raw[i] & 0xFF) < 16)
				strbuf.append("0");
			strbuf.append(Long.toString(raw[i] & 0xFF, 16));
		}
		return strbuf.toString();
	}

	public static boolean isDigit(String text) {
		return text != null && text.matches("[0-9]+");
	}

	public static String RGBtoBGR(String color) {
		String colorOut = "";
		if (color != null && color.length() == 6) {
			colorOut = color.substring(4, 6) + color.substring(2, 4) + color.substring(0, 2);
		}
		return colorOut;
	}

    public static int[] objectToIntArray(List<Creature> list) {
        list = list.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (list == null || list.isEmpty()) {
            return ArrayUtils.EMPTY_INT_ARRAY;
        }
        final int[] tmp = new int[list.size()];
        int i = 0;
        for (final Creature cr : list) {
            tmp[i++] = cr.getObjectId();
        }
        return tmp;
    }

    /**
     * Method return value after reduce scaling
     * with upper approximation
     *
     * @param value
     * @return
     */
    public static double scaleValue(double value) {
        return (value * SCALE + 1) / SCALE;
    }
}
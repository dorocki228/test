package l2s.gameserver.network.l2.components;

import l2s.gameserver.data.string.StringsHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.utils.Language;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CustomMessage implements IBroadcastPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomMessage.class);

    private static final String[] PARAMS = {"{0}", "{1}", "{2}", "{3}", "{4}", "{5}", "{6}", "{7}", "{8}", "{9}"};

    private final String address;
    private List<Object> args;

    public CustomMessage(String address) {
        this.address = address;
    }

    public CustomMessage addString(String arg) {
        if (args == null)
            args = new ArrayList<>();
        args.add(arg);
        return this;
    }

    public CustomMessage addNumber(int i) {
        return addString(String.valueOf(i));
    }

    public CustomMessage addNumber(long l) {
        return addString(String.valueOf(l));
    }

    public CustomMessage addCustomMessage(CustomMessage msg) {
        if (args == null)
            args = new ArrayList<>();
        args.add(msg);
        return this;
    }

    public String toString(Player player) {
        return toString(player.getLanguage());
    }

    public String toString(Language lang) {
        String text = StringsHolder.getInstance().getString(address, lang);

        TextStringBuilder msg = new TextStringBuilder(text);
        if (args != null)
            for (int i = 0; i < args.size(); ++i) {
                Object arg = args.get(i);
                if (arg instanceof CustomMessage)
                    msg.replaceFirst(PARAMS[i], ((CustomMessage) arg).toString(lang));
                else
                    msg.replaceFirst(PARAMS[i], String.valueOf(arg));
            }

        if (StringUtils.isEmpty(msg)) {
            LOGGER.warn("CustomMessage: string: " + address + " not found for lang: " + lang + "!");
            return "";
        }

        return msg.toString();
    }

    @Override
    public L2GameServerPacket packet(Player player) {
        return new SystemMessagePacket(SystemMsg.S1).addString(toString(player));
    }
}

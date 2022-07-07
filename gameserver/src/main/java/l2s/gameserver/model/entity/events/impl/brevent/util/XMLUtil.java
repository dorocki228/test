package l2s.gameserver.model.entity.events.impl.brevent.util;

import l2s.gameserver.utils.Location;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
/**
 * @author : Nami
 * @date : 20.06.2018
 * @time : 18:22
 * <p/>
 */
public class XMLUtil {
    public static String getAttributeValue(Node n, String item) {
        final Node d = n.getAttributes().getNamedItem(item);
        if (d == null) {
            return StringUtils.EMPTY;
        }
        final String val = d.getNodeValue();
        if (val == null) {
            return StringUtils.EMPTY;
        }
        return val;
    }

    public static boolean getAttributeBooleanValue(Node n, String item, boolean dflt) {
        final Node d = n.getAttributes().getNamedItem(item);
        if (d == null) {
            return dflt;
        }
        final String val = d.getNodeValue();
        if (val == null) {
            return dflt;
        }
        return Boolean.parseBoolean(val);
    }

    public static int getAttributeIntValue(Node n, String item, int dflt) {
        final Node d = n.getAttributes().getNamedItem(item);
        if (d == null) {
            return dflt;
        }
        final String val = d.getNodeValue();
        if (val == null) {
            return dflt;
        }
        return Integer.parseInt(val);
    }

    public static long getAttributeLongValue(Node n, String item, long dflt) {
        final Node d = n.getAttributes().getNamedItem(item);
        if (d == null) {
            return dflt;
        }
        final String val = d.getNodeValue();
        if (val == null) {
            return dflt;
        }
        return Long.parseLong(val);
    }

    public static String get(Node n, String item) {
        final Node d = n.getAttributes().getNamedItem(item);
        if (d == null) {
            return StringUtils.EMPTY;
        }
        final String val = d.getNodeValue();
        if (val == null) {
            return StringUtils.EMPTY;
        }
        return val;
    }

    public static boolean get(Node n, String item, boolean dflt) {
        final Node d = n.getAttributes().getNamedItem(item);
        if (d == null) {
            return dflt;
        }
        final String val = d.getNodeValue();
        if (val == null) {
            return dflt;
        }
        return Boolean.parseBoolean(val);
    }

    public static byte get(Node n, String item, byte dflt) {
        final Node d = n.getAttributes().getNamedItem(item);
        if (d == null) {
            return dflt;
        }
        final String val = d.getNodeValue();
        if (val == null) {
            return dflt;
        }
        return Byte.parseByte(val);
    }

    public static int get(Node n, String item, int dflt) {
        final Node d = n.getAttributes().getNamedItem(item);
        if (d == null) {
            return dflt;
        }
        final String val = d.getNodeValue();
        if (val == null) {
            return dflt;
        }
        return Integer.parseInt(val);
    }

    public static long get(Node n, String item, long dflt) {
        final Node d = n.getAttributes().getNamedItem(item);
        if (d == null) {
            return dflt;
        }
        final String val = d.getNodeValue();
        if (val == null) {
            return dflt;
        }
        return Long.parseLong(val);
    }

    public static double get(Node n, String item, double dflt) {
        final Node d = n.getAttributes().getNamedItem(item);
        if (d == null) {
            return dflt;
        }
        final String val = d.getNodeValue();
        if (val == null) {
            return dflt;
        }
        return Double.parseDouble(val);
    }

    public static int[] get(Node n, String item, int[] dflt) {
        final Node d = n.getAttributes().getNamedItem(item);
        if (d == null) {
            return dflt;
        }

        final String val = d.getNodeValue();
        if (val == null) {
            return dflt;
        }

        String strs[] = get(n, item).split(";");
        int values[] = new int[strs.length];

        for (int i = 0; i < strs.length; i++) {
            values[i] = Integer.parseInt(strs[i]);
        }

        return values;
    }

    public static Location get(Node n, String item, Location dflt) {
        final Node d = n.getAttributes().getNamedItem(item);
        if (d == null) {
            return dflt;
        }
        final String val = d.getNodeValue();
        if (val == null) {
            return dflt;
        }

        String strs[] = get(n, item).split(";");
        int values[] = new int[strs.length];

        for (int i = 0; i < strs.length; i++) {
            values[i] = Integer.parseInt(strs[i]);
        }
        return new Location(values[0], values[1], values[2]);
    }

    public static <E extends Enum<E>> E get(Node n, String item, Class<E> enumClass) {
        final Node d = n.getAttributes().getNamedItem(item);
        if (d == null) {
            return null;
        }
        final Object val = d.getNodeValue();
        if (val == null) {
            return null;
        }

        if(val != null && enumClass.isInstance(val))
            return (E) val;
        if(val instanceof String)
            return Enum.valueOf(enumClass, (String) val);

        throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + " required, but found: " + val + "!");
    }

    public static boolean isNodeName(Node node, String name) {
        return node != null && node.getNodeName().equals(name);
    }
}

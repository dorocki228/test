package l2s.gameserver.network.l2.components.hwid;

import com.google.common.base.Suppliers;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * @author Java-man
 * @since 28.03.2019
 *
 * For test server.
 */
public final class EmptyHwidHolder implements HwidHolder {
    private final byte[] hwid;
    private final Supplier<String> stringSupplier;
    private final Supplier<Integer> hashCodeSupplier;

    public EmptyHwidHolder(String hwid) {
        this.hwid = hwid.getBytes(StandardCharsets.UTF_8);
        stringSupplier = Suppliers.ofInstance(hwid);
        hashCodeSupplier = Suppliers.memoize(() -> generateHashCode(this.hwid));
    }

    private static int generateHashCode(byte[] hwid) {
        int result = 1;
        for (byte element : hwid) {
            result = 257 * result + element;
        }
        return result;
    }

    @Override
    public byte[] asByteArray() {
        return Arrays.copyOf(hwid, hwid.length);
    }

    @Override
    public String asString() {
        return stringSupplier.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof EmptyHwidHolder) {
            EmptyHwidHolder hwidHolder1 = (EmptyHwidHolder) o;
            return Arrays.equals(hwid, hwidHolder1.hwid);
        }
        if (o instanceof HwidHolder) {
            HwidHolder hwidHolder1 = (HwidHolder) o;
            return Arrays.equals(hwid, hwidHolder1.asByteArray());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashCodeSupplier.get();
    }

    @Override
    public String toString() {
        return "EmptyHwidHolder{" +
                "hwid=" + asString() +
                '}';
    }
}
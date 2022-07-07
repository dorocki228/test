package l2s.gameserver.network.l2.components.hwid;

import com.google.common.base.Suppliers;
import com.google.common.hash.HashCode;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * @author Java-man
 * @since 28.03.2019
 */
public final class DefaultHwidHolder implements HwidHolder {
    private final byte[] hwid;
    private final Supplier<String> stringSupplier;
    private final Supplier<Integer> hashCodeSupplier;

    public DefaultHwidHolder(byte[] hwid) {
        this.hwid = Arrays.copyOf(hwid, hwid.length);
        stringSupplier = Suppliers.memoize(() -> HashCode.fromBytes(this.hwid).toString());
        hashCodeSupplier = Suppliers.memoize(() -> generateHashCode(this.hwid));
    }

    public DefaultHwidHolder(String hwid) {
        this.hwid = HashCode.fromString(hwid).asBytes();
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
        if (o instanceof DefaultHwidHolder) {
            DefaultHwidHolder hwidHolder1 = (DefaultHwidHolder) o;
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
        return "DefaultHwidHolder{" +
                "hwid=" + asString() +
                '}';
    }
}
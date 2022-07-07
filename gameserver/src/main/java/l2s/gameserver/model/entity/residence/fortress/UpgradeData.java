package l2s.gameserver.model.entity.residence.fortress;

public class UpgradeData {
    /**
     * Временный бланк
     */
    private String param;
    private long price;

    public UpgradeData(final String param, final long price) {
        this.param = param;
        this.price = price;
    }

    public String getParam() {
        return param;
    }

    public long getPrice() {
        return price;
    }
}

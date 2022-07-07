package l2s.authserver.network.gamecomm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class GameServerDescription {
    private final Map<Integer, HostInfo> _hosts;
    private int _serverType;
    private int _ageLimit;
    private int _protocol;
    private boolean _isOnline;
    private boolean _isPvp;
    private boolean _isShowingBrackets;
    private boolean _isGmOnly;
    private int _maxPlayers;

    private final Set<String> _accounts;

    private final String ipAddress;

    public GameServerDescription(String ipAddress) {
        this.ipAddress = ipAddress;
        _hosts = new HashMap<>();
        _accounts = new CopyOnWriteArraySet<>();
    }

    public void addHost(HostInfo host) {
        _hosts.put(host.getId(), host);
    }

    public HostInfo removeHost(int id) {
        return _hosts.remove(id);
    }

    public HostInfo[] getHosts() {
        return _hosts.values().toArray(new HostInfo[0]);
    }

    public void setMaxPlayers(int maxPlayers) {
        _maxPlayers = maxPlayers;
    }

    public int getMaxPlayers() {
        return _maxPlayers;
    }

    public int getOnline() {
        return _accounts.size();
    }

    public Set<String> getAccounts() {
        return _accounts;
    }

    public void addAccount(String account) {
        _accounts.add(account);
    }

    public void removeAccount(String account) {
        _accounts.remove(account);
    }

    public int getServerType() {
        return _serverType;
    }

    public boolean isOnline() {
        return _isOnline;
    }

    public void setOnline(boolean online) {
        _isOnline = online;
    }

    public void setServerType(int serverType) {
        _serverType = serverType;
    }

    public boolean isPvp() {
        return _isPvp;
    }

    public void setPvp(boolean pvp) {
        _isPvp = pvp;
    }

    public boolean isShowingBrackets() {
        return _isShowingBrackets;
    }

    public void setShowingBrackets(boolean showingBrackets) {
        _isShowingBrackets = showingBrackets;
    }

    public boolean isGmOnly() {
        return _isGmOnly;
    }

    public void setGmOnly(boolean gmOnly) {
        _isGmOnly = gmOnly;
    }

    public int getAgeLimit() {
        return _ageLimit;
    }

    public void setAgeLimit(int ageLimit) {
        _ageLimit = ageLimit;
    }

    public int getProtocol() {
        return _protocol;
    }

    public void setProtocol(int protocol) {
        _protocol = protocol;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setDown() {
        setOnline(false);
        _accounts.clear();
    }

    public static class HostInfo {
        private final int _id;
        private final String _ip;
        private final String _innerIP;
        private final int _port;
        private final String _key;

        public HostInfo(int id, String ip, String innerIP, int port, String key) {
            _id = id;
            _ip = ip;
            _innerIP = innerIP;
            _port = port;
            _key = key;
        }

        public int getId() {
            return _id;
        }

        public String getIP() {
            return _ip;
        }

        public String getInnerIP() {
            return _innerIP;
        }

        public int getPort() {
            return _port;
        }

        public String getKey() {
            return _key;
        }
    }
}

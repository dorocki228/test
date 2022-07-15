package l2s.commons.ban;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 10.04.2019
 * Developed for L2-Scripts.com
 **/
public class BanManager {
	private final Map<BanBindType, Map<String, BanInfo>> cachedBans = new HashMap<>();

	public Map<BanBindType, Map<String, BanInfo>> getCachedBans() {
		return cachedBans;
	}

	public BanInfo getBanInfoIfBanned(BanBindType bindType, Object bindValueObj) {
		String bindValue = String.valueOf(bindValueObj);
		if (StringUtils.isEmpty(bindValue))
			return null;

		Map<String, BanInfo> bans = getCachedBans().get(bindType);
		if (bans == null)
			return null;

		BanInfo banInfo = bans.get(bindValue);
		if (banInfo == null)
			return null;

		if(banInfo.getEndTime() != -1 && banInfo.getEndTime() < (System.currentTimeMillis() / 1000))
			return null;

		return banInfo;
	}

	public boolean isBanned(BanBindType bindType, Object bindValueObj) {
		return getBanInfoIfBanned(bindType, bindValueObj) != null;
	}
}

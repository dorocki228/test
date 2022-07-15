package l2s.commons.ban;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 11.04.2019
 * Developed for L2-Scripts.com
 **/
public class BanInfo {
	private final int endTime;
	private final String reason;

	public BanInfo(int endTime, String reason) {
		this.endTime = endTime;
		this.reason = reason;
	}

	public int getEndTime() {
		return endTime;
	}

	public String getReason() {
		return reason;
	}
}

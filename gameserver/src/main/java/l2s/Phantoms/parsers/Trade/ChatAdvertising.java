package l2s.Phantoms.parsers.Trade;

public class ChatAdvertising
{
	private String _ads_text;
	private int _ads_chance;
	private int _ads_time;
	
	public ChatAdvertising(int ads_time,int ads_chance)
	{
		_ads_time = ads_time;
		_ads_chance = ads_chance;
	}
	
	public int getAdsChance()
	{
		return _ads_chance;
	}
	
	public int getAdsTime()
	{
		return _ads_time;
	}

	public String getAdsText() 
	{
		return _ads_text;
	}

	public void setAdsText(String _ads_text) 
	{
		this._ads_text = _ads_text;
	}
}

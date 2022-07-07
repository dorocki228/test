package l2s.gameserver.templates.gve;

public class SimpleReward
{
	private int exp;
	private int sp;
	private int adena;

	public SimpleReward()
	{
        exp = 0;
        sp = 0;
        adena = 0;
	}

	public SimpleReward(int exp, int sp, int adena)
	{
		this.exp = exp;
		this.sp = sp;
		this.adena = adena;
	}

	public void clear()
	{
        exp = 0;
        sp = 0;
        adena = 0;
	}

	public int getExp()
	{
		return exp;
	}

	public int getSp()
	{
		return sp;
	}

	public int getAdena()
	{
		return adena;
	}

	public void addReward(SimpleReward reward)
	{
        exp += reward.getExp();
        sp += reward.getSp();
        adena += reward.getAdena();
	}
}

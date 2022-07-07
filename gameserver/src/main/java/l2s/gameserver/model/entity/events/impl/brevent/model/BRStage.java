package l2s.gameserver.model.entity.events.impl.brevent.model;

/**
 * @author : Nami
 * @date : 21.06.2018
 * @time : 17:08
 * <p/>
 */
public class BRStage {
    private final int stageNumber;
    private final int safeTime;
    private final int runTime;
    private final int damageInitial;
    private final int damageFinal;
    private final int radius;

    public BRStage(int stageNumber, int safeTime, int runTime, int damageInitial, int damageFinal, int radius)
    {
        this.stageNumber = stageNumber;
        this.safeTime = safeTime;
        this.runTime = runTime;
        this.damageInitial = damageInitial;
        this.damageFinal = damageFinal;
        this.radius = radius;
    }

    public int getStageNumber()
    {
        return stageNumber;
    }

    public int getSafeTime()
    {
        return safeTime;
    }

    public int getRunTime()
    {
        return runTime;
    }

    public int getDamageInitial()
    {
        return damageInitial;
    }

    public int getDamageFinal()
    {
        return damageFinal;
    }

    public int getRadius()
    {
        return radius;
    }
}

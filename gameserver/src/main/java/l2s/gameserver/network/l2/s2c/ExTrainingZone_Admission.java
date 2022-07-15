package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.actor.instances.player.TrainingCamp;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Sdw
 */
public class ExTrainingZone_Admission implements IClientOutgoingPacket
{
	private final int _timeElapsed;
	private final int _timeRemaining;
	private final double _maxExp;
	private final double _maxSp;

	public ExTrainingZone_Admission(int level, int timeElapsed, int timeRemaing)
	{
		_timeElapsed = timeElapsed;
		_timeRemaining = timeRemaing;

		double experience = (Experience.getExpForLevel(level) * Experience.getTrainingRate(level)) / TrainingCamp.TRAINING_DIVIDER;
		_maxExp = experience * Config.RATE_XP_BY_LVL[level];
		_maxSp = (experience * Config.RATE_SP_BY_LVL[level]) / 250d;
	}

	public ExTrainingZone_Admission(TrainingCamp trainingCamp)
	{
		this(trainingCamp.getLevel(), 0, trainingCamp.getMaxDuration());
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_TRAININGZONE_ADMISSION.writeId(packetWriter);
		packetWriter.writeD(_timeElapsed); // Training time elapsed in minutes, max : 600 - 10hr RU / 300 - 5hr NA
		packetWriter.writeD(_timeRemaining); // Time remaining in seconds, max : 36000 - 10hr RU / 18000 - 5hr NA
		packetWriter.writeF(_maxExp); // Training time elapsed in minutes * this value = acquired exp IN GAME DOESN'T SEEM LIKE THE FIELD IS LIMITED
		packetWriter.writeF(_maxSp); // Training time elapsed in minutes * this value = acquired sp IN GAME LIMITED TO INTEGER.MAX_VALUE SO THE MULTIPLY WITH REMAINING TIME CANT EXCEED IT (so field max value can't exceed 3579139.0 for 10hr) !! // Note real sp gain is exp gained / 250

		return true;
	}
}
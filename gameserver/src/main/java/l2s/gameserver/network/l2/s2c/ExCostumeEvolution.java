package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class ExCostumeEvolution implements IClientOutgoingPacket {
	public static final ExCostumeEvolution FAIL = new ExCostumeEvolution(0, 0, 0);

	private final int result;
	private final int resultCostumeId;
	private final int resultCostumeCount;

	public ExCostumeEvolution(int result, int resultCostumeId, int resultCostumeCount) {
		this.result = result;
		this.resultCostumeId = resultCostumeId;
		this.resultCostumeCount = resultCostumeCount;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter) {
		OutgoingExPackets.EX_COSTUME_EVOLUTION.writeId(packetWriter);
		packetWriter.writeC(result); //Result
		packetWriter.writeD(0);  //TargetSize
		//if (TargetSize <= 0) {
			int count = 1;
			packetWriter.writeD(count);  //ResultSize
			for (int i = 0; i < count; i++) {
				packetWriter.writeD(resultCostumeId);    //CostumeID
				packetWriter.writeQ(resultCostumeCount);    //Amount
			}
		/*}
		else {
			for (TargetSize) {
				"d" //CostumeID
				"Q" //Amount
			}
		}*/

		return true;
	}
}
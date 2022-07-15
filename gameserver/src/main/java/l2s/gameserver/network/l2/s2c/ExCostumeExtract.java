package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class ExCostumeExtract implements IClientOutgoingPacket {
	public static ExCostumeExtract FAIL = new ExCostumeExtract(0, 0, 0, 0, 0, 0);

	private final int result;
	private final int extractItemId;
	private final long extractItemCount;
	private final int resultItemId;
	private final long resultItemCount;
	private final long totalCount;

	public ExCostumeExtract(int result, int extractItemId, long extractItemCount, int resultItemId, long resultItemCount, long totalCount) {
		this.result = result;
		this.extractItemId = extractItemId;
		this.extractItemCount = extractItemCount;
		this.resultItemId = resultItemId;
		this.resultItemCount = resultItemCount;
		this.totalCount = totalCount;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter) {
		OutgoingExPackets.EX_COSTUME_EXTRACT.writeId(packetWriter);
		packetWriter.writeC(result);   //Result
		packetWriter.writeD(extractItemId);   //ExtractItemID
		packetWriter.writeQ(extractItemCount);   //ExtractItemNum
		packetWriter.writeD(resultItemId);   //ResultItemClassID
		packetWriter.writeQ(resultItemCount);   //ResultItemNum
		packetWriter.writeQ(totalCount);   //TotalNum*/

		return true;
	}
}
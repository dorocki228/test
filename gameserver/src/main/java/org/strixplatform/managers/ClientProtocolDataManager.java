package org.strixplatform.managers;

import org.strixplatform.configs.MainConfig;
import org.strixplatform.logging.Log;
import org.strixplatform.network.ReadDataBuffer;
import org.strixplatform.utils.DataUtils;
import org.strixplatform.utils.ServerResponse;
import org.strixplatform.utils.StrixClientData;

public class ClientProtocolDataManager
{
	public static ClientProtocolDataManager getInstance()
	{
		return LazyHolder.INSTANCE;
	}

	public StrixClientData getDecodedData(byte[] dataArray, final int clientDataChecksum)
	{
		try
		{
			// Validate array and checksum
			if(dataArray == null || dataArray.length < MainConfig.CLIENT_DATA_SIZE)
			{
				Log.error("Received client data nulled or not use Strix-Platform modules(Clear pacth or Strix-Platform not loaded)");
				return null;
			}

			final StrixClientData clientData = new StrixClientData();

			// Decode data
			DataUtils.getDecodedDataFromKey(dataArray, DataUtils.getRealDataChecksum(clientDataChecksum));
			// Checksum check
			final int decodedDataChecksum = DataUtils.getDataChecksum(dataArray, false);
			if(decodedDataChecksum != DataUtils.getRealDataChecksum(clientDataChecksum))
			{
				Log.error("Received client data not valide. Client checksum: " + DataUtils.getRealDataChecksum(clientDataChecksum) + " Decoded checksum: " + decodedDataChecksum);
				clientData.setServerResponse(ServerResponse.RESPONSE_FAILED_CLIENT_DATA_CHECKSUM_CHECK);
				return clientData;
			}

			// Debug
			if(MainConfig.STRIX_PLATFORM_DEBUG_ENABLED)
			{
				String data = "";
				for(int i = 0; i < 192; i++)
				{
					data += (char) dataArray[i];
				}
				Log.debug("ClientProtocolDataManager: first 192 byte " + data);
			}

			// Read data
			final ReadDataBuffer dataBuffer = new ReadDataBuffer(dataArray);
			clientData.setClientHWID(dataBuffer.ReadS()); // HWID
			clientData.setVMPKey(dataBuffer.ReadS()); // VMPKey
			clientData.setHWIDChecksum(dataBuffer.ReadQ()); // HWIDChecksum
			clientData.setDetectionResponse(dataBuffer.ReadQ()); // DetectionInfo
			clientData.setLaunchStateResponse(dataBuffer.ReadQ()); // LaunchState
			clientData.setSessionId(dataBuffer.ReadQ()); // ClientSessionId
			clientData.setFilesChecksum(dataBuffer.ReadQ()); // FilesChecksum
			clientData.setClientSideVersion(dataBuffer.ReadH()); // ClientSideVersion
			clientData.setActiveWindowCount(dataBuffer.ReadH()); // ActiveWindowCount

			ClientGameSessionManager.getInstance().checkClientData(clientData);

			return clientData;
		}
		catch(final Exception e)
		{
			Log.error("Cannot decode Strix data from client. Please send this error and all needed info to Strix-Platform support! Exception: " + e.getLocalizedMessage());
			return null;
		}
	}

	private static class LazyHolder
	{
		private static final ClientProtocolDataManager INSTANCE = new ClientProtocolDataManager();
	}
}
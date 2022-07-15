package l2s.gameserver.model.entity.votereward;

import com.google.common.flogger.FluentLogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * reworked by Bonux
 */
public class VoteApiService {
	private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();

	

	public static String getApiResponse(String endpoint) {
		HttpURLConnection connection = null;
		try {
			StringBuilder stringBuilder = new StringBuilder();
			URL url = new URL(endpoint);
			connection = (HttpURLConnection) url.openConnection();
			connection.addRequestProperty("User-Agent", "Mozilla/5.0");
			connection.setRequestMethod("GET");
			connection.setReadTimeout(5000);
			connection.connect();

			int responseCode = connection.getResponseCode();
			if(responseCode != 200) {
				LOGGER.atWarning().log( "VoteApiService::getApiResponse returned error CODE[%s] LINK[%s]", responseCode, endpoint );
				return null;
			}

			try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())))
			{
				String line;
				while((line = reader.readLine()) != null) {
					stringBuilder.append(line).append("\n");
				}
			}
			return stringBuilder.toString();
		}
		catch (Exception e) {
			LOGGER.atWarning().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Something went wrong in VoteApiService::getApiResponse LINK[%s]", endpoint );
		}
		finally {
			if(connection != null)
				connection.disconnect();
		}
		return null;
	}
}
package org.strixplatform;

import l2s.gameserver.GameServer;
import org.strixplatform.configs.MainConfig;
import org.strixplatform.database.DatabaseManager;
import org.strixplatform.logging.Log;
import org.strixplatform.managers.ClientBanManager;
import org.strixplatform.utils.ThreadPoolManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class StrixPlatform
{
	public static void main(String... args) throws Exception
	{
		getInstance();

		Class<?> clazz = GameServer.class;
		final Method main = clazz.getDeclaredMethod("main", String[].class);

		main.invoke(null, new Object[] { args });
	}

	public static StrixPlatform getInstance()
	{
		return LazyHolder.INSTANCE;
	}

	public boolean isPlatformEnabled()
	{
		return MainConfig.STRIX_PLATFORM_ENABLED;
	}

	public boolean isAuthLogEnabled()
	{
		return MainConfig.STRIX_PLATFORM_ENABLED_AUTHLOG;
	}

	public boolean isBackNotificationEnabled()
	{
		return MainConfig.STRIX_PLATFORM_CLIENT_BACK_NOTIFICATION_ENABLED;
	}

	public boolean isPlatformAntibrute() { return MainConfig.STRIX_PLATFORM_ANTIBRUTE;}

	public boolean isPlatformDraw() { return MainConfig.STRIX_PLATFORM_DRAW;}

	public String isPlatformDrawText() { return MainConfig.STRIX_PLATFORM_DRAW_TEXT;}

	public int getProtocolVersionDataSize()
	{
		return MainConfig.PROTOCOL_VERSION_DATA_SIZE;
	}

	public int getClientDataSize()
	{
		return MainConfig.CLIENT_DATA_SIZE;
	}

	public void checkClientSideVersion()
	{
		if(MainConfig.STRIX_PLATFORM_CHECK_CLIENT_SIDE_VERSION)
		{
			if(MainConfig.STRIX_PLATFORM_MANUAL_CLIENT_SIDE_VERSION < 0)
			{
				BufferedReader in = null;
				try
				{
					final URL url = new URL(MainConfig.STRIX_CLIENT_UPDATE_CHECK_URL);
					final URLConnection conn = url.openConnection();
					conn.setDefaultUseCaches(false);
					conn.setRequestProperty("User-Agent", "StrixPlatform/" + MainConfig.STRIX_PLATFORM_KEY + "/" + MainConfig.STRIX_PLATFORM_SECOND_KEY);
					in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
					String buffer;
					int loadedVersion = -1;
					while((buffer = in.readLine()) != null)
					{
						if(buffer.length() > 3)
						{
							Log.error("Update server Strix-Platform not avaliable on this time, or your firewall or server configuration cannot use out connection to Strix-Platform server. This option seted to DISABLED...");
							MainConfig.STRIX_PLATFORM_CHECK_CLIENT_SIDE_VERSION = false;
							return;
						}
						loadedVersion = Integer.parseInt(buffer);
					}
					MainConfig.CLIENT_SIDE_VERSION_STORED = loadedVersion;
				}
				catch(final Exception e)
				{
					Log.error("Error on check client side version. Please, check your server configuration, firewall, network, etc... Exception: " + e.getLocalizedMessage());
				}
				finally
				{
					if(in != null)
					{
						try
						{
							in.close();
						}
						catch(final Exception e)
						{
							Log.error("Error on close loaded buffer. Send this info to Strix-Platform team support! Exception: " + e.getLocalizedMessage());
						}
					}
				}
			}
			else
			{
				startClientSideVersionCheckThread();
			}
		}
		else
		{
			Log.info("Automatical update check disabled. If needed, see Strix-Platform configuration file from path " + MainConfig.CONFIG_FILE);
		}
	}

	private void startClientSideVersionCheckThread()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run()
			{
				MainConfig.reparseClientSideVersion();
			}
		}, 5000L, 30000L);
	}

	private StrixPlatform()
	{
		try
		{
			Log.info("|============= Strix-Platform =============|");
			MainConfig.init();
			if(MainConfig.STRIX_PLATFORM_ENABLED)
			{
				checkClientSideVersion();
				DatabaseManager.getInstance().getConnection().close();
				ClientBanManager.getInstance();
			}
		}
		catch(final Exception e)
		{
			Log.error("An error occurred during initialization. Disabling protection ...");
			MainConfig.STRIX_PLATFORM_ENABLED = false;
		}
		finally
		{
			Log.info("|============= Strix-Platform =============|");
		}
	}

	private static class LazyHolder
	{
		private static final StrixPlatform INSTANCE = new StrixPlatform();
	}
}
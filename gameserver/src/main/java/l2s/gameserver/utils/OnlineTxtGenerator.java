package l2s.gameserver.utils;

import l2s.gameserver.model.GameObjectsStorage;

import java.io.File;
import java.io.FileWriter;

public class OnlineTxtGenerator implements Runnable
{
	@Override
	public void run()
	{
		try
		{
			File out = new File("data/webserver/online.txt");
			out.delete();
			out.createNewFile();
			FileWriter fw = new FileWriter(out);
			fw.write(String.valueOf(GameObjectsStorage.getPlayers().size()));
			fw.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

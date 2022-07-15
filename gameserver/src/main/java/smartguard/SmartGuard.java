package smartguard;

import l2s.gameserver.GameServer;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.handler.admincommands.AdminCommandHandler;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.World;
import l2s.gameserver.network.l2.GameCryptSmartGuard;
import smartguard.api.ISmartGuardService;
import smartguard.api.integration.DatabaseConnection;
import smartguard.api.integration.ISmartPlayer;
import smartguard.api.integration.IWorldService;
import smartguard.core.utils.LogUtils;
import smartguard.integration.SmartPlayer;
import smartguard.menu.SmartGuardMenu;
import smartguard.spi.SmartGuardSPI;

import java.util.List;
import java.util.stream.Collectors;

public class SmartGuard
{
    public static void main(String[] args)
    {
	    try
	    {
		    if(!SmartGuard.class.getProtectionDomain().getCodeSource().equals(GameCryptSmartGuard.class.getProtectionDomain().getCodeSource()))
		    {
                System.out.println("Error! Library [smrt.jar] is not first in your classpath list, SmartGuard will not work properly!");
			    return;
		    }
	    }
	    catch (Exception e)
	    {}

        /*if (args.length == 0)
        {
            System.out.println("Main class not specified!");
            return;
        }*/

        SmartGuardSPI.setDatabaseService(() ->
                new DatabaseConnection(DatabaseFactory.getInstance().getConnection()));

        SmartGuardSPI.setWorldService(new IWorldService()
        {
            @Override
            public ISmartPlayer getPlayerByObjectId(int i) {
                return new SmartPlayer(World.getPlayer(i));
            }

            @Override
            public List<ISmartPlayer> getAllPlayers() {
                return GameObjectsStorage.getPlayers(false, false).stream().map(SmartPlayer::new).collect(Collectors.toList());
            }
        });

        ISmartGuardService svc = SmartGuardSPI.getSmartGuardService();

        if(!svc.init())
        {
            System.out.println("Failed to init SmartGuard!");
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> SmartGuardSPI.getSmartGuardService().getSmartGuardBus().onShutdown()));

        try
        {
            LogUtils.log("SmartGuard has been initialized.");

            /*Class<?> c = Class.forName(args[0]);
            Method main = c.getDeclaredMethod("main", String[].class);
            String[] mainArgs = Arrays.copyOfRange(args, 1, args.length);
            main.invoke(null, (Object) mainArgs);*/
            GameServer.main(new String[] {});
        }
        catch (Exception e)
        {
            LogUtils.log("GameServer failed to start!");
            LogUtils.log(e);
            return;
        }

        SmartGuardSPI.getSmartGuardService().getSmartGuardBus().onStartup();

        try
        {
            AdminCommandHandler.getInstance().registerAdminCommandHandler(new SmartGuardMenu());
        }
        catch (Exception e)
        {
            LogUtils.log("Error initializing SmartGuard AdminCommandHandler!");
            LogUtils.log(e);
        }
    }
}
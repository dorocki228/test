package l2s.gameserver.model.farm.listener;

import l2s.gameserver.dao.SteadBarnDAO;
import l2s.gameserver.data.xml.holder.SteadDataHolder;
import l2s.gameserver.listener.game.OnShutdownListener;
import l2s.gameserver.model.farm.Stead;

public class OnShutdown implements OnShutdownListener {
    @Override
    public void onShutdown() {
        SteadDataHolder.getInstance().getSteads().forEach(Stead::shutdown);
        SteadBarnDAO.getInstance().save();
    }
}
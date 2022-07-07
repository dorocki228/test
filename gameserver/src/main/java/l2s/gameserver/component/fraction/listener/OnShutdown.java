package l2s.gameserver.component.fraction.listener;

import l2s.gameserver.component.fraction.FractionTreasure;
import l2s.gameserver.listener.game.OnShutdownListener;

public class OnShutdown implements OnShutdownListener {
    @Override
    public void onShutdown() {
        FractionTreasure.getInstance().save();
    }
}
package l2s.gameserver.time

import l2s.commons.listener.ListenerList
import l2s.gameserver.GameServer
import l2s.gameserver.ThreadPoolManager
import l2s.gameserver.listener.GameListener
import l2s.gameserver.listener.game.OnDayNightChangeListener
import l2s.gameserver.listener.game.OnStartListener
import l2s.gameserver.model.GameObjectsStorage
import l2s.gameserver.network.l2.s2c.ClientSetTimePacket
import org.apache.logging.log4j.LogManager
import java.util.*

/**
 * 1 день в игре это 4 часа реального времени
 */
object GameTimeService {

    private val LOGGER = LogManager.getLogger(GameTimeService::class.java)

    private const val TICKS_PER_SECOND = 10
    private const val MILLIS_IN_TICK = 1000 / TICKS_PER_SECOND

    private val gameStartTime: Long

    private val listenerEngine = GameTimeListenerList()

    private val dayChangeNotify = DayChangeNotify()

    /**
     * Вычисляем смещение до начала игровых суток
     *
     * @return смещение в миллисекнду до начала игровых суток (6:00AM)
     */
    private val dayStartTime: Long
        get() {
            val dayStart = Calendar.getInstance()

            val HOUR_OF_DAY = dayStart.get(Calendar.HOUR_OF_DAY)

            dayStart.add(Calendar.HOUR_OF_DAY, -(HOUR_OF_DAY + 1) % 4)
            dayStart.set(Calendar.MINUTE, 0)
            dayStart.set(Calendar.SECOND, 0)
            dayStart.set(Calendar.MILLISECOND, 0)

            return dayStart.timeInMillis
        }

    val isNowNight: Boolean
        get() = gameHour < 6

    val gameTime: Int
        get() = gameTicks / MILLIS_IN_TICK

    val gameHour: Int
        get() = gameTime / 60 % 24

    val gameMin: Int
        get() = gameTime % 60

    val gameTicks: Int
        get() = ((System.currentTimeMillis() - gameStartTime) / MILLIS_IN_TICK).toInt()

    init {
        gameStartTime = dayStartTime

        GameServer.getInstance().addListener(OnStartListenerImpl())

        val msg = StringBuilder()
        msg.append("GameTimeController: initialized.").append(" ")
        msg.append("Current time is ")
        msg.append(gameHour).append(":")
        if (gameMin < 10)
            msg.append("0")
        msg.append(gameMin)
        msg.append(" in the ")
        if (isNowNight)
            msg.append("night")
        else
            msg.append("day")
        msg.append(".")

        LOGGER.info(msg.toString())

        var nightStart: Long = 0

        while (gameStartTime + nightStart < System.currentTimeMillis())
            nightStart += (4 * 60 * 60 * 1000).toLong()

        var dayStart = (60 * 60 * 1000).toLong()
        while (gameStartTime + dayStart < System.currentTimeMillis())
            dayStart += (4 * 60 * 60 * 1000).toLong()

        dayStart -= System.currentTimeMillis() - gameStartTime
        nightStart -= System.currentTimeMillis() - gameStartTime

        ThreadPoolManager.getInstance().scheduleAtFixedRate(dayChangeNotify, nightStart, 4 * 60 * 60 * 1000L)
        ThreadPoolManager.getInstance().scheduleAtFixedRate(dayChangeNotify, dayStart, 4 * 60 * 60 * 1000L)
    }

    private class OnStartListenerImpl : OnStartListener {

        override fun onStart() {
            ThreadPoolManager.getInstance().execute(dayChangeNotify)
        }

    }

    private class DayChangeNotify : Runnable {

        override fun run() {
            if (isNowNight)
                listenerEngine.onNight()
            else
                listenerEngine.onDay()

            for (player in GameObjectsStorage.getPlayers()) {
                player.checkDayNightMessages()
                player.sendPacket(ClientSetTimePacket())
            }
        }

    }

    private class GameTimeListenerList : ListenerList<GameServer>() {

        fun onDay() {
            for (listener in getListeners())
                if (OnDayNightChangeListener::class.java.isInstance(listener))
                    (listener as OnDayNightChangeListener).onDay()
        }

        fun onNight() {
            for (listener in getListeners())
                if (OnDayNightChangeListener::class.java.isInstance(listener))
                    (listener as OnDayNightChangeListener).onNight()
        }

    }

    fun <T : GameListener> addListener(listener: T): Boolean {
        return listenerEngine.add(listener)
    }

    fun <T : GameListener> removeListener(listener: T): Boolean {
        return listenerEngine.remove(listener)
    }

}
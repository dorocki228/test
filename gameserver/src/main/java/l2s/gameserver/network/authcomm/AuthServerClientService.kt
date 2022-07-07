package l2s.gameserver.network.authcomm

import l2s.gameserver.network.l2.GameClient
import l2s.gameserver.security.HwidUtils
import org.apache.commons.lang3.StringUtils
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * @author Java-man
 * @since 17.06.2019
 */
object AuthServerClientService {

    private val waitingClients = mutableMapOf<String, GameClient>()
    private val authedClients = mutableMapOf<String, GameClient>()
    private val readLock: Lock
    private val writeLock: Lock

    init {
        val lock = ReentrantReadWriteLock()
        readLock = lock.readLock()
        writeLock = lock.writeLock()
    }

    fun addWaitingClient(client: GameClient): GameClient? {
        writeLock.lock()
        try {
            return waitingClients.put(client.login, client)
        } finally {
            writeLock.unlock()
        }
    }

    fun removeWaitingClient(account: String): GameClient? {
        writeLock.lock()
        try {
            return waitingClients.remove(account)
        } finally {
            writeLock.unlock()
        }
    }

    fun addAuthedClient(client: GameClient): GameClient? {
        writeLock.lock()
        try {
            return authedClients.put(client.login, client)
        } finally {
            writeLock.unlock()
        }
    }

    fun removeAuthedClient(login: String): GameClient? {
        writeLock.lock()
        try {
            return authedClients.remove(login)
        } finally {
            writeLock.unlock()
        }
    }

    fun getAuthedClient(login: String): GameClient? {
        readLock.lock()
        try {
            return authedClients[login]
        } finally {
            readLock.unlock()
        }
    }

    fun getAuthedClientsByIP(ip: String): List<GameClient> {
        if (StringUtils.isEmpty(ip))
            return emptyList()

        readLock.lock()
        try {
            return authedClients.values
                .filter { client -> client.ipAddr.equals(ip, ignoreCase = true) }
        } finally {
            readLock.unlock()
        }
    }

    fun getAuthedClientsByHWID(hwid: String): List<GameClient> {
        if (StringUtils.isEmpty(hwid))
            return emptyList()

        readLock.lock()
        try {
            return authedClients.values
                .filter { client -> HwidUtils.isSameHWID(client, hwid) }
        } finally {
            readLock.unlock()
        }
    }

    fun removeClient(client: GameClient): GameClient? {
        writeLock.lock()
        try {
            return if (client.isAuthed)
                authedClients.remove(client.login)
            else
                waitingClients.remove(client.login)
        } finally {
            writeLock.unlock()
        }
    }

    fun getAccounts(): Array<String> {
        readLock.lock()
        try {
            return authedClients.keys.toTypedArray()
        } finally {
            readLock.unlock()
        }
    }

}
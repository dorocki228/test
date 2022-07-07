package communication.messages

/**
 * @author Java-man
 * @since 13.06.2019
 */
sealed class AuthGameMessage

data class GameServerDescription(
    val serverType: Int, val pvpServer: Boolean, val ageLimit: Int, val gmOnly: Boolean,
    val displayBrackets: Boolean, val maximumOnline: Int
)

data class GameServerHost(val externalHostName: String, val internalHostName: String, val port: Short)

data class AuthRequest(
    val protocolVersion: Int, val requestId: Int, val acceptAlternateId: Boolean,
    val description: GameServerDescription, val hosts: List<GameServerHost>
) : AuthGameMessage()

//data class AuthResponse() : AuthGameMessage()

data class ChangeAllowedHwid(val accountName: String, val hwid: String) : AuthGameMessage()
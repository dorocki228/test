import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object Properties {
    val fileDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")

    val formattedTime: String
        get() = LocalDateTime.now().format(fileDateTimeFormatter)

    val manifest: Map<String, String>
        get() = mapOf(
            "Built-By" to "Pvp-game",
            "Built-Date" to LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)),
            "Multi-Release" to "true"
        )
}
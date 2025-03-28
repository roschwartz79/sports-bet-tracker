import java.time.LocalDate
import java.util.UUID

data class Bet(
  val id: UUID = UUID.randomUUID(),
  val date: LocalDate = LocalDate.now(),
  val sport: String,
  val league: String,
  val matchDescription: String,
  val betType: String,
  val amount: Double,
  val odds: String,
  val result: String
)

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.time.LocalDate
import java.util.UUID

enum class Screen {
  ANALYTICS,
  HOME,
  ADD_BET,
  VIEW_BETS
}

val savedBets = mutableStateListOf<Bet>()

@Composable
fun App() {
  var currentScreen by remember { mutableStateOf(Screen.HOME) }
  var betBeingEdited by remember { mutableStateOf<Bet?>(null) }

  MaterialTheme {
    Surface(modifier = Modifier.fillMaxSize().padding(16.dp)) {
      when (currentScreen) {
        Screen.HOME -> HomeScreen(onNavigate = { currentScreen = it })
        Screen.ADD_BET -> BetEntryScreen(
          onBack = {
            currentScreen = Screen.HOME
            betBeingEdited = null
          },
          betToEdit = betBeingEdited,
          navigateTo = { screen ->
            currentScreen = screen
            betBeingEdited = null
          }
        )

        Screen.VIEW_BETS -> BetListScreen(
          onBack = { currentScreen = Screen.HOME },
          onEdit = {
            betBeingEdited = it
            currentScreen = Screen.ADD_BET
          },
          onDelete = { bet -> savedBets.removeIf { it.id == bet.id } },
          onMarkResult = { bet, result ->
            val index = savedBets.indexOfFirst { it.id == bet.id }
            if (index != -1) {
              savedBets[index] = bet.copy(result = result)
            }
          }
        )
        Screen.ANALYTICS -> AnalyticsScreen(onBack = { currentScreen = Screen.HOME })
      }
    }
  }
}

@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text("Welcome to Sports Bet Tracker!", style = MaterialTheme.typography.h5)
    Spacer(modifier = Modifier.height(24.dp))
    Button(onClick = { onNavigate(Screen.ADD_BET) }) {
      Text("Add New Bet")
    }
    Spacer(modifier = Modifier.height(12.dp))
    Button(onClick = { onNavigate(Screen.VIEW_BETS) }) {
      Text("View Bet History")
    }
    Spacer(modifier = Modifier.height(12.dp))
    Button(onClick = { onNavigate(Screen.ANALYTICS) }) {
      Text("View Analytics")
    }
  }
}

@Composable
fun BetEntryScreen(onBack: () -> Unit, betToEdit: Bet? = null, navigateTo: (Screen) -> Unit) {
  val oddsSigns = listOf("+", "-")
  val years = (2020..2030).toList()
  val months = (1..12).toList()
  val days = (1..31).toList()

// Dropdown options
  val sports = listOf("Football", "Basketball", "Hockey", "Baseball", "Soccer", "MMA", "Tennis")
  val leagues = listOf("NFL", "NBA", "NHL", "MLB", "EPL", "UFC", "ATP")
  val betTypes = listOf("Moneyline", "Spread", "Over/Under", "Prop Bet", "Parlay", "Futures", "Live Bet")
  val betResults = listOf("Pending", "Won", "Lost")

// Pre-fill state from betToEdit if editing
  var matchDesc by remember { mutableStateOf(betToEdit?.matchDescription ?: "") }
  var sport by remember { mutableStateOf(betToEdit?.sport ?: sports.first()) }
  var league by remember { mutableStateOf(betToEdit?.league ?: leagues.first()) }
  var betType by remember { mutableStateOf(betToEdit?.betType ?: betTypes.first()) }
  var amount by remember { mutableStateOf(betToEdit?.amount?.toString() ?: "50") }
  var result by remember { mutableStateOf(betToEdit?.result ?: betResults.first()) }

// Odds split into sign/value
  val initialOddsSign = if (betToEdit?.odds?.startsWith("-") == true) "-" else "+"
  val initialOddsValue = betToEdit?.odds?.removePrefix("+")?.removePrefix("-") ?: "110"

  var oddsSign by remember { mutableStateOf(initialOddsSign) }
  var oddsValue by remember { mutableStateOf(initialOddsValue) }

// Date (broken into year/month/day)
  val initialDate = betToEdit?.date ?: LocalDate.now()

  var selectedYear by remember { mutableStateOf(initialDate.year) }
  var selectedMonth by remember { mutableStateOf(initialDate.monthValue) }
  var selectedDay by remember { mutableStateOf(initialDate.dayOfMonth) }


  val isFormValid = matchDesc.isNotBlank()
      && amount.toDoubleOrNull()?.let { it > 0 } == true
      && oddsValue.toIntOrNull()?.let { it > 0 } == true


  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text("Enter Bet Details", style = MaterialTheme.typography.h6)

    OutlinedTextField(
      value = matchDesc,
      onValueChange = { matchDesc = it },
      label = { Text("Match Description") },
      modifier = Modifier.fillMaxWidth()
    )

    Text("Date", style = MaterialTheme.typography.subtitle2)

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      DropdownMenuBox(
        selected = selectedYear.toString(),
        options = years.map { it.toString() },
        onSelected = { selectedYear = it.toInt() },
        label = "Year",
        modifier = Modifier.weight(1f)
      )
      DropdownMenuBox(
        selected = selectedMonth.toString(),
        options = months.map { it.toString() },
        onSelected = { selectedMonth = it.toInt() },
        label = "Month",
        modifier = Modifier.weight(1f)
      )
      DropdownMenuBox(
        selected = selectedDay.toString(),
        options = days.map { it.toString() },
        onSelected = { selectedDay = it.toInt() },
        label = "Day",
        modifier = Modifier.weight(1f)
      )
    }

    DropdownMenuBox(
      selected = sport,
      options = sports,
      onSelected = { sport = it },
      label = "Sport"
    )

    DropdownMenuBox(
      selected = league,
      options = leagues,
      onSelected = { league = it },
      label = "League"
    )

    DropdownMenuBox(
      selected = betType,
      options = betTypes,
      onSelected = { betType = it },
      label = "Bet Type"
    )

    OutlinedTextField(
      value = amount,
      onValueChange = { amount = it },
      label = { Text("Amount Wagered") },
      modifier = Modifier.fillMaxWidth()
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      var expanded by remember { mutableStateOf(false) }

      // Dropdown for +/-
      Box {
        OutlinedTextField(
          value = oddsSign,
          onValueChange = {},
          readOnly = true,
          label = { Text("Sign") },
          modifier = Modifier.width(80.dp),
          trailingIcon = {
            IconButton(onClick = { expanded = !expanded }) {
              Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
          }
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
          oddsSigns.forEach { sign ->
            DropdownMenuItem(onClick = {
              oddsSign = sign
              expanded = false
            }) {
              Text(sign)
            }
          }
        }
      }

      // Text field for numeric odds value
      OutlinedTextField(
        value = oddsValue,
        onValueChange = { oddsValue = it },
        label = { Text("Odds Value") },
        modifier = Modifier.weight(1f)
      )
    }

    DropdownMenuBox(
      selected = result,
      options = betResults,
      onSelected = { result = it },
      label = "Result"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Button(
        onClick = {
          val fullOdds = "$oddsSign$oddsValue"
          val selectedDate = LocalDate.of(selectedYear, selectedMonth, selectedDay)

          val updatedBet = Bet(
            id = betToEdit?.id ?: UUID.randomUUID(),
            sport = sport,
            league = league,
            matchDescription = matchDesc,
            betType = betType,
            amount = amount.toDouble(),
            odds = fullOdds,
            result = result,
            date = selectedDate
          )

          if (betToEdit != null) {
            val index = savedBets.indexOfFirst { it.id == betToEdit.id }
            if (index != -1) savedBets[index] = updatedBet
          } else {
            savedBets.add(updatedBet)
          }
        },
        enabled = isFormValid
      ) {
        Text("Save Bet")
      }

      Button(onClick = onBack) {
        Text("Back")
      }

      Button(onClick = {
        navigateTo(Screen.VIEW_BETS)
      }) {
        Text("View Bet History")
      }
    }
  }
}

@Composable
fun BetListScreen(
  onBack: () -> Unit,
  onEdit: (Bet) -> Unit,
  onDelete: (Bet) -> Unit,
  onMarkResult: (Bet, String) -> Unit
) {
  Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    Text("Bet List", style = MaterialTheme.typography.h6)
    Spacer(modifier = Modifier.height(16.dp))

    if (savedBets.isEmpty()) {
      Text("No bets saved yet.")
    } else {
      LazyColumn(modifier = Modifier.weight(1f)) {
        items(savedBets) { bet ->
          val backgroundColor = when (bet.result.lowercase()) {
            "won" -> Color(0xFFDFFFE0)
            "lost" -> Color(0xFFFFE0E0)
            else -> Color.White
          }

          Card(
            backgroundColor = backgroundColor,
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            elevation = 4.dp
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text("${bet.sport} - ${bet.league}", style = MaterialTheme.typography.subtitle1)
              Text("Date: ${bet.date}")
              Text("Match: ${bet.matchDescription}")
              Text("Type: ${bet.betType}")
              Text("Amount: $${bet.amount}")
              Text("Odds: ${bet.odds}")
              Text("Result: ${bet.result}")
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                "ID: ${bet.id}",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
              )

              Spacer(modifier = Modifier.height(8.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = { onEdit(bet) },
                  colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFBBDEFB)) // light blue
                ) {
                  Text("Edit")
                }
                Button(
                  onClick = { onDelete(bet) },
                  colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFCDD2)) // light red
                ) {
                  Text("Delete")
                }
                Button(
                  onClick = { onMarkResult(bet, "Won") },
                  colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFC8E6C9)) // light green
                ) {
                  Text("Mark Won")
                }
                Button(
                  onClick = { onMarkResult(bet, "Lost") },
                  colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF8A80)) // brighter red
                ) {
                  Text("Mark Lost")
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onBack) {
      Text("Back")
    }
  }
}

@Composable
fun AnalyticsScreen(onBack: () -> Unit) {
  val profitBySport = savedBets
    .filter { it.result.lowercase() != "pending" }
    .groupBy { it.sport }
    .mapValues { (_, bets) ->
      bets.sumOf { bet ->
        val odds = bet.odds.removePrefix("+").toDoubleOrNull() ?: 0.0
        val payout = if (bet.odds.startsWith("-"))
          bet.amount / (odds / 100.0)
        else
          bet.amount * (odds / 100.0)

        if (bet.result.lowercase() == "won") payout else -bet.amount
      }
    }

  Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    Text("Profit by Sport", style = MaterialTheme.typography.h6)
    Spacer(modifier = Modifier.height(16.dp))
    if (profitBySport.isEmpty()) {
      Text("No completed bets yet.")
    } else {
      profitBySport.forEach { (sport, profit) ->
        Text("$sport: $${"%.2f".format(profit)}")
      }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onBack) {
      Text("Back")
    }
  }
}

@Composable
fun DropdownMenuBox(
  selected: String,
  options: List<String>,
  onSelected: (String) -> Unit,
  label: String,
  modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    OutlinedTextField(
      value = selected,
      onValueChange = {},
      label = { Text(label) },
      readOnly = true,
      modifier = Modifier
        .fillMaxWidth()
    )

    // Transparent clickable overlay
    Box(
      modifier = Modifier
        .matchParentSize()
        .clickable { expanded = !expanded }
    )

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false }
    ) {
      options.forEach { option ->
        DropdownMenuItem(onClick = {
          onSelected(option)
          expanded = false
        }) {
          Text(option)
        }
      }
    }
  }
}

fun main() = application {
  val windowState = rememberWindowState(
    size = DpSize(width = 1000.dp, height = 800.dp)
  )

  Window(
    onCloseRequest = ::exitApplication,
    title = "Sports Bet Tracker",
    state = windowState
  ) {
    App()
  }
}

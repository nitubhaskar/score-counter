package com.example.scorecounter

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.ColorUtils
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList
import java.util.HashMap
import java.util.UUID

data class Player(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

data class PlayerScore(
    val score: Int = 0,
    val isWinner: Boolean = false
)

data class Round(
    val roundNumber: Int,
    val scores: SnapshotStateMap<String, PlayerScore>
)

enum class SortOrder {
    DESCENDING, // Highest score green
    ASCENDING   // Lowest score green
}

sealed class Screen {
    object PlayerSelection : Screen()
    object GameTable : Screen()
}

data class EditingCellState(
    val roundIndex: Int,
    val playerId: String,
    val currentScore: Int,
    val isWinner: Boolean
)

// Concept Image Color Palette
private val DarkHeaderBackground = Color(0xFF26262B)
private val DarkRoundColBackground = Color(0xFF2E3036)
private val DarkGridBackground = Color(0xFF07242B)
private val DarkGridAltBackground = Color(0xFF0A2E37)
private val EmeraldGreen = Color(0xFF22C55E)

private val LightHeaderBackground = Color(0xFFE2E8F0)
private val LightRoundColBackground = Color(0xFFCBD5E1)
private val LightGridBackground = Color(0xFFF1F5F9)
private val LightGridAltBackground = Color(0xFFE2E8F0)
private val LightEmeraldGreen = Color(0xFF10B981)

/**
 * Generates a vibrant, theme-adaptive high-contrast color for a player name.
 * Uses HSL color space to guarantee excellent legibility on both dark and light backgrounds.
 */
fun getPlayerColor(player: Player, isDarkMode: Boolean): Color {
    val rawHash = player.id.hashCode() + player.name.hashCode()
    val hash = if (rawHash < 0) -rawHash else rawHash
    val hue = (hash % 360).toFloat()
    val saturation = 0.85f
    val lightness = if (isDarkMode) 0.72f else 0.36f
    val argb = ColorUtils.HSLToColor(floatArrayOf(hue, saturation, lightness))
    return Color(argb)
}

private val SleekDarkColorScheme = darkColorScheme(
    primary = EmeraldGreen,
    onPrimary = Color.White,
    primaryContainer = DarkHeaderBackground,
    onPrimaryContainer = Color.White,
    secondary = EmeraldGreen,
    onSecondary = Color.White,
    background = DarkGridBackground,
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFF334155)
)

private val SleekLightColorScheme = lightColorScheme(
    primary = LightEmeraldGreen,
    onPrimary = Color.White,
    primaryContainer = LightHeaderBackground,
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = LightEmeraldGreen,
    onSecondary = Color.White,
    background = LightGridBackground,
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outlineVariant = Color(0xFFCBD5E1)
)

private const val PREFS_NAME = "score_counter_game_prefs"
private const val KEY_GAME_DATA = "game_data_json"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(true) }
            val colorScheme = if (isDarkMode) SleekDarkColorScheme else SleekLightColorScheme

            MaterialTheme(colorScheme = colorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScoreCounterApp(
                        isDarkMode = isDarkMode,
                        onToggleTheme = { isDarkMode = !isDarkMode }
                    )
                }
            }
        }
    }
}

@Composable
fun KeepScreenOnEffect(keepOn: Boolean) {
    val currentView = LocalView.current
    DisposableEffect(keepOn) {
        currentView.keepScreenOn = keepOn
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}

@Composable
fun ScoreCounterApp(
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf<Screen>(Screen.PlayerSelection) }

    // Keep screen on state toggle
    var keepScreenOn by remember { mutableStateOf(true) }
    KeepScreenOnEffect(keepScreenOn)

    // Master list of all created players
    val allPlayers = remember { mutableStateListOf<Player>() }

    // Set of selected player IDs for the game
    val selectedPlayerIds = remember { mutableStateListOf<String>() }

    // Active participating players in current table column order
    val activeGamePlayers = remember { mutableStateListOf<Player>() }

    // Game rounds: list of Round
    val rounds = remember { mutableStateListOf<Round>() }

    // Sorting mode for total score green highlight
    var sortOrder by remember { mutableStateOf(SortOrder.DESCENDING) }

    // Active dialog for editing score
    var editingCell by remember { mutableStateOf<EditingCellState?>(null) }

    // Flag to ensure initial load completes before saving
    var isLoaded by remember { mutableStateOf(false) }

    fun syncActiveGamePlayers() {
        val temp = ArrayList<Player>()
        for (p in activeGamePlayers) {
            if (p.id in selectedPlayerIds) {
                temp.add(p)
            }
        }
        for (id in selectedPlayerIds) {
            var inTemp = false
            for (p in temp) {
                if (p.id == id) {
                    inTemp = true
                    break
                }
            }
            if (!inTemp) {
                for (p in allPlayers) {
                    if (p.id == id) {
                        temp.add(p)
                        break
                    }
                }
            }
        }
        activeGamePlayers.clear()
        activeGamePlayers.addAll(temp)
    }

    fun saveCurrentState() {
        if (!isLoaded) return
        try {
            val root = JSONObject()
            root.put("isDarkMode", isDarkMode)
            root.put("keepScreenOn", keepScreenOn)
            root.put("currentScreen", if (currentScreen is Screen.GameTable) "GameTable" else "PlayerSelection")
            root.put("sortOrder", sortOrder.name)

            val playersArray = JSONArray()
            for (p in allPlayers) {
                val pObj = JSONObject()
                pObj.put("id", p.id)
                pObj.put("name", p.name)
                playersArray.put(pObj)
            }
            root.put("allPlayers", playersArray)

            val selArray = JSONArray()
            for (id in selectedPlayerIds) {
                selArray.put(id)
            }
            root.put("selectedPlayerIds", selArray)

            val ordArray = JSONArray()
            for (p in activeGamePlayers) {
                ordArray.put(p.id)
            }
            root.put("activeGamePlayerIds", ordArray)

            val roundsArray = JSONArray()
            for (r in rounds) {
                val rObj = JSONObject()
                rObj.put("roundNumber", r.roundNumber)
                val scoresObj = JSONObject()
                for (entry in r.scores.entries) {
                    val pid = entry.key
                    val ps = entry.value
                    val psObj = JSONObject()
                    psObj.put("score", ps.score)
                    psObj.put("isWinner", ps.isWinner)
                    scoresObj.put(pid, psObj)
                }
                rObj.put("scores", scoresObj)
                roundsArray.put(rObj)
            }
            root.put("rounds", roundsArray)

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_GAME_DATA, root.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Load persisted state on launch
    LaunchedEffect(Unit) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_GAME_DATA, null)
            if (jsonStr != null && jsonStr.length > 0) {
                val root = JSONObject(jsonStr)

                if (root.has("keepScreenOn")) {
                    keepScreenOn = root.getBoolean("keepScreenOn")
                }

                if (root.has("sortOrder")) {
                    sortOrder = try { SortOrder.valueOf(root.getString("sortOrder")) } catch (e: Exception) { SortOrder.DESCENDING }
                }

                allPlayers.clear()
                if (root.has("allPlayers")) {
                    val pArray = root.getJSONArray("allPlayers")
                    var i = 0
                    while (i < pArray.length()) {
                        val pObj = pArray.getJSONObject(i)
                        allPlayers.add(Player(id = pObj.getString("id"), name = pObj.getString("name")))
                        i++
                    }
                }

                selectedPlayerIds.clear()
                if (root.has("selectedPlayerIds")) {
                    val selArray = root.getJSONArray("selectedPlayerIds")
                    var i = 0
                    while (i < selArray.length()) {
                        selectedPlayerIds.add(selArray.getString(i))
                        i++
                    }
                }

                activeGamePlayers.clear()
                if (root.has("activeGamePlayerIds")) {
                    val ordArray = root.getJSONArray("activeGamePlayerIds")
                    var i = 0
                    while (i < ordArray.length()) {
                        val id = ordArray.getString(i)
                        for (p in allPlayers) {
                            if (p.id == id) {
                                activeGamePlayers.add(p)
                                break
                            }
                        }
                        i++
                    }
                }

                rounds.clear()
                if (root.has("rounds")) {
                    val rArray = root.getJSONArray("rounds")
                    var i = 0
                    while (i < rArray.length()) {
                        val rObj = rArray.getJSONObject(i)
                        val rNum = rObj.getInt("roundNumber")
                        val scoresMap = mutableStateMapOf<String, PlayerScore>()
                        val scoresObj = rObj.getJSONObject("scores")
                        val keys = scoresObj.keys()
                        while (keys.hasNext()) {
                            val pid = keys.next()
                            val psObj = scoresObj.getJSONObject(pid)
                            scoresMap[pid] = PlayerScore(psObj.getInt("score"), psObj.getBoolean("isWinner"))
                        }
                        rounds.add(Round(roundNumber = rNum, scores = scoresMap))
                        i++
                    }
                }

                if (root.has("currentScreen") && root.getString("currentScreen") == "GameTable" && rounds.size > 0) {
                    currentScreen = Screen.GameTable
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Default initial players if none saved
        if (allPlayers.size == 0) {
            val defaults = listOf(
                Player(name = "Player 1"),
                Player(name = "Player 2"),
                Player(name = "Player 3"),
                Player(name = "Player 4")
            )
            allPlayers.addAll(defaults)
            for (p in defaults) {
                selectedPlayerIds.add(p.id)
                activeGamePlayers.add(p)
            }
        }

        syncActiveGamePlayers()
        isLoaded = true
    }

    fun startNewGame() {
        rounds.clear()
        syncActiveGamePlayers()
        if (activeGamePlayers.size > 0) {
            val initialScores = mutableStateMapOf<String, PlayerScore>()
            for (player in activeGamePlayers) {
                initialScores[player.id] = PlayerScore(0, false)
            }
            rounds.add(Round(roundNumber = 1, scores = initialScores))
        }
        currentScreen = Screen.GameTable
        saveCurrentState()
    }

    fun continueGame() {
        syncActiveGamePlayers()
        currentScreen = Screen.GameTable
        saveCurrentState()
    }

    when (currentScreen) {
        is Screen.PlayerSelection -> {
            PlayerSelectionScreen(
                players = allPlayers,
                selectedIds = selectedPlayerIds,
                hasActiveGame = rounds.size > 0,
                isDarkMode = isDarkMode,
                keepScreenOn = keepScreenOn,
                onToggleTheme = onToggleTheme,
                onToggleKeepScreenOn = {
                    keepScreenOn = !keepScreenOn
                    saveCurrentState()
                },
                onAddPlayer = { name ->
                    val newPlayer = Player(name = name)
                    allPlayers.add(newPlayer)
                    selectedPlayerIds.add(newPlayer.id)
                    activeGamePlayers.add(newPlayer)
                    saveCurrentState()
                },
                onToggleSelect = { id ->
                    if (id in selectedPlayerIds) {
                        selectedPlayerIds.remove(id)
                        var removeIdx = -1
                        for (i in 0 until activeGamePlayers.size) {
                            if (activeGamePlayers[i].id == id) {
                                removeIdx = i
                                break
                            }
                        }
                        if (removeIdx != -1) {
                            activeGamePlayers.removeAt(removeIdx)
                        }
                    } else {
                        selectedPlayerIds.add(id)
                        var found = false
                        for (p in activeGamePlayers) {
                            if (p.id == id) {
                                found = true
                                break
                            }
                        }
                        if (!found) {
                            for (p in allPlayers) {
                                if (p.id == id) {
                                    activeGamePlayers.add(p)
                                    break
                                }
                            }
                        }
                    }
                    saveCurrentState()
                },
                onDeletePlayer = { player ->
                    allPlayers.remove(player)
                    selectedPlayerIds.remove(player.id)
                    var removeIdx = -1
                    for (i in 0 until activeGamePlayers.size) {
                        if (activeGamePlayers[i].id == player.id) {
                            removeIdx = i
                            break
                        }
                    }
                    if (removeIdx != -1) {
                        activeGamePlayers.removeAt(removeIdx)
                    }
                    saveCurrentState()
                },
                onStartGame = {
                    startNewGame()
                },
                onContinueGame = {
                    continueGame()
                }
            )
        }
        is Screen.GameTable -> {
            GameTableScreen(
                players = activeGamePlayers,
                rounds = rounds,
                sortOrder = sortOrder,
                isDarkMode = isDarkMode,
                keepScreenOn = keepScreenOn,
                onToggleTheme = onToggleTheme,
                onToggleKeepScreenOn = {
                    keepScreenOn = !keepScreenOn
                    saveCurrentState()
                },
                onToggleSortOrder = {
                    sortOrder = if (sortOrder == SortOrder.DESCENDING) SortOrder.ASCENDING else SortOrder.DESCENDING
                    saveCurrentState()
                },
                onAddRound = {
                    val nextRoundNum = rounds.size + 1
                    val newScores = mutableStateMapOf<String, PlayerScore>()
                    for (player in activeGamePlayers) {
                        newScores[player.id] = PlayerScore(0, false)
                    }
                    rounds.add(Round(roundNumber = nextRoundNum, scores = newScores))
                    saveCurrentState()
                },
                onCellClick = { roundIndex, playerId, currentScore, isWinner ->
                    editingCell = EditingCellState(roundIndex, playerId, currentScore, isWinner)
                },
                onSwapColumns = { indexA, indexB ->
                    if (indexA >= 0 && indexA < activeGamePlayers.size && indexB >= 0 && indexB < activeGamePlayers.size) {
                        val temp = activeGamePlayers[indexA]
                        activeGamePlayers[indexA] = activeGamePlayers[indexB]
                        activeGamePlayers[indexB] = temp
                        saveCurrentState()
                    }
                },
                onBackToPlayers = {
                    currentScreen = Screen.PlayerSelection
                    saveCurrentState()
                }
            )

            val cell = editingCell
            if (cell != null) {
                var player: Player? = null
                for (p in activeGamePlayers) {
                    if (p.id == cell.playerId) {
                        player = p
                        break
                    }
                }

                NumberPadDialog(
                    playerName = player?.name ?: "Player",
                    roundNumber = cell.roundIndex + 1,
                    initialScore = cell.currentScore,
                    initialIsWinner = cell.isWinner,
                    onDismiss = { editingCell = null },
                    onConfirm = { newScore, isWinner ->
                        if (cell.roundIndex >= 0 && cell.roundIndex < rounds.size) {
                            rounds[cell.roundIndex].scores[cell.playerId] = PlayerScore(newScore, isWinner)
                            saveCurrentState()
                        }
                        editingCell = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSelectionScreen(
    players: List<Player>,
    selectedIds: List<String>,
    hasActiveGame: Boolean,
    isDarkMode: Boolean,
    keepScreenOn: Boolean,
    onToggleTheme: () -> Unit,
    onToggleKeepScreenOn: () -> Unit,
    onAddPlayer: (String) -> Unit,
    onToggleSelect: (String) -> Unit,
    onDeletePlayer: (Player) -> Unit,
    onStartGame: () -> Unit,
    onContinueGame: () -> Unit
) {
    var newPlayerName by remember { mutableStateOf("") }
    val greenColor = if (isDarkMode) EmeraldGreen else LightEmeraldGreen

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Score Counter", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onToggleKeepScreenOn) {
                        Icon(
                            imageVector = if (keepScreenOn) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Keep Screen Awake",
                            tint = if (keepScreenOn) greenColor else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add Players & Select Participants",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Add player row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newPlayerName,
                    onValueChange = { newPlayerName = it },
                    label = { Text("Player Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newPlayerName.trim().isNotEmpty()) {
                            onAddPlayer(newPlayerName.trim())
                            newPlayerName = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }

            // Player List Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                if (players.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No players added yet. Add players above!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        items(players, key = { it.id }) { player ->
                            val isSelected = player.id in selectedIds
                            val nameColor = getPlayerColor(player, isDarkMode)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onToggleSelect(player.id) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { onToggleSelect(player.id) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = player.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = nameColor
                                )
                                IconButton(onClick = { onDeletePlayer(player) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (hasActiveGame) {
                    Button(
                        onClick = onContinueGame,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Continue Game")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Continue Ongoing Game",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = onStartGame,
                    enabled = selectedIds.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start New Game")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasActiveGame) "Start New Game (${selectedIds.size} Selected)" else "Start Game (${selectedIds.size} Selected)",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTableScreen(
    players: SnapshotStateList<Player>,
    rounds: List<Round>,
    sortOrder: SortOrder,
    isDarkMode: Boolean,
    keepScreenOn: Boolean,
    onToggleTheme: () -> Unit,
    onToggleKeepScreenOn: () -> Unit,
    onToggleSortOrder: () -> Unit,
    onAddRound: () -> Unit,
    onCellClick: (roundIndex: Int, playerId: String, currentScore: Int, isWinner: Boolean) -> Unit,
    onSwapColumns: (indexA: Int, indexB: Int) -> Unit,
    onBackToPlayers: () -> Unit
) {
    // Header selection state for arrow-based reordering
    var selectedHeaderPlayerId by remember { mutableStateOf<String?>(null) }

    // Calculate total score for each player dynamically on every recomposition
    val playerTotals = HashMap<String, Int>()
    for (p in players) {
        var sum = 0
        for (r in rounds) {
            sum += r.scores[p.id]?.score ?: 0
        }
        playerTotals[p.id] = sum
    }

    // Find target score to highlight green based on mode (DESCENDING -> max green, ASCENDING -> min green)
    var targetScoreForGreen: Int? = null
    if (playerTotals.size > 0) {
        for (score in playerTotals.values) {
            if (targetScoreForGreen == null) {
                targetScoreForGreen = score
            } else if (sortOrder == SortOrder.DESCENDING && score > targetScoreForGreen) {
                targetScoreForGreen = score
            } else if (sortOrder == SortOrder.ASCENDING && score < targetScoreForGreen) {
                targetScoreForGreen = score
            }
        }
    }

    val greenColor = if (isDarkMode) EmeraldGreen else LightEmeraldGreen
    val headerBg = if (isDarkMode) DarkHeaderBackground else LightHeaderBackground
    val roundColBg = if (isDarkMode) DarkRoundColBackground else LightRoundColBackground
    val gridBg = if (isDarkMode) DarkGridBackground else LightGridBackground
    val gridAltBg = if (isDarkMode) DarkGridAltBackground else LightGridAltBackground

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Score Counter", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackToPlayers) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Setup")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleKeepScreenOn) {
                        Icon(
                            imageVector = if (keepScreenOn) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Keep Screen Awake",
                            tint = if (keepScreenOn) greenColor else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = headerBg,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRound,
                containerColor = greenColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Round",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        val roundColWidth = 44.dp
        val horizontalScrollState = rememberScrollState()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(gridBg)
        ) {
            val totalAvailableWidth = maxWidth - roundColWidth
            val playerCount = if (players.size > 0) players.size else 1
            val sharedWidth = totalAvailableWidth / playerCount
            val playerColWidth = if (sharedWidth > 80.dp) sharedWidth else 80.dp

            Column(modifier = Modifier.fillMaxSize()) {
                
                // --- HEADER SECTION (Rows 1 & 2 matching Concept Image) ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                ) {
                    // ROW 1: Player Names + Top-Left Green Sort Arrow
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Top-Left Corner: Green Sort Arrow Button
                        Box(
                            modifier = Modifier
                                .width(roundColWidth)
                                .height(56.dp)
                                .clickable { onToggleSortOrder() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (sortOrder == SortOrder.DESCENDING)
                                    Icons.Default.ArrowDownward
                                else
                                    Icons.Default.ArrowUpward,
                                contentDescription = "Toggle Green Target",
                                tint = greenColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Horizontally scrollable Player Names Row
                        Row(
                            modifier = Modifier
                                .horizontalScroll(horizontalScrollState)
                                .fillMaxWidth()
                        ) {
                            var index = 0
                            for (player in players) {
                                val currentIndex = index
                                val nameColor = getPlayerColor(player, isDarkMode)
                                val isSelected = (selectedHeaderPlayerId == player.id)

                                Box(
                                    modifier = Modifier
                                        .width(playerColWidth)
                                        .height(56.dp)
                                        .padding(horizontal = 2.dp)
                                        .background(
                                            color = if (isSelected) greenColor.copy(alpha = 0.25f) else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .then(
                                            if (isSelected)
                                                Modifier.border(2.dp, greenColor, RoundedCornerShape(8.dp))
                                            else
                                                Modifier
                                        )
                                        .clickable {
                                            selectedHeaderPlayerId = if (isSelected) null else player.id
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            // Left Arrow Button -> Swaps column currentIndex with currentIndex - 1
                                            Box(
                                                modifier = Modifier
                                                    .width(24.dp)
                                                    .fillMaxHeight()
                                                    .clickable(enabled = currentIndex > 0) {
                                                        onSwapColumns(currentIndex, currentIndex - 1)
                                                        selectedHeaderPlayerId = null
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "◀",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (currentIndex > 0) greenColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                                )
                                            }

                                            // Player Name
                                            Text(
                                                text = player.name,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.weight(1f)
                                            )

                                            // Right Arrow Button -> Swaps column currentIndex with currentIndex + 1
                                            Box(
                                                modifier = Modifier
                                                    .width(24.dp)
                                                    .fillMaxHeight()
                                                    .clickable(enabled = currentIndex < players.size - 1) {
                                                        onSwapColumns(currentIndex, currentIndex + 1)
                                                        selectedHeaderPlayerId = null
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "▶",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (currentIndex < players.size - 1) greenColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = player.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = nameColor,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                index++
                            }
                        }
                    }

                    // ROW 2: Player Total Scores + Top-Left `#` Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Top-Left Cell: `#` Symbol
                        Box(
                            modifier = Modifier
                                .width(roundColWidth)
                                .height(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = greenColor
                            )
                        }

                        // Horizontally scrollable Player Total Scores Row
                        Row(
                            modifier = Modifier
                                .horizontalScroll(horizontalScrollState)
                                .fillMaxWidth()
                        ) {
                            for (player in players) {
                                val total = playerTotals[player.id] ?: 0
                                val isGreen = targetScoreForGreen != null && total == targetScoreForGreen

                                Box(
                                    modifier = Modifier
                                        .width(playerColWidth)
                                        .height(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = total.toString(),
                                        fontSize = 19.sp,
                                        fontWeight = if (isGreen) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isGreen) greenColor else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // --- ROUND ROWS BODY (Scrollable vertically) ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    var roundIdx = 0
                    for (round in rounds) {
                        val currentRoundIdx = roundIdx
                        val rowBg = if (currentRoundIdx % 2 == 0) gridBg else gridAltBg

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                        ) {
                            // Round index label cell (`1.`, `2.`, `3.`... on dark grey background)
                            Box(
                                modifier = Modifier
                                    .width(roundColWidth)
                                    .height(52.dp)
                                    .background(roundColBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${round.roundNumber}.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = greenColor
                                )
                            }

                            // Player score cells for this round
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(horizontalScrollState)
                                    .fillMaxWidth()
                            ) {
                                for (player in players) {
                                    val playerScore = round.scores[player.id] ?: PlayerScore(0, false)

                                    Box(
                                        modifier = Modifier
                                            .width(playerColWidth)
                                            .height(52.dp)
                                            .clickable {
                                                onCellClick(currentRoundIdx, player.id, playerScore.score, playerScore.isWinner)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = playerScore.score.toString(),
                                                fontSize = 18.sp,
                                                fontWeight = if (playerScore.isWinner) FontWeight.ExtraBold else FontWeight.Medium,
                                                color = if (playerScore.isWinner) greenColor else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                                                textAlign = TextAlign.Center
                                            )
                                            if (playerScore.isWinner) {
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Winner Star",
                                                    tint = greenColor,
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .align(Alignment.Top)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        roundIdx++
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPadDialog(
    playerName: String,
    roundNumber: Int,
    initialScore: Int,
    initialIsWinner: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (score: Int, isWinner: Boolean) -> Unit
) {
    var scoreInput by remember { mutableStateOf(initialScore.toString()) }
    var isWinner by remember { mutableStateOf(initialIsWinner) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$playerName (Round $roundNumber)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Score display box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (scoreInput.isEmpty() || scoreInput == "-") scoreInput.ifEmpty { "0" } else scoreInput,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Winner Checkbox Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isWinner = !isWinner }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Checkbox(
                        checked = isWinner,
                        onCheckedChange = { isWinner = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isWinner) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = "Round Winner Star",
                        tint = if (isWinner) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Round Winner",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = if (isWinner) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Number Pad Grid
                val buttons = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("-", "0", "⌫")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    buttons.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { label ->
                                Button(
                                    onClick = {
                                        when (label) {
                                            "⌫" -> {
                                                if (scoreInput.isNotEmpty()) {
                                                    scoreInput = scoreInput.dropLast(1)
                                                }
                                            }
                                            "-" -> {
                                                scoreInput = if (scoreInput.startsWith("-")) {
                                                    scoreInput.removePrefix("-")
                                                } else {
                                                    if (scoreInput == "0") "-" else "-$scoreInput"
                                                }
                                            }
                                            else -> {
                                                if (scoreInput == "0" || scoreInput == "-0") {
                                                    scoreInput = if (scoreInput.startsWith("-")) "-$label" else label
                                                } else {
                                                    scoreInput += label
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (label == "⌫" || label == "-")
                                            MaterialTheme.colorScheme.surfaceVariant
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons (Clear / Cancel / OK)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { scoreInput = "0" }) {
                        Text("Clear", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }

                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val parsed = scoreInput.toIntOrNull() ?: 0
                                onConfirm(parsed, isWinner)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("OK", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

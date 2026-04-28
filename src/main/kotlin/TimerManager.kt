import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerManager(private val game: Game)
{
    var timeLeftWhite by mutableIntStateOf(900)
        private set
    var timeLeftBlack by mutableIntStateOf(900)
        private set

    private var timerJob: Job? = null
    private val gameScope = CoroutineScope(Dispatchers.Default)

    fun startTimer()
    {
        timerJob?.cancel()

        timerJob = gameScope.launch()
        {
            while (isActive && game.gameState == GameState.PLAYING)
            {
                delay(1000L)

                if (game.playerOnTurn == Player.WHITE)
                {
                    if (timeLeftWhite > 0)
                    {
                        timeLeftWhite--
                    }
                    else
                    {
                        game.message = "TIMEOUT!" + "   " + whoWon(Player.BLACK)
                        game.gameState = GameState.TIMEOUT

                        game.historyManager.addMoveToHistory(Move(
                            -1,
                            (-1 to -1),
                            (-1 to -1),
                            null,
                            false,
                            null,
                            null,
                            false,
                            game.gameState,
                            game.playerOnTurn))

                        timerJob?.cancel()
                    }
                }
                else
                {
                    if (timeLeftBlack > 0)
                    {
                        timeLeftBlack--
                    }
                    else
                    {
                        game.message = "TIMEOUT!" + "   " + whoWon(Player.WHITE)
                        game.gameState = GameState.TIMEOUT

                        game.historyManager.addMoveToHistory(Move(
                            -1,
                            (-1 to -1),
                            (-1 to -1),
                            null,
                            false,
                            null,
                            null,
                            false,
                            game.gameState,
                            game.playerOnTurn))

                        timerJob?.cancel()
                    }
                }
            }
        }
    }
    fun stopTimer()
    {
        timerJob?.cancel()
        timerJob = null
    }
    fun reset()
    {
        stopTimer()

        timeLeftWhite = 900
        timeLeftBlack = 900

        startTimer()
    }
}
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlin.collections.plus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlin.collections.plus
import kotlin.div
import kotlin.math.abs
import kotlin.text.compareTo
import kotlin.text.get
import kotlin.text.set


class Game
{
    var board by mutableStateOf(Board())

    var message by mutableStateOf<String>("")
    var playerOnTurn by mutableStateOf(Player.WHITE)

    var gameState by mutableStateOf(GameState.PLAYING)
    var checkState by mutableStateOf(CheckState(false, null))

    var selectedStartSquare by mutableStateOf<Pair<Int, Int>?>(null)
    var isEndSquareSelected by mutableStateOf<Boolean>(false)

    var moveOptions by mutableStateOf(MoveOptions(emptyList(), emptyList()))
    var capturedPieces by mutableStateOf(listOf<ChessPiece>())

    var timeLeftWhite by mutableIntStateOf(900)
    var timeLeftBlack by mutableIntStateOf(900)
    private var timerJob: Job? = null
    private val gameScope = CoroutineScope(Dispatchers.Default)

    var promotionSquare by mutableStateOf<Pair<Int, Int>?>(null)
    var pendingPromotionPlayer by mutableStateOf<Player?>(null)

    fun init()
    {
        board.clear()
        board.setUpInitialBoard()
        board.setUpCastlingRights()
    }
    fun restartGame()
    {

    }
    fun resignGame()
    {
        gameState = GameState.RESIGNED

        timerJob?.cancel()

        if(playerOnTurn == Player.WHITE)
        {
            message = "RESIGNED!" + "   " + whoWon(Player.BLACK)
        }
        else
        {
            message = "RESIGNED!" + "   " + whoWon(Player.WHITE)
        }
    }

    fun onSquareClick(row: Int, col: Int)
    {
        if (gameState != GameState.PLAYING) return

        squareSelection(row, col)

        if( isEndSquareSelected )
        {
            val (fromRow, fromCol) = selectedStartSquare ?: return
            val (toRow, toCol) = row to col

            val legalMoves = moveOptions.moves.map { it.first }.toSet()

            if (toRow to toCol in legalMoves)
            {
                val movingPiece = board.grid[fromRow][fromCol]

                //TODO hystory

                val tempBoard = board.copy()

                if (movingPiece!!.type == Piece.KING && abs(fromCol - toCol) == 2)
                {
                    tempBoard.grid[fromRow][fromCol] = null;
                    tempBoard.grid[toRow][toCol] = movingPiece;

                    if (toCol < fromCol)
                    {
                        tempBoard.grid[fromRow][toCol + 1] = tempBoard.grid[fromRow][0];
                        tempBoard.grid[fromRow][0] = null
                    }
                    else
                    {
                        tempBoard.grid[fromRow][toCol - 1] = tempBoard.grid[fromRow][7];
                        tempBoard.grid[fromRow][7] = null
                    }
                }
                else if(movingPiece!!.type == Piece.PAWN && (toRow to toCol) == tempBoard.enPassantTarget)
                {
                    val capturedPiece = tempBoard.grid[fromRow][toCol]!!
                    capturedPieces += capturedPiece

                    tempBoard.grid[toRow][toCol] = movingPiece
                    tempBoard.grid[fromRow][fromCol] = null
                    tempBoard.grid[fromRow][toCol] = null
                }
                else
                {
                    val capturedPiece = tempBoard.grid[toRow][toCol]
                    if(capturedPiece != null)
                    {
                        capturedPieces += capturedPiece
                    }

                    tempBoard.set(toRow, toCol, movingPiece)
                    tempBoard.set(fromRow, fromCol, null)
                }

                if((toRow to toCol) == (0 to 0))
                {
                    tempBoard.castlingRights.blackQueenSide=false
                }
                if((toRow to toCol) == (0 to 7))
                {
                    tempBoard.castlingRights.blackKingSide=false
                }
                if((toRow to toCol) == (7 to 0))
                {
                    tempBoard.castlingRights.whiteQueenSide=false
                }
                if((toRow to toCol) == (7 to 7))
                {
                    tempBoard.castlingRights.whiteKingSide=false
                }
                if((toRow to toCol) == (0 to 4))
                {
                    tempBoard.castlingRights.blackKingSide=false
                    tempBoard.castlingRights.blackQueenSide=false
                }
                if((toRow to toCol) == (7 to 4))
                {
                    tempBoard.castlingRights.whiteKingSide=false
                    tempBoard.castlingRights.whiteQueenSide=false
                }

                if(movingPiece!!.type == Piece.PAWN && abs(toRow - fromRow) == 2)
                {
                    tempBoard.enPassantTarget=((fromRow + toRow)/2) to toCol
                }
                else
                {
                    tempBoard.enPassantTarget= null
                }

                board = tempBoard


                if (checkPromotionConditions(movingPiece!!,toRow, toCol))
                {
                    promotionSquare = toRow to toCol
                    pendingPromotionPlayer = movingPiece.player
                    return
                }

                evaluateCheck(playerOnTurn)
                evaluateEndConditions(playerOnTurn)

                switchPlayerOnTurn()
            }

            selectedStartSquare = null
            isEndSquareSelected = false
            moveOptions=MoveOptions(emptyList(),emptyList())
        }
    }

    fun squareSelection(row: Int, col: Int)
    {
        val piece = board.grid[row][col]

        if( selectedStartSquare == null )
        {
            if( piece != null && piece.player == playerOnTurn)
            {
                selectedStartSquare = row to col

                val validator= MoveValidator(board)
                moveOptions=validator.getLegalMoves(row, col)
            }
        }
        else if( piece == null || piece.player != playerOnTurn)
        {
            isEndSquareSelected = true
        }
        else
        {
            selectedStartSquare = row to col

            val validator= MoveValidator(board)
            moveOptions=validator.getLegalMoves(row, col)
        }
    }

    fun evaluateCheck(player: Player)
    {
        val validator = CheckValidator(board)

        if (validator.isPlayerGivingCheck(player))
        {
            val enemy = if (player == Player.WHITE) Player.BLACK else Player.WHITE
            checkState = CheckState(true, findKing(board, enemy))
        }
        else
        {
            checkState = CheckState(false, null)
        }
    }
    fun evaluateEndConditions(player: Player)
    {
        val validator = CheckValidator(board)

        if (validator.isOpponentCheckmatedByPlayer(player))
        {
            message = "CHECKMATE!" + "  " + whoWon(player)
            gameState = GameState.CHECKMATE
        }
        else if (validator.isStalemateCausedByPlayer(player))
        {
            message = "STALEMATE!" + "  " + whoWon(null)
            gameState = GameState.STALEMATE
        }
        else if (validator.isDraw())
        {
            message = "DRAW!" + "  " + whoWon(null)
            gameState = GameState.DRAW
        }
        else
        {
            message = ""
            gameState = GameState.PLAYING
        }

        if(gameState != GameState.PLAYING)
        {
            timerJob?.cancel()
        }
    }
    fun switchPlayerOnTurn()
    {
        if(playerOnTurn == Player.WHITE)
        {
            playerOnTurn = Player.BLACK
        }
        else
        {
            playerOnTurn = Player.WHITE
        }
    }

    fun startTimer()
    {
        timerJob?.cancel()

        timerJob = gameScope.launch()
        {
            while (isActive && gameState == GameState.PLAYING)
            {
                delay(1000L)

                if (playerOnTurn == Player.WHITE)
                {
                    if (timeLeftWhite > 0)
                    {
                        timeLeftWhite--
                    }
                    else
                    {
                        message = "TIMEOUT!" + "   " + whoWon(Player.BLACK)
                        gameState = GameState.TIMEOUT
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
                        message = "TIMEOUT!" + "   " + whoWon(Player.WHITE)
                        gameState = GameState.TIMEOUT
                        timerJob?.cancel()
                    }
                }
            }
        }
    }

    fun checkPromotionConditions(movingPiece: ChessPiece,row:Int,col:Int):Boolean
    {
        return movingPiece.type == Piece.PAWN &&
                ((movingPiece.player == Player.WHITE && row == 0) || (movingPiece.player == Player.BLACK && row == 7))
    }
    fun pawnPromotion(pieceType: Piece)
    {
        val (row, col) = promotionSquare!!

        val newBoard = board.copy()
        newBoard.set(row, col, ChessPiece(pieceType, pendingPromotionPlayer!!) )
        board = newBoard

        evaluateCheck(pendingPromotionPlayer!!)
        evaluateEndConditions(pendingPromotionPlayer!!)

        switchPlayerOnTurn()

        promotionSquare = null
        pendingPromotionPlayer = null

        selectedStartSquare = null
        isEndSquareSelected = false
        moveOptions=MoveOptions(emptyList(),emptyList())
    }
}
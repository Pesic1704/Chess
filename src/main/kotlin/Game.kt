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
import kotlin.math.abs


class Game
{
    var board by mutableStateOf(Board())

    var message by mutableStateOf("")
    var playerOnTurn by mutableStateOf(Player.WHITE)

    var gameState by mutableStateOf(GameState.PLAYING)
    var checkState by mutableStateOf(CheckState(false, null))

    var selectedStartSquare by mutableStateOf<Pair<Int, Int>?>(null)
    private var isEndSquareSelected by mutableStateOf(false)

    var moveOptions by mutableStateOf(MoveOptions(emptyList(), emptyList()))
    var capturedPieces by mutableStateOf(listOf<ChessPiece>())

    var timeLeftWhite by mutableIntStateOf(900)
    var timeLeftBlack by mutableIntStateOf(900)

    private var timerJob: Job? = null
    private val gameScope = CoroutineScope(Dispatchers.Default)

    var promotionSquare by mutableStateOf<Pair<Int, Int>?>(null)
    var pendingPromotionPlayer by mutableStateOf<Player?>(null)

    private var lastBoardState: Long? = null
    private val boardStateHistory = mutableMapOf<Long,Int>()
    private var fiftyMoveCounter:Int = 0

    fun init()
    {
        val tempBoard = Board()

        tempBoard.clearBoard()
        tempBoard.setUpInitialBoard()
        tempBoard.setUpCastlingRights()
        tempBoard.setUpEnPassantTarget()

        board = tempBoard
    }
    fun restartGame()
    {
        init()

        message = ""
        playerOnTurn = Player.WHITE

        gameState = GameState.PLAYING
        checkState = CheckState(false, null)

        selectedStartSquare = null
        isEndSquareSelected = false

        moveOptions = MoveOptions(emptyList(), emptyList())
        capturedPieces = listOf()

        timeLeftWhite = 900
        timeLeftBlack = 900

        timerJob?.cancel()
        timerJob = null

        promotionSquare =null
        pendingPromotionPlayer = null

        boardStateHistory.clear()
        lastBoardState = null
        fiftyMoveCounter = 0
    }
    fun resignGame()
    {
        gameState = GameState.RESIGNED

        timerJob?.cancel()

        message = "RESIGNED!" + "   " + if(playerOnTurn == Player.WHITE)
                                             whoWon(Player.BLACK)
                                        else
                                             whoWon(Player.WHITE)

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

                //TODO history

                val tempBoard = board.copy()

                if (movingPiece!!.type == Piece.KING && abs(fromCol - toCol) == 2)
                {
                    tempBoard.grid[fromRow][fromCol] = null
                    tempBoard.grid[toRow][toCol] = movingPiece

                    if (toCol < fromCol)
                    {
                        tempBoard.grid[fromRow][toCol + 1] = tempBoard.grid[fromRow][0]
                        tempBoard.grid[fromRow][0] = null
                    }
                    else
                    {
                        tempBoard.grid[fromRow][toCol - 1] = tempBoard.grid[fromRow][7]
                        tempBoard.grid[fromRow][7] = null
                    }

                    fiftyMoveCounter++
                }
                else if(movingPiece.type == Piece.PAWN && (toRow to toCol) == tempBoard.enPassantTarget)
                {
                    val capturedPiece = tempBoard.grid[fromRow][toCol]!!
                    capturedPieces += capturedPiece

                    tempBoard.grid[toRow][toCol] = movingPiece
                    tempBoard.grid[fromRow][fromCol] = null
                    tempBoard.grid[fromRow][toCol] = null

                    fiftyMoveCounter = 0
                }
                else
                {
                    val capturedPiece = tempBoard.grid[toRow][toCol]

                    if(capturedPiece != null)
                    {
                        capturedPieces += capturedPiece
                        fiftyMoveCounter=0
                    }
                    else if(movingPiece.type == Piece.PAWN )
                    {
                        fiftyMoveCounter=0
                    }
                    else
                    {
                        fiftyMoveCounter++
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

                if(movingPiece.type == Piece.PAWN && abs(toRow - fromRow) == 2)
                {
                    tempBoard.enPassantTarget=((fromRow + toRow)/2) to toCol
                }
                else
                {
                    tempBoard.enPassantTarget= null
                }

                board = tempBoard

                if (checkPromotionConditions(movingPiece,toRow))
                {
                    promotionSquare = toRow to toCol
                    pendingPromotionPlayer = movingPiece.player
                    return
                }

                lastBoardState = calculateBoardStateHash()
                boardStateHistory[lastBoardState!!] = (boardStateHistory[lastBoardState] ?: 0) + 1

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
        val checkValidator = CheckValidator(board)
        val drawValidator = DrawValidator(board,lastBoardState!!,boardStateHistory,fiftyMoveCounter)

        if (checkValidator.isOpponentCheckmatedByPlayer(player))
        {
            message = "CHECKMATE!" + "  " + whoWon(player)
            gameState = GameState.CHECKMATE
        }
        else if (checkValidator.isStalemateCausedByPlayer(player))
        {
            message = "STALEMATE!" + "  " + whoWon(null)
            gameState = GameState.STALEMATE
        }
        else if (drawValidator.isDraw())
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
        playerOnTurn = if (playerOnTurn == Player.WHITE)
                            Player.BLACK
                        else
                            Player.WHITE
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

    fun checkPromotionConditions(movingPiece: ChessPiece,row:Int):Boolean
    {
        return movingPiece.type == Piece.PAWN &&
                ((movingPiece.player == Player.WHITE && row == 0) || (movingPiece.player == Player.BLACK && row == 7))
    }
    fun pawnPromotion(pieceType: Piece)
    {
        val (row, col) = promotionSquare!!

        val tempBoard = board.copy()
        tempBoard.set(row, col, ChessPiece(pieceType, pendingPromotionPlayer!!) )
        board = tempBoard

        lastBoardState = calculateBoardStateHash()
        boardStateHistory[lastBoardState!!] = (boardStateHistory[lastBoardState] ?: 0) + 1

        evaluateCheck(pendingPromotionPlayer!!)
        evaluateEndConditions(pendingPromotionPlayer!!)

        switchPlayerOnTurn()

        promotionSquare = null
        pendingPromotionPlayer = null

        selectedStartSquare = null
        isEndSquareSelected = false
        moveOptions=MoveOptions(emptyList(),emptyList())
    }

    fun calculateBoardStateHash(): Long
    {
        var hash: Long = 1

        for (i in 0..7)
        {
            for (j in 0..7)
            {
                val piece = board.grid[i][j]
                val pieceValue = if (piece == null)
                                    0
                                else
                                    (if (piece.player == Player.WHITE) 1 else 7) + piece.type.ordinal
                hash = 31 * hash + pieceValue
            }
        }

        hash = 31 * hash + if (board.castlingRights.whiteKingSide) 1 else 0
        hash = 31 * hash + if (board.castlingRights.whiteQueenSide) 1 else 0
        hash = 31 * hash + if (board.castlingRights.blackKingSide) 1 else 0
        hash = 31 * hash + if (board.castlingRights.blackQueenSide) 1 else 0

        if (board.enPassantTarget != null)
        {
            hash = 31 * hash + board.enPassantTarget!!.first
            hash = 31 * hash + board.enPassantTarget!!.second
        }

        hash = 31 * hash + (if (playerOnTurn == Player.WHITE) 1 else 0)

        return hash
    }

}
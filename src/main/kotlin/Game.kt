import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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

    var moveIndex:Int = 0
    val movesHistory = mutableStateListOf<Move>()

    val boardSnapshots = mutableStateListOf<Board>()

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

        moveIndex = 0
        movesHistory.clear()

        boardSnapshots.clear()
    }
    fun resignGame()
    {
        gameState = GameState.RESIGNED

        movesHistory.add(Move(
            -1,
            (-1 to -1),
            (-1 to -1),
            null,
            false,
            null,
            null,
            false,
            gameState,
            playerOnTurn))

        timerJob?.cancel()

        message = "RESIGNED!" + "   " + if(playerOnTurn == Player.WHITE)
                                             whoWon(Player.BLACK)
                                        else
                                             whoWon(Player.WHITE)

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

                        movesHistory.add(Move(
                            -1,
                            (-1 to -1),
                            (-1 to -1),
                            null,
                            false,
                            null,
                            null,
                            false,
                            gameState,
                            playerOnTurn))

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

                        movesHistory.add(Move(
                            -1,
                            (-1 to -1),
                            (-1 to -1),
                            null,
                            false,
                            null,
                            null,
                            false,
                            gameState,
                            playerOnTurn))

                        timerJob?.cancel()
                    }
                }
            }
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
                moveIndex++

                val movingPiece = board.grid[fromRow][fromCol] ?:return

                val tempBoard = board.copy()

                if (movingPiece.type == Piece.KING && abs(fromCol - toCol) == 2)
                {
                    executeCastling(tempBoard,movingPiece,fromRow,fromCol,toRow,toCol)
                }
                else if(movingPiece.type == Piece.PAWN && (toRow to toCol) == tempBoard.enPassantTarget)
                {
                    executeEnPassant(tempBoard,movingPiece,fromRow,fromCol,toRow,toCol)
                }
                else
                {
                    executeNormalMove(tempBoard,movingPiece,fromRow,fromCol,toRow,toCol)
                }

                updateCastlingRights(tempBoard,fromRow,fromCol,toRow,toCol)
                updateEnPassantTarget(tempBoard,movingPiece,fromRow,toRow,toCol)

                board = tempBoard
                boardSnapshots.add(board.copy())

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

    fun executeCastling(board: Board, movingPiece: ChessPiece, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int)
    {
        board.grid[fromRow][fromCol] = null
        board.grid[toRow][toCol] = movingPiece

        if (toCol < fromCol)
        {
            board.grid[fromRow][toCol + 1] = board.grid[fromRow][0]
            board.grid[fromRow][0] = null

            movesHistory.add(Move(
                moveIndex,
                (fromRow to fromCol),
                (toRow to toCol),
                movingPiece,
                false,
                null,
                MoveType.CASTLE_QUEENS_SIDE,
                false,
                GameState.PLAYING,
                playerOnTurn))
        }
        else
        {
            board.grid[fromRow][toCol - 1] = board.grid[fromRow][7]
            board.grid[fromRow][7] = null

            movesHistory.add(Move(
                moveIndex,
                (fromRow to fromCol),
                (toRow to toCol),
                movingPiece,
                false,
                null,
                MoveType.CASTLE_KINGS_SIDE,
                false,
                GameState.PLAYING,
                playerOnTurn))
        }

        fiftyMoveCounter++
    }
    fun executeEnPassant(board: Board, movingPiece: ChessPiece, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int)
    {
        val capturedPiece = board.grid[fromRow][toCol]!!
        capturedPieces += capturedPiece

        board.grid[toRow][toCol] = movingPiece
        board.grid[fromRow][fromCol] = null
        board.grid[fromRow][toCol] = null

        movesHistory.add(Move(
            moveIndex,
            (fromRow to fromCol),
            (toRow to toCol),
            movingPiece,
            true,
            null,
            MoveType.EN_PASSANT,
            false,
            GameState.PLAYING,
            playerOnTurn))

        fiftyMoveCounter = 0
    }
    fun executeNormalMove(board: Board, movingPiece: ChessPiece, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int)
    {
        val capturedPiece = board.grid[toRow][toCol]

        if (capturedPiece != null)
        {
            capturedPieces += capturedPiece
            fiftyMoveCounter = 0

            movesHistory.add(Move(
                moveIndex,
                (fromRow to fromCol),
                (toRow to toCol),
                movingPiece,
                true,
                null,
                MoveType.NORMAL,
                false,
                GameState.PLAYING,
                playerOnTurn))
        }
        else
        {
            if (movingPiece.type == Piece.PAWN)
            {
                fiftyMoveCounter = 0
            }
            else
            {
                fiftyMoveCounter++
            }

            movesHistory.add(Move(
                moveIndex,
                (fromRow to fromCol),
                (toRow to toCol),
                movingPiece,
                false,
                null,
                MoveType.NORMAL,
                false,
                GameState.PLAYING,
                playerOnTurn)
            )
        }


        board.grid[toRow][toCol] = movingPiece
        board.grid[fromRow][fromCol] = null
    }

    fun updateCastlingRights(board: Board, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int)
    {
        if (fromRow == CastlingSquare.WHITE_KING.row && fromCol == CastlingSquare.WHITE_KING.col)
        {
            board.castlingRights.whiteKingSide = false
            board.castlingRights.whiteQueenSide = false
        }
        if (fromRow == CastlingSquare.BLACK_KING.row && fromCol == CastlingSquare.BLACK_KING.col)
        {
            board.castlingRights.blackKingSide = false
            board.castlingRights.blackQueenSide = false
        }

        if ((fromRow == CastlingSquare.WHITE_KING_ROOK.row && fromCol == CastlingSquare.WHITE_KING_ROOK.col) ||
            (toRow == CastlingSquare.WHITE_KING_ROOK.row && toCol == CastlingSquare.WHITE_KING_ROOK.col))
        {
            board.castlingRights.whiteKingSide = false
        }
        if ((fromRow == CastlingSquare.WHITE_QUEEN_ROOK.row && fromCol == CastlingSquare.WHITE_QUEEN_ROOK.col) ||
            (toRow == CastlingSquare.WHITE_QUEEN_ROOK.row && toCol == CastlingSquare.WHITE_QUEEN_ROOK.col))
        {
            board.castlingRights.whiteQueenSide = false
        }

        if ((fromRow == CastlingSquare.BLACK_KING_ROOK.row && fromCol == CastlingSquare.BLACK_KING_ROOK.col) ||
            (toRow == CastlingSquare.BLACK_KING_ROOK.row && toCol == CastlingSquare.BLACK_KING_ROOK.col))
        {
            board.castlingRights.blackKingSide = false
        }
        if ((fromRow == CastlingSquare.BLACK_QUEEN_ROOK.row && fromCol == CastlingSquare.BLACK_QUEEN_ROOK.col) ||
            (toRow == CastlingSquare.BLACK_QUEEN_ROOK.row && toCol == CastlingSquare.BLACK_QUEEN_ROOK.col))
        {
            board.castlingRights.blackQueenSide = false
        }
    }
    fun updateEnPassantTarget(board: Board, movingPiece: ChessPiece, fromRow: Int, toRow: Int, toCol: Int)
    {
        if(movingPiece.type == Piece.PAWN && abs(toRow - fromRow) == 2)
        {
            board.enPassantTarget=((fromRow + toRow)/2) to toCol
        }
        else
        {
            board.enPassantTarget= null
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
        tempBoard.grid[row][col]= ChessPiece(pieceType, pendingPromotionPlayer!!)
        board = tempBoard

        boardSnapshots.removeAt(boardSnapshots.lastIndex)
        boardSnapshots.add(board.copy())

        val last = movesHistory.removeAt(movesHistory.lastIndex)
        val updated = last.copy(promotion = pieceType)
        movesHistory.add(updated)

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

    fun evaluateCheck(player: Player)
    {
        val validator = CheckValidator(board)

        if (validator.isPlayerGivingCheck(player))
        {
            val enemy = if (player == Player.WHITE) Player.BLACK else Player.WHITE
            checkState = CheckState(true, findKing(board, enemy))

            val last = movesHistory.removeAt(movesHistory.lastIndex)
            val updated = last.copy(check=true)
            movesHistory.add(updated)
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
            movesHistory.add(Move(
                -1,
                (-1 to -1),
                (-1 to -1),
                null,
                false,
                null,
                null,
                false,
                gameState,
                playerOnTurn))

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

    fun getMovesHistoryFormated() : List<String>
    {
        val res = mutableListOf<String>()

        movesHistory.forEach{
            var text = ""

            if((it.end == GameState.CHECKMATE && it.player==Player.WHITE) ||
                (it.end == GameState.RESIGNED && it.player==Player.BLACK) ||
                (it.end == GameState.TIMEOUT && it.player==Player.BLACK))
            {
                text+= "1-0"
            }
            else if((it.end == GameState.CHECKMATE && it.player==Player.BLACK) ||
                (it.end == GameState.RESIGNED && it.player==Player.WHITE) ||
                (it.end == GameState.TIMEOUT && it.player==Player.WHITE))
            {
                text+= "0-1"
            }
            else if(it.end == GameState.CHECKMATE || it.end == GameState.DRAW)
            {
                text+= "1/2-1/2"
            }
            else
            {
                text+= it.index.toString() + ". "

                if(it.type == MoveType.CASTLE_KINGS_SIDE)
                {
                    text +="O-O"
                }
                else if(it.type == MoveType.CASTLE_QUEENS_SIDE)
                {
                    text += "O-O-O"
                }
                else if(it.type == MoveType.EN_PASSANT ||
                    (it.type == MoveType.NORMAL && it.capturedPiece && it.movingPiece!!.type == Piece.PAWN))
                {
                    text+= colToString(it.from.second) + "x" + colToString(it.to.second) + (8-it.to.first).toString()
                }
                else
                {
                    if(it.movingPiece!!.type == Piece.KNIGHT)
                    {
                        text += "N"
                    }
                    else if(it.movingPiece.type == Piece.BISHOP)
                    {
                        text += "B"
                    }
                    else if(it.movingPiece.type == Piece.ROOK)
                    {
                        text += "R"
                    }
                    else if(it.movingPiece.type == Piece.QUEEN)
                    {
                        text += "Q"
                    }
                    else if(it.movingPiece.type == Piece.KING)
                    {
                        text += "K"
                    }

                    if(it.capturedPiece && it.movingPiece.type != Piece.PAWN)
                    {
                        text += "x"
                    }

                    text+= colToString(it.to.second) + (8-it.to.first).toString()

                    if(it.movingPiece.type == Piece.PAWN && it.promotion==Piece.KNIGHT)
                    {
                        text += "=N"
                    }
                    else if(it.movingPiece.type == Piece.PAWN && it.promotion==Piece.BISHOP)
                    {
                        text += "=B"
                    }
                    else if(it.movingPiece.type == Piece.PAWN && it.promotion==Piece.ROOK)
                    {
                        text += "=R"
                    }
                    else if(it.movingPiece.type == Piece.PAWN && it.promotion==Piece.QUEEN)
                    {
                        text += "=Q"
                    }

                }

                if(it.check)
                {
                    text+= "+"
                }
            }

            res.add(text)
        }
        return res
    }
    fun goToMove(index: Int)
    {
        if(gameState != GameState.PLAYING)
        {
            if (index in boardSnapshots.indices)
            {
                board = boardSnapshots[index].copy()
            }
        }
    }
}
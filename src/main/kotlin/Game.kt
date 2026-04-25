import androidx.compose.runtime.mutableStateOf
import kotlin.collections.get
import kotlin.collections.plus
import kotlin.collections.set
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class Game
{
    var board by mutableStateOf(Board())

    var message by mutableStateOf<String>("")
    var playerOnTurn by mutableStateOf(Player.WHITE)

    var gameState by mutableStateOf(GameState.PLAYING)
    var checkState by mutableStateOf(CheckState(false, null))

    var selectedStartSquare by mutableStateOf<Pair<Int, Int>?>(null)
    var selectedEndSquare by mutableStateOf<Pair<Int, Int>?>(null)

    var moveOptions by mutableStateOf(MoveOptions(emptyList(), emptyList()))
    var capturedPieces by mutableStateOf(listOf<ChessPiece>())




    fun init()
    {
        board.clear()
        board.setUpInitialBoard()
    }

    fun onSquareClick(row: Int, col: Int)
    {
        if (gameState != GameState.PLAYING) return

        squareSelection(row, col)

        if( selectedEndSquare != null )
        {
            val validator= MoveValidator(board)
            moveOptions=validator.getLegalMoves(row, col)

            val (fromRow, fromCol) = selectedStartSquare ?: return
            val (row, col) = selectedEndSquare ?: return

            if (row to col in moveOptions.moves.map{it.first})
            {
                val movingPiece = board.grid[fromRow][fromCol]
                val capturedPiece = board.grid[row][col]

                if (capturedPiece != null)
                {
                    capturedPieces = capturedPieces + capturedPiece


                }

                //TODO en passant,castling,pawn promotion,hystory

                board.set(row,col,movingPiece)
                board.set(fromRow,fromCol,null)



                evaluateEndConditions(playerOnTurn)

                if(playerOnTurn == Player.WHITE)
                {
                    playerOnTurn = Player.BLACK
                }
                else
                {
                    playerOnTurn = Player.WHITE
                }

                board=board.copy()
            }

            selectedStartSquare = null
            selectedEndSquare = null

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
            }
        }
        else if( piece != null && piece.player != playerOnTurn)
        {
            selectedEndSquare = row to col
        }
        else
        {
            selectedStartSquare = row to col
        }
    }
    fun evaluateEndConditions(player: Player)
    {
        val validator = CheckValidator(board)

        if (validator.isOpponentCheckmatedByPlayer(player))
        {
            message = "CHECKMATE!"
            gameState = GameState.CHECKMATE
        }
        else if (validator.isStalemateCausedByPlayer(player))
        {
            message = "STALEMATE!"
            gameState = GameState.STALEMATE
        }
        else if (validator.isDraw())
        {
            message = "DRAW!"
            gameState = GameState.DRAW
        }
        else
        {
            message = ""
            gameState = GameState.PLAYING
        }

        if (validator.isPlayerGivingCheck(player))
        {
            checkState = CheckState(true, findKing(board,player))
        }
        else
        {
            checkState = CheckState(false, null)
        }
    }
}
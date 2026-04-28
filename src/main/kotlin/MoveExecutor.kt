import kotlin.collections.plus
import kotlin.math.abs

class MoveExecutor(private val game: Game)
{
    fun checkCastlingConditions(movingPiece: ChessPiece,fromCol: Int,toCol: Int):Boolean
    {
        return movingPiece.type == Piece.KING && abs(fromCol - toCol) == 2
    }
    fun executeCastling(board: Board, movingPiece: ChessPiece, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int)
    {
        board.grid[fromRow][fromCol] = null
        board.grid[toRow][toCol] = movingPiece

        if (toCol < fromCol)
        {
            board.grid[fromRow][toCol + 1] = board.grid[fromRow][0]
            board.grid[fromRow][0] = null

            game.historyManager.addMoveToHistory(Move(
                game.historyManager.getMovesCounter(),
                (fromRow to fromCol),
                (toRow to toCol),
                movingPiece,
                false,
                null,
                MoveType.CASTLE_QUEENS_SIDE,
                false,
                GameState.PLAYING,
                game.playerOnTurn))
        }
        else
        {
            board.grid[fromRow][toCol - 1] = board.grid[fromRow][7]
            board.grid[fromRow][7] = null

            game.historyManager.addMoveToHistory(Move(
                game.historyManager.getMovesCounter(),
                (fromRow to fromCol),
                (toRow to toCol),
                movingPiece,
                false,
                null,
                MoveType.CASTLE_KINGS_SIDE,
                false,
                GameState.PLAYING,
                game.playerOnTurn))
        }

        game.fiftyMoveCounter++
    }
    fun checkEnPassantConditions(movingPiece:ChessPiece,toRow:Int,toCol:Int,tempBoard:Board): Boolean
    {
        return movingPiece.type == Piece.PAWN && (toRow to toCol) == tempBoard.enPassantTarget
    }
    fun executeEnPassant(board: Board, movingPiece: ChessPiece, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int)
    {
        val capturedPiece = board.grid[fromRow][toCol]!!
        game.capturedPieces += capturedPiece

        board.grid[toRow][toCol] = movingPiece
        board.grid[fromRow][fromCol] = null
        board.grid[fromRow][toCol] = null

        game.historyManager.addMoveToHistory(Move(
            game.historyManager.getMovesCounter(),
            (fromRow to fromCol),
            (toRow to toCol),
            movingPiece,
            true,
            null,
            MoveType.EN_PASSANT,
            false,
            GameState.PLAYING,
            game.playerOnTurn))

        game.fiftyMoveCounter = 0
    }
    fun executeNormalMove(board: Board, movingPiece: ChessPiece, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int)
    {
        val capturedPiece = board.grid[toRow][toCol]

        if (capturedPiece != null)
        {
            game.capturedPieces += capturedPiece
            game.fiftyMoveCounter = 0

            game.historyManager.addMoveToHistory(Move(
                game.historyManager.getMovesCounter(),
                (fromRow to fromCol),
                (toRow to toCol),
                movingPiece,
                true,
                null,
                MoveType.NORMAL,
                false,
                GameState.PLAYING,
                game.playerOnTurn))
        }
        else
        {
            if (movingPiece.type == Piece.PAWN)
            {
                game.fiftyMoveCounter = 0
            }
            else
            {
                game.fiftyMoveCounter++
            }

            game.historyManager.addMoveToHistory(Move(
                game.historyManager.getMovesCounter(),
                (fromRow to fromCol),
                (toRow to toCol),
                movingPiece,
                false,
                null,
                MoveType.NORMAL,
                false,
                GameState.PLAYING,
                game.playerOnTurn)
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
}
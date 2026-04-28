import androidx.compose.runtime.mutableStateListOf

class HistoryManager(private val game: Game)
{
    private var moveIndex:Int = 0
    private val movesHistory = mutableStateListOf<Move>()
    private val boardSnapshots = mutableStateListOf<Board>()

    fun getMovesCounter(): Int
    {
        return moveIndex
    }
    fun increaseMoveCounter()
    {
        moveIndex++
    }
    fun addMoveToHistory(move: Move)
    {
        movesHistory.add(move)
    }
    fun popLastMove(): Move
    {
        return movesHistory.removeAt(movesHistory.lastIndex)
    }
    fun addBoardToSnapshots(board: Board)
    {
        boardSnapshots.add(board)
    }
    fun removeLastBoard()
    {
        boardSnapshots.removeAt(boardSnapshots.lastIndex)
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
        if(game.gameState != GameState.PLAYING)
        {
            if (index in boardSnapshots.indices)
            {
                game.board = boardSnapshots[index].clone()
            }
        }
    }

    fun reset()
    {
        moveIndex =0
        movesHistory.clear()
        boardSnapshots.clear()
    }
}
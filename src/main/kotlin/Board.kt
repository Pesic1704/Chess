class Board
{
    val grid = Array(8) { arrayOfNulls<ChessPiece?>(8) }

    fun get(row: Int, col: Int) = grid[row][col]

    fun set(row: Int, col: Int, piece: ChessPiece?)
    {
        grid[row][col] = piece
    }

    fun move()
    {
        //TODO
    }

    fun clear()
    {
        for (row in 0..7)
            for (col in 0..7)
                grid[row][col] = null
    }

    fun setUpInitialBoard()
    {
        grid[0][0] = ChessPiece(Piece.ROOK, Player.BLACK)
        grid[0][1] = ChessPiece(Piece.KNIGHT, Player.BLACK)
        grid[0][2] = ChessPiece(Piece.BISHOP, Player.BLACK)
        grid[0][3] = ChessPiece(Piece.QUEEN, Player.BLACK)
        grid[0][4] = ChessPiece(Piece.KING, Player.BLACK)
        grid[0][5] = ChessPiece(Piece.BISHOP, Player.BLACK)
        grid[0][6] = ChessPiece(Piece.KNIGHT, Player.BLACK)
        grid[0][7] = ChessPiece(Piece.ROOK, Player.BLACK)

        for (col in 0..7)
        {
            grid[1][col] = ChessPiece(Piece.PAWN, Player.BLACK)
        }

        grid[7][0] = ChessPiece(Piece.ROOK, Player.WHITE)
        grid[7][1] = ChessPiece(Piece.KNIGHT, Player.WHITE)
        grid[7][2] = ChessPiece(Piece.BISHOP, Player.WHITE)
        grid[7][3] = ChessPiece(Piece.QUEEN, Player.WHITE)
        grid[7][4] = ChessPiece(Piece.KING, Player.WHITE)
        grid[7][5] = ChessPiece(Piece.BISHOP, Player.WHITE)
        grid[7][6] = ChessPiece(Piece.KNIGHT, Player.WHITE)
        grid[7][7] = ChessPiece(Piece.ROOK, Player.WHITE)

        for (col in 0..7)
        {
            grid[6][col] = ChessPiece(Piece.PAWN, Player.WHITE)
        }
    }

    fun copy(): Board
    {
        val newBoard = Board()

        for (row in 0..7)
            for (col in 0..7)
                newBoard.grid[row][col] = grid[row][col]

        return newBoard
    }

}
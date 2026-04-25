import kotlin.collections.map

class MoveValidator(private val board: Board)
{

    fun getRookMoves( row: Int, col: Int): MoveResult
    {
        val moves = mutableListOf<Pair<Pair<Int, Int>, Int>> ()
        val captures = mutableListOf<Pair<Int, Int>> ()

        val piece = board.grid[row][col] ?: return MoveResult(moves.toList(),captures.toList())

        val directions = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)

        for ((dirRow, dirCol) in directions)
        {

            var newRow = row + dirRow
            var newCol = col + dirCol

            while (newRow in 0..7 && newCol in 0..7)
            {

                val target = board.grid[newRow][newCol]

                if (target == null)
                {
                    moves.add(newRow to newCol to 0)
                }
                else
                {
                    if (target.player != piece.player)
                    {
                        moves.add(newRow to newCol to 0)
                        captures.add(newRow to newCol)
                    }

                    break
                }

                newRow += dirRow
                newCol += dirCol
            }
        }

        return MoveResult(moves.toList(),captures.toList())
    }
    
}
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import androidx.compose.material3.*

fun main() = application()
{
    val game = remember { Game().apply { init() } }

    Window(onCloseRequest = ::exitApplication, title = "Chess")
    {
        App(game)
    }
}

@Composable
fun App(game: Game)
{
    LaunchedEffect(Unit)
    {
        game.startTimer()
    }

    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()

    val commonTextStyle = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp,
        color = Color.Black
    )

    Column(
        modifier = Modifier.fillMaxSize()
    )
    {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFB58863))
        )
        {
            topBarItems(game)
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        )
        {
            Column(
                modifier = Modifier
                    .background(Color(0xFFB58863))
                    .wrapContentWidth()
                    .fillMaxHeight(1f)
                    .padding(10.dp)
            )
            {
                Text(
                    text = "Match history",
                    style = commonTextStyle
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f, fill = false)
                )
                {
                    game.getMovesHistoryFormated().forEachIndexed { index, moveText ->
                        Text(
                            text = moveText,
                            style = commonTextStyle,
                            modifier = Modifier.clickable {
                                game.goToMove(index)
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            )
            {
                Column(horizontalAlignment = Alignment.CenterHorizontally)
                {
                    Text(
                        "White captures",
                        style = commonTextStyle
                    )

                    game.capturedPieces
                        .filter { it.player == Player.WHITE }
                        .forEach{ piece ->
                            Text(
                                text = piece.type.getSymbol(),
                                fontSize = 40.sp,
                                color = Color.White
                            )
                        }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally)
                {
                    gameInfoText(game)

                    Box(
                        modifier = Modifier
                            .horizontalScroll(hScroll)
                            .verticalScroll(vScroll)
                    )
                    {
                        ChessBoard(
                            game=game,
                            onSquareClick = { row, col -> game.onSquareClick(row, col) }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally)
                {
                    Text(
                        "Black captures",
                        style = commonTextStyle
                    )

                    game.capturedPieces
                        .filter { it.player == Player.BLACK }
                        .forEach{ piece ->
                            Text(
                                text = piece.type.getSymbol(),
                                fontSize = 40.sp,
                                color = Color.Black
                            )
                        }
                }
            }
        }
    }

    if(game.promotionSquare != null)
    {
        promotionDialog(game)
    }
}

@Composable
fun ChessBoard(
    game: Game,
    onSquareClick: (row: Int, col: Int) -> Unit
)
{
    val lightSquare = Color(0xFFF0D9B5)
    val darkSquare = Color(0xFFB58863)
    val checkSquare = Color(0xFFE53935)

    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Column()
        {
            for (row in 0..7)
            {
                Box(
                    modifier = Modifier.size(36.dp, 96.dp),
                    contentAlignment = Alignment.Center
                )
                {
                    Text("${8 - row}", fontSize = 24.sp)
                }
            }
        }

        Column()
        {
            Box(
                modifier = Modifier
                    .border(4.dp, Color(0xFF5C4033), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF5C4033))
                    .padding(8.dp)
            )
            {
                Column()
                {
                    for (row in 0..7)
                    {
                        Row()
                        {
                            for (col in 0..7)
                            {
                                val piece = game.board.grid[row][col]
                                val isCheck = game.checkState.isCheck && game.checkState.kingPosition == (row to col)
                                val isSelected = game.selectedStartSquare == (row to col)
                                val isMovable = (row to col) in game.moveOptions.moves.map { it.first }.toSet()
                                val isCapturable = (row to col) in game.moveOptions.captures

                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .background(if (isWhiteSquare(row,col)) lightSquare else darkSquare)
                                        .then(if (isCheck)
                                                        Modifier.background(checkSquare.copy(alpha = 0.4f))
                                                    else
                                                        Modifier
                                        )
                                        .then(if (isSelected)
                                                        Modifier.border(3.dp, Color.Green)
                                                    else
                                                        Modifier
                                        )
                                        .clickable { onSquareClick(row, col) },
                                    contentAlignment = Alignment.Center
                                )
                                {
                                    if (isCapturable)
                                    {
                                        Box(modifier = Modifier.fillMaxSize().padding(4.dp).border(3.dp, Color.Black.copy(alpha = 0.35f), CircleShape))
                                    }
                                    else if (isMovable)
                                    {
                                        Box(modifier = Modifier.size(14.dp).background(Color.Black.copy(alpha = 0.35f), CircleShape))
                                    }

                                    if (piece != null)
                                    {
                                        Text(
                                            text = piece.type.getSymbol(),
                                            fontSize = 64.sp,
                                            modifier = Modifier.scale(1.4f),
                                            color = if (piece.player == Player.WHITE)
                                                        Color.White
                                                    else
                                                        Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row()
            {
                for (c in 'a'..'h')
                {
                    Box(
                        modifier = Modifier.size(96.dp, 36.dp),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Text("$c", fontSize = 24.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun topBarItems(game: Game)
{
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF4A2F1A))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    )
    {

        timerBox(
            label = "White",
            minutes = game.timeLeftWhite / 60,
            seconds = game.timeLeftWhite % 60,
            background = Color.DarkGray,
            textColor = Color.White
        )

        Row()
        {
            styledButton("RESTART GAME") { game.restartGame() }
            styledButton("RESIGN GAME") { game.resignGame() }
        }

        timerBox(
            label = "Black",
            minutes = game.timeLeftBlack / 60,
            seconds = game.timeLeftBlack % 60,
            background = Color(0xFFB58863),
            textColor = Color.White
        )
    }
}

@Composable
fun gameInfoText(game : Game)
{
    Row()
    {
        if (game.gameState != GameState.PLAYING)
        {
            Text(
                text = game.message,
                color = Color.Red,
                style = MaterialTheme.typography.headlineMedium
            )
        }
        else
        {
            Text(
                text = if (game.playerOnTurn == Player.WHITE)
                    "White to move"
                else
                    "Black to move",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
fun timerBox(
    label: String,
    minutes: Int,
    seconds: Int,
    background: Color,
    textColor: Color
)
{
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(background, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
    {
        Text(
            text = label,
            fontSize = 12.sp,
            color = textColor.copy(alpha = 0.8f)
        )

        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun styledButton(
    label: String,
    onClick: () -> Unit
)
{
    Button(
        modifier = Modifier
            .padding(8.dp)
            .height(48.dp),
        onClick = { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF31302E),
            contentColor = Color.White
        )
    )
    {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        )
    }
}

@Composable
fun promotionDialog(game: Game)
{
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
    {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Pawn Promotion",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                getPromotionPieces().forEach{
                    styledButton(it.getSymbol() + " " + it.getLabel())
                    {
                        game.pawnPromotion(it)
                    }
                }

            }
        }
    }
}

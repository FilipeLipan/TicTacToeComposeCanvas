package com.github.filipelipan.tiktaktoecompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.filipelipan.tiktaktoecompose.ui.theme.GameViewModel
import com.github.filipelipan.tiktaktoecompose.ui.theme.TikTakToeComposeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    val gameViewModel by viewModels<GameViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TikTakToeComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(Modifier.fillMaxSize()) {
                        val title = when (gameViewModel.gameState.value) {
                            is GameState.Win -> {
                                val marker =
                                    when ((gameViewModel.gameState.value as GameState.Win).winnerMark) {
                                        Mark.Ball -> "Ball"
                                        Mark.Cross -> "Cross"
                                    }
                                "$marker has won!!"
                            }
                            is GameState.Gaming -> {
                                "Gaming time"
                            }
                            is GameState.Tie -> {
                                "it's a tie"
                            }
                        }
                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 16.dp, bottom = 30.dp),
                            text = title,
                            fontSize = 24.sp
                        )

                        TikTakToeGame(
                            modifier = Modifier
                                .height(240.dp)
                                .align(Alignment.CenterHorizontally)
                                .width(240.dp),
                            boardSize = 240.dp,
                            boardPositions = gameViewModel.boardPositions,
                            mark = { position ->
                                gameViewModel.mark(position)
                            },
                        )

                        if (gameViewModel.gameState.value is GameState.Win || gameViewModel.gameState.value is GameState.Tie) {
                            Button(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 30.dp)
                                    .wrapContentSize(),
                                onClick = { gameViewModel.restart() }
                            ) {
                                Text(text = "Restart")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TikTakToeGame(
    modifier: Modifier,
    boardSize: Dp,
    boardPositions: MutableList<BoardMark>,
    mark: (BoardPosition) -> Unit,
    boardWidth: Dp = 4.dp,
    rectPadding: Float = 25f
) {
    var rectMap: MutableMap<BoardPosition, Rect> = mutableMapOf()


    Canvas(modifier = modifier
        .pointerInput(true) {
            detectTapGestures { tapOffSet ->
                rectMap.forEach {
                    if (it.value.contains(tapOffSet)) {
                        mark(it.key)
                    }
                }
            }
        }
    ) {
        drawTikTakToeBoard(
            boardSize = boardSize,
            width = boardWidth.toPx()
        )
        rectMap = drawTikTakToeClicks(
            offset = Offset.Zero,
            boardSize = boardSize,
        )

        boardPositions.forEach {
            when (it.mark) {
                Mark.Ball -> this.drawBall(
                    offset = it.boardPosition.offset.plus(
                        Offset(
                            rectPadding / 2,
                            rectPadding / 2
                        )
                    ),
                    size = it.boardPosition.size.width - rectPadding,
                    sweepPortion = 360f,
                    strokeWidth = 10.dp
                )
                Mark.Cross -> this.drawCross(
                    offset = it.boardPosition.offset.plus(
                        Offset(
                            rectPadding / 2,
                            rectPadding / 2
                        )
                    ),
                    size = it.boardPosition.size.width - rectPadding,
                    pathPortion = 100f,
                    strokeWidth = 10.dp
                )
            }
        }
    }
}

fun DrawScope.drawTikTakToeClicks(
    offset: Offset,
    boardSize: Dp,
): MutableMap<BoardPosition, Rect> {
    val rectMap = mutableMapOf<BoardPosition, Rect>()
    val sizeInPx = boardSize.toPx()
    val widthDistance = (sizeInPx / 3)
    val heightDistance = (sizeInPx / 3)

    for (i in 0..2) {
        for (j in 0..2) {
            val size = Size(widthDistance, heightDistance)
            val rectOffset = Offset(
                (offset.x + widthDistance) * i,
                (offset.y + heightDistance) * j
            )

            val rect = Rect(offset = rectOffset, size = size)

            rectMap.put(BoardPosition(x = i, y = j, offset = rectOffset, size = size), rect)
        }
    }
    return rectMap
}


fun DrawScope.drawTikTakToeBoard(
    boardSize: Dp,
    width: Float
) {
    val sizeInPx = boardSize.toPx()
    val widthDistance = (sizeInPx / 3)
    val heightDistance = (sizeInPx / 3)

    val pathVertical = Path().apply {
        moveTo(widthDistance, 0f)
        lineTo(widthDistance, sizeInPx)
    }
    val pathVertical2 = Path().apply {
        moveTo(widthDistance * 2, 0f)
        lineTo(widthDistance * 2, sizeInPx)
    }

    val pathHorizontal = Path().apply {
        moveTo(0f, heightDistance)
        lineTo(sizeInPx, heightDistance)
    }
    val pathHorizontal2 = Path().apply {
        moveTo(0f, heightDistance * 2)
        lineTo(sizeInPx, heightDistance * 2)
    }

    drawPath(path = pathVertical, color = Color.Black, style = Stroke(width = width))
    drawPath(path = pathVertical2, color = Color.Black, style = Stroke(width = width))
    drawPath(path = pathHorizontal, color = Color.Black, style = Stroke(width = width))
    drawPath(path = pathHorizontal2, color = Color.Black, style = Stroke(width = width))
}

fun DrawScope.drawBall(
    offset: Offset,
    size: Float,
    sweepPortion: Float,
    strokeWidth: Dp
) {
    drawArc(
        color = Color.Green,
        startAngle = 0f,
        sweepAngle = sweepPortion,
        useCenter = false,
        topLeft = offset.plus(Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2)),
        size = Size(size - strokeWidth.toPx(), size - strokeWidth.toPx()),
        style = Stroke(
            width = strokeWidth.toPx()
        )
    )
}

fun DrawScope.drawCross(
    offset: Offset,
    size: Float,
    pathPortion: Float,
    strokeWidth: Dp
) {
    val halfStrokeWidth = strokeWidth.toPx().div(2)
    val path = Path().apply {
        moveTo(offset.x + halfStrokeWidth, offset.y + halfStrokeWidth)
        lineTo(offset.x + size - halfStrokeWidth, offset.y + size - halfStrokeWidth)
    }

    val outPath = Path()
    PathMeasure().apply {
        setPath(path, false)
        getSegment(0f, pathPortion * length, outPath, true)
    }

    val secondPath = Path().apply {
        moveTo(offset.x + halfStrokeWidth, offset.y + size - halfStrokeWidth)
        lineTo(offset.x + size - halfStrokeWidth, offset.y + halfStrokeWidth)
    }

    val secondOutPath = Path()
    PathMeasure().apply {
        setPath(secondPath, false)
        getSegment(0f, pathPortion * length, secondOutPath, true)
    }

    drawPath(path = outPath, color = Color.Red, style = Stroke(width = strokeWidth.toPx()))
    drawPath(path = secondOutPath, color = Color.Red, style = Stroke(width = strokeWidth.toPx()))
}


private fun CoroutineScope.animateFloatToOne(animatable: Animatable<Float, AnimationVector1D>) {
    launch {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TikTakToeComposeTheme {
        Ball(modifier = Modifier, size = 100.dp)
    }
}

@Composable
fun TikTakToeBoard(
    modifier: Modifier,
    boardSize: Dp,
    onBoardClick: (BoardPosition) -> Unit
) {
    var rectMap: MutableMap<BoardPosition, Rect> = mutableMapOf()

    Canvas(modifier = modifier
        .pointerInput(true) {
            detectTapGestures { tapOffSet ->
                rectMap.forEach {
                    if (it.value.contains(tapOffSet)) {
                        onBoardClick(it.key)
                    }
                }
            }
        }
    ) {
        drawTikTakToeBoard(
            boardSize = boardSize,
            width = 4f
        )

        rectMap = drawTikTakToeClicks(
            offset = Offset.Zero,
            boardSize = boardSize,
        )
    }
}

@Composable
fun Cross(
    modifier: Modifier,
    size: Dp
) {
    val scope = rememberCoroutineScope()
    val animatableRemember = remember { Animatable(0f) }

    Canvas(modifier = modifier) {
        drawCross(
            offset = Offset.Zero,
            size = size.toPx(),
            pathPortion = animatableRemember.value,
            strokeWidth = 10.dp
        )
    }
    scope.animateFloatToOne(animatableRemember)
}


@Composable
fun Ball(
    modifier: Modifier,
    size: Dp,
    offset: Offset = Offset.Zero
) {
    val scope = rememberCoroutineScope()
    val animatableRemember = remember { Animatable(0f) }

    Canvas(modifier = modifier) {
        this.drawBall(
            offset = offset,
            size = size.toPx(),
            sweepPortion = 360f * animatableRemember.value,
            strokeWidth = 10.dp
        )
    }
    scope.animateFloatToOne(animatableRemember)
}

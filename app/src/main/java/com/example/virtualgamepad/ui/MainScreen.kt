package com.example.virtualgamepad.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.virtualgamepad.ConnectionViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalDensity

@Composable
fun MainScreen(viewModel: ConnectionViewModel) {
    val connected by viewModel.connected.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Connection status and control
        Box(modifier = Modifier
            .align(Alignment.TopStart)
            .background(Color.Transparent)) {
            val statusColor = if (connected) Color(0xFF2E7D32) else Color(0xFFD32F2F)
            Box(modifier = Modifier
                .size(16.dp)
                .align(Alignment.CenterStart)
                .background(statusColor))
            Text(text = if (connected) "Connected" else "Disconnected",
                modifier = Modifier.align(Alignment.CenterStart))
            Button(onClick = {
                if (!connected) viewModel.connect() else viewModel.disconnect()
            }) {
                Text(if (!connected) "Connect" else "Disconnect")
            }
        }

        // Controller area (full screen)
        ControllerArea(
            modifier = Modifier.fillMaxSize(),
            viewModel = viewModel
        )
    }
}

@Composable
fun ControllerArea(modifier: Modifier = Modifier, viewModel: ConnectionViewModel) {
    // Offset state in pixels
    var offset by remember { mutableStateOf(Offset(100f, 300f)) }
    val scope = rememberCoroutineScope()

    Box(modifier = modifier) {
        DraggableButton(
            id = "BUTTON_A",
            offset = offset,
            onDrag = { change -> offset = Offset(offset.x + change.x, offset.y + change.y) },
            onDown = {
                scope.launch { viewModel.sendMessage("KEY_DOWN:BUTTON_A") }
            },
            onUp = {
                scope.launch { viewModel.sendMessage("KEY_UP:BUTTON_A") }
            }
        )
    }
}

@Composable
fun DraggableButton(
    id: String,
    offset: Offset,
    onDrag: (Offset) -> Unit,
    onDown: () -> Unit,
    onUp: () -> Unit
) {
    val size = 80.dp
    Box(modifier = Modifier
        .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
        .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                onDrag(Offset(dragAmount.x, dragAmount.y))
            }
        }
    ) {
        // The actual press detection is nested inside to avoid interfering with drag gestures
        Box(modifier = Modifier
            .size(size)
            .pointerInput(Unit) {
                forEachGesture {
                    awaitPointerEventScope {
                        val down = awaitFirstDown()
                        // onDown
                        onDown()
                        // wait for up or cancel
                        var upEvent = false
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.changes.any { it.changedToUpIgnoreConsumed() }) {
                                upEvent = true
                                break
                            }
                        }
                        if (upEvent) onUp()
                    }
                }
            }
            .background(MaterialTheme.colors.primary)
        ) {
            Text(text = id, color = Color.White, modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    // A lightweight preview that doesn't require the real ViewModel or network
    var connected by remember { mutableStateOf(true) }
    val density = LocalDensity.current
    Box(modifier = Modifier.fillMaxSize()) {
        // Top-left status row (preview-only)
        Box(modifier = Modifier
            .align(Alignment.TopStart)
        ) {
            val statusColor = if (connected) Color(0xFF2E7D32) else Color(0xFFD32F2F)
            Box(modifier = Modifier
                .size(16.dp)
                .background(statusColor)
                .align(Alignment.CenterStart)
            )
            Text(text = if (connected) "Connected" else "Disconnected",
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp))
        }

        // Simple controller preview using the existing DraggableButton
        ControllerAreaPreview(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun ControllerAreaPreview(modifier: Modifier = Modifier) {
    var offset by remember { mutableStateOf(Offset(100f, 300f)) }

    Box(modifier = modifier) {
        DraggableButton(
            id = "BUTTON_A",
            offset = offset,
            onDrag = { change -> offset = Offset(offset.x + change.x, offset.y + change.y) },
            onDown = { /* preview stub */ },
            onUp = { /* preview stub */ }
        )
    }
}

@Preview(showBackground = true, name = "DraggableButton Preview")
@Composable
fun PreviewDraggableButton() {
    var offset by remember { mutableStateOf(Offset(40f, 40f)) }
    Box(modifier = Modifier.fillMaxSize()) {
        DraggableButton(
            id = "A",
            offset = offset,
            onDrag = { change -> offset = Offset(offset.x + change.x, offset.y + change.y) },
            onDown = { /* stub */ },
            onUp = { /* stub */ }
        )
    }
}

@Preview(showBackground = true, name = "ControllerArea Preview")
@Composable
fun PreviewControllerAreaSingle() {
    ControllerAreaPreview(modifier = Modifier.fillMaxSize())
}

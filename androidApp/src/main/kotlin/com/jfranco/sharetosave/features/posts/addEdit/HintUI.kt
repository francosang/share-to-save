package com.jfranco.sharetosave.features.posts.addEdit

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HintUI(
    state: TextFieldState,
    hint: String,
    modifier: Modifier = Modifier,
    isHintVisible: Boolean = false,
    textStyle: TextStyle,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    onFocusChange: (FocusState) -> Unit
) {
    Box {
        BasicTextField(
            state = state,
            lineLimits = lineLimits,
            textStyle = textStyle,
            modifier = modifier
                .onFocusChanged { onFocusChange(it) }
        )
        if (isHintVisible) {
            Text(text = hint, style = textStyle, color = Color.DarkGray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HintPreview() {
    HintUI(
        state = TextFieldState("Text"),
        hint = "Hint",
        textStyle = TextStyle(),
        onFocusChange = {}
    )
}
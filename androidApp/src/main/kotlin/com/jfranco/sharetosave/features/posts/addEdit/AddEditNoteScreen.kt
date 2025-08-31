package com.jfranco.sharetosave.features.posts.addEdit

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jfranco.sharetosave.features.posts.list.Note
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

data class AddEditNoteScreen(
    val note: Note?
) : Screen {
    @Composable
    override fun Content() {
        AddEditScreenUI(noteColor = note?.color ?: -1)
    }
}


@Composable
fun AddEditScreenUI(noteColor: Int) {
    val viewModel = viewModel<AddEditNoteViewModel>()
    val state by viewModel.collectAsState()
    val navigator = LocalNavigator.currentOrThrow

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val noteBgAnimation = remember {
        Animatable(
            Color(
                if (noteColor != -1) noteColor else state.color
            )
        )
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            AddEditNoteSideEffect.SaveNote ->
                navigator.pop()

            is AddEditNoteSideEffect.ShowSnackBar ->
                snackBarHostState.showSnackbar(
                    message = sideEffect.message
                )
        }
    }

    Scaffold(
        floatingActionButton = {
            //fire an event to ViewModel to save a note, during onClick operation
            FloatingActionButton(
                onClick = {
                    viewModel.onEvent(AddEditNoteEvent.SaveNote)
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Save Note")
            }
        },
        content = { padding ->
            //making a Row to select colors
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(noteBgAnimation.value)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    //make circles for each color we have
                    Note.noteColors.forEach { color ->
                        val colorInt = color.toArgb()

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .shadow(15.dp, CircleShape)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = 4.dp,
                                    color = if (state.color == colorInt) {
                                        Color.Black  //color is selected
                                    } else {
                                        Color.Transparent  //color is deselected
                                    },
                                    shape = CircleShape
                                )
                                .clickable {
                                    scope.launch {
                                        noteBgAnimation.animateTo(
                                            targetValue = Color(colorInt),
                                            animationSpec = tween(durationMillis = 500)
                                        )
                                    }

                                    viewModel.onEvent(AddEditNoteEvent.ChangeColor(colorInt))
                                }

                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // For Title
                HintUI(
                    state = state.title.text,
                    hint = state.title.hint,
                    onFocusChange = {
                        viewModel.onEvent(AddEditNoteEvent.ChangeTitleFocus(it))
                    },
                    isHintVisible = state.title.isHintVisible,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(16.dp))

                // For Content
                HintUI(
                    state = state.content.text,
                    hint = state.content.hint,
                    onFocusChange = {
                        viewModel.onEvent(AddEditNoteEvent.ChangeContentFocus(it))
                    },
                    isHintVisible = state.content.isHintVisible,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    )
}
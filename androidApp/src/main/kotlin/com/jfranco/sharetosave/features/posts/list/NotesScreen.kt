package com.jfranco.sharetosave.features.posts.list

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.util.Consumer
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jfranco.sharetosave.features.posts.addEdit.AddEditNoteScreen
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect


class NotesScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = viewModel<NotesViewModel>()
        val state by viewModel.collectAsState()

        val snackBarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow

        HandleIntent(context) { intent ->
            Log.i("MainActivity", "New Intent: ${intent.hashCode()}, ${intent.action}")
            Log.i("MainActivity", "New Intent data: ${intent.dataString}")
        }

        viewModel.collectSideEffect { sideEffect ->
            when (sideEffect) {
                is NotesSideEffect.NavigateToAddEditNoteScreen ->
                    navigator.push(AddEditNoteScreen(sideEffect.note))
            }
        }

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        viewModel.onEvent(NotesEvent.AddEditNoteScreen(null))
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
                }
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Note",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        IconButton(
                            onClick = { viewModel.onEvent(NotesEvent.ToggleOrderSection) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Sort"
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = state.isOrderSectionVisible,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        OrderSection(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            noteOrder = state.noteOrder,
                            onOrderChange = {
                                viewModel.onEvent(NotesEvent.Order(it))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.notes) { note ->
                            NoteItemUI(
                                note = note,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onEvent(NotesEvent.AddEditNoteScreen(note))
                                    },
                                onShareClicked = {
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, note.content)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                    context.startActivity(shareIntent)

                                },
                                onDeleteClicked = {
                                    viewModel.onEvent(NotesEvent.DeleteNote(note))

                                    // TODO: move to an effect
                                    // after deleting the note, show the snackbar
                                    scope.launch {
                                        Log.d("message", "delete")
                                        val result = snackBarHostState.showSnackbar(
                                            message = "Note Deleted!",
                                            actionLabel = "Undo"
                                        )

                                        when (result) {
                                            SnackbarResult.ActionPerformed -> {
                                                viewModel.onEvent(NotesEvent.RestoreNote)
                                            }

                                            else -> {}
                                        }

                                    }
                                }
                            )

                            //adding space b/w each note Item
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun HandleIntent(context: Context, handleIntentAction: (intent: Intent) -> Unit) {
    LaunchedEffect(Unit) {
        Log.d("MainActivity", "HandleIntent...")

        callbackFlow {
            Log.d("MainActivity", "callbackFlow...")

            val componentActivity = context as ComponentActivity
            val currentIntent = componentActivity.intent

            Log.d("MainActivity", "Intent: ${currentIntent.hashCode()}")
            Log.d("MainActivity", "Action: ${currentIntent.action}")
            Log.d("MainActivity", "Data  : ${currentIntent.data}")

            val consumer = Consumer<Intent> { trySend(it) }
            componentActivity.addOnNewIntentListener(consumer)
            awaitClose {
                Log.d("MainActivity", "removeOnNewIntentListener...")
                componentActivity.removeOnNewIntentListener(consumer)
            }
        }.collectLatest {
            Log.d("MainActivity", "handleIntentAction...")
            handleIntentAction(it)
        }
    }

//    LaunchedEffect(Unit) {
//        Log.d("MainActivity", "HandleIntent...")
//        callbackFlow {
//            Log.d("MainActivity", "callbackFlow...")
//
//            val componentActivity = context as ComponentActivity
//            val currentIntent = componentActivity.intent
//
//            Log.d("MainActivity", "Intent: ${currentIntent.hashCode()}")
//            Log.d("MainActivity", "Action: ${currentIntent.action}")
//            Log.d("MainActivity", "Data  : ${currentIntent.data}")
//
//            if (currentIntent?.data != null) {
//                Log.d("MainActivity", "trySend...")
//                trySend(currentIntent)
//            }
//            val consumer = Consumer<Intent> { trySend(it) }
//            componentActivity.addOnNewIntentListener(consumer)
//            awaitClose {
//                Log.d("MainActivity", "removeOnNewIntentListener...")
//                componentActivity.removeOnNewIntentListener(consumer)
//            }
//        }.collectLatest {
//            Log.d("MainActivity", "handleIntentAction...")
//            handleIntentAction(it)
//        }
//    }
}
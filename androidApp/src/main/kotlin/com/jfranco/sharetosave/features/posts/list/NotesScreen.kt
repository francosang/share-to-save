package com.jfranco.sharetosave.features.posts.list

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jfranco.sharetosave.features.posts.addEdit.AddEditScreenDestinationArgs
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AddEditScreenDestination
import com.ramcosta.composedestinations.generated.destinations.RemindersScreenDestination
import com.ramcosta.composedestinations.generated.destinations.TagsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Destination<RootGraph>(start = true)
@Composable
fun NotesListScreen(
    navigator: DestinationsNavigator
) {
    val viewModel = hiltViewModel<NotesViewModel>()
    val state by viewModel.collectAsState()

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(state.isDrawerOpen) {
        if (state.isDrawerOpen) drawerState.open() else drawerState.close()
    }

    LaunchedEffect(drawerState) {
        snapshotFlow { drawerState.currentValue }.collect { value ->
            if (value == DrawerValue.Closed) viewModel.onEvent(NotesEvent.CloseDrawer)
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is NotesSideEffect.NavigateToAddEditNoteScreen ->
                navigator.navigate(
                    AddEditScreenDestination(
                        AddEditScreenDestinationArgs(sideEffect.note)
                    )
                )

            is NotesSideEffect.ShowSnackbar -> {
                scope.launch {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    val result = snackBarHostState.showSnackbar(
                        message = sideEffect.message,
                        actionLabel = sideEffect.actionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onEvent(NotesEvent.RestoreNote)
                    }
                }
            }

            NotesSideEffect.NavigateToRemindersScreen ->
                navigator.navigate(RemindersScreenDestination)

            is NotesSideEffect.NavigateToTagsScreen ->
                navigator.navigate(TagsScreenDestination(sideEffect.alertOpened))
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Nav section
                NavigationDrawerItem(
                    label = { Text("Notes") },
                    selected = state.activeTagFilter == null,
                    onClick = { viewModel.onEvent(NotesEvent.SelectTag(null)) },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Reminders") },
                    selected = false,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null
                        )
                    },
                    onClick = { viewModel.onEvent(NotesEvent.OpenRemindersScreen) },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Tags section header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.onEvent(NotesEvent.OpenTagsScreen()) }) {
                        Text("Edit")
                    }
                }

                // Tag filter items
                state.tags.forEach { tag ->
                    NavigationDrawerItem(
                        label = { Text(tag.name) },
                        selected = state.activeTagFilter == tag.id,
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (tag.color != 0) Color(tag.color)
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                            )
                        },
                        onClick = { viewModel.onEvent(NotesEvent.SelectTag(tag.id)) },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                // Add new tag inline
                NavigationDrawerItem(
                    label = { Text("New tag", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    selected = false,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = { viewModel.onEvent(NotesEvent.OpenTagsScreen(openAlert = true)) },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    ) {
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
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
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
                        IconButton(onClick = { viewModel.onEvent(NotesEvent.ToggleDrawer) }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                        }

                        Text(
                            text = if (state.activeTagFilter != null) {
                                state.tags.find { it.id == state.activeTagFilter }?.name ?: "Your Notes"
                            } else {
                                "Your Notes"
                            },
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

                    val visibleNotes = if (state.activeTagFilter != null) {
                        state.notes.filter { note -> state.activeTagFilter in note.tagIds }
                    } else {
                        state.notes
                    }

                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(visibleNotes) { note ->
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
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        )
    }
}

package com.jfranco.sharetosave.features.posts.addEdit

import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jfranco.sharetosave.domain.Note
import com.jfranco.sharetosave.domain.Tag
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.serialization.Serializable
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.LocalDateTime

@Serializable
data class AddEditScreenDestinationArgs(
    val note: Note? = null,
    val text: String? = null,
    val fileUri: Uri? = null,
    val mimeType: String? = null,
    val fromShare: Boolean = false,
)

@Destination<RootGraph>(
    navArgs = AddEditScreenDestinationArgs::class
)
@Composable
fun AddEditScreen(
    navigator: DestinationsNavigator
) {
    Log.d("MyApp", "AddEditScreen ------------------")

    val viewModel = hiltViewModel<AddEditNoteViewModel>()
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is AddEditNoteSideEffect.NavigateBackWithResult ->
                navigator.navigateUp()
        }
    }

    Screen(state, navigator, viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(
    state: AddEditNoteState,
    navigator: DestinationsNavigator,
    viewModel: AddEditNoteViewModel
) {
    Log.d("MyApp", "AddEditScreen: state: $state")

    val noteBgAnimation by animateColorAsState(
        targetValue = Color(state.color),
        animationSpec = tween(durationMillis = 300)
    )

    Scaffold(
        containerColor = noteBgAnimation,
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                modifier = Modifier.imePadding(),
                visible = state.saveEnabled
            ) {
                FloatingActionButton(
                    modifier = Modifier,
                    onClick = {
                        viewModel.onEvent(
                            AddEditNoteEvent.SaveNote(
                                Note(
                                    id = state.noteId,
                                    title = state.title.state.text.toString(),
                                    content = state.content.state.text.toString(),
                                    attachmentPath = state.attachmentPath,
                                    attachmentMimeType = state.attachmentMimeType,
                                    created = LocalDateTime.now(),
                                    edited = null,
                                    color = state.color
                                )
                            )
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Save Note")
                }
            }
        },
        content = { padding ->
            Content(
                padding, state,
                onFocusChange = {
                    viewModel.onEvent(it)
                },
                onColorChanged = { colorInt ->
                    viewModel.onEvent(AddEditNoteEvent.ChangeColor(colorInt))
                },
                onToggleTag = { tagId ->
                    viewModel.onEvent(AddEditNoteEvent.ToggleTag(tagId))
                },
                onToggleTagPanel = {
                    viewModel.onEvent(AddEditNoteEvent.ToggleTagPanel)
                }
            )
        }
    )
}

@Composable
private fun Content(
    padding: PaddingValues,
    state: AddEditNoteState,
    onColorChanged: (Int) -> Unit,
    onFocusChange: (AddEditNoteEvent.ChangeFocus) -> Unit,
    onToggleTag: (Long) -> Unit,
    onToggleTagPanel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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
                                Color.Black
                            } else {
                                Color.Transparent
                            },
                            shape = CircleShape
                        )
                        .clickable {
                            onColorChanged(colorInt)
                        }
                )
            }
        }

        TagPickerPanel(
            tags = state.tags,
            selectedTagIds = state.selectedTagIds,
            isExpanded = state.isTagPanelExpanded,
            onTogglePanel = onToggleTagPanel,
            onToggleTag = onToggleTag,
        )

        if (state.isAttachmentLoading || state.attachmentPath != null) {
            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (state.isAttachmentLoading) {
                    CircularProgressIndicator()
                } else {
                    AsyncImage(
                        model = state.attachmentPath,
                        contentDescription = "Attachment",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        HintUI(
            state = state.title.state,
            hint = state.title.hint,
            onFocusChange = {
                onFocusChange(AddEditNoteEvent.ChangeTitleFocus(it))
            },
            isHintVisible = state.title.isHintVisible,
            lineLimits = TextFieldLineLimits.SingleLine,
            textStyle = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(16.dp))

        HintUI(
            state = state.content.state,
            hint = state.content.hint,
            onFocusChange = {
                onFocusChange(AddEditNoteEvent.ChangeContentFocus(it))
            },
            isHintVisible = state.content.isHintVisible,
            textStyle = MaterialTheme.typography.bodyMedium,
            lineLimits = TextFieldLineLimits.MultiLine(),
            modifier = Modifier.fillMaxHeight()
        )
    }
}

@Composable
private fun TagPickerPanel(
    tags: List<Tag>,
    selectedTagIds: List<Long>,
    isExpanded: Boolean,
    onTogglePanel: () -> Unit,
    onToggleTag: (Long) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onTogglePanel)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Tags", style = MaterialTheme.typography.labelLarge)
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                              else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse tags" else "Expand tags"
            )
        }
        AnimatedVisibility(visible = isExpanded) {
            if (tags.isEmpty()) {
                Text(
                    "No tags yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(bottom = 8.dp)
                ) {
                    items(tags) { tag ->
                        val selected = tag.id?.let { it in selectedTagIds } == true
                        FilterChip(
                            selected = selected,
                            onClick = { tag.id?.let { onToggleTag(it) } },
                            label = { Text(tag.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(tag.color).copy(alpha = 0.4f)
                            ),
                            leadingIcon = if (selected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}